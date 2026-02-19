package com.eazybrew.vend.controller;

import com.eazybrew.vend.apiresponse.ApiResponse;
import com.eazybrew.vend.dto.request.CompanyCreateRequest;
import com.eazybrew.vend.dto.request.CompanyUpdateRequest;
import com.eazybrew.vend.dto.response.CompanyCreationResponse;
import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Company Management", description = "Company and client management APIs")
public class CompanyController {

    private final CompanyService companyService;

    @Operation(
            summary = "Create a new company with admin user",
            description = "Creates a new company and an admin user for that company. Only accessible to super admin.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Company created successfully with admin user",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad request - validation errors or company/user already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - requires SUPER_ADMIN role",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CompanyCreationResponse>> createCompany(@Valid @RequestBody CompanyCreateRequest request) {
        CompanyCreationResponse creationResponse = companyService.createCompanyWithAdmin(request);

        ApiResponse<CompanyCreationResponse> response = new ApiResponse<>(HttpStatus.CREATED);
        response.setMessage("Company created successfully with admin user");
        response.setData(creationResponse);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update company status",
            description = "Enable or disable a company. Only accessible to super admin.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Company status updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    @PatchMapping("/{companyId}/status")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Company>> updateCompanyStatus(
            @PathVariable Long companyId,
            @RequestBody Map<String, Boolean> status) {

        Boolean enabled = status.get("enabled");
        if (enabled == null) {
            ApiResponse<Company> errorResponse = new ApiResponse<>(HttpStatus.BAD_REQUEST);
            errorResponse.setMessage("Enabled status must be provided");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        Company updatedCompany = companyService.updateCompanyStatus(companyId, enabled);

        ApiResponse<Company> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("Company status updated successfully");
        response.setData(updatedCompany);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Refresh API key",
            description = "Generate a new API key for a company. Only accessible to super admin.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @PostMapping("/{companyId}/api-key/refresh")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshApiKey(@PathVariable Long companyId) {
        String newApiKey = companyService.refreshApiKey(companyId);

        ApiResponse<Map<String, String>> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("API key refreshed successfully");
        response.setData(Map.of("apiKey", newApiKey));

        return ResponseEntity.ok(response);
    }

//    @Operation(
//            summary = "Get all companies",
//            description = "Retrieves a list of all companies. Only accessible to super admin.",
//            security = { @SecurityRequirement(name = "bearerAuth") }
//    )
//    @GetMapping
//    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
//    public ResponseEntity<ApiResponse<List<Company>>> getAllCompanies() {
//        List<Company> companies = companyService.getAllCompanies();
//
//        ApiResponse<List<Company>> response = new ApiResponse<>(HttpStatus.OK);
//        response.setMessage("Companies retrieved successfully");
//        response.setData(companies);
//
//        return ResponseEntity.ok(response);
//    }

    @Operation(
            summary = "Get paged companies",
            description = "Retrieves companies in pages; only super admins may call.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<Company>>> getAllCompanies(
            @Parameter(description = "Page number (zero‑based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Sort in format 'property,direction'", example = "id,asc")
            @RequestParam(name = "sort", defaultValue = "id,asc") String sort
    ) {
        String[] sortParams = sort.split(",");
        Sort.Direction dir = "desc".equalsIgnoreCase(sortParams[1]) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortObj = Sort.by(dir, sortParams[0]);
        PageRequest pageable = PageRequest.of(page, size, sortObj);

        Page<Company> pageResult = companyService.getAllCompanies(pageable);
        ApiResponse<Page<Company>> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("Companies retrieved successfully");
        response.setData(pageResult);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get company by ID",
            description = "Retrieves a company by ID. Accessible to super admin and company admin.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @GetMapping("/{companyId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN') or " +
            "(hasRole('ROLE_ADMIN') and @userCompanyService.isUserFromCompany(authentication.principal.id, #companyId))")
    public ResponseEntity<ApiResponse<Company>> getCompanyById(@PathVariable Long companyId) {
        Company company = companyService.getCompanyById(companyId);

        ApiResponse<Company> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("Company retrieved successfully");
        response.setData(company);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update company details",
            description = "Update general information for a company. Accessible to super admin and company admin.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Company details updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad request - validation errors or company name/email already exists",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    @PutMapping("/{companyId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Company>> updateCompanyDetails(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyUpdateRequest request) {

        Company updatedCompany = companyService.updateCompanyDetails(companyId, request);

        ApiResponse<Company> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("Company details updated successfully");
        response.setData(updatedCompany);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete company",
            description = "Delete a company. Only accessible to super admin.",
            security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Company deleted successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Company not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    @DeleteMapping("/{companyId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> deleteCompany(@PathVariable Long companyId) {
        Boolean result = companyService.deleteCompany(companyId);

        ApiResponse<Boolean> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("Company deleted successfully");
        response.setData(result);

        return ResponseEntity.ok(response);
    }
}
