
package com.eazybrew.vend.service.impl;

import com.eazybrew.vend.model.User;
import com.eazybrew.vend.repository.UserRepository;
import com.eazybrew.vend.service.UserCompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;



@Slf4j
@RequiredArgsConstructor
@Service("userCompanyService")
public class UserCompanyServiceImpl implements UserCompanyService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isUserFromCompany(Long userId, Long companyId) {
        if (userId == null || companyId == null) {
            return false;
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            log.warn("User with ID {} not found for company validation", userId);
            return false;
        }

        User user = userOptional.get();
        if (user.getCompany() == null) {
            log.warn("User with ID {} has no associated company", userId);
            return false;
        }

        boolean hasAccess = user.getCompany().getId().equals(companyId);
        if (!hasAccess) {
            log.warn("User {} attempted to access company {} but belongs to company {}",
                    userId, companyId, user.getCompany().getId());
        }

        return hasAccess;
    }
}
