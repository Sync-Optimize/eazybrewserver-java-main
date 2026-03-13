package com.eazybrew.vend.service.impl;

import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.model.User;
import com.eazybrew.vend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final ResourceLoader resourceLoader;

    @Value("${brevo.api.url}")
    private String brevoApiUrl;

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.from.email}")
    private String fromEmail;

    @Value("${brevo.from.name}")
    private String fromName;

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Boolean> sendCompanyCreationEmailAsync(Company company, User adminUser, String temporaryPassword) {
        log.info("Sending company creation email asynchronously to: {}", adminUser.getEmail());
        boolean result = sendCompanyCreationEmail(company, adminUser, temporaryPassword);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public boolean sendCompanyCreationEmail(Company company, User adminUser, String temporaryPassword) {
        try {
            String htmlTemplate = loadEmailTemplate("templates/company-account-created.html");

            // Format the current date
            String createdDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // Replace placeholders in the template
            String htmlContent = htmlTemplate
                    .replace("{{companyName}}", company.getName())
                    .replace("{{adminName}}", adminUser.getFullName())
                    .replace("{{adminEmail}}", adminUser.getEmail())
                    .replace("{{temporaryPassword}}", temporaryPassword)
                    .replace("{{createdDate}}", createdDate);

            return sendEmail(
                    adminUser.getEmail(),
                    adminUser.getFullName(),
                    "Your EazyBrew Company Account Has Been Created",
                    htmlContent
            );
        } catch (Exception e) {
            log.error("Failed to send company creation email", e);
            return false;
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Boolean> sendPasswordChangedEmailAsync(User user) {
        log.info("Sending password changed notification email asynchronously to: {}", user.getEmail());
        try {
            String htmlTemplate = loadEmailTemplate("templates/password-changed.html");

            // Replace placeholders in the template
            String htmlContent = htmlTemplate
                    .replace("{{userName}}", user.getEmail());

            boolean result = sendEmail(
                    user.getEmail(),
                    user.getFullName(),
                    "EazyBrew - Your Password Has Been Changed",
                    htmlContent
            );

            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Failed to send password changed notification email", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Boolean> sendPasswordResetEmailAsync(User user, String code, String subject) {
        log.info("Sending password reset notification email asynchronously to: {}", user.getEmail());
        try {
            String htmlTemplate = loadEmailTemplate("templates/password-reset.html");

            // Replace placeholders in the template
            String htmlContent = htmlTemplate
                    .replace("{{userName}}", user.getEmail())
                    .replace("{{code}}", code);

            boolean result = sendEmail(
                    user.getEmail(),
                    user.getEmail(),
                    subject,
                    htmlContent
            );

            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Failed to send password reset notification email", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    public CompletableFuture<Boolean> sendWelcomeEmailAsync(User user) {
        log.info("Sending welcome email asynchronously to: {}", user.getEmail());
        try {
            String htmlTemplate = loadEmailTemplate("templates/welcome.html");

            // Replace placeholders in the template
            String htmlContent = htmlTemplate
                    .replace("{{userEmail}}", user.getEmail());

            boolean result = sendEmail(
                    user.getEmail(),
                    user.getEmail(),
                    "Welcome to EazyBrew!",
                    htmlContent
            );

            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            log.error("Failed to send welcome email", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    @Override
    public boolean sendEmail(String toEmail, String toName, String subject, String htmlContent) {
        BufferedReader br = null;
        HttpURLConnection conn = null;
        StringBuffer response = new StringBuffer();

        log.info("========== BREVO EMAIL DEBUG START ==========");
        log.info("Brevo API URL: {}", brevoApiUrl);
        log.info("Brevo API Key (first 20 chars): {}", brevoApiKey != null && brevoApiKey.length() > 20 ? brevoApiKey.substring(0, 20) + "..." : brevoApiKey);
        log.info("From Email: {}", fromEmail);
        log.info("From Name: {}", fromName);
        log.info("To Email: {}", toEmail);
        log.info("To Name: {}", toName);
        log.info("Subject: {}", subject);

        try {
            URL url = new URL(brevoApiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("api-key", brevoApiKey);

            // Build the Brevo-compatible JSON payload
            JSONObject payload = new JSONObject();

            // Sender details
            JSONObject sender = new JSONObject();
            sender.put("email", fromEmail);
            sender.put("name", fromName);
            payload.put("sender", sender);

            // Recipient details
            JSONArray toArray = new JSONArray();
            JSONObject recipient = new JSONObject();
            recipient.put("email", toEmail);
            recipient.put("name", toName);
            toArray.put(recipient);
            payload.put("to", toArray);

            // Email content
            payload.put("subject", subject);
            payload.put("htmlContent", htmlContent);

            // Log the payload (without htmlContent to keep logs readable)
            JSONObject logPayload = new JSONObject(payload.toString());
            logPayload.put("htmlContent", "[HTML_CONTENT_OMITTED - " + htmlContent.length() + " chars]");
            log.info("Request Payload: {}", logPayload.toString(2));

            // Send the request
            OutputStream os = conn.getOutputStream();
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();
            log.info("Request sent to Brevo API");

            // Get response
            int responseCode = conn.getResponseCode();
            log.info("Response Code: {}", responseCode);

            if (responseCode >= 200 && responseCode < 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                log.info("SUCCESS - Email sent to {}: {}", toEmail, response.toString());
                log.info("========== BREVO EMAIL DEBUG END ==========");
                return true;
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                log.error("FAILED - Email to {}: HTTP {} - {}", toEmail, responseCode, response.toString());
                log.info("========== BREVO EMAIL DEBUG END ==========");
                return false;
            }
        } catch (Exception e) {
            log.error("EXCEPTION while sending email to {}: {}", toEmail, e.getMessage(), e);
            log.info("========== BREVO EMAIL DEBUG END ==========");
            return false;

        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (Exception e) {
                log.error("Error closing resources", e);
            }
        }
    }

    /**
     * Load an email template from the classpath resources
     *
     * @param templatePath The path to the template file
     * @return The template content as a string
     * @throws IOException If the template cannot be loaded
     */
    private String loadEmailTemplate(String templatePath) throws IOException {
        try {
            Resource resource = resourceLoader.getResource("classpath:" + templatePath);
            byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load email template: " + templatePath, e);
            throw new CustomException("Failed to load email template", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}