import { useState, useEffect } from 'react';
import { Badge } from 'antd';
import { useWebSocket } from '@/hooks/useWebSocket';
import { TaskLog } from '@/types';

/**
 * Hook for managing task logs with WebSocket
 */
export const useTaskLogs = (taskId: number | null, enabled = true) => {
  const [logs, setLogs] = useState<TaskLog[]>([]);
  const [progress, setProgress] = useState(0);
  const [isConnected, setIsConnected] = useState(false);
  const [completed, setCompleted] = useState(false);
  const [result, setResult] = useState<{ success: boolean; output?: string; error?: string } | null>(null);

  const { disconnect } = useWebSocket(taskId || null, {
    enabled,
    onLog: (level, message, source) => {
      const log: TaskLog = {
        id: Date.now() + Math.random(),
        taskId: taskId!,
        logLevel: level as any,
        message,
        timestamp: new Date().toISOString(),
        source,
      };
      setLogs((prev) => [...prev, log];
    },
    onProgress: (newProgress) => {
      setProgress(newProgress);
    },
    onComplete: (success, output, error) => {
      setCompleted(true);
      setProgress(100);
      setResult({ success, output, error });
    },
    onMessage: (message) => {
      if (message.type === 'welcome') {
        setIsConnected(true);
      }
    },
  });

  const clearLogs = () => {
    setLogs([]);
    setProgress(0);
    setCompleted(false);
    setResult(null);
  };

  return {
    logs,
    progress,
    isConnected,
    completed,
    result,
    clearLogs,
    disconnect,
  };
};

export default useTaskLogs;
