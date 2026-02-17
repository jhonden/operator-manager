import { useEffect, useRef, useCallback } from 'react';
import { message } from 'antd';

interface WebSocketMessage {
  type?: string;
  taskId?: number;
  level?: string;
  message?: string;
  progress?: number;
  success?: boolean;
  output?: string;
  error?: string;
  source?: string;
  timestamp?: number;
}

interface UseWebSocketOptions {
  onLog?: (level: string, message: string, source?: string) => void;
  onProgress?: (progress: number, message?: string) => void;
  onComplete?: (success: boolean, output?: string, error?: string) => void;
  onMessage?: (message: WebSocketMessage) => void;
  reconnect?: boolean;
  reconnectInterval?: number;
}

interface UseWebSocketReturn {
  isConnected: boolean;
  sendMessage: (message: any) => void;
  disconnect: () => void;
}

/**
 * WebSocket hook for real-time task logs
 */
export const useWebSocket = (
  taskId: number | null,
  options: UseWebSocketOptions = {}
): UseWebSocketReturn => {
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout>();
  const isConnectedRef = useRef(false);

  const { onLog, onProgress, onComplete, onMessage, reconnect = true, reconnectInterval = 3000 } = options;

  const connect = useCallback(() => {
    if (!taskId) return;

    const wsUrl = `ws://localhost:8080/api/ws/tasks/${taskId}`;

    try {
      wsRef.current = new WebSocket(wsUrl);

      wsRef.current.onopen = () => {
        isConnectedRef.current = true;
        console.log('WebSocket connected');
      };

      wsRef.current.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data);

          if (onMessage) {
            onMessage(message);
          }

          switch (message.type || message) {
            case 'welcome':
              console.log('Connected to task log stream');
              break;

            case 'log':
              if (onLog && message.level && message.message) {
                onLog(message.level, message.message, message.source);
              }
              break;

            case 'progress':
              if (onProgress && message.progress !== undefined) {
                onProgress(message.progress, message.message);
              }
              break;

            case 'completion':
              if (onComplete && message.success !== undefined) {
                onComplete(
                  message.success,
                  message.output,
                  message.error
                );
              }
              // Close connection after completion
              if (wsRef.current) {
                wsRef.current.close();
              }
              break;
          }
        } catch (error) {
          console.error('Failed to parse WebSocket message:', error);
        }
      };

      wsRef.current.onerror = (error) => {
        console.error('WebSocket error:', error);
        message.error('WebSocket connection error');
      };

      wsRef.current.onclose = () => {
        isConnectedRef.current = false;
        console.log('WebSocket connection closed');

        // Attempt to reconnect if enabled
        if (reconnect) {
          reconnectTimeoutRef.current = setTimeout(() => {
            connect();
          }, reconnectInterval);
        }
      };

    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
      message.error('Failed to establish WebSocket connection');
    }
  }, [taskId, onLog, onProgress, onComplete, onMessage, reconnect, reconnectInterval]);

  const disconnect = useCallback(() => {
    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
    }
    isConnectedRef.current = false;

    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
    }
  }, []);

  const sendMessage = useCallback((message: any) => {
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify(message));
    }
  }, []);

  // Auto-connect when taskId changes
  useEffect(() => {
    if (taskId) {
      connect();
    }

    return () => {
      disconnect();
    };
  }, [taskId, connect, disconnect]);

  return {
    isConnected: isConnectedRef.current,
    sendMessage,
    disconnect,
  };
};

export default useWebSocket;
