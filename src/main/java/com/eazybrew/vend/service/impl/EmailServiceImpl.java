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

    @Value("${zeptomail.api.url}")
    private String zeptoApiUrl;

    @Value("${zeptomail.api.key}")
    private String zeptoApiKey;

    @Value("${zeptomail.from.email}")
    private String fromEmail;

    @Value("${zeptomail.from.name}")
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
    public CompletableFuture<Boolean> sendPasswordResetEmailAsync(User user,String code, String subject) {
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
            log.error("Failed to send password ret notification email", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    @Override
    public boolean sendEmail(String toEmail, String toName, String subject, String htmlContent) {
        BufferedReader br = null;
        HttpURLConnection conn = null;
        StringBuffer response = new StringBuffer();

        try {
            URL url = new URL(zeptoApiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", "Zoho-enczapikey " + zeptoApiKey);

            // Create JSON payload
            JSONObject payload = new JSONObject();

            // From details
            JSONObject from = new JSONObject();
            from.put("address", fromEmail);
            from.put("name", fromName);
            payload.put("from", from);

            // To details
            JSONArray toArray = new JSONArray();
            JSONObject recipient = new JSONObject();
            JSONObject emailAddress = new JSONObject();
            emailAddress.put("address", toEmail);
            emailAddress.put("name", toName);
            recipient.put("email_address", emailAddress);
            toArray.put(recipient);
            payload.put("to", toArray);

            // Email content
            payload.put("subject", subject);
            payload.put("htmlbody", htmlContent);

            // Send the request
            OutputStream os = conn.getOutputStream();
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            os.flush();

            // Get response
            int responseCode = conn.getResponseCode();

            if (responseCode >= 200 && responseCode < 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                log.info("Email sent successfully to {}: {}", toEmail, response.toString());
                return true;
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                log.error("Failed to send email to {}: {} - {}", toEmail, responseCode, response.toString());
                return false;
            }
        } catch (Exception e) {
            log.error("Exception while sending email to " + toEmail, e);
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