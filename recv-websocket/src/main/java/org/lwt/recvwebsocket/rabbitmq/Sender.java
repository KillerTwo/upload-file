package org.lwt.recvwebsocket.rabbitmq;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.lwt.recvwebsocket.finals.RabbitConstant;
import org.lwt.recvwebsocket.websocket.WebSocketServerEndpoint;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import groovy.util.logging.Slf4j;
/**
 * 	发送消息类
 * @author Administrator
 *
 */
@Component
@Slf4j
public class Sender implements RabbitTemplate.ConfirmCallback, ReturnCallback {
 
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }
 
    // 消息发送确认回调方法(到达服务器后就返回)
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
    	System.out.println("confirm消息发送成功:" + correlationData);
    }
 
    // 消息发送失败回调方法（没有对于的消费者监听发送队列时返回）
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("returnedMessage消息发送失败:" + new String(message.getBody()));
    }
    /**
     * 	发送消息，不需要实现任何接口，供外部调用
     *
     * @param msg 发送的数据
     */
    public String send(String msg) {
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
        String response = (String) rabbitTemplate.convertSendAndReceive(RabbitConstant.MY_EXCHANGE, RabbitConstant.PK_MY_KEY, msg.getBytes(), 
        		correlationId);		// 发送数据接收消费者的响应数据
        
        //System.out.println("发送消息成功。。。");
        System.out.println("发送的消息为："+msg);
        //System.out.println("接收到消费者的响应消息："+response);
       return response;
    }
}
