package com.habitbeat.backend.auth.repository;

import com.habitbeat.backend.auth.model.Session;
import com.habitbeat.backend.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findBySessionTokenHashAndExpiresAtAfterAndRevokedAtIsNull(
            String tokenHash, LocalDateTime now);
    
    List<Session> findByUserAndRevokedAtIsNull(User user);
    
    @Modifying
    @Query("UPDATE Session s SET s.revokedAt = :now WHERE s.user = :user AND s.revokedAt IS NULL")
    void revokeAllUserSessions(User user, LocalDateTime now);
    
    @Query("SELECT s FROM Session s WHERE s.revokedAt IS NULL AND s.expiresAt > :now")
    List<Session> findValidSessions(LocalDateTime now);
}