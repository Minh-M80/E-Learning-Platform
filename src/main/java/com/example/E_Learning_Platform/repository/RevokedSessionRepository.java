package com.example.E_Learning_Platform.repository;

import com.example.E_Learning_Platform.entity.RevokedSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedSessionRepository extends JpaRepository<RevokedSession, String> {
}
