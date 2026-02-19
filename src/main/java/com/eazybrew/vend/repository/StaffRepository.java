package com.eazybrew.vend.repository;

import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.model.Staff;
import com.eazybrew.vend.model.enums.RecordStatusConstant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findAllByCompanyId(Long companyId);
    List<Staff> findAllByCompanyIdAndRecordStatus(Long companyId, RecordStatusConstant recordStatus);
    Optional<Staff> findByIdAndRecordStatus(Long id, RecordStatusConstant recordStatusConstant);
    Optional<Staff> findById(Long id);
    Page<Staff> findAllByCompanyId(Long companyId, Pageable pageable);
    long countAllByCompanyAndRecordStatus(Company company, RecordStatusConstant recordStatus);
    long countAllByRecordStatus(RecordStatusConstant recordStatus);
    Page<Staff> findAllByCompanyIdAndRecordStatus(Long companyId, RecordStatusConstant recordStatusConstant, Pageable pageable);
    Optional<Staff> findByCompanyIdAndCardSerial(Long companyId, String cardSerialNumber);
    Optional<Staff> findByCompanyIdAndCardSerialAndRecordStatus(Long companyId, String cardSerialNumber,
                                                                RecordStatusConstant recordStatusConstant);
    Optional<Staff> findByCompanyIdAndId(Long companyId, Long id);
    Optional<Staff> findByCompanyIdAndIdAndRecordStatus(Long companyId, Long id, RecordStatusConstant recordStatusConstant);
    boolean existsByCompanyIdAndCardSerial(Long companyId, String cardSerial);
    boolean existsByCompanyIdAndCardSerialAndRecordStatus(Long companyId, String cardSerial,
                                                          RecordStatusConstant recordStatusConstant);
    boolean existsByCardSerial(String cardSerial);
    boolean existsByCardSerialAndRecordStatus(String cardSerial, RecordStatusConstant recordStatusConstant);

    boolean existsByCompanyIdAndEmail(Long companyId, String email);
    boolean existsByCompanyIdAndEmailAndRecordStatus(Long companyId, String email,
                                                     RecordStatusConstant recordStatusConstant);
    boolean existsByEmail(String email);
    boolean existsByEmailAndRecordStatus(String email, RecordStatusConstant recordStatusConstant);

    boolean existsByCompanyIdAndEmployeeId(Long companyId, String employeeId);
    boolean existsByCompanyIdAndEmployeeIdAndRecordStatus(Long companyId, String employeeId, RecordStatusConstant recordStatusConstant);
    boolean existsByEmployeeId(String employeeId);
    boolean existsByEmployeeIdAndRecordStatus(String employeeId, RecordStatusConstant recordStatusConstant);
}
