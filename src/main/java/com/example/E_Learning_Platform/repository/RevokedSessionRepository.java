package com.example.E_Learning_Platform.repository;

import com.example.E_Learning_Platform.entity.RevokedSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RevokedSessionRepository extends JpaRepository<RevokedSession, String> {
}
