package org.lwt.recvwebsocket.rabbitmq;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.lwt.recvwebsocket.finals.RabbitConstant;
import org.lwt.recvwebsocket.websocket.WebSocketServerEndpoint;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import groovy.util.logging.Slf4j;
/**
 * 发送消息类
 * @author Administrator
 *
 */
@Component
@Slf4j
public class Sender implements RabbitTemplate.ConfirmCallback, ReturnCallback {
 
    @Autowired
    private RabbitTemplate rabbitTemplate;
 
    @Autowired
    private WebSocketServerEndpoint webSocketServerEndpoint;
    
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }
 
    //消息发送确认回调方法(到达服务器后就返回)
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
    	System.out.println("消息发送成功:" + correlationData);
    }
 
    //消息发送失败回调方法（）
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        System.out.println("消息发送失败:" + new String(message.getBody()));
    }
 
    /**
     * 发送消息，不需要实现任何接口，供外部调用
     *
     * @param messageVo
     */
    public void send(String msg) {
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(RabbitConstant.EXCHANGE, RabbitConstant.RK_QUALIFICATION, msg.getBytes(), 
        		(new MessagePostProcessor() {
					
					@Override
					public Message postProcessMessage(Message message) throws AmqpException {
						message.getMessageProperties().setReplyTo("callBackQueue"); 		// 设置默认队列
						return message;
					}
				}),
        		correlationId);
        System.out.println("发送消息成功。。。");
        webSocketServerEndpoint.sendMessageToAll(new String(msg));
    }
}
