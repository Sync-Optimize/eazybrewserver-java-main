package com.eazybrew.vend.nomba.service;

import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.nomba.dto.request.NombaTerminalPaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NombaServiceImpl implements NombaService {

        @Value("${nomba.base-url}")
        private String baseUrl;

        @Value("${nomba.account-id}")
        private String accountId;

        private final NombaTokenService nombaTokenService;
        private final RestTemplate restTemplate;

        @Override
        public String pushPaymentToTerminal(String terminalId, String merchantTxRef, BigDecimal amountNaira) {
                String token = nombaTokenService.getAccessToken();

                String url = baseUrl + "/v1/terminals/payment-request/" + terminalId;

                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("accountId", accountId);

                // Nomba terminal API expects amount in Naira (whole number, not kobo)
                long amountInNaira = amountNaira.longValue();

                NombaTerminalPaymentRequest body = NombaTerminalPaymentRequest.builder()
                                .merchantTxRef(merchantTxRef)
                                .amount(amountInNaira)
                                .currency("NGN")
                                .build();

                HttpEntity<NombaTerminalPaymentRequest> request = new HttpEntity<>(body, headers);

                log.info("[Nomba] Pushing payment to terminal {}. URL={}, ref={}, amountNaira={}, sending={}",
                                terminalId, url, merchantTxRef, amountNaira, amountInNaira);

                try {
                        @SuppressWarnings("unchecked")
                        ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) (ResponseEntity<?>) restTemplate
                                        .postForEntity(URI.create(url), request, Map.class);
                        Map<String, Object> responseBody = response.getBody();
                        log.info("[Nomba] Response status={}, body={}", response.getStatusCode(), responseBody);

                        if (!response.getStatusCode().is2xxSuccessful() ||
                                        responseBody == null || !"00".equals(responseBody.get("code"))) {
                                String msg = responseBody != null
                                                ? String.valueOf(responseBody.getOrDefault("description",
                                                                responseBody.getOrDefault("message", "Unknown error")))
                                                : "No response body";
                                throw new CustomException("Nomba terminal push failed: " + msg, HttpStatus.BAD_GATEWAY);
                        }

                        // Extract Nomba's paymentId from the response data — this is what Nomba echoes
                        // back as merchantTxRef in webhook payloads, so we store it for lookup
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                        String nombaPaymentId = data != null ? String.valueOf(data.getOrDefault("paymentId", "")) : "";
                        log.info("[Nomba] Nomba paymentId={}", nombaPaymentId);
                        return nombaPaymentId;

                } catch (CustomException e) {
                        throw e;
                } catch (Exception e) {
                        log.error("[Nomba] Error pushing to terminal {}: {}", terminalId, e.getMessage(), e);
                        throw new CustomException("Failed to reach Nomba terminal: " + e.getMessage(),
                                        HttpStatus.BAD_GATEWAY);
                }
        }
}
