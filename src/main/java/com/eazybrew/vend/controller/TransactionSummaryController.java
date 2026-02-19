package com.eazybrew.vend.controller;

import com.eazybrew.vend.apiresponse.ApiResponse;
import com.eazybrew.vend.dto.response.TransactionSummaryResponse;
import com.eazybrew.vend.dto.response.TransactionResponse;
import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.model.enums.RecordStatusConstant;
import com.eazybrew.vend.model.enums.TransactionStatus;
import com.eazybrew.vend.repository.CompanyRepository;
import com.eazybrew.vend.repository.DeviceRepository;
import com.eazybrew.vend.repository.StaffRepository;
import com.eazybrew.vend.repository.TransactionRepository;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Transaction Summary", description = "APIs for transaction summaries")
@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor
public class TransactionSummaryController {

    private final CompanyRepository companyRepository;
    private final DeviceRepository deviceRepository;
    private final StaffRepository staffRepository;
    private final TransactionRepository transactionRepository;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Summary retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionSummaryResponse.class)))
    })
    @Operation(summary = "Get overall summary", description = "Retrieve counts of companies, devices, staff and total completed transaction amount")
    public ResponseEntity<ApiResponse<TransactionSummaryResponse>> getSummary() {
        long companyCount = companyRepository.count();
        long deviceCount = deviceRepository.count();
        long staffCount = staffRepository.count();
        BigDecimal totalAmount = transactionRepository.getTotalAmount();

        TransactionSummaryResponse summary = new TransactionSummaryResponse(
                companyCount, deviceCount, staffCount, totalAmount
        );
        ApiResponse<TransactionSummaryResponse> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("Summary retrieved successfully");
        response.setData(summary);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @GetMapping("/company/{companyId}/summary")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Company summary retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionSummaryResponse.class)))
    })
    @Operation(summary = "Get summary for a company", description = "Retrieve counts of devices, staff and total completed transaction amount for a specific company")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<ApiResponse<TransactionSummaryResponse>> getSummaryCompany(
            @Parameter(description = "ID of the parent company")
            @PathVariable Long companyId) {
        Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));
        long companyCount = 1;
        long deviceCount = deviceRepository.countAllByCompanyAndRecordStatus(company, RecordStatusConstant.ACTIVE);
        long staffCount = staffRepository.countAllByCompanyAndRecordStatus(company, RecordStatusConstant.ACTIVE);
        BigDecimal totalAmount = transactionRepository.getTotalAmountByCompany(company);

        TransactionSummaryResponse summary = new TransactionSummaryResponse(
                companyCount, deviceCount, staffCount, totalAmount
        );
        ApiResponse<TransactionSummaryResponse> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("Summary retrieved successfully");
        response.setData(summary);
        return new ResponseEntity<>(response, response.getStatus());
    }


}