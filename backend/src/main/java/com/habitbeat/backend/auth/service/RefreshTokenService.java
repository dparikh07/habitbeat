package com.habitbeat.backend.auth.service;

import com.habitbeat.backend.auth.config.JwtProperties;
import com.habitbeat.backend.auth.model.Session;
import com.habitbeat.backend.auth.model.User;
import com.habitbeat.backend.auth.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final SessionRepository sessionRepository;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateRefreshToken() {
        byte[] tokenBytes = new byte[jwtProperties.getRefreshTokenBytes()];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    public Session createSession(User user, String refreshToken, String ip, String userAgent) {
        Session session = new Session();
        session.setUser(user);
        session.setSessionTokenHash(passwordEncoder.encode(refreshToken));
        session.setExpiresAt(LocalDateTime.now().plus(jwtProperties.getRefreshTokenExpiry()));
        session.setIp(ip);
        session.setUserAgent(userAgent);
        session.setLastUsedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    public Optional<Session> validateAndRotateToken(String refreshToken) {
        List<Session> validSessions = sessionRepository.findValidSessions(LocalDateTime.now());
        return validSessions.stream()
                .filter(s -> passwordEncoder.matches(refreshToken, s.getSessionTokenHash()))
                .findFirst()
                .map(session -> {
                    String newToken = generateRefreshToken();
                    session.setSessionTokenHash(passwordEncoder.encode(newToken));
                    session.setLastUsedAt(LocalDateTime.now());
                    return sessionRepository.save(session);
                });
    }
    
    public Optional<Session> findValidSession(String refreshToken) {
        List<Session> validSessions = sessionRepository.findValidSessions(LocalDateTime.now());
        return validSessions.stream()
                .filter(s -> passwordEncoder.matches(refreshToken, s.getSessionTokenHash()))
                .findFirst();
    }

    public void revokeSession(Session session) {
        session.setRevokedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }
}