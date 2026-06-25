package com.gltu.labreservation.common;

import com.gltu.labreservation.entity.User;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class TokenStore {

    private final Map<String, LoginUser> tokens = new ConcurrentHashMap<>();

    public String issue(User user) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, new LoginUser(user.getId(), user.getRole()));
        return token;
    }

    public LoginUser get(String token) {
        return tokens.get(token);
    }

    public record LoginUser(Long userId, String role) {
    }
}

