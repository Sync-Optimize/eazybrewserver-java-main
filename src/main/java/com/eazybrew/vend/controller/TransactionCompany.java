package com.eazybrew.vend.controller;

import com.eazybrew.vend.apiresponse.ApiResponse;
import com.eazybrew.vend.dto.response.TransactionResponse;
import com.eazybrew.vend.model.Transaction;
import com.eazybrew.vend.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transaction-company")
@Tag(name = "Transaction Company", description = "TransactionCompany API")
public class TransactionCompany {

    private final TransactionService transactionService;

    @Operation(
            summary = "Get Transactions by a company",
            description = "Retrieves Transactions done by a company. Accessible to super admin and company admin.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/{companyId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactionByCompany(@PathVariable Long companyId,
                                                                                   @Parameter(description = "Page number (zero‑based)", example = "0")
                                                                        @RequestParam(name = "page", defaultValue = "0") int page,
                                                                                   @Parameter(description = "Page size", example = "10")
                                                                            @RequestParam(name = "size", defaultValue = "10") int size,
                                                                                   @Parameter(description = "Sort in format 'property,direction'", example = "id,asc")
                                                                            @RequestParam(name = "sort", defaultValue = "id,asc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction dir = "desc".equalsIgnoreCase(sortParams[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortObj = Sort.by(dir, sortParams[0]);
        PageRequest pageable = PageRequest.of(page, size, sortObj);
        Page<Transaction> transactions = transactionService.getTransactionsByCompany(companyId, pageable);

        // Convert Transaction entities to TransactionResponse DTOs
        Page<TransactionResponse> transactionResponses = transactions.map(TransactionResponse::fromEntity);

        ApiResponse<Page<TransactionResponse>> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("Transactions retrieved successfully");
        response.setData(transactionResponses);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get Transactions by a company and staff",
            description = "Retrieves Transactions done by a company and by a staff. Accessible to super admin and company admin.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/{companyId}/staff/{staffId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactionByCompanyAndStaff(@PathVariable Long companyId,
                                                                                          @PathVariable Long staffId,
                                                                                  @Parameter(description = "Page number (zero‑based)", example = "0")
                                                                                  @RequestParam(name = "page", defaultValue = "0") int page,
                                                                                  @Parameter(description = "Page size", example = "10")
                                                                                  @RequestParam(name = "size", defaultValue = "10") int size,
                                                                                  @Parameter(description = "Sort in format 'property,direction'", example = "id,asc")
                                                                                  @RequestParam(name = "sort", defaultValue = "id,asc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction dir = "desc".equalsIgnoreCase(sortParams[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortObj = Sort.by(dir, sortParams[0]);
        PageRequest pageable = PageRequest.of(page, size, sortObj);
        Page<Transaction> transactions = transactionService.getTransactionsByCompanyAndStaffId(companyId, staffId,
                pageable);

        // Convert Transaction entities to TransactionResponse DTOs
        Page<TransactionResponse> transactionResponses = transactions.map(TransactionResponse::fromEntity);

        ApiResponse<Page<TransactionResponse>> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("Transactions retrieved successfully");
        response.setData(transactionResponses);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get Transactions by a company and staff",
            description = "Retrieves Transactions done by a company and by a staff. Accessible to super admin and company admin.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping()
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(@Parameter(description = "Page number (zero‑based)", example = "0")
                                                                              @RequestParam(name = "page", defaultValue = "0") int page,
                                                                          @Parameter(description = "Page size", example = "10")
                                                                          @RequestParam(name = "size", defaultValue = "10") int size,
                                                                          @Parameter(description = "Sort in format 'property,direction'", example = "id,asc")
                                                                              @RequestParam(name = "sort", defaultValue = "id,asc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction dir = "desc".equalsIgnoreCase(sortParams[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortObj = Sort.by(dir, sortParams[0]);
        PageRequest pageable = PageRequest.of(page, size, sortObj);
        Page<Transaction> transactions = transactionService.getTransactions(pageable);

        // Convert Transaction entities to TransactionResponse DTOs
        Page<TransactionResponse> transactionResponses = transactions.map(TransactionResponse::fromEntity);

        ApiResponse<Page<TransactionResponse>> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("Transactions retrieved successfully");
        response.setData(transactionResponses);

        return ResponseEntity.ok(response);
    }
}
