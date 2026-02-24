package com.eazybrew.vend.nomba.controller;

import com.eazybrew.vend.model.Transaction;
import com.eazybrew.vend.model.enums.TransactionStatus;
import com.eazybrew.vend.repository.TransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Receives Nomba payment webhooks, verifies the HMAC-SHA256 signature,
 * and marks matching transactions as COMPLETED or FAILED.
 *
 * Endpoint must be registered in your Nomba dashboard under Webhook Settings.
 */
@Tag(name = "Nomba Management", description = "Nomba Terminal Webhook")
@RestController
@RequestMapping("/api/nomba")
@RequiredArgsConstructor
@Slf4j
public class NombaController {

    @Value("${nomba.webhook-secret}")
    private String webhookSecret;

    private final TransactionRepository transactionRepository;

    @Operation(summary = "Nomba payment webhook", description = "Receives payment completion notification from Nomba terminal")
    @PostMapping("/webhook")
    public void nombaWebhook(HttpServletRequest request, HttpServletResponse response) {
        // Always respond 200 quickly to prevent Nomba from retrying
        response.setStatus(200);

        String requestBody;
        try {
            requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8).trim();
        } catch (Exception e) {
            log.error("[Nomba Webhook] Failed to read request body: {}", e.getMessage());
            return;
        }

        log.info("[Nomba Webhook] Raw body received: {}", requestBody);

        String signature = request.getHeader("nomba-signature");
        if (signature == null) {
            signature = request.getHeader("nomba-sig-value");
        }
        String timestamp = request.getHeader("nomba-timestamp");
        String secret = webhookSecret != null ? webhookSecret.trim().replaceAll("^\"|\"$", "") : null;

        if (signature == null || timestamp == null || secret == null || secret.isBlank()) {
            log.warn(
                    "[Nomba Webhook] Missing signature headers or webhook secret is not configured. Proceeding with logging only.");
        } else {
            // -------------------------------------------------------
            // NOMBA SIGNATURE VERIFICATION
            // Signed string format (from Nomba spec):
            // eventType:requestId:userId:walletId:transactionId:transactionType:transactionTime:transactionResponseCode:timestamp
            // -------------------------------------------------------
            try {
                JSONObject body = new JSONObject(requestBody);

                String eventType = body.optString("event_type", "");
                String requestId = body.optString("requestId", "");

                JSONObject data = body.optJSONObject("data");
                JSONObject merchant = data != null ? data.optJSONObject("merchant") : null;
                JSONObject transaction = data != null ? data.optJSONObject("transaction") : null;

                String userId = merchant != null ? merchant.optString("userId", "") : "";
                String walletId = merchant != null ? merchant.optString("walletId", "") : "";
                String transactionId = transaction != null ? transaction.optString("transactionId", "") : "";
                String transactionType = transaction != null ? transaction.optString("type", "") : "";
                String transactionTime = transaction != null ? transaction.optString("time", "") : "";
                String transactionResponseCode = transaction != null ? transaction.optString("responseCode", "") : "";
                if ("null".equals(transactionResponseCode))
                    transactionResponseCode = "";

                String signedPayload = eventType + ":" + requestId + ":" + userId + ":" + walletId + ":"
                        + transactionId + ":" + transactionType + ":" + transactionTime + ":"
                        + transactionResponseCode + ":" + timestamp;

                log.debug("[Nomba Webhook] Signed payload string: {}", signedPayload);

                String expectedHex = hmacSha256Hex(secret, signedPayload);
                String expectedBase64 = hmacSha256Base64(secret, signedPayload);

                log.debug("[Nomba Webhook] Expected (hex): {}", expectedHex);
                log.debug("[Nomba Webhook] Expected (base64): {}", expectedBase64);
                log.debug("[Nomba Webhook] Received signature: {}", signature);

                if (!signature.equals(expectedHex) && !signature.equals(expectedBase64)) {
                    log.warn("[Nomba Webhook] Invalid signature — request rejected.");
                    return;
                }
                log.info("[Nomba Webhook] Signature verified successfully.");
            } catch (Exception e) {
                log.error("[Nomba Webhook] Signature verification failed: {}", e.getMessage(), e);
                return;
            }
        }

        // -------------------------------------------------------
        // PROCESS PAYLOAD
        // -------------------------------------------------------
        try {
            JSONObject body = new JSONObject(requestBody);
            String eventType = body.optString("event_type", "");
            JSONObject data = body.optJSONObject("data");
            JSONObject txObj = data != null ? data.optJSONObject("transaction") : null;
            JSONObject orderObj = data != null ? data.optJSONObject("order") : null;

            // Try all reference candidates in order of priority
            String merchantTxRef = null;
            if (txObj != null && !txObj.optString("merchantTxRef", "").isBlank()) {
                merchantTxRef = txObj.optString("merchantTxRef");
            } else if (orderObj != null && !orderObj.optString("orderReference", "").isBlank()) {
                merchantTxRef = orderObj.optString("orderReference");
            } else if (orderObj != null && !orderObj.optString("orderId", "").isBlank()) {
                merchantTxRef = orderObj.optString("orderId");
            } else if (data != null && !data.optString("paymentId", "").isBlank()) {
                merchantTxRef = data.optString("paymentId");
            }

            log.info("[Nomba Webhook] event_type={}, merchantTxRef={}", eventType, merchantTxRef);

            if (merchantTxRef == null || merchantTxRef.isBlank()) {
                log.warn("[Nomba Webhook] No recognizable transaction reference in payload — ignoring.");
                return;
            }

            Optional<Transaction> transactionOptional = transactionRepository.findByReferenceNumber(merchantTxRef);
            if (transactionOptional.isEmpty()) {
                log.warn("[Nomba Webhook] No transaction found for ref: {}", merchantTxRef);
                return;
            }

            Transaction tx = transactionOptional.get();

            if ("payment_success".equalsIgnoreCase(eventType) || "SUCCESS".equalsIgnoreCase(eventType)) {
                if (TransactionStatus.COMPLETED.equals(tx.getStatus())) {
                    log.info("[Nomba Webhook] Transaction {} already COMPLETED, skipping.", merchantTxRef);
                    return;
                }
                tx.setStatus(TransactionStatus.COMPLETED);
                transactionRepository.save(tx);
                log.info("[Nomba Webhook] Transaction {} marked as COMPLETED.", merchantTxRef);

            } else if ("payment_failed".equalsIgnoreCase(eventType) || "FAILED".equalsIgnoreCase(eventType)) {
                tx.setStatus(TransactionStatus.FAILED);
                transactionRepository.save(tx);
                log.info("[Nomba Webhook] Transaction {} marked as FAILED.", merchantTxRef);

            } else {
                log.info("[Nomba Webhook] Unrecognised event '{}' — no status change made.", eventType);
            }

        } catch (Exception e) {
            log.error("[Nomba Webhook] Error processing payload: {}", e.getMessage(), e);
        }
    }

    // ---- HMAC Helpers ----

    private String hmacSha256Hex(String secret, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] hash = computeHmac(secret, data);
        return HexFormat.of().formatHex(hash);
    }

    private String hmacSha256Base64(String secret, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] hash = computeHmac(secret, data);
        return Base64.getEncoder().encodeToString(hash);
    }

    private byte[] computeHmac(String secret, String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }
}
