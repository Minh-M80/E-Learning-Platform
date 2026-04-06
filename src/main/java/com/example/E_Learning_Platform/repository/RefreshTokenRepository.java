package com.example.E_Learning_Platform.repository;

import com.example.E_Learning_Platform.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findBySessionIdAndRevokedFalse(String sessionId);
}
