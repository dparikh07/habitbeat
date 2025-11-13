package com.habitbeat.backend.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public boolean isAllowed(String key, int maxAttempts, Duration window) {
        String currentCount = redisTemplate.opsForValue().get(key);
        
        if (currentCount == null) {
            redisTemplate.opsForValue().set(key, "1", window);
            return true;
        }
        
        int count = Integer.parseInt(currentCount);
        if (count >= maxAttempts) {
            return false;
        }
        
        redisTemplate.opsForValue().increment(key);
        return true;
    }
    
    public void reset(String key) {
        redisTemplate.delete(key);
    }
}