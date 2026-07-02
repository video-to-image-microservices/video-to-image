package video.to.image.management_ms.infra.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            return Optional.ofNullable((T) value);
        } catch (RuntimeException ex) {
            log.warn("Redis cache read failed for key={}: {}", key, ex.getMessage());
            return Optional.empty();
        }
    }

    public void put(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (RuntimeException ex) {
            log.warn("Redis cache write failed for key={}: {}", key, ex.getMessage());
        }
    }

    public void evict(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RuntimeException ex) {
            log.warn("Redis cache eviction failed for key={}: {}", key, ex.getMessage());
        }
    }
}