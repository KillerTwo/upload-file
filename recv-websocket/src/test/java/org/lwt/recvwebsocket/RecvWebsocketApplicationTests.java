package org.lwt.recvwebsocket;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.lwt.recvwebsocket.rabbitmq.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RecvWebsocketApplicationTests {
	@Autowired
	private Sender sender;
	@Test
	public void contextLoads() {
		sender.send("hello world...");
	}

}
