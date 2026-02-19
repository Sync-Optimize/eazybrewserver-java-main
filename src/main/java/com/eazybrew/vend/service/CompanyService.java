package com.eazybrew.vend.service;

import com.eazybrew.vend.dto.request.CompanyCreateRequest;
import com.eazybrew.vend.dto.request.CompanyUpdateRequest;
import com.eazybrew.vend.dto.response.CompanyCreationResponse;
import com.eazybrew.vend.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CompanyService {
    CompanyCreationResponse createCompanyWithAdmin(CompanyCreateRequest request);
    Company updateCompanyStatus(Long companyId, boolean enabled);
    Company updateCompanyDetails(Long companyId, CompanyUpdateRequest request);
    String refreshApiKey(Long companyId);
    List<Company> getAllCompanies();
    /** returns companies in a paged fashion */
    Page<Company> getAllCompanies(Pageable pageable);
    Company getCompanyById(Long companyId);
    Company getCompanyByApiKey(String apiKey);

    /**
     * Delete a company
     *
     * @param companyId The company ID
     * @return true if the company was deleted successfully, false otherwise
     */
    Boolean deleteCompany(Long companyId);
}
