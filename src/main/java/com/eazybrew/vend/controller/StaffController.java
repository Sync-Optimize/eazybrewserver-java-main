
package com.eazybrew.vend.controller;

import com.eazybrew.vend.apiresponse.ApiResponse;
import com.eazybrew.vend.dto.request.StaffRequest;
import com.eazybrew.vend.dto.response.StaffResponse;
import com.eazybrew.vend.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Staff Management APIs.
 * Only SUPER_ADMIN or COMPANY_ADMIN of the given company may invoke these endpoints.
 */
@Tag(name = "Staff Management", description = "Create, update, enable/disable, and list staff under a company")
@RestController
@RequestMapping("/api/companies/{companyId}/staffs")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @Operation(
            summary = "Create a new staff",
            description = "Adds a staff member under the specified company. Requires fullname, email, employeeId, and parentCompanyId.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(
            @Parameter(description = "ID of the parent company") @PathVariable Long companyId,
            @Valid @RequestBody StaffRequest request
    ) {
        StaffResponse created = staffService.createStaff(companyId, request);
        ApiResponse<StaffResponse> resp = new ApiResponse<>(HttpStatus.CREATED);
        resp.setMessage("Staff created successfully");
        resp.setData(created);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @Operation(
            summary = "Update existing staff",
            description = "Updates the specified staff's details. All fields will be overwritten; validation applies.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{staffId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<ApiResponse<StaffResponse>> updateStaff(
            @Parameter(description = "ID of the parent company") @PathVariable Long companyId,
            @Parameter(description = "ID of the staff to update") @PathVariable Long staffId,
            @Valid @RequestBody StaffRequest request
    ) {
        StaffResponse updated = staffService.updateStaff(companyId, staffId, request);
        ApiResponse<StaffResponse> resp = new ApiResponse<>(HttpStatus.OK);
        resp.setMessage("Staff updated successfully");
        resp.setData(updated);
        return ResponseEntity.ok(resp);
    }

    @Operation(
            summary = "Enable a staff",
            description = "Marks the specified staff as enabled.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{staffId}/enable")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<Void> enableStaff(
            @Parameter(description = "ID of the parent company") @PathVariable Long companyId,
            @Parameter(description = "ID of the staff to enable") @PathVariable Long staffId
    ) {
        staffService.setStaffEnabled(companyId, staffId, true);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Disable a staff",
            description = "Marks the specified staff as disabled.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{staffId}/disable")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<Void> disableStaff(
            @Parameter(description = "ID of the parent company") @PathVariable Long companyId,
            @Parameter(description = "ID of the staff to disable") @PathVariable Long staffId
    ) {
        staffService.setStaffEnabled(companyId, staffId, false);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "List staff (paged)",
            description = "Returns a paged list of all staff under the given company.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<ApiResponse<Page<StaffResponse>>> listStaffs(
            @Parameter(description = "ID of the parent company") @PathVariable Long companyId,
            @Parameter(description = "Pagination parameters") @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<StaffResponse> page = staffService.getStaffsByClient(companyId, pageable);
        ApiResponse<Page<StaffResponse>> resp = new ApiResponse<>(HttpStatus.OK);
        resp.setMessage("Staff page retrieved successfully");
        resp.setData(page);
        return ResponseEntity.ok(resp);
    }

    @Operation(
            summary = "Get staff by cardSerialNumber",
            description = "Retrieves a single staff under the company by their card serial number.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/cards/{cardSerialNumber}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<ApiResponse<StaffResponse>> getByCardSerial(
            @Parameter(description = "ID of the parent company") @PathVariable Long companyId,
            @Parameter(description = "Card serial number of the staff") @PathVariable String cardSerialNumber
    ) {
        StaffResponse staff = staffService.getStaffByCardSerialNumber(companyId, cardSerialNumber);
        ApiResponse<StaffResponse> resp = new ApiResponse<>(HttpStatus.OK);
        resp.setMessage("Staff retrieved by cardSerialNumber successfully");
        resp.setData(staff);
        return ResponseEntity.ok(resp);
    }

    @Operation(
            summary = "Delete staff",
            description = "Delete a staff member. Only accessible to super admin or company admin.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{staffId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<ApiResponse<Boolean>> deleteStaff(
            @Parameter(description = "ID of the parent company") @PathVariable Long companyId,
            @Parameter(description = "ID of the staff to delete") @PathVariable Long staffId
    ) {
        Boolean result = staffService.deleteStaff(companyId, staffId);
        ApiResponse<Boolean> resp = new ApiResponse<>(HttpStatus.OK);
        resp.setMessage("Staff deleted successfully");
        resp.setData(result);
        return ResponseEntity.ok(resp);
    }
}
