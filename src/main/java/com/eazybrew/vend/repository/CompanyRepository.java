package com.eazybrew.vend.repository;

import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.model.enums.RecordStatusConstant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByIdAndRecordStatus(Long id, RecordStatusConstant  recordStatus);
    long countAllByRecordStatus(RecordStatusConstant recordStatusConstant);
    boolean existsByName(String name);
    boolean existsByNameAndRecordStatus(String name, RecordStatusConstant recordStatus);
    Page<Company> findAllByRecordStatus(RecordStatusConstant recordStatus, Pageable pageable);
    Optional<Company> findByApiKey(String apiKey);
    Optional<Company> findByApiKeyAndRecordStatus(String apiKey, RecordStatusConstant  recordStatus);
    boolean existsByApiKey(String apiKey);
    boolean existsByApiKeyAndRecordStatus(String apiKey, RecordStatusConstant recordStatus);
    boolean existsByCompanyEmail(String companyEmail);
}
