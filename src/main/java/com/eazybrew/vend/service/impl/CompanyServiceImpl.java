
package com.eazybrew.vend.service.impl;

import com.eazybrew.vend.dto.request.CompanyCreateRequest;
import com.eazybrew.vend.dto.request.CompanyUpdateRequest;
import com.eazybrew.vend.dto.response.CompanyCreationResponse;
import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.model.Role;
import com.eazybrew.vend.model.User;
import com.eazybrew.vend.model.enums.ERole;
import com.eazybrew.vend.model.enums.RecordStatusConstant;
import com.eazybrew.vend.model.enums.UserStatus;
import com.eazybrew.vend.repository.CompanyRepository;
import com.eazybrew.vend.repository.RoleRepository;
import com.eazybrew.vend.repository.UserRepository;
import com.eazybrew.vend.service.CompanyService;
import com.eazybrew.vend.service.EmailService;
import com.eazybrew.vend.util.GeneratorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final GeneratorUtils generatorUtils;

    private static final int MAX_RETRIES = 3;

    @Override
    @Transactional
    public CompanyCreationResponse createCompanyWithAdmin(CompanyCreateRequest request) {
        // Check if company with this name already exists
        if (companyRepository.existsByNameAndRecordStatus(request.getName(), RecordStatusConstant.ACTIVE)) {
            throw new CustomException("Company with this name already exists", HttpStatus.BAD_REQUEST);
        }

        // Check if user with admin email already exists
        if (userRepository.existsByEmail(request.getAdminEmail())) {
            throw new CustomException("User with this email already exists", HttpStatus.BAD_REQUEST);
        }

        // Create and save company with API key
        Company company = buildCompany(request);

        int attempts = 0;
        boolean saved = false;

        // Try to save with unique API key, retry if collision
        while (!saved && attempts < MAX_RETRIES) {
            try {
                company = companyRepository.save(company);
                saved = true;
            } catch (DataIntegrityViolationException e) {
                if (e.getMessage().contains("api_key")) {
                    log.warn("API key collision detected, generating a new one (attempt {})", attempts + 1);
                    company.setApiKey(generatorUtils.generateApiKey());
                    attempts++;
                } else {
                    // Some other constraint violation, rethrow
                    throw e;
                }
            }
        }

        if (!saved) {
            throw new CustomException("Failed to create company after multiple attempts", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        log.info("Company created: {}", company);

        // Generate random password for admin user
        String rawPassword = generatorUtils.generateSecurePassword(12);

        // Create admin user
        User adminUser = createAdminUser(request, company, rawPassword);
        log.info("Admin user created for company: {}, user: {}", company.getName(), adminUser.getEmail());
        // Store final references to use in the lambda
        final Company finalCompany = company;
        final User finalAdminUser = adminUser;
        final String finalPassword = rawPassword;

        // Send welcome email to the company admin asynchronously
        // Register a callback to be executed after the transaction is successfully committed
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // This code will only execute if the transaction commits successfully
                    log.info("Transaction committed successfully, sending welcome email to: {}", finalAdminUser.getEmail());

                    emailService.sendCompanyCreationEmailAsync(finalCompany, finalAdminUser, finalPassword)
                            .thenAccept(emailSent -> {
                                if (emailSent) {
                                    log.info("Welcome email sent successfully to company admin: {}", finalAdminUser.getEmail());
                                } else {
                                    log.warn("Failed to send welcome email to company admin: {}", finalAdminUser.getEmail());
                                }
                            })
                            .exceptionally(e -> {
                                log.error("Error sending welcome email to company admin: {}", finalAdminUser.getEmail(), e);
                                return null;
                            });
                }
            });
        } else {
            log.warn("No active transaction found, email will not be sent after commit");
        }
        // Return focused response with only relevant information
        return CompanyCreationResponse.fromEntities(company, adminUser, rawPassword);
    }

    // Other methods remain the same

    // Helper methods for company and admin user creation
    private Company buildCompany(CompanyCreateRequest request) {
        return Company.builder()
                .name(request.getName())
                .address(request.getAddress())
                .companyEmail(request.getAdminEmail())
                .companyPhoneNumber(request.getAdminPhoneNumber())
//                .website(request.getWebsite())
//                .description(request.getDescription())
//                .industry(request.getIndustry())
                .enabled(true)
                .apiKey(generatorUtils.generateApiKey())
                .build();
    }

    private User createAdminUser(CompanyCreateRequest request, Company company, String rawPassword) {
        if (userRepository.existsByEmail(request.getAdminEmail())) {
            throw new CustomException("Email is already in use!", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByPhoneNumber(request.getAdminPhoneNumber())) {
            throw new CustomException("Phone Number is already in use!", HttpStatus.BAD_REQUEST);
        }
        User adminUser = User.builder()
                .email(request.getAdminEmail())
                .phoneNumber(request.getAdminPhoneNumber())
                .firstName(request.getAdminFirstName())
                .lastName(request.getAdminLastName())
                .fullName(request.getAdminFirstName() + " " + request.getAdminLastName())
                .password(passwordEncoder.encode(rawPassword))
                .status(UserStatus.ACTIVE)
                .company(company)
                .enforcePasswordReset(true) // Enforce password change on first login
                .passwordExpiryDate(LocalDateTime.now().plusDays(90)) // Password expires in 90 days
                .accountNonLocked(true)
                .failedAttempt(0)
                .roles(new HashSet<>())
                .build();

        // Add ADMIN role to user
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new CustomException("Error: Admin Role not found", HttpStatus.INTERNAL_SERVER_ERROR));

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        adminUser.setRoles(roles);

        return userRepository.save(adminUser);
    }
    @Override
    @Transactional
    public Company updateCompanyStatus(Long companyId, boolean enabled) {
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

        company.setEnabled(enabled);
        return companyRepository.save(company);
    }

    @Override
    @Transactional
    public String refreshApiKey(Long companyId) {
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

        String newApiKey = generatorUtils.generateApiKey();
        company.setApiKey(newApiKey);
        companyRepository.save(company);

        return newApiKey;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Company getCompanyById(Long companyId) {
        return companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Company getCompanyByApiKey(String apiKey) {
        return companyRepository.findByApiKeyAndRecordStatus(apiKey, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Invalid API key", HttpStatus.UNAUTHORIZED));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Company> getAllCompanies(Pageable pageable) {

        return companyRepository.findAllByRecordStatus(RecordStatusConstant.ACTIVE, pageable);
    }

    @Override
    @Transactional
    public Company updateCompanyDetails(Long companyId, CompanyUpdateRequest request) {
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

        // Check if company name is being changed and if it already exists
        if (!company.getName().equals(request.getName()) &&
                companyRepository.existsByNameAndRecordStatus(request.getName(), RecordStatusConstant.ACTIVE)) {
            throw new CustomException("Company with this name already exists", HttpStatus.BAD_REQUEST);
        }



        // Update company fields
        company.setName(request.getName());
        company.setAddress(request.getAddress());


        if (request.getWebsite() != null) {
            company.setWebsite(request.getWebsite());
        }

        if (request.getIndustry() != null) {
            company.setIndustry(request.getIndustry());
        }

        if (request.getDescription() != null) {
            company.setDescription(request.getDescription());
        }

        return companyRepository.save(company);
    }

    @Override
    @Transactional
    public Boolean deleteCompany(Long companyId) {
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

        company.setRecordStatus(RecordStatusConstant.DELETED);
        companyRepository.save(company);

        log.info("Company deleted: {}", company.getId());
        return Boolean.TRUE;
    }
}
