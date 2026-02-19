package com.eazybrew.vend.service;

import com.eazybrew.vend.dto.request.StaffRequest;
import com.eazybrew.vend.dto.response.StaffResponse;
import com.eazybrew.vend.model.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StaffService {
    StaffResponse createStaff(Long companyId, StaffRequest request);
    StaffResponse updateStaff(Long companyId, Long staffId, StaffRequest request);
    void setStaffEnabled(Long companyId, Long staffId, boolean enabled);
    List<StaffResponse> getStaffsByClient(Long companyId);
    /**
     * Returns a page of staffs for the given company
     */
    Page<StaffResponse> getStaffsByClient(Long companyId, Pageable pageable);

    /**
     * Returns a single staff by cardSerialNumber under the given company.
     */
    StaffResponse getStaffByCardSerialNumber(Long companyId, String cardSerialNumber);
    Staff getStaffByCardSerialNumberInternal(Long companyId, String cardSerialNumber);
    Staff getStaffById(Long companyId, Long id);

    /**
     * Delete a staff
     *
     * @param companyId The company ID
     * @param staffId The staff ID
     * @return true if the staff was deleted successfully, false otherwise
     */
    Boolean deleteStaff(Long companyId, Long staffId);
}
