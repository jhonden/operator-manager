package com.operator.infrastructure.scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Handler for Task Logs
 *
 * Handles real-time task log streaming to clients
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
public class TaskLogWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(TaskLogWebSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());

        // Extract task ID from URL path: /ws/tasks/{taskId}
        String uri = session.getUri().toString();
        String taskId = extractTaskId(uri);

        if (taskId != null) {
            sessions.put(taskId, session);
            log.debug("Session registered for task: {}", taskId);

            // Send connection confirmation
            sendWelcomeMessage(session, taskId);
        } else {
            log.warn("No task ID found in URI: {}", uri);
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing task ID"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received message from {}: {}", session.getId(), payload);

        // Handle client messages (ping, status requests, etc.)
        if ("ping".equals(payload)) {
            session.sendMessage(new TextMessage("pong"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {} - {}", session.getId(), status.getReason());

        // Remove session
        sessions.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session: {}", session.getId(), exception);

        // Remove session
        sessions.entrySet().removeIf(entry -> entry.getValue().getId().equals(session.getId()));
    }

    /**
     * Broadcast log message to all subscribers for a task
     */
    public void broadcastLog(Long taskId, LogLevel level, String message, String source) {
        String taskIdStr = String.valueOf(taskId);
        WebSocketSession session = sessions.get(taskIdStr);

        if (session != null && session.isOpen()) {
            try {
                TaskLogMessage logMessage = new TaskLogMessage(
                        taskId,
                        level.toString(),
                        message,
                        source,
                        System.currentTimeMillis()
                );

                String json = objectMapper.writeValueAsString(logMessage);
                session.sendMessage(new TextMessage(json));

            } catch (IOException e) {
                log.error("Failed to send log message for task: {}", taskId, e);
            }
        }
    }

    /**
     * Broadcast task progress update
     */
    public void broadcastProgress(Long taskId, int progress, String message) {
        String taskIdStr = String.valueOf(taskId);
        WebSocketSession session = sessions.get(taskIdStr);

        if (session != null && session.isOpen()) {
            try {
                TaskProgressMessage progressMessage = new TaskProgressMessage(
                        taskId,
                        progress,
                        message,
                        System.currentTimeMillis()
                );

                String json = objectMapper.writeValueAsString(progressMessage);
                session.sendMessage(new TextMessage(json));

            } catch (IOException e) {
                log.error("Failed to send progress update for task: {}", taskId, e);
            }
        }
    }

    /**
     * Broadcast task completion
     */
    public void broadcastCompletion(Long taskId, boolean success, String output, String error) {
        String taskIdStr = String.valueOf(taskId);
        WebSocketSession session = sessions.get(taskIdStr);

        if (session != null && session.isOpen()) {
            try {
                TaskCompletionMessage completionMessage = new TaskCompletionMessage(
                        taskId,
                        success,
                        output,
                        error,
                        System.currentTimeMillis()
                );

                String json = objectMapper.writeValueAsString(completionMessage);
                session.sendMessage(new TextMessage(json));

            } catch (IOException e) {
                log.error("Failed to send completion message for task: {}", taskId, e);
            }
        }
    }

    /**
     * Send welcome message
     */
    private void sendWelcomeMessage(WebSocketSession session, String taskId) throws IOException {
        WelcomeMessage welcome = new WelcomeMessage(
                Long.parseLong(taskId),
                "Connected to task log stream",
                System.currentTimeMillis()
        );

        String json = objectMapper.writeValueAsString(welcome);
        session.sendMessage(new TextMessage(json));
    }

    /**
     * Extract task ID from URI
     */
    private String extractTaskId(String uri) {
        // URI format: /ws/tasks/{taskId}
        String[] parts = uri.split("/");
        if (parts.length >= 4) {
            return parts[parts.length - 1];
        }
        return null;
    }

    /**
     * Get active session count for a task
     */
    public int getSessionCount(Long taskId) {
        String taskIdStr = String.valueOf(taskId);
        WebSocketSession session = sessions.get(taskIdStr);
        return (session != null && session.isOpen()) ? 1 : 0;
    }

    // ==================== Message Types ====================

    private record TaskLogMessage(
            Long taskId,
            String level,
            String message,
            String source,
            long timestamp
    ) {
    }

    private record TaskProgressMessage(
            Long taskId,
            int progress,
            String message,
            long timestamp
    ) {
    }

    private record TaskCompletionMessage(
            Long taskId,
            boolean success,
            String output,
            String error,
            long timestamp
    ) {
    }

    private record WelcomeMessage(
            Long taskId,
            String message,
            long timestamp
    ) {
    }

    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}
