package com.operator.infrastructure.storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.operator.infrastructure.scheduler.TaskLogWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket Configuration
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    private final TaskLogWebSocketHandler taskLogWebSocketHandler;

    public WebSocketConfig(TaskLogWebSocketHandler taskLogWebSocketHandler) {
        this.taskLogWebSocketHandler = taskLogWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(taskLogWebSocketHandler, "/ws/tasks/{taskId}")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}
