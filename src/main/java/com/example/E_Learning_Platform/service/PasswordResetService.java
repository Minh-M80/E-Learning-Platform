package com.example.E_Learning_Platform.service;

import com.example.E_Learning_Platform.dto.request.ResetPasswordRequest;
import com.example.E_Learning_Platform.entity.User;
import com.example.E_Learning_Platform.exception.AppException;
import com.example.E_Learning_Platform.exception.ErrorCode;
import com.example.E_Learning_Platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Value("${app.reset-password.token-ttl-minutes}")
    private long tokenTtlMinutes;

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String token = UUID.randomUUID().toString().replace("-", "");

        String redisKey = buildResetKey(token);
        stringRedisTemplate.opsForValue().set(
                redisKey,
                user.getEmail(),
                Duration.ofMinutes(tokenTtlMinutes)
        );

        mailService.sendResetToken(user.getEmail(), token, tokenTtlMinutes);
    }

    public void resetPassword(ResetPasswordRequest request) {
        String redisKey = buildResetKey(request.getToken());
        String email = stringRedisTemplate.opsForValue().get(redisKey);

        if (email == null || email.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        stringRedisTemplate.delete(redisKey);
    }

    private String buildResetKey(String token) {
        return "reset-password:" + token;
    }
}
