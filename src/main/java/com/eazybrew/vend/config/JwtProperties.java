package com.eazybrew.vend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret = "eazyBrewSecretKeyWhichShouldBeVeryLongAndSecureInProduction";
    private long expirationMs = 86400000; // 1 day
    private String tokenPrefix = "Bearer ";
    private String headerString = "Authorization";
}
