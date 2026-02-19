package com.eazybrew.vend.service;

import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.model.User;

import java.util.concurrent.CompletableFuture;

public interface EmailService {

    /**
     * Send a welcome email to a newly created company admin asynchronously
     *
     * @param company The newly created company
     * @param adminUser The admin user of the company
     * @param temporaryPassword The temporary password generated for the admin
     * @return CompletableFuture<Boolean> that will complete with true if email was sent successfully, false otherwise
     */
    CompletableFuture<Boolean> sendCompanyCreationEmailAsync(Company company, User adminUser, String temporaryPassword);

    /**
     * Send a welcome email to a newly created company admin (synchronous version)
     *
     * @param company The newly created company
     * @param adminUser The admin user of the company
     * @param temporaryPassword The temporary password generated for the admin
     * @return true if email was sent successfully, false otherwise
     */
    boolean sendCompanyCreationEmail(Company company, User adminUser, String temporaryPassword);

    /**
     * Send a generic email with custom content
     *
     * @param toEmail Recipient email address
     * @param toName Recipient name
     * @param subject Email subject
     * @param htmlContent HTML content of the email
     * @return true if email was sent successfully, false otherwise
     */
    boolean sendEmail(String toEmail, String toName, String subject, String htmlContent);

    /**
     * Send a password changed notification email asynchronously
     *
     * @param user The user who changed their password
     * @return CompletableFuture<Boolean> that will complete with true if email was sent successfully, false otherwise
     */
    CompletableFuture<Boolean> sendPasswordChangedEmailAsync(User user);

    CompletableFuture<Boolean> sendPasswordResetEmailAsync(User user,String code, String subject);
}
