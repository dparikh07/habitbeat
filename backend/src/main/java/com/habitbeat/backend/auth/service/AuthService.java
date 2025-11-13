package com.habitbeat.backend.auth.service;

import com.habitbeat.backend.auth.dto.AuthResponse;
import com.habitbeat.backend.auth.dto.SignupRequest;
import com.habitbeat.backend.auth.dto.LoginRequest;
import com.habitbeat.backend.auth.model.*;
import com.habitbeat.backend.auth.repository.*;
import com.habitbeat.backend.auth.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final PasswordCredentialRepository passwordCredentialRepository;
    private final UserProfileRepository userProfileRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            // Don't reveal if email exists - same response time
            return;
        }

        User user = new User();
        user.setEmail(request.getEmail().toLowerCase());
        user.setProfileSetupDone(false);
        user = userRepository.save(user);

        PasswordCredential credential = new PasswordCredential();
        credential.setUser(user);
        credential.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        passwordCredentialRepository.save(credential);

        String rawToken = generateVerificationToken();
        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setPurpose("email_verify");
        token.setTokenHash(passwordEncoder.encode(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        tokenRepository.save(token);

        emailService.sendVerificationEmail(user.getEmail(), rawToken);
    }

    @Transactional
    public AuthResponse verifyEmail(String token, HttpServletRequest request, HttpServletResponse response) {
        List<VerificationToken> validTokens = tokenRepository.findValidTokensByPurpose("email_verify", LocalDateTime.now());
        VerificationToken verificationToken = validTokens.stream()
                .filter(t -> passwordEncoder.matches(token, t.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        User user = verificationToken.getUser();
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        verificationToken.setConsumedAt(LocalDateTime.now());
        tokenRepository.save(verificationToken);

        String refreshToken = refreshTokenService.generateRefreshToken();
        refreshTokenService.createSession(user, refreshToken, 
                request.getRemoteAddr(), request.getHeader("User-Agent"));

        // Set refresh token cookie
        response.addCookie(cookieUtil.createRefreshTokenCookie(refreshToken));

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        
        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        return authResponse;
    }

    @Transactional
    public void resendVerification(String email) {
        userRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
            if (user.getEmailVerifiedAt() != null) return;

            tokenRepository.invalidateUserTokens(user, "email_verify", LocalDateTime.now());

            String rawToken = generateVerificationToken();
            VerificationToken token = new VerificationToken();
            token.setUser(user);
            token.setPurpose("email_verify");
            token.setTokenHash(passwordEncoder.encode(rawToken));
            token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            tokenRepository.save(token);

            emailService.sendVerificationEmail(user.getEmail(), rawToken);
        });
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        if (user.getEmailVerifiedAt() == null) {
            throw new RuntimeException("Email not verified");
        }
        
        PasswordCredential credential = passwordCredentialRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        String refreshToken = refreshTokenService.generateRefreshToken();
        refreshTokenService.createSession(user, refreshToken,
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        
        // Set refresh token cookie
        httpResponse.addCookie(cookieUtil.createRefreshTokenCookie(refreshToken));
        
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        return response;
    }
    
    public AuthResponse refresh(String refreshToken) {
        Session session = refreshTokenService.validateAndRotateToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        String accessToken = jwtService.generateAccessToken(session.getUser().getId(), session.getUser().getEmail());
        
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        return response;
    }
    
    @Transactional
    public void logout(String refreshToken) {
        Session session = refreshTokenService.findValidSession(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid session"));
        
        refreshTokenService.revokeSession(session);
    }
    
    @Transactional
    public void logoutAll(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        sessionRepository.revokeAllUserSessions(user, LocalDateTime.now());
    }
    
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
            tokenRepository.invalidateUserTokens(user, "password_reset", LocalDateTime.now());
            
            String rawToken = generateVerificationToken();
            VerificationToken token = new VerificationToken();
            token.setUser(user);
            token.setPurpose("password_reset");
            token.setTokenHash(passwordEncoder.encode(rawToken));
            token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            tokenRepository.save(token);
            
            emailService.sendPasswordResetEmail(user.getEmail(), rawToken);
        });
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword) {
        List<VerificationToken> validTokens = tokenRepository.findValidTokensByPurpose("password_reset", LocalDateTime.now());
        VerificationToken verificationToken = validTokens.stream()
                .filter(t -> passwordEncoder.matches(token, t.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));
        
        User user = verificationToken.getUser();
        
        PasswordCredential credential = passwordCredentialRepository.findByUser(user)
                .orElse(new PasswordCredential());
        credential.setUser(user);
        credential.setPasswordHash(passwordEncoder.encode(newPassword));
        passwordCredentialRepository.save(credential);
        
        verificationToken.setConsumedAt(LocalDateTime.now());
        tokenRepository.save(verificationToken);
        
        sessionRepository.revokeAllUserSessions(user, LocalDateTime.now());
    }
    
    public UUID getUserIdFromToken(String token) {
        return jwtService.getUserIdFromToken(token);
    }
    
    private String generateVerificationToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}