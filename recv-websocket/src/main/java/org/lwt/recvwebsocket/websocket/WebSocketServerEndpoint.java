package org.lwt.recvwebsocket.websocket;


import com.alibaba.fastjson.JSONObject;

import groovy.util.logging.Slf4j;

import org.springframework.stereotype.Component;
 
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
 
/**
 * ServerEndpoint
 * <p>
 * 使用springboot的唯一区别是要@Component声明下，而使用独立容器是由容器自己管理websocket的，但在springboot中连容器都是spring管理的。
 * <p>
 * 虽然@Component默认是单例模式的，但springboot还是会为每个websocket连接初始化一个bean，所以可以用一个静态set保存起来。
 */
@ServerEndpoint("/ws/websocket/{userId}") //WebSocket客户端建立连接的地址
@Component
@Slf4j
public class WebSocketServerEndpoint {
 
    /**
     * 	存活的session集合（使用线程安全的map保存）
     */
    private static Map<String, Session> livingSessions = new ConcurrentHashMap<>();
 
    /**
     *	 建立连接的回调方法
     *
     * @param session 与客户端的WebSocket连接会话
     * @param userId  用户名，WebSocket支持路径参数
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        //livingSessions.put(session.getId(), session);
      System.out.println("session.getId:"+session.getId());
        livingSessions.put(userId, session);
        System.out.println(userId + "进入连接");
    }
 
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") String userId) {
        System.out.println("接收到用户"+userId+"发送过来的消息" + " : " + message);
        // sendMessageToAll(userId + " : " + message);
       // recvMessage(userId, message);
    }
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        System.err.println(error.getStackTrace() + "");
    }
 
 
    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        livingSessions.remove(session.getId());
        System.out.println(userId + " 关闭连接");
    }
 
    /**
     * 	单独发送消息
     *
     * @param session
     * @param message
     */
    /*public void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    
    /**
     * 	单独发送消息
     *
     * @param userId	指定用户的id
     * @param message	要发送的消息
     * @return boolean  发送消息成功返回true，发送消息失败返回false
     */
    public boolean sendMessage(String userId, String message) {
        try {
        	Session session = livingSessions.get(userId);
        	if(session != null) {
        	  session.getBasicRemote().sendText(message);
        	  return true;
        	}else {
        	  return false;
        	}
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
 
    /**
     * 	群发消息
     *
     * @param message
     */
    public void sendMessageToAll(String message) {
        livingSessions.forEach((sessionId, session) -> {
       try {
				session.getBasicRemote().sendText(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
        });
    }
   /* public String recvMessage(String userId, String message) {
    	return message;
    }*/
 
}
