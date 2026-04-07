package com.example.E_Learning_Platform.repository;

import com.example.E_Learning_Platform.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findBySessionIdAndRevokedFalse(String sessionId);
}
