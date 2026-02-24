package com.eazybrew.vend.nomba.service;

import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.nomba.dto.request.NombaTokenRequest;
import com.eazybrew.vend.nomba.dto.response.NombaTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

/**
 * Manages the Nomba OAuth2 access token with in-memory caching.
 * Refreshes the token 60 seconds before it expires.
 */
@Service
@Slf4j
public class NombaTokenService {

    @Value("${nomba.base-url}")
    private String baseUrl;

    @Value("${nomba.account-id}")
    private String accountId;

    @Value("${nomba.client-id}")
    private String clientId;

    @Value("${nomba.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;

    // In-memory token cache
    private String cachedToken = null;
    private Instant cachedTokenExpiry = Instant.EPOCH;

    public NombaTokenService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Returns a valid Nomba access token, refreshing if it's about to expire.
     */
    public synchronized String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(cachedTokenExpiry.minusSeconds(60))) {
            log.debug("Using cached Nomba access token");
            return cachedToken;
        }
        log.info("Refreshing Nomba access token");
        return fetchNewToken();
    }

    private String fetchNewToken() {
        String url = baseUrl + "/v1/auth/token/issue";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("accountId", accountId);

        NombaTokenRequest body = NombaTokenRequest.builder()
                .grantType("client_credentials")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();

        HttpEntity<NombaTokenRequest> request = new HttpEntity<>(body, headers);

        ResponseEntity<NombaTokenResponse> response = restTemplate.postForEntity(url, request,
                NombaTokenResponse.class);

        NombaTokenResponse responseBody = response.getBody();
        if (responseBody == null || !"00".equals(responseBody.getCode()) || responseBody.getData() == null) {
            log.error("Failed to obtain Nomba access token: {}", responseBody);
            throw new CustomException("Failed to authenticate with Nomba",
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        }

        cachedToken = responseBody.getData().getAccess_token();
        String expiresAt = responseBody.getData().getExpiresAt();
        // Parse ISO-8601 expiry from Nomba
        cachedTokenExpiry = Instant.parse(expiresAt);

        log.info("Nomba access token obtained, expires at {}", expiresAt);
        return cachedToken;
    }
}
