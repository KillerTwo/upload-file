package org.lwt.recvwebsocket.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
/**
 * WebSocket配置
 * @author Administrator
 *
 */
@Configuration
public class WebSocketConfig {
 
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
 
}
