package com.habitbeat.backend.auth.controller;

import com.habitbeat.backend.auth.dto.*;
import com.habitbeat.backend.auth.service.AuthService;
import com.habitbeat.backend.auth.service.RateLimitService;
import com.habitbeat.backend.auth.util.CookieUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CookieUtil cookieUtil;
    private final RateLimitService rateLimitService;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Check your email to verify."));
    }

    @GetMapping("/verify")
    public ResponseEntity<AuthResponse> verifyEmail(
            @RequestParam String token,
            HttpServletRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.verifyEmail(token, request, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/verification/resend")
    public ResponseEntity<Map<String, String>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "If the email exists, we've sent a link."));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        
        String clientIp = httpRequest.getRemoteAddr();
        String rateLimitKey = "login:" + clientIp;
        
        if (!rateLimitService.isAllowed(rateLimitKey, 5, java.time.Duration.ofMinutes(15))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(null);
        }
        
        try {
            AuthResponse response = authService.login(request, httpRequest, httpResponse);
            rateLimitService.reset(rateLimitKey); // Reset on successful login
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Don't reset rate limit on failed login
            throw e;
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromCookies(request);
        AuthResponse response = authService.refresh(refreshToken);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromCookies(request);
        authService.logout(refreshToken);
        response.addCookie(cookieUtil.createExpiredRefreshTokenCookie());
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            String token = authHeader.substring(7);
            UUID userId = authService.getUserIdFromToken(token);
            authService.logoutAll(userId);
            response.addCookie(cookieUtil.createExpiredRefreshTokenCookie());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping("/password/forgot")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = httpRequest.getRemoteAddr();
        String rateLimitKey = "forgot:" + clientIp;
        
        if (!rateLimitService.isAllowed(rateLimitKey, 3, java.time.Duration.ofMinutes(60))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Too many requests. Please try again later."));
        }
        
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "If the email exists, we've sent a reset link."));
    }
    
    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated."));
    }
    
    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new RuntimeException("No refresh token found");
        }
        
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No refresh token found"));
    }
}