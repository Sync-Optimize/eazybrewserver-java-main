package com.eazybrew.vend.controller;

import com.eazybrew.vend.apiresponse.ApiResponse;
import com.eazybrew.vend.dto.request.TransactionRequest;
import com.eazybrew.vend.dto.response.DeviceResponse;
import com.eazybrew.vend.dto.response.TransactionResponse;
import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.service.DeviceService;
import com.eazybrew.vend.service.TransactionService;
import com.eazybrew.vend.util.EncryptionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "APIs for managing transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final DeviceService deviceService;
    private final EncryptionUtil encryptionUtil;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate a transaction", description = "Initiate a new transaction with encrypted API key in header")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transaction initiated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid API key",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Device not registered or disabled",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> initiateTransaction(
            @Parameter(description = "Encrypted API key", required = true)
            @RequestHeader("X-API-KEY") String encryptedApiKey,
            @Valid @RequestBody TransactionRequest request) {

        try {
            // Decrypt the API key from the header
            String decryptedApiKey = encryptionUtil.decrypt(encryptedApiKey);

            // Set the decrypted API key in the request
            request.setApiKey(decryptedApiKey);

            // Process the transaction
            TransactionResponse response = transactionService.processTransaction(request);
            ApiResponse<TransactionResponse> responseApi = new ApiResponse<>(HttpStatus.OK);
            responseApi.setMessage("Devices retrieved successfully");
            responseApi.setData(response);
            return new ResponseEntity<>(responseApi, responseApi.getStatus());

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Decryption failed")) {
                log.error("Failed to decrypt API key: {}", encryptedApiKey);
                throw new CustomException("Invalid encrypted API key", HttpStatus.UNAUTHORIZED);
            }
            throw e;
        }
    }
//
//    @GetMapping("/generate-encrypted-key")
//    @Operation(summary = "Generate encrypted API key", description = "Generate an encrypted API key for client use (for testing purposes)")
//    public ResponseEntity<ApiResponse> generateEncryptedKey(@RequestParam String apiKey) {
//        String encryptedKey = encryptionUtil.generateEncryptedApiKey(apiKey);
//        return ResponseEntity.ok(new ApiResponse(true, "Encrypted API key generated successfully", encryptedKey));
//    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get device by serial number", description = "Get a device by its serial number. Requires SUPERADMIN role.")
    public ResponseEntity< ApiResponse<DeviceResponse>> getDeviceByDeviceId(@PathVariable String deviceId) {
        DeviceResponse response = deviceService.getDeviceByDeviceId(deviceId);
        ApiResponse<DeviceResponse> responseApi = new ApiResponse<>(HttpStatus.OK);
        responseApi.setMessage("Device data retrieved successfully");
        responseApi.setData(response);
        return new ResponseEntity<>(responseApi, responseApi.getStatus());
    }
}
