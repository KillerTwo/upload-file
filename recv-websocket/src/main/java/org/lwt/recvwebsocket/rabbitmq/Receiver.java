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
		
		String recvMsg = new String(msg.getBody(),"utf-8");
		Map<String, Object> recvMap = JsonUtil.getMapFromJson(recvMsg); 
		/**********************/
		int isLogin = (int) recvMap.get("isLogin");
		String userId = (String) recvMap.get("userId");
		System.err.println("receiver"+userId);
		// 判断接收数据的用户是否已经登录
		if(isLogin == 0) {
			// 表示该包是消息确认包，用来判断指定用户是否登录
			// 发送一条“isOK”是否可以发送数据的消息个客户端
			Map<String, Object> isOKMap = new HashMap<>();
			if(webSocketServerEndpoint.sendMessage(userId, "isOK")) {
				System.out.println("用户已经准备好接收数据，可以发送数据。。。");
				isOKMap.put("userId", "admin");
				isOKMap.put("status", "isOK");
				return JsonUtil.getJsonFromMap(isOKMap);
				
			}else {
			  System.out.println("用户还没有登陆。");
			  isOKMap.put("userId", "admin");
        isOKMap.put("status", "noLogin");
        return JsonUtil.getJsonFromMap(isOKMap);
			}
		}else {
		  String fileName = (String) recvMap.get("fileName");
	    String ext = (String) recvMap.get("ext");
	    String nameExcludeExt = fileName.substring(0, fileName.indexOf("."+ext));
	    String downloadUrl = "/file/download/"+nameExcludeExt+"/"+ext;    // 接收到的文件的下载地址。
	    System.out.println(downloadUrl);
	    
	    System.out.println("接收到的消息为。"+recvMsg);
	    byte[] bytes = EncryptUtil
	            .decodeByteByBase64((String)recvMap.get("data"));
	    String newMd5 = DigestUtils.md5Hex(bytes);
	    Map<String, Object> resMap = new HashMap<>();
	    /*RandomAccessFile randomFile = null;
	    try {
	      randomFile = new RandomAccessFile(path+"/"+fileName, "rw");
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    }*/
	    try {
	      long packnum = new Long((int)recvMap.get("packnum"));
	      long packcount = new Long((int)recvMap.get("packcount"));
	      int isEnd = (int) recvMap.get("isEnd");
	      if(isEnd == 0) {
	        /*if(randomFile != null) {
	          randomFile.close();   // 最后在关闭文件
	        }*/
	        if(packnum >= packcount) {
	          // 整个文件的md5验证通过
	          String allMd5 = (String) recvMap.get("allMD5");
	          String newAllMd5 = DigestUtils.md5Hex(new FileInputStream(new File(path+"/"+fileName)));
	          System.out.println("文件md5:"+allMd5);
	          System.out.println("接收到的文件Md5: "+newAllMd5);
	          
	          if(recvMap.get("allMD5").equals(DigestUtils.md5Hex(new FileInputStream(new File(path+"/"+fileName))))) {
	            System.out.println("最后一个包发送完成");
	            System.out.println("整个文件校验通过");
	            resMap.put("packId", recvMap.get("packid"));
	            resMap.put("msg", "0");
	            webSocketServerEndpoint.sendMessageToAll(downloadUrl);    // 文件存储完成后，将下载链接发送给前端下载为文件
	          }else {
	            resMap.put("packId", recvMap.get("packid"));
	            resMap.put("msg", "1");
	            if(new File(path+"/"+fileName).exists()) {
	              if(new File(path+"/"+fileName).delete()) {
	                System.out.println("整个文件md5校验失败，删除文件成功。");
	              }
	            }
	          }
	        }else {
	          resMap.put("packId", recvMap.get("packid"));
	          resMap.put("msg", "1");
	          if(new File(path+"/"+fileName).exists()) {
	            if(new File(path+"/"+fileName).delete()) {
	              System.out.println("丢包，删除已存储的部分文件成功。");
	            }
	          }
	          System.err.println("发生丢包。。。");
	        }
	      }else {
	        if(recvMap.get("md5").equals(newMd5)) {
	          System.out.println("单个包MD5验证通过。");
	          
	          try(RandomAccessFile randomFile = new RandomAccessFile(path+"/"+fileName, "rw");){
	            try {
	              randomFile.seek(randomFile.length());
	              randomFile.write(bytes);
	            } catch (IOException e) {
	              e.printStackTrace();
	            }
	          }
	          resMap.put("packId", recvMap.get("packid"));
	          resMap.put("msg", "0");
	        }else {
	          System.out.println("单个包MD5验证不通过。");
	          resMap.put("packId", recvMap.get("packid"));
	          resMap.put("msg", "1");
	        }
	      }
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    String response = JsonUtil.getJsonFromMap(resMap);
	    System.out.println("返回响应。。。"+response);
	    // return 返回响应给发送者。
	    return response;
		}
	}
}
