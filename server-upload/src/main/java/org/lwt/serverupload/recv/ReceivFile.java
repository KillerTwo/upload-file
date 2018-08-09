package org.lwt.serverupload.recv;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.lwt.serverupload.rabbitmq.UploadFileImpl;
import org.lwt.serverupload.tools.EncryptUtil;
import org.lwt.serverupload.tools.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(value = "/upload")
public class ReceivFile {
	/**
	 * 返回上传文件的页面
	 * 
	 * @return
	 */
	@RequestMapping(value = "/selectfile", method = RequestMethod.GET)
	public String mapUploadView() {
		return "upload/upload";
	}

	@RequestMapping(value = "/rest", method = RequestMethod.GET)
	@ResponseBody
	public String test() {
		return "upload";
	}

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
			path = ResourceUtils.getURL("").getPath() + "customtemppath";
			path = path.substring(1, path.length());
			System.out.println(path);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		//分包的数量
		if(isFinish == 1) {
			long packCount = (long) Math.ceil(size / 1024D);
			System.out.println("分包的数量："+ packCount);
			System.err.println("是否上传完成："+isFinish);
			System.out.println("整个文件大小："+size);
			System.out.println("文件的id："+fileId);
			System.out.println("文件的MD5值："+fileMd5);
			System.out.println("文件扩展名："+ext);
			String contentType = file.getContentType();
			fileName = file.getOriginalFilename();
			System.out.println(contentType + "--文件名--" + fileName);
			
			
			File tempFile = new File(path);
			if (!tempFile.exists()) {
				tempFile.mkdirs();
				System.out.println("创建文件成功。");
			}
			System.out.println(path + "/" + fileName);
			// 将文件存储到本地
			try (BufferedInputStream inputStream = new BufferedInputStream(file.getInputStream());
					RandomAccessFile randomFile = new RandomAccessFile(path + "/" + fileName, "rw")) {
				randomFile.seek(randomFile.length());
				byte[] buf = new byte[1024];
				int length = 0;
				while ((length = inputStream.read(buf)) != -1) {
					randomFile.write(buf, 0, length);
					//System.out.println(length);
					//System.err.println("文件的字节数组：" + buf);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}else {
			
			// 最后一个文件上传完成， 发送到RabbitMq服务器。
			System.err.println("上传完成："+isFinish);
			try {
				UploadFileImpl uploadimpl = new UploadFileImpl();
				System.err.println("文件名称是："+fileName);
				success = uploadimpl.sendData(path + "/" + fileName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (success) {
				System.out.println("上传文件成功。");
				if(new File(path + "/" + fileName).delete()) {
					System.out.println("删除文件成功。");
				};
				return "上传文件成功。";
			} else {
				if(new File(path + "/" + fileName).delete()) {
					System.out.println("删除文件成功。");
				};
				System.out.println("上传文件失败。");
				return "上传文件失败。";
			}
		}
		
		
		
		/*
		 * try { // 将文件存储到本地 file.transferTo(new File(path+"\\"+fileName)); } catch
		 * (IllegalStateException e1) { e1.printStackTrace(); } catch (IOException e1) {
		 * e1.printStackTrace(); }
		 */
		/*List<byte[]> listByte = new ArrayList<>();
		try (BufferedInputStream inputStream = new BufferedInputStream(file.getInputStream());
				RandomAccessFile randomFile = new RandomAccessFile(path + "\\" + fileName, "rw")) {
			randomFile.seek(randomFile.length());
			byte[] buf = new byte[1024];
			int length = 0;
			while ((length = inputStream.read(buf)) != -1) {
				randomFile.write(buf, 0, length);
				listByte.add(buf);
				System.out.println(length);
				System.err.println("文件的字节数组：" + buf);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		
		
		
		/*
		 * try { byte[] bytes = file.getBytes(); String encodeStr =
		 * EncryptUtil.encodeByBase64(bytes);
		 * System.err.println("加密后的文件字符串："+encodeStr); uploadimpl.sendData(encodeStr,
		 * fileName, "C:\\Users\\Administrator\\Documents\\test\\recv\\"); } catch
		 * (Exception e) { e.printStackTrace(); }
		 */
		return "上传文件为结束。";
		
		//return "上传文件成功。";
	}
	/**
	 * 下载文件
	 * @param id
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@GetMapping(value="{id}")
	public void download(@PathVariable String id, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		System.out.println(id);
		String path = ResourceUtils.getURL("").getPath() + "recvtemp";
		path = path.substring(1, path.length());
		File recvTemp = new File(path);
		if(!recvTemp.exists()) {
			recvTemp.mkdirs();
		}
		System.out.println(path);
	/*	String fileName = "text.txt";
		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(new File(path+"\\"+"test01.txt")));
				BufferedOutputStream outputStream = new BufferedOutputStream(response.getOutputStream());) {
			response.setContentType("application/x-download");
			//response.setContentType("multipart/form-data;charset=UTF-8");
			response.setHeader("Content-Disposition", "attachment;fileName="+  fileName +";filename*=utf-8''"+URLEncoder.encode(fileName,"UTF-8"));
			//response.addHeader("Content-Disposition", "attachment;filename=test.txt");
			IOUtils.copy(inputStream, outputStream);
			outputStream.flush();
		}*/

	}

}
