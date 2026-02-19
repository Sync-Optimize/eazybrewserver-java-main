
package com.eazybrew.vend.service;

/**
 * Service interface for validating user-company relationships
 * Used for authorization checks in security expressions
 */
public interface UserCompanyService {

    /**
     * Check if a user belongs to a specific company
     *
     * @param userId The ID of the user to check
     * @param companyId The ID of the company to check against
     * @return true if the user belongs to the company, false otherwise
     */
    boolean isUserFromCompany(Long userId, Long companyId);
}
