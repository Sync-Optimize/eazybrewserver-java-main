package com.eazybrew.vend.util;

import com.eazybrew.vend.model.enums.RecordStatusConstant;
import com.eazybrew.vend.repository.CompanyRepository;
import com.eazybrew.vend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneratorUtils {

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_-+=<>?";
    private static final String PASSWORD_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + SPECIAL_CHARS;
    private static final Random RANDOM = new SecureRandom();

    private final CompanyRepository companyRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Generate a secure random password with a mix of characters, numbers, and special characters
     * @param length The length of the password
     * @return A random password
     */
    public String generateSecurePassword(int length) {
        if (length < 8) {
            length = 8; // Minimum password length for security
        }

        StringBuilder password = new StringBuilder(length);

        // Ensure password contains at least one of each type of character
        password.append(CHAR_LOWER.charAt(RANDOM.nextInt(CHAR_LOWER.length())));
        password.append(CHAR_UPPER.charAt(RANDOM.nextInt(CHAR_UPPER.length())));
        password.append(NUMBER.charAt(RANDOM.nextInt(NUMBER.length())));
        password.append(SPECIAL_CHARS.charAt(RANDOM.nextInt(SPECIAL_CHARS.length())));

        // Fill the rest with random characters
        for (int i = 4; i < length; i++) {
            password.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }

        // Shuffle the password to make it more secure
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int j = RANDOM.nextInt(passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    /**
     * Generate a guaranteed unique API key for a company using a multi-layered approach
     * @return A unique API key
     */
    public String generateApiKey() {
        // First attempt: Standard random-based keys
        String apiKey = tryGenerateRandomApiKey();
        if (apiKey != null) {
            return apiKey;
        }

        // Second attempt: Time-based keys with UUID
        apiKey = tryGenerateTimeBasedApiKey();
        if (apiKey != null) {
            return apiKey;
        }

        // Final fallback: Guaranteed unique key with timestamp, counter, and random component
        return generateGuaranteedUniqueKey();
    }

    /**
     * Generate a unique transaction ID and ensure it doesn't already exist in the database
     * Format: TX-{timestamp}-{random}
     * @return A unique transaction ID
     */
    public String generateTransactionId() {
        final int MAX_ATTEMPTS = 10;
        String transactionId;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            // Get current timestamp in milliseconds
            long timestamp = Instant.now().toEpochMilli();

            // Generate a random component (6 digits)
            int randomComponent = 100000 + RANDOM.nextInt(900000);

            // Combine into a transaction ID
            transactionId = String.format("TX-%d-%d", timestamp, randomComponent);

            // Check if this transaction ID already exists
            if (!transactionRepository.existsByTransactionId(transactionId)) {
                if (attempt > 1) {
                    log.info("Generated unique transaction ID on attempt {}: {}", attempt, transactionId);
                }
                return transactionId;
            }

            // If we get here, there was a collision (extremely unlikely)
            log.warn("Transaction ID collision detected on attempt {}, regenerating...", attempt);

            // Add a small delay to ensure the next timestamp is different
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // If we've exhausted our regular attempts, use a guaranteed unique approach
        log.warn("Failed to generate unique transaction ID after {} attempts, using fallback method", MAX_ATTEMPTS);
        return generateGuaranteedUniqueTransactionId();
    }

    /**
     * Fallback method to generate a guaranteed unique transaction ID
     * This uses UUID and timestamp to ensure uniqueness
     * @return A guaranteed unique transaction ID
     */
    private String generateGuaranteedUniqueTransactionId() {
        String transactionId;
        int counter = 0;

        do {
            // Get current time with nanosecond precision
            long timestamp = System.nanoTime();

            // Use UUID for guaranteed uniqueness
            String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

            // Combine into a transaction ID
            transactionId = String.format("TX-%d-%s-%d", timestamp, uuid, counter++);

            // Extremely unlikely to need more than a couple iterations
            if (counter > 10) {
                // Last resort: use double UUID
                transactionId = "TX-" + UUID.randomUUID().toString().replace("-", "") +
                        "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                log.error("Using emergency transaction ID generation method: {}", transactionId);
                break;
            }

        } while (transactionRepository.existsByTransactionId(transactionId));

        log.info("Generated guaranteed unique transaction ID: {}", transactionId);
        return transactionId;
    }

    /**
     * First attempt: Try to generate a random API key
     * @return A unique API key or null if all attempts failed
     */
    private String tryGenerateRandomApiKey() {
        final int MAX_RANDOM_ATTEMPTS = 10;

        for (int attempt = 1; attempt <= MAX_RANDOM_ATTEMPTS; attempt++) {
            byte[] randomBytes = new byte[32];
            RANDOM.nextBytes(randomBytes);
            String candidate = "key_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

            if (!companyRepository.existsByApiKeyAndRecordStatus(candidate, RecordStatusConstant.ACTIVE)) {
                log.debug("Generated unique random API key on attempt {}", attempt);
                return candidate;
            }
        }

        log.warn("Failed to generate unique random API key after {} attempts", MAX_RANDOM_ATTEMPTS);
        return null;
    }

    /**
     * Second attempt: Generate time-based keys with UUID
     * @return A unique API key or null if all attempts failed
     */
    private String tryGenerateTimeBasedApiKey() {
        final int MAX_UUID_ATTEMPTS = 5;

        for (int attempt = 1; attempt <= MAX_UUID_ATTEMPTS; attempt++) {
            // Include timestamp as prefix to make collision extremely unlikely
            long timestamp = Instant.now().toEpochMilli();
            String candidate = "key_" + timestamp + "_" + UUID.randomUUID().toString().replace("-", "");

            if (!companyRepository.existsByApiKeyAndRecordStatus(candidate, RecordStatusConstant.ACTIVE)) {
                log.debug("Generated unique time-based API key on attempt {}", attempt);
                return candidate;
            }

            // Introduce a very small delay to ensure unique timestamp if we need to retry
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        log.warn("Failed to generate unique time-based API key after {} attempts", MAX_UUID_ATTEMPTS);
        return null;
    }

    /**
     * Final fallback: Generate an absolutely guaranteed unique key
     * This uses a combination of timestamp (down to nanoseconds), thread ID, and
     * a counter that keeps incrementing until we find a unique value
     *
     * @return A guaranteed unique API key
     */
    private String generateGuaranteedUniqueKey() {
        log.warn("Falling back to guaranteed unique key generation method");

        long counter = 0;
        String candidate;

        do {
            // Get current time with nanosecond precision
            long timestamp = System.nanoTime();

            // Get thread ID for additional uniqueness
            long threadId = Thread.currentThread().getId();

            // Create random bytes for more entropy
            byte[] randomBytes = new byte[8];
            RANDOM.nextBytes(randomBytes);
            String randomComponent = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

            // Combine all factors into a key
            candidate = String.format("key_%d_%d_%d_%s",
                    timestamp, threadId, counter++, randomComponent);

            // Extremely unlikely to need more than a couple iterations
            if (counter > 100) {
                log.error("Extreme anomaly: Failed to generate unique key after 100 attempts with guaranteed method");

                // Last resort: add millisecond sleep and generate with yet another approach
                try {
                    TimeUnit.MILLISECONDS.sleep(RANDOM.nextInt(50) + 50); // 50-100ms random sleep
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // This will be unique unless there's a serious system clock issue
                return "key_emergency_" + System.currentTimeMillis() + "_" +
                        UUID.randomUUID().toString().replace("-", "") + "_" +
                        UUID.randomUUID().toString().replace("-", "");
            }

            // Introduce a tiny delay if we need to retry
            if (companyRepository.existsByApiKeyAndRecordStatus(candidate, RecordStatusConstant.ACTIVE)) {
                try {
                    TimeUnit.MICROSECONDS.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        } while (companyRepository.existsByApiKeyAndRecordStatus(candidate, RecordStatusConstant.ACTIVE));

        log.info("Generated guaranteed unique API key after {} attempts", counter);
        return candidate;
    }

    /**
     * Generate a unique UUID string
     * @return A UUID string
     */
    public String generateUUID() {
        return UUID.randomUUID().toString();
    }
}