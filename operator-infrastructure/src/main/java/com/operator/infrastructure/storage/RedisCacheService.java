package com.operator.infrastructure.storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis Cache Service
 *
 * Provides caching operations using Redis
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Service
public class RedisCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== String Operations ====================

    /**
     * Set value
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * Set value with expiration
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * Set value with expiration (Duration)
     */
    public void set(String key, Object value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    /**
     * Get value
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Get value with type
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = get(key);
        return value != null ? (T) value : null;
    }

    /**
     * Delete key
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * Delete multiple keys
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * Check if key exists
     */
    public Boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Set expiration
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * Set expiration (Duration)
     */
    public Boolean expire(String key, Duration duration) {
        return redisTemplate.expire(key, duration);
    }

    /**
     * Get expiration time
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    // ==================== Hash Operations ====================

    /**
     * Set hash field
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * Get hash field
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * Get all hash fields
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * Delete hash fields
     */
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * Check if hash field exists
     */
    public Boolean hExists(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    // ==================== List Operations ====================

    /**
     * Push value to list (left)
     */
    public Long lPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * Push value to list (right)
     */
    public Long rPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * Pop value from list (left)
     */
    public Object lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * Pop value from list (right)
     */
    public Object rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * Get list range
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * Get list size
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // ==================== Set Operations ====================

    /**
     * Add to set
     */
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * Get all set members
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * Check if member exists in set
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * Remove from set
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    // ==================== Sorted Set Operations ====================

    /**
     * Add to sorted set
     */
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * Get range from sorted set (by score)
     */
    public Set<Object> zRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * Get range from sorted set with scores
     */
    public Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object>> zRangeByScoreWithScores(
            String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
    }

    /**
     * Remove from sorted set
     */
    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    // ==================== Common Utilities ====================

    /**
     * Get all keys matching pattern
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * Increment value
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * Increment by value
     */
    public Long incrementBy(String key, long value) {
        return redisTemplate.opsForValue().increment(key, value);
    }

    /**
     * Decrement value
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * Decrement by value
     */
    public Long decrementBy(String key, long value) {
        return redisTemplate.opsForValue().decrement(key, value);
    }

    // ==================== Cache-specific Methods ====================

    /**
     * Cache operator data
     */
    public void cacheOperator(Long operatorId, Object data) {
        String key = buildOperatorKey(operatorId);
        set(key, data, Duration.ofHours(1));
        log.debug("Cached operator: {}", operatorId);
    }

    /**
     * Get cached operator data
     */
    public Object getCachedOperator(Long operatorId) {
        String key = buildOperatorKey(operatorId);
        return get(key);
    }

    /**
     * Cache package data
     */
    public void cachePackage(Long packageId, Object data) {
        String key = buildPackageKey(packageId);
        set(key, data, Duration.ofHours(1));
        log.debug("Cached package: {}", packageId);
    }

    /**
     * Get cached package data
     */
    public Object getCachedPackage(Long packageId) {
        String key = buildPackageKey(packageId);
        return get(key);
    }

    /**
     * Cache user data
     */
    public void cacheUser(Long userId, Object data) {
        String key = buildUserKey(userId);
        set(key, data, Duration.ofMinutes(30));
        log.debug("Cached user: {}", userId);
    }

    /**
     * Get cached user data
     */
    public Object getCachedUser(Long userId) {
        String key = buildUserKey(userId);
        return get(key);
    }

    /**
     * Invalidate operator cache
     */
    public void invalidateOperator(Long operatorId) {
        String key = buildOperatorKey(operatorId);
        delete(key);
        log.debug("Invalidated operator cache: {}", operatorId);
    }

    /**
     * Invalidate package cache
     */
    public void invalidatePackage(Long packageId) {
        String key = buildPackageKey(packageId);
        delete(key);
        log.debug("Invalidated package cache: {}", packageId);
    }

    /**
     * Invalidate user cache
     */
    public void invalidateUser(Long userId) {
        String key = buildUserKey(userId);
        delete(key);
        log.debug("Invalidated user cache: {}", userId);
    }

    // ==================== Key Builders ====================

    private String buildOperatorKey(Long operatorId) {
        return "operator:" + operatorId;
    }

    private String buildPackageKey(Long packageId) {
        return "package:" + packageId;
    }

    private String buildUserKey(Long userId) {
        return "user:" + userId;
    }

    private String buildTaskKey(Long taskId) {
        return "task:" + taskId;
    }
}
