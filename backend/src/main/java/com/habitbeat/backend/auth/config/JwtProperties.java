package com.habitbeat.backend.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private String signingKey;
    private Duration accessTokenExpiry = Duration.ofMinutes(15);
    private Duration refreshTokenExpiry = Duration.ofDays(30);
    private int refreshTokenBytes = 32;
    private String cookieDomain;
}