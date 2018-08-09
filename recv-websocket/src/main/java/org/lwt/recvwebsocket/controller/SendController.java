package org.lwt.recvwebsocket.controller;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import org.lwt.recvwebsocket.rabbitmq.Sender;
import org.lwt.recvwebsocket.websocket.WebSocketServerEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SendController {
	
	@Autowired
	private Sender sender;
	@Autowired
	private WebSocketServerEndpoint webSocketServerEndpoint;
	
	
	@GetMapping(value="/sends")
	public String send() {
		//webSocketServerEndpoint.sendMessage(session, "this is controller.");
		//System.out.println("controller中获取session值："+session.getId());
		sender.send("hello world...");
		return "发送成功";
	}
}
