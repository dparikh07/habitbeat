package com.habitbeat.backend.auth.util;

import com.habitbeat.backend.auth.config.JwtProperties;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtil {
    private final JwtProperties jwtProperties;

    public Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // TODO: Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge((int) jwtProperties.getRefreshTokenExpiry().toSeconds());
        if (jwtProperties.getCookieDomain() != null) {
            cookie.setDomain(jwtProperties.getCookieDomain());
        }
        return cookie;
    }

    public Cookie createExpiredRefreshTokenCookie() {
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        if (jwtProperties.getCookieDomain() != null) {
            cookie.setDomain(jwtProperties.getCookieDomain());
        }
        return cookie;
    }
}