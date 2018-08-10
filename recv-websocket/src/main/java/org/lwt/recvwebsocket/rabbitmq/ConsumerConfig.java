/*package org.lwt.recvwebsocket.rabbitmq;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

import org.lwt.recvwebsocket.finals.RabbitConstant;
import org.lwt.recvwebsocket.websocket.WebSocketServerEndpoint;
import org.lwt.serverupload.tools.JsonUtil;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
 
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
 
*//**
 * 	配置RabbitMq消费者
 * @author Administrator
 *
 *//*
 
@Configuration
@EnableRabbit
@groovy.util.logging.Slf4j
public class ConsumerConfig implements RabbitListenerConfigurer {
 
    @Autowired
    private ConnectionFactory connectionFactory;
 
    @Autowired
    private WebSocketServerEndpoint webSocketServerEndpoint;
    
    @Autowired
    private Sender sender;
    @Bean
    public DefaultMessageHandlerMethodFactory handlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setMessageConverter(new MappingJackson2MessageConverter());
        return factory;
    }
 
    @Bean
    public SimpleMessageListenerContainer messageContainer(@Qualifier("queueTransaction") Queue transaction, @Qualifier("queueContract") Queue contract, @Qualifier("queueQualification") Queue qualification) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueues(transaction, contract, qualification);
        container.setMaxConcurrentConsumers(1);
        container.setConcurrentConsumers(1);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL); //设置确认模式手工确认
        container.setMessageListener(new ChannelAwareMessageListener() {
            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
            	System.err.println("接收消息");
            	*//*****************发送响应消息*******************//*
            	Map<String, Object> responseMap = 
            			new HashMap<>();
            	responseMap.put("packid", "121");
            	responseMap.put("status", "0");
            	String response = JsonUtil.getJsonFromMap(responseMap);
            	String response = "这是响应消息<>";
            	//com.rabbitmq.client.BasicProperties props =  (com.rabbitmq.client.BasicProperties) message.getMessageProperties();
            	 BasicProperties replyProps = new BasicProperties()
                 		.builder().build();
            	 //System.out.println("回传队列名："+message.getMessageProperties().getReplyTo());
            	channel.basicPublish("", 
            			message.getMessageProperties().getReplyTo(), replyProps, 
						response.getBytes("UTF-8"));
            	channel.basicPublish("", 
            			"callbackqueue", replyProps, 
						response.getBytes("UTF-8"));
            	
            	*//***********************************//*
                byte[] body = message.getBody();
                System.out.println("receive msg : " + new String(body));
                try {
                	// 使用socket推送接收到的消息。
                    webSocketServerEndpoint.sendMessageToAll(new String(body));
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);//确认消息成功消费
                    System.err.println("消息推送成功。。。");
                } catch (IOException e) {
                    System.err.println("消息推送前台出错：" + e.getMessage() + "/r/n重新发送");
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true); //重新发送
                }
            }
        });
        return container;
    }
 
    //rabbitmqTemplate监听返回队列的消息
    *//**
     * 	在此处消费者返回的响应，并做相应的处理（重发）
     * 	@return
     *//*
    @Bean
    public SimpleMessageListenerContainer replyListenerContainer() {
    	 SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
         //container.setQueues("");
    	 container.setQueueNames("callbackqueue");
         container.setMaxConcurrentConsumers(1);
         container.setConcurrentConsumers(1);
         container.setAcknowledgeMode(AcknowledgeMode.MANUAL); //设置确认模式手工确认
         container.setMessageListener(new ChannelAwareMessageListener() {
             @Override
             public void onMessage(Message message, Channel channel) throws Exception {
             	System.err.println("接收回传消息。。。");
             	*//***********************************//*
                 byte[] body = message.getBody();
                 System.err.println("回传消息 : " + new String(body));
                 //if(new String(body) == "") {   // 重发数据
                	 sender.send("这是重发数据。。。");
                 //}
                 try {
                 	// 使用socket推送接收到的消息。
                     //webSocketServerEndpoint.sendMessageToAll(new String(body));
                     channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);//确认消息成功消费
                     //System.err.println("消息推送成功。。。");
                 } catch (IOException e) {
                     //System.err.println("消息推送前台出错：" + e.getMessage() + "/r/n重新发送");
                     channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true); //重新发送
                 }
             }
         });
         
         return container;
    }
    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
        rabbitListenerEndpointRegistrar.setMessageHandlerMethodFactory(handlerMethodFactory());
    }
}

*/