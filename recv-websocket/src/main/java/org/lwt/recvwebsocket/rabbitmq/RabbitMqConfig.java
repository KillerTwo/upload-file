package org.lwt.recvwebsocket.rabbitmq;

import org.lwt.recvwebsocket.finals.RabbitConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 	配置队列和交换机
 * @author Administrator
 *
 */
@Configuration
public class RabbitMqConfig {
 
    /**
     *	 声明队列
     *
     * @return
     */
	@Bean
    public Queue queueSpringBoot() {
        // true表示不持久化该队列
        return new Queue(RabbitConstant.QUEUE_SPRING_BOOT, false);
    }
	
    /**
     * 	声明交互器
     *
     * @return
     */
    @Bean
    DirectExchange directExchange() {
        return new DirectExchange(RabbitConstant.MY_EXCHANGE);
    }
    
    /**
     * 	绑定
     * @return
     */
    @Bean
    public Binding bindingMyExchange() {
        return BindingBuilder.bind(queueSpringBoot()).to(directExchange()).with(RabbitConstant.PK_MY_KEY);
    }

}
