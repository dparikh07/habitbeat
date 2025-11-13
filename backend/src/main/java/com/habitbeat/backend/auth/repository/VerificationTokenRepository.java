package com.habitbeat.backend.auth.repository;

import com.habitbeat.backend.auth.model.VerificationToken;
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
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByTokenHashAndPurposeAndConsumedAtIsNullAndExpiresAtAfter(
            String tokenHash, String purpose, LocalDateTime now);
    
    @Modifying
    @Query("UPDATE VerificationToken v SET v.consumedAt = :now WHERE v.user = :user AND v.purpose = :purpose AND v.consumedAt IS NULL")
    void invalidateUserTokens(User user, String purpose, LocalDateTime now);
    
    @Query("SELECT v FROM VerificationToken v WHERE v.purpose = :purpose AND v.consumedAt IS NULL AND v.expiresAt > :now")
    List<VerificationToken> findValidTokensByPurpose(String purpose, LocalDateTime now);
}