package com.eazybrew.vend.bootstrap;

import com.eazybrew.vend.model.Role;
import com.eazybrew.vend.model.enums.ERole;
import com.eazybrew.vend.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        log.info("Initializing application data...");
        initRoles();
    }

    private void initRoles() {
        // Check and create USER role if it doesn't exist
        if (!roleRepository.existsByName(ERole.ROLE_USER)) {
            Role userRole = new Role();
            userRole.setName(ERole.ROLE_USER);
            roleRepository.save(userRole);
            log.info("Created role: {}", ERole.ROLE_USER);
        } else {
            log.info("Role {} already exists, skipping creation", ERole.ROLE_USER);
        }

        // Check and create ADMIN role if it doesn't exist
        if (!roleRepository.existsByName(ERole.ROLE_ADMIN)) {
            Role adminRole = new Role();
            adminRole.setName(ERole.ROLE_ADMIN);
            roleRepository.save(adminRole);
            log.info("Created role: {}", ERole.ROLE_ADMIN);
        } else {
            log.info("Role {} already exists, skipping creation", ERole.ROLE_ADMIN);
        }

        // Check and create SUPER_ADMIN role if it doesn't exist
        if (!roleRepository.existsByName(ERole.ROLE_SUPER_ADMIN)) {
            Role superAdminRole = new Role();
            superAdminRole.setName(ERole.ROLE_SUPER_ADMIN);
            roleRepository.save(superAdminRole);
            log.info("Created role: {}", ERole.ROLE_SUPER_ADMIN);
        } else {
            log.info("Role {} already exists, skipping creation", ERole.ROLE_SUPER_ADMIN);
        }
    }
}
