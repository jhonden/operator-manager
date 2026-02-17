package com.operator.infrastructure.scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

/**
 * Redis Queue Service
 *
 * Manages task queue using Redis Sorted Set (for priority queue)
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Service
public class RedisQueueService {

    private static final Logger log = LoggerFactory.getLogger(RedisQueueService.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String TASK_QUEUE_KEY = "task:queue";

    public RedisQueueService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Enqueue task to priority queue
     *
     * @param taskId Task ID
     * @param priority Priority (higher value = higher priority)
     */
    public void enqueueTask(Long taskId, int priority) {
        log.debug("Enqueueing task: {} with priority: {}", taskId, priority);

        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        // Use negative priority so higher priority = higher score = first in queue
        zSetOps.add(TASK_QUEUE_KEY, taskId, -priority);
    }

    /**
     * Dequeue task with highest priority
     *
     * @return Task ID or null if queue is empty
     */
    public Long dequeueTask() {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        // Pop highest priority task (highest score)
        Object taskId = zSetOps.popMax(TASK_QUEUE_KEY);

        if (taskId != null) {
            log.debug("Dequeued task: {}", taskId);
            return (Long) taskId;
        }

        return null;
    }

    /**
     * Get queue size
     */
    public Long getQueueSize() {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Long size = zSetOps.size(TASK_QUEUE_KEY);
        return size != null ? size : 0L;
    }

    /**
     * Clear all tasks from queue
     */
    public void clearQueue() {
        log.warn("Clearing task queue");

        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        zSetOps.removeRange(TASK_QUEUE_KEY, 0, -1);
    }

    /**
     * Get all task IDs in queue (for debugging)
     */
    public java.util.Set<Object> getAllTasks() {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        return zSetOps.range(TASK_QUEUE_KEY, 0, -1);
    }

    /**
     * Requeue a failed task
     */
    public void requeueTask(Long taskId, int priority) {
        log.info("Requeuing task: {} with priority: {}", taskId, priority);

        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        zSetOps.add(TASK_QUEUE_KEY, taskId, -priority);
    }

    /**
     * Remove task from queue
     */
    public boolean removeTask(Long taskId) {
        log.debug("Removing task from queue: {}", taskId);

        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        return Boolean.TRUE.equals(zSetOps.remove(TASK_QUEUE_KEY, taskId));
    }

    /**
     * Check if task is in queue
     */
    public boolean isTaskQueued(Long taskId) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();
        Long rank = zSetOps.rank(TASK_QUEUE_KEY, taskId);
        return rank != null;
    }
}
