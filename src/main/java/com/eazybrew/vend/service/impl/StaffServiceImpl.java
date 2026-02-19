
package com.eazybrew.vend.service.impl;

import com.eazybrew.vend.dto.request.StaffRequest;
import com.eazybrew.vend.dto.response.StaffResponse;
import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.model.Staff;
import com.eazybrew.vend.model.enums.RecordStatusConstant;
import com.eazybrew.vend.repository.StaffRepository;
import com.eazybrew.vend.service.CompanyService;
import com.eazybrew.vend.service.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepo;
    private final CompanyService companyService;



    @Override
    @Transactional
    public StaffResponse createStaff(Long companyId, StaffRequest req) {
        // Validate parent company exists
        Company company = companyService.getCompanyById(companyId);

        // Uniqueness checks
        if (staffRepo.existsByCardSerialAndRecordStatus(req.getCardSerialNumber(), RecordStatusConstant.ACTIVE)) {
            throw new CustomException("cardSerialNumber already in use", HttpStatus.BAD_REQUEST);
        }
        if (staffRepo.existsByEmailAndRecordStatus( req.getEmail(), RecordStatusConstant.ACTIVE)) {
            throw new CustomException("email already in use", HttpStatus.BAD_REQUEST);
        }
        if (staffRepo.existsByEmployeeIdAndRecordStatus(req.getEmployeeId(), RecordStatusConstant.ACTIVE)) {
            throw new CustomException("employeeId already in use", HttpStatus.BAD_REQUEST);
        }

        Staff staff = Staff.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .employeeId(req.getEmployeeId())
                .cardSerial(req.getCardSerialNumber())
                .company(company)
                .enabled(true)
                .build();

        staff = staffRepo.save(staff);
        return toResponse(staff);
    }

    @Override
    @Transactional
    public StaffResponse updateStaff(Long companyId, Long staffId, StaffRequest req) {
        Staff staff = staffRepo.findByIdAndRecordStatus(staffId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Staff not found", HttpStatus.NOT_FOUND));
        if (!staff.getCompany().getId().equals(companyId)) {
            throw new CustomException("Staff does not belong to company", HttpStatus.BAD_REQUEST);
        }

        // Check conflicts only if value changed
        if (!staff.getCardSerial().equals(req.getCardSerialNumber())
                && staffRepo.existsByCompanyIdAndCardSerialAndRecordStatus(companyId,
                req.getCardSerialNumber(), RecordStatusConstant.ACTIVE)) {
            throw new CustomException("cardSerialNumber already in use", HttpStatus.BAD_REQUEST);
        }
        if (!staff.getEmail().equals(req.getEmail())
                && staffRepo.existsByCompanyIdAndEmailAndRecordStatus(companyId, req.getEmail(),
                RecordStatusConstant.ACTIVE)) {
            throw new CustomException("email already in use", HttpStatus.BAD_REQUEST);
        }
        if (!staff.getEmployeeId().equals(req.getEmployeeId())
                && staffRepo.existsByCompanyIdAndEmployeeIdAndRecordStatus(companyId, req.getEmployeeId(),
                RecordStatusConstant.ACTIVE)) {
            throw new CustomException("employeeId already in use", HttpStatus.BAD_REQUEST);
        }

        // Apply updates
        staff.setFullName(req.getFullName());
        staff.setEmail(req.getEmail());
        staff.setEmployeeId(req.getEmployeeId());
        staff.setCardSerial(req.getCardSerialNumber());
        staff = staffRepo.save(staff);

        return toResponse(staff);
    }

    @Override
    @Transactional
    public void setStaffEnabled(Long companyId, Long staffId, boolean enabled) {
        Staff staff = staffRepo.findByIdAndRecordStatus(staffId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Staff not found", HttpStatus.NOT_FOUND));
        if (!staff.getCompany().getId().equals(companyId)) {
            throw new CustomException("Staff does not belong to company", HttpStatus.BAD_REQUEST);
        }
        staff.setEnabled(enabled);
        staffRepo.save(staff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getStaffsByClient(Long companyId) {
        return staffRepo.findAllByCompanyIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StaffResponse> getStaffsByClient(Long companyId, Pageable pageable) {
        return staffRepo.findAllByCompanyIdAndRecordStatus(companyId,RecordStatusConstant.ACTIVE, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffResponse getStaffByCardSerialNumber(Long companyId, String cardSerialNumber) {
        Staff staff = staffRepo.findByCompanyIdAndCardSerialAndRecordStatus(companyId, cardSerialNumber,
                        RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Staff with cardSerialNumber not found",
                        HttpStatus.NOT_FOUND));
        return toResponse(staff);
    }

    @Override
    public Staff getStaffByCardSerialNumberInternal(Long companyId, String cardSerialNumber) {
        Staff staff = staffRepo.findByCompanyIdAndCardSerialAndRecordStatus(companyId, cardSerialNumber,
                        RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Staff with cardSerialNumber not found",
                        HttpStatus.NOT_FOUND));
        return staff;
    }

    @Override
    public Staff getStaffById(Long companyId, Long id) {
        Staff staff = staffRepo.findByCompanyIdAndIdAndRecordStatus(companyId, id, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Staff with id not found",
                        HttpStatus.NOT_FOUND));
        return staff;
    }

    private StaffResponse toResponse(Staff s) {
        return new StaffResponse(
                s.getId(),
                s.getFullName(),
                s.getCardSerial(),
                s.getEmail(),
                s.getEmployeeId(),
                s.getCompany().getId(),
                s.getCompany().getName(),
                s.isEnabled()
        );
    }

    @Override
    @Transactional
    public Boolean deleteStaff(Long companyId, Long staffId) {
        Staff staff = staffRepo.findByIdAndRecordStatus(staffId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Staff not found", HttpStatus.NOT_FOUND));

        if (!staff.getCompany().getId().equals(companyId)) {
            throw new CustomException("Staff does not belong to company", HttpStatus.BAD_REQUEST);
        }

        staff.setRecordStatus(RecordStatusConstant.DELETED);
        staffRepo.save(staff);

        log.info("Staff deleted: {}", staff.getId());
        return Boolean.TRUE;
    }
}
