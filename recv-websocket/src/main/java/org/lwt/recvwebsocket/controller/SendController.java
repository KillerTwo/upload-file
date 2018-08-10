package org.lwt.recvwebsocket.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import org.apache.commons.codec.cli.Digest;
import org.apache.commons.codec.digest.DigestUtils;
import org.lwt.recvwebsocket.rabbitmq.CountDown;
import org.lwt.recvwebsocket.rabbitmq.Sender;
import org.lwt.recvwebsocket.websocket.WebSocketServerEndpoint;
import org.lwt.serverupload.tools.EncryptUtil;
import org.lwt.serverupload.tools.FileUtils;
import org.lwt.serverupload.tools.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class SendController {
	
	@Autowired
	private Sender sender;
	@Autowired
	private WebSocketServerEndpoint webSocketServerEndpoint;
	
	/**
	 * 保存文件
	 * 接收的参数：
	 * @param size 整个文件的大小
	 * @param fileId  文件的唯一id
	 * @param fileMd5   文件的MD5值
	 * @param ext 文件扩展名
	 * @param file 上传的文件对象
	 * @param isFinish 是否上传完成最后一个包0表示上传完成，1表示还未上传完成
	 * 
	 * 构造一个结构体
	 * {
	 * 		"fileSize" : 文件大小,
	 * 		"fileId" : 文件的唯一id,
	 * 		"fileMD5" : 文件的MD5值,
	 * 		"ext" : 文件的扩展名,
	 * 		"packCount" : 分包的数量
	 * 		"data" : 文件内容（字节数组）
	 * }
	 *
	 * 
	 */
	@RequestMapping(value = "/chunk")
	@ResponseBody
	public String recvFile(@RequestParam(value="file", required=false) MultipartFile file, 
			@RequestParam(value="isFinish", required=false) Integer isFinish,
			@RequestParam(value="size", required=false) Long size,
			@RequestParam(value="id", required=false) String fileId,
			@RequestParam(value="fileMD5", required=false) String fileMd5,
			@RequestParam(value="ext", required=false) String ext,
			@RequestParam(value="fileName", required=false) String fileName,
			HttpServletRequest request) {

		// 创建一个临时目录，用来存放上传的文件
		boolean success = false;	// 是否发送成功
		String path = "";
		try {
			path = ResourceUtils.getURL("").getPath() + "uploadtemp";
			path = path.substring(1, path.length());
			//System.out.println(path);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		//分包的数量
		System.err.println(isFinish);
		if(isFinish == 1) {
			long packCount = (long) Math.ceil(size / 1024D);
			String contentType = file.getContentType();
			fileName = file.getOriginalFilename();
			System.out.println(contentType + "--文件名--" + fileName);
			if (!new File(path).exists()) {
				new File(path).mkdirs();
				System.out.println("recvFile目录创建成功。");
			}
			//System.out.println(path + "/" + fileName);
			// 将文件存储到本地
			try (BufferedInputStream inputStream = new BufferedInputStream(file.getInputStream());
					RandomAccessFile randomFile = new RandomAccessFile(path + "/" + fileName, "rw")) {
				System.err.println("将文件存储到本地1: "+path + "/" + fileName);
				byte[] buf = new byte[1024];
				int length = 0;
				while ((length = inputStream.read(buf)) != -1) {
					randomFile.seek(randomFile.length());
					randomFile.write(buf, 0, length);
				}
				System.err.println("将文件存储到本地2: "+path + "/" + fileName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return "文件还未上传完成。";
		}else {
			// 最后一个包上传完成后在进行数据发送
			try(FileInputStream in = new FileInputStream(new File(path + "/" + fileName));
					FileChannel channel = in.getChannel();){
				String fileMD5 = DigestUtils.md5Hex(in);
				long packCount = (long) Math.ceil(channel.size() / 1024D);		// 分包数量
				long packnum = 1;												// 当前包的序号
				byte[] buffer=new byte[1024];
				int offset = 0;
				System.err.println(path + "/" + fileName);
				if(new File(path + "/" + fileName).exists()) {
					System.out.println("文件存在");
				}else {
					System.out.println("文件不存在");
				}
				if((offset = in.read(buffer)) != -1) {
					System.out.println("(offset = in.read(buffer)) != -1");
				}else {
					System.out.println(offset);
				}
				while((offset = in.read(buffer)) != -1) {
					byte[] bytes = new byte[offset];
					bytes = Arrays.copyOf(buffer, offset);
					String message = "";
					message = getJsonMap(fileName, ext, fileId, packCount, bytes, fileMD5, packnum++);
					System.out.println("发送的数据为："+message);
					String res  = sender.send(message);
					int sendTime = 0;
					Map<String, Object> resMap = JsonUtil.getMapFromJson(res);
					// 如果接收到错误的响应则进行最多三次数据重发
					while((sendTime < 3 && resMap == null) || (sendTime < 3 && !"0".equals(resMap.get("msg")))) {
						System.out.println("recvFile进入while循环 ");
						sendTime++;
						res = sender.send(message);
						resMap = JsonUtil.getMapFromJson(res);
					}
					if(resMap != null) {
						success = "0".equals(resMap.get("msg")) ? true : false;
					}else {
						success = false;
					}
					
				}
			}catch(Exception e) {
				e.printStackTrace();
				return "";
			}
			if(new File(path + "/" + fileName).exists()) {
				if(new File(path + "/" + fileName).delete()) {
					System.err.println("recvFile文件传输完成，删除临时文件成功。");
				}
			}
			if(new File(path + "/" + fileName).exists()) {
				System.err.println("文件存在");
			}else {
				System.err.println("文件不存在");
			}
			System.err.println("recvFile上传完成："+isFinish);
			if(success) {
				return "recvFile发送数据成功。";
			}else {
				return "recvFile发送数据失败。";
			}
		}
	}
	
	private String getJsonMap(String fileName,String ext,String fileId,long packCount,byte[] temp,String fileMd5, long packnum){
		// 发送一个字节数组
    	Map<String, Object> bytePackge = new HashMap<>();	// 皴法，封装一个字节包发送到RabbitMq
    	bytePackge.put("fileName", fileName);
    	bytePackge.put("ext", ext);
    	bytePackge.put("date", System.currentTimeMillis()+1);
    	bytePackge.put("packid", fileId);
    	bytePackge.put("packcount", packCount);
    	bytePackge.put("md5", DigestUtils.md5Hex(temp));
    	bytePackge.put("packSize", new Long(1024));
    	bytePackge.put("allMD5", fileMd5);
    	bytePackge.put("packnum", packnum);
    	bytePackge.put("data", EncryptUtil.encodeByBase64(temp));
    	String message = JsonUtil.getJsonFromMap(bytePackge);
    	return message;
	}
	
}