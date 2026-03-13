package com.eazybrew.vend.service.impl;

import com.eazybrew.vend.config.JwtUtils;
import com.eazybrew.vend.dto.request.*;
import com.eazybrew.vend.dto.response.JwtResponse;
import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.model.PasswordResetToken;
import com.eazybrew.vend.model.Role;
import com.eazybrew.vend.model.User;
import com.eazybrew.vend.model.enums.ERole;
import com.eazybrew.vend.model.enums.UserStatus;
import com.eazybrew.vend.repository.RoleRepository;
import com.eazybrew.vend.repository.UserRepository;
import com.eazybrew.vend.security.UserPrincipal;
import com.eazybrew.vend.service.AuthService;
import com.eazybrew.vend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final com.eazybrew.vend.repository.PasswordResetTokenRepository tokenRepository;

    // token validity in hours
    private static final long RESET_TOKEN_EXPIRY_HOURS = 24;

    @Override
    @Transactional
    public String signup(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new CustomException("Email is already in use!", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByPhoneNumber(signupRequest.getPhoneNumber())) {
            throw new CustomException("Phone number is already in use!", HttpStatus.BAD_REQUEST);
        }

        // Create new user's account
        User user = User.builder()
                .email(signupRequest.getEmail())
                .phoneNumber(signupRequest.getPhoneNumber())
                .status(UserStatus.ACTIVE)
                .password(encoder.encode(signupRequest.getPassword()))
                .build();

        Set<String> strRoles = signupRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new CustomException("Error: Role is not found.", HttpStatus.BAD_REQUEST));
                        roles.add(adminRole);
                        break;
                    case "super_admin":
                        Role superAdminRole = roleRepository.findByName(ERole.ROLE_SUPER_ADMIN)
                                .orElseThrow(() -> new CustomException("Error: Role is not found.", HttpStatus.BAD_REQUEST));
                        roles.add(superAdminRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new CustomException("Error: Role is not found.", HttpStatus.BAD_REQUEST));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        // Send welcome email after the transaction commits successfully
        final User finalUser = savedUser;
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("User created successfully: {}, sending welcome email", finalUser.getEmail());
                    emailService.sendWelcomeEmailAsync(finalUser)
                            .thenAccept(emailSent -> {
                                if (emailSent) {
                                    log.info("Welcome email sent successfully to: {}", finalUser.getEmail());
                                } else {
                                    log.warn("Failed to send welcome email to: {}", finalUser.getEmail());
                                }
                            })
                            .exceptionally(e -> {
                                log.error("Error sending welcome email to: {}", finalUser.getEmail(), e);
                                return null;
                            });
                }
            });
        }

        return "User Created Successfully";
    }

    @Override
    public JwtResponse signIn(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateToken(authentication);

            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // if initial‑password reset is still pending, return reset‑required response
            if (userDetails.isEnforcePasswordReset()) {
                throw new CustomException("Please change your Initial Password", HttpStatus.NOT_ACCEPTABLE);
            }

            return JwtResponse.builder()
                    .token(jwt)
                    .id(userDetails.getId())
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .roles(roles)
                    .build();
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new CustomException("Invalid username or password", HttpStatus.UNAUTHORIZED);
        } catch (org.springframework.security.authentication.LockedException e) {
            throw new CustomException("Account is locked", HttpStatus.UNAUTHORIZED);
        } catch (org.springframework.security.authentication.DisabledException e) {
            throw new CustomException("Account is disabled", HttpStatus.UNAUTHORIZED);
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new CustomException("Authentication failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest resetRequest) {
        Authentication auth;
        try {
            // authenticate using old credentials
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            resetRequest.getUsername(),
                            resetRequest.getOldPassword()
                    )
            );
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new CustomException("Current password is incorrect", HttpStatus.UNAUTHORIZED);
        } catch (org.springframework.security.authentication.LockedException e) {
            throw new CustomException("Account is locked", HttpStatus.UNAUTHORIZED);
        } catch (org.springframework.security.authentication.DisabledException e) {
            throw new CustomException("Account is disabled", HttpStatus.UNAUTHORIZED);
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new CustomException("Authentication failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        // update password and clear reset flag
        user.setPassword(encoder.encode(resetRequest.getNewPassword()));
        user.setEnforcePasswordReset(false);
        User updatedUser = userRepository.save(user);
        // Store final reference to use in the lambda
        final User finalUser = updatedUser;

        // Register a callback to be executed after the transaction is successfully committed
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // This code will only execute if the transaction commits successfully
                    log.info("Password changed successfully for user: {}, sending notification email", finalUser.getEmail());

                    emailService.sendPasswordChangedEmailAsync(finalUser)
                            .thenAccept(emailSent -> {
                                if (emailSent) {
                                    log.info("Password changed notification email sent successfully to: {}", finalUser.getEmail());
                                } else {
                                    log.warn("Failed to send password changed notification email to: {}", finalUser.getEmail());
                                }
                            })
                            .exceptionally(e -> {
                                log.error("Error sending password changed notification email to: {}", finalUser.getEmail(), e);
                                return null;
                            });
                }
            });
        } else {
            log.warn("No active transaction found, password changed email will not be sent after commit");
        }
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Initiating forgot password flow for email: {}", request.getEmail());
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            log.info("User found for email: {}. Deleting existing reset tokens.", user.getEmail());
            tokenRepository.deleteByUser(user);
            log.debug("Existing tokens deleted for user: {}", user.getEmail());

            // generate a unique 6-digit numeric token
            String token;
            do {
                token = String.format("%06d", new Random().nextInt(1000000));
            } while (tokenRepository.findByToken(token).isPresent());
            log.debug("Generated 6-digit reset token: {} for user: {}", token, user.getEmail());
            LocalDateTime expiry = LocalDateTime.now().plusHours(RESET_TOKEN_EXPIRY_HOURS);
            PasswordResetToken prt = new PasswordResetToken(token, user, expiry);
            tokenRepository.save(prt);
            log.info("Password reset token saved with expiry {} for user {}", expiry, user.getEmail());

            String subject = "Password Reset Request";

            // Send the password reset email after the transaction commits
            // so the token is guaranteed to be persisted
            final String finalToken = token;
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("Transaction committed, sending password reset email to: {}", user.getEmail());
                        emailService.sendPasswordResetEmailAsync(user, finalToken, subject)
                                .thenAccept(success -> log.info("Password reset email sent to {}: {}", user.getEmail(), success))
                                .exceptionally(e -> {
                                    log.error("Failed to send password reset email to {}", user.getEmail(), e);
                                    return null;
                                });
                    }
                });
            } else {
                log.warn("No active transaction, sending password reset email immediately to: {}", user.getEmail());
                emailService.sendPasswordResetEmailAsync(user, token, subject)
                        .thenAccept(success -> log.info("Password reset email sent to {}: {}", user.getEmail(), success))
                        .exceptionally(e -> {
                            log.error("Failed to send password reset email to {}", user.getEmail(), e);
                            return null;
                        });
            }
        } else {
            log.warn("No user found with email: {}. Ignoring forgot password request.", request.getEmail());
        }
    }

    @Override
    @Transactional
    public void resetForgotPassword(ResetForgotPasswordRequest request) {
        log.info("Reset forgotten password requested for token: {}", request.getToken());
        PasswordResetToken prt = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new CustomException("Invalid or expired token", HttpStatus.BAD_REQUEST));
        if (prt.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Password reset token expired for token: {}", request.getToken());
            throw new CustomException("Token has expired", HttpStatus.BAD_REQUEST);
        }
        User user = prt.getUser();
        log.info("Token valid. Resetting password for user: {}", user.getEmail());
        user.setPassword(encoder.encode(request.getNewPassword()));
        user.setEnforcePasswordReset(false);
        userRepository.save(user);
        log.info("Password updated for user: {}", user.getEmail());
        tokenRepository.deleteByUser(user);
        log.debug("Password reset token deleted for user: {}", user.getEmail());
        emailService.sendPasswordChangedEmailAsync(user)
                .thenAccept(success -> log.info("Password change notification email sent to {}: {}", user.getEmail(), success))
                .exceptionally(e -> {
                    log.error("Failed to send password change notification to {}", user.getEmail(), e);
                    return null;
                });
    }
}


