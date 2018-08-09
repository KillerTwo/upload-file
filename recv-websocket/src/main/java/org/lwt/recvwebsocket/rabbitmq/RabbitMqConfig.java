package org.lwt.recvwebsocket.rabbitmq;

import org.lwt.recvwebsocket.finals.RabbitConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class RabbitMqConfig {
 
    /**
     * 声明队列
     *
     * @return
     */
    @Bean
    public Queue queueTransaction() {
        // true表示持久化该队列
        return new Queue(RabbitConstant.QUEUE_TRANSACTION, false);
    }
 
    @Bean
    public Queue queueContract() {
        // true表示持久化该队列
        return new Queue(RabbitConstant.QUEUE_CONTRACT, false);
    }
 
    @Bean
    public Queue queueQualification() {
        // true表示持久化该队列
        return new Queue(RabbitConstant.QUEUE_QUALIFICATION, false);
    }
    @Bean
    public Queue callBackQueue() {
    	return new Queue("callbackqueue", false);
    }
    
    /**
     * 声明交互器
     *
     * @return
     */
    @Bean
    DirectExchange directExchange() {
        return new DirectExchange(RabbitConstant.EXCHANGE);
    }
    @Bean
    public DirectExchange myExchange05() {
    	return new DirectExchange(RabbitConstant.MY_EXCHANGE);
    }
    /**
     * 绑定
     *
     * @return
     */
    @Bean
    public Binding bindingTransaction() {
        return BindingBuilder.bind(queueTransaction()).to(directExchange()).with(RabbitConstant.RK_TRANSACTION);
    }
 
    /**
     * 绑定
     *
     * @return
     */
    @Bean
    public Binding bindingContract() {
        return BindingBuilder.bind(queueContract()).to(directExchange()).with(RabbitConstant.RK_CONTRACT);
    }
 
    /**
     * 绑定
     *
     * @return
     */
    @Bean
    public Binding bindingQualification() {
        return BindingBuilder.bind(queueQualification()).to(directExchange()).with(RabbitConstant.RK_QUALIFICATION);
    }
    /**
     * 绑定
     * @return
     */
    @Bean
    public Binding bindingMyexchange() {
    	return BindingBuilder.bind(myExchange05()).to(myExchange05()).with(RabbitConstant.MY_EXCHANGE);
    }
 
}
