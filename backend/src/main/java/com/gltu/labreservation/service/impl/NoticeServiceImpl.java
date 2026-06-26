package com.gltu.labreservation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gltu.labreservation.entity.Notice;
import com.gltu.labreservation.mapper.NoticeMapper;
import com.gltu.labreservation.service.NoticeService;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    private static final String ADMIN_CACHE_KEY = "lab-reservation:notices:admin";
    private static final String PUBLIC_CACHE_KEY = "lab-reservation:notices:public";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final ObjectMapper objectMapper;

    public NoticeServiceImpl(ObjectProvider<StringRedisTemplate> redisTemplateProvider, ObjectMapper objectMapper) {
        this.redisTemplateProvider = redisTemplateProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Notice> listWithCache(String role) {
        boolean admin = "ADMIN".equalsIgnoreCase(role);
        String cacheKey = admin ? ADMIN_CACHE_KEY : PUBLIC_CACHE_KEY;
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            try {
                String cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    return objectMapper.readValue(cached, new TypeReference<List<Notice>>() {
                    });
                }
            } catch (Exception exception) {
                System.out.println("Redis notice cache read failed, fallback to database: " + exception.getMessage());
            }
        }

        List<Notice> notices = listFromDatabase(admin);
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(notices), CACHE_TTL);
            } catch (Exception exception) {
                System.out.println("Redis notice cache write failed: " + exception.getMessage());
            }
        }
        return notices;
    }

    @Override
    public void clearNoticeCache() {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.delete(List.of(ADMIN_CACHE_KEY, PUBLIC_CACHE_KEY));
        } catch (RedisConnectionFailureException exception) {
            System.out.println("Redis notice cache clear failed: " + exception.getMessage());
        }
    }

    private List<Notice> listFromDatabase(boolean admin) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .orderByDesc(Notice::getStatus)
                .orderByDesc(Notice::getCreateTime);
        if (!admin) {
            wrapper.in(Notice::getStatus, 1, 2);
        }
        return list(wrapper);
    }
}
