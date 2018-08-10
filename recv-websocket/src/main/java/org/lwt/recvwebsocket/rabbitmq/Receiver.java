package org.lwt.recvwebsocket.rabbitmq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.lwt.recvwebsocket.finals.RabbitConstant;
import org.lwt.recvwebsocket.websocket.WebSocketServerEndpoint;
import org.lwt.serverupload.tools.EncryptUtil;
import org.lwt.serverupload.tools.JsonUtil;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
/**
 *	 消费者类
 * 	@author Administrator
 *
 */
@Component
public class Receiver {
	
	 @Autowired
	 private WebSocketServerEndpoint webSocketServerEndpoint;
	
	/**
	 *	 接收队列名为RabbitConstant.QUEUE_QUALIFICATION的队列中的消息。
	 * @param msg
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	@RabbitListener(queues=RabbitConstant.QUEUE_SPRING_BOOT)
	@RabbitHandler 
	public String receiver(Message msg) throws UnsupportedEncodingException {
		
		String path = "";
		try {
			path = ResourceUtils.getURL("").getPath() + "recvtemp";
			path = path.substring(1, path.length());
			
			File tempFile = new File(path);
			if(!tempFile.exists()) {
				if(tempFile.mkdirs()) {
					System.out.println("创建目录成功。");
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String recvMsg = new String(msg.getBody());
		System.err.println(recvMsg);
		Map<String, Object> recvMap = JsonUtil.getMapFromJson(recvMsg); 
		
		String fileName = (String) recvMap.get("fileName");
		String downloadUrl = "http://localhost:8080/download/"+fileName;		// 接收到的文件的下载地址。
		
		
		System.out.println("接收到的消息为。"+recvMsg);
		byte[] bytes = EncryptUtil
        		.decodeByteByBase64((String)recvMap.get("data"));
		String newMd5 = DigestUtils.md5Hex(bytes);
		Map<String, Object> resMap = new HashMap<>();
		RandomAccessFile randomFile = null;
		try {
			randomFile = new RandomAccessFile(path+"/"+fileName, "rw");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(recvMap.get("md5").equals(newMd5)) {
			System.out.println("单个包MD5验证通过。");
			
			try {
				randomFile.seek(randomFile.length());
				randomFile.write(bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			resMap.put("packId", recvMap.get("packid"));
			resMap.put("msg", "0");
		}else {
			System.out.println("单个包MD5验证不通过。");
			resMap.put("packId", recvMap.get("packid"));
			resMap.put("msg", "1");
		}
		try {
			if(recvMap.get("packnum") == recvMap.get("packcount")) {
				// 整个文件的md5验证通过
				try {
					if(recvMap.get("allMD5").equals(DigestUtils.md5Hex(new FileInputStream(new File(path+"/"+fileName))))) {
						System.out.println("最后一个包发送完成");
						System.out.println("整个文件iaoy通过");
						if(randomFile != null) {
							randomFile.close(); 	// 最后在关闭文件
						}
						webSocketServerEndpoint.sendMessageToAll(downloadUrl);		// 文件存储完成后，将下载链接发送给前端下载为文件
					}else {
						if(new File(path+"/"+fileName).exists()) {
							if(new File(path+"/"+fileName).delete()) {
								System.out.println("整个文件md5校验失败，删除文件成功。");
							}
						}
					}
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		String response = JsonUtil.getJsonFromMap(resMap);
		System.out.println("返回响应。。。");
		// return 返回响应给发送者。
		return response;
	}
}
