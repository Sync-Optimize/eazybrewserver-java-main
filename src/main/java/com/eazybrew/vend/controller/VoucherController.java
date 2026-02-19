package com.eazybrew.vend.controller;

import com.eazybrew.vend.apiresponse.ApiResponse;
import com.eazybrew.vend.dto.request.VoucherFundRequest;
import com.eazybrew.vend.dto.request.VoucherRequest;
import com.eazybrew.vend.dto.response.VoucherResponse;
import com.eazybrew.vend.model.Voucher;
import com.eazybrew.vend.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@Tag(name = "Voucher Management API's", description = "APIs for managing company vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping("/companies/{companyId}")
    @Operation(summary = "Create a new voucher", description = "Create a new voucher for a company or for staff members")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Voucher created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoucherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict - Voucher already exists",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<ApiResponse<Object>> createVoucher(
            @PathVariable Long companyId,
            @Valid @RequestBody VoucherRequest request) {

        // Check if staffIds is provided
        if (request.getStaffIds() != null && !request.getStaffIds().isEmpty()) {
            // Create vouchers for multiple staff members
            List<Voucher> vouchers = voucherService.createStaffVouchers(
                    companyId,
                    request.getStaffIds(),
                    request.getDailyLimit(),
                    request.getTotalAmount(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            List<VoucherResponse> responses = vouchers.stream()
                    .map(VoucherResponse::fromEntity)
                    .collect(Collectors.toList());

            ApiResponse<Object> apiResponse = new ApiResponse<>(HttpStatus.CREATED);
            apiResponse.setMessage("Vouchers created successfully for " + vouchers.size() + " staff members");
            apiResponse.setData(responses);

            return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
        } else {
            // Create voucher for company only
            Voucher voucher = voucherService.createVoucher(
                    companyId,
                    request.getDailyLimit(),
                    request.getTotalAmount(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            VoucherResponse response = VoucherResponse.fromEntity(voucher);
            ApiResponse<Object> apiResponse = new ApiResponse<>(HttpStatus.CREATED);
            apiResponse.setMessage("Voucher created successfully");
            apiResponse.setData(response);

            return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
        }
    }

    @PostMapping("/companies/{companyId}/fund")
    @Operation(summary = "Fund a voucher", description = "Fund existing vouchers or create new ones if they don't exist")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Voucher funded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoucherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Object>> fundVoucher(
            @PathVariable Long companyId,
            @Valid @RequestBody VoucherFundRequest request) {

        // Check if staffIds is provided
        if (request.getStaffIds() != null && !request.getStaffIds().isEmpty()) {
            // Fund vouchers for multiple staff members
            List<Voucher> vouchers = voucherService.fundStaffVouchers(
                    companyId,
                    request.getStaffIds(),
                    request.getAmount(),
                    request.getDailyLimit(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            List<VoucherResponse> responses = vouchers.stream()
                    .map(VoucherResponse::fromEntity)
                    .collect(Collectors.toList());

            ApiResponse<Object> apiResponse = new ApiResponse<>(HttpStatus.OK);
            apiResponse.setMessage("Vouchers funded successfully for " + vouchers.size() + " staff members");
            apiResponse.setData(responses);

            return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
        } else {
            // Fund voucher for company only
            Voucher voucher = voucherService.fundVoucher(
                    companyId,
                    request.getAmount(),
                    request.getDailyLimit(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            VoucherResponse response = VoucherResponse.fromEntity(voucher);
            ApiResponse<Object> apiResponse = new ApiResponse<>(HttpStatus.OK);
            apiResponse.setMessage("Voucher funded successfully");
            apiResponse.setData(response);

            return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
        }
    }

    @GetMapping("/companies/{companyId}")
    @Operation(summary = "Get voucher by company", description = "Get a voucher for a company")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Voucher retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoucherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Voucher not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherByCompany(
            @PathVariable Long companyId) {

        Voucher voucher = voucherService.getVoucherByCompany(companyId);

        if (voucher == null) {
            ApiResponse<VoucherResponse> apiResponse = new ApiResponse<>(HttpStatus.NOT_FOUND);
            apiResponse.setMessage("Voucher not found for company");
            return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
        }

        VoucherResponse response = VoucherResponse.fromEntity(voucher);
        ApiResponse<VoucherResponse> apiResponse = new ApiResponse<>(HttpStatus.OK);
        apiResponse.setMessage("Voucher retrieved successfully");
        apiResponse.setData(response);

        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }

    @GetMapping("/companies/{companyId}/all")
    @Operation(summary = "Get all vouchers by company", description = "Get all vouchers for a company")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vouchers retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoucherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<ApiResponse<Page<VoucherResponse>>> getVouchersByCompany(
            @PathVariable Long companyId,
            Pageable pageable) {

        Page<Voucher> vouchers = voucherService.getVouchersByCompany(companyId, pageable);
        Page<VoucherResponse> responses = vouchers.map(VoucherResponse::fromEntity);

        ApiResponse<Page<VoucherResponse>> apiResponse = new ApiResponse<>(HttpStatus.OK);
        apiResponse.setMessage("Vouchers retrieved successfully");
        apiResponse.setData(responses);

        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }

    @GetMapping("/companies/{companyId}/staff/{staffId}")
    @Operation(summary = "Get voucher by staff", description = "Get a voucher for a staff member")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Voucher retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoucherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Voucher not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherByStaff(
            @PathVariable Long companyId,
            @PathVariable Long staffId) {

        Voucher voucher = voucherService.getVoucherByStaff(companyId, staffId);

        if (voucher == null) {
            ApiResponse<VoucherResponse> apiResponse = new ApiResponse<>(HttpStatus.NOT_FOUND);
            apiResponse.setMessage("Voucher not found for staff member");
            return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
        }

        VoucherResponse response = VoucherResponse.fromEntity(voucher);
        ApiResponse<VoucherResponse> apiResponse = new ApiResponse<>(HttpStatus.OK);
        apiResponse.setMessage("Voucher retrieved successfully");
        apiResponse.setData(response);

        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }

    @GetMapping("/companies/{companyId}/staff")
    @Operation(summary = "Get vouchers by staff list", description = "Get vouchers for multiple staff members")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vouchers retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoucherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getVouchersByStaffList(
            @PathVariable Long companyId,
            @RequestParam List<Long> staffIds) {

        List<Voucher> vouchers = voucherService.getVouchersByStaffList(companyId, staffIds);
        List<VoucherResponse> responses = vouchers.stream()
                .map(VoucherResponse::fromEntity)
                .collect(Collectors.toList());

        ApiResponse<List<VoucherResponse>> apiResponse = new ApiResponse<>(HttpStatus.OK);
        apiResponse.setMessage("Vouchers retrieved successfully");
        apiResponse.setData(responses);

        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }

    @GetMapping("/companies/{companyId}/staff/{staffId}/all")
    @Operation(summary = "Get all vouchers by staff", description = "Get all vouchers for a staff member")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vouchers retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoucherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<ApiResponse<Page<VoucherResponse>>> getVouchersByStaff(
            @PathVariable Long companyId,
            @PathVariable Long staffId,
            Pageable pageable) {

        Page<Voucher> vouchers = voucherService.getVouchersByStaff(companyId, staffId, pageable);
        Page<VoucherResponse> responses = vouchers.map(VoucherResponse::fromEntity);

        ApiResponse<Page<VoucherResponse>> apiResponse = new ApiResponse<>(HttpStatus.OK);
        apiResponse.setMessage("Vouchers retrieved successfully");
        apiResponse.setData(responses);

        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }

    @PutMapping("/companies/{companyId}/disable")
    @Operation(summary = "Disable a voucher", description = "Disable a voucher for a company")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Voucher disabled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoucherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Voucher not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<VoucherResponse>> disableVoucher(
            @PathVariable Long companyId) {

        Voucher voucher = voucherService.disableVoucher(companyId);
        VoucherResponse response = VoucherResponse.fromEntity(voucher);

        ApiResponse<VoucherResponse> apiResponse = new ApiResponse<>(HttpStatus.OK);
        apiResponse.setMessage("Voucher disabled successfully");
        apiResponse.setData(response);

        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }

    @PutMapping("/companies/{companyId}/enable")
    @Operation(summary = "Enable a voucher", description = "Enable a voucher for a company")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Voucher enabled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoucherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Voucher not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<VoucherResponse>> enableVoucher(
            @PathVariable Long companyId) {

        Voucher voucher = voucherService.enableVoucher(companyId);
        VoucherResponse response = VoucherResponse.fromEntity(voucher);

        ApiResponse<VoucherResponse> apiResponse = new ApiResponse<>(HttpStatus.OK);
        apiResponse.setMessage("Voucher enabled successfully");
        apiResponse.setData(response);

        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }

    @PutMapping("/companies/{companyId}/staff/{staffId}/disable")
    @Operation(summary = "Disable a staff voucher", description = "Disable a voucher for a staff member")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Voucher disabled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoucherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Voucher not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<VoucherResponse>> disableStaffVoucher(
            @PathVariable Long companyId,
            @PathVariable Long staffId) {

        Voucher voucher = voucherService.disableStaffVoucher(companyId, staffId);
        VoucherResponse response = VoucherResponse.fromEntity(voucher);

        ApiResponse<VoucherResponse> apiResponse = new ApiResponse<>(HttpStatus.OK);
        apiResponse.setMessage("Staff voucher disabled successfully");
        apiResponse.setData(response);

        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }

    @PutMapping("/companies/{companyId}/staff/disable")
    @Operation(summary = "Disable multiple staff vouchers", description = "Disable vouchers for multiple staff members")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vouchers disabled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoucherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vouchers not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> disableStaffVouchers(
            @PathVariable Long companyId,
            @RequestParam List<Long> staffIds) {

        List<Voucher> vouchers = voucherService.disableStaffVouchers(companyId, staffIds);
        List<VoucherResponse> responses = vouchers.stream()
                .map(VoucherResponse::fromEntity)
                .collect(Collectors.toList());

        ApiResponse<List<VoucherResponse>> apiResponse = new ApiResponse<>(HttpStatus.OK);
        apiResponse.setMessage("Staff vouchers disabled successfully");
        apiResponse.setData(responses);

        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }

    @PutMapping("/companies/{companyId}/staff/{staffId}/enable")
    @Operation(summary = "Enable a staff voucher", description = "Enable a voucher for a staff member")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Voucher enabled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoucherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Voucher not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<VoucherResponse>> enableStaffVoucher(
            @PathVariable Long companyId,
            @PathVariable Long staffId) {

        Voucher voucher = voucherService.enableStaffVoucher(companyId, staffId);
        VoucherResponse response = VoucherResponse.fromEntity(voucher);

        ApiResponse<VoucherResponse> apiResponse = new ApiResponse<>(HttpStatus.OK);
        apiResponse.setMessage("Staff voucher enabled successfully");
        apiResponse.setData(response);

        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }

    @PutMapping("/companies/{companyId}/staff/enable")
    @Operation(summary = "Enable multiple staff vouchers", description = "Enable vouchers for multiple staff members")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vouchers enabled successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoucherResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vouchers not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> enableStaffVouchers(
            @PathVariable Long companyId,
            @RequestParam List<Long> staffIds) {

        List<Voucher> vouchers = voucherService.enableStaffVouchers(companyId, staffIds);
        List<VoucherResponse> responses = vouchers.stream()
                .map(VoucherResponse::fromEntity)
                .collect(Collectors.toList());

        ApiResponse<List<VoucherResponse>> apiResponse = new ApiResponse<>(HttpStatus.OK);
        apiResponse.setMessage("Staff vouchers enabled successfully");
        apiResponse.setData(responses);

        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }
}
