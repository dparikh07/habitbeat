package com.habitbeat.backend.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "email")
@Data
public class EmailProperties {
    private String host;
    private int port;
    private String username;
    private String password;
    private String fromAddress;
    private String fromName;
}