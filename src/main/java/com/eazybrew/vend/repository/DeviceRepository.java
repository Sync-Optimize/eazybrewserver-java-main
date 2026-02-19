package com.eazybrew.vend.repository;

import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.model.Device;
import com.eazybrew.vend.model.enums.RecordStatusConstant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByDeviceIdAndRecordStatus(String deviceId, RecordStatusConstant recordStatusConstant);
    Optional<Device> findByIdAndRecordStatus(Long id, RecordStatusConstant recordStatusConstant);
    boolean existsByDeviceIdAndRecordStatus(String deviceId, RecordStatusConstant recordStatusConstant);
    boolean existsByVendingMachineIdAndRecordStatus(String vendingMachineId, RecordStatusConstant recordStatusConstant);

    Optional<Device> findByVendingMachineIdAndRecordStatus(String vendingMachineId, RecordStatusConstant recordStatusConstant);

    List<Device> findByCompanyAndRecordStatus(Company company, RecordStatusConstant recordStatusConstant);

    Page<Device> findByCompanyAndRecordStatus(Company company, RecordStatusConstant recordStatusConstant, Pageable pageable);
    long countAllByCompanyAndRecordStatus(Company company, RecordStatusConstant recordStatusConstant);
    long countAllByRecordStatus(RecordStatusConstant recordStatusConstant);

//    Page<Device> findAllAndRecordStatus(RecordStatusConstant recordStatusConstant, Pageable pageable);
    // Alternative approach using @Query annotation
    @Query("SELECT d FROM Device d WHERE d.recordStatus = :status")
    Page<Device> findAllWithStatus(@Param("status") RecordStatusConstant recordStatusConstant, Pageable pageable);

    // NEW: ensure vendingMachineName is unique
    boolean existsByVendingMachineNameLikeIgnoreCaseAndCompanyAndRecordStatus(String vendingMachineName, Company company,
                                                                RecordStatusConstant recordStatus);

    // if you need to look up by vendingMachineName later:
    Optional<Device> findByVendingMachineNameAndRecordStatus(String vendingMachineName, RecordStatusConstant recordStatus);
}
