package com.eazybrew.vend.controller;

import com.eazybrew.vend.apiresponse.ApiResponse;
import com.eazybrew.vend.dto.request.PaymentRequestDto;
import com.eazybrew.vend.dto.response.DeviceResponse;
import com.eazybrew.vend.dto.response.PaymentReturnDataResponseDto;
import com.eazybrew.vend.dto.response.PaymentStatusResponseDto;
import com.eazybrew.vend.dto.response.TransactionResponse;
import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.service.DeviceService;
import com.eazybrew.vend.service.PaymentRequestService;
import com.eazybrew.vend.util.EncryptionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/processrequests")
@RequiredArgsConstructor
@Tag(name = "Payment Request Managements", description = "APIs for managing payment requestss")
public class PaymentRequestController {

    private final PaymentRequestService paymentRequestService;
    private final DeviceService deviceService;
    private final EncryptionUtil encryptionUtil;

    @PostMapping
    @Operation(summary = "Process a payment request", description = "Process a new payment request")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment request processed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> processPaymentRequest(
            @RequestHeader("X-API-KEY") String encryptedApiKey,
            @Valid @RequestBody PaymentRequestDto request) {

        try {
            // Decrypt the API key from the header
            String decryptedApiKey = encryptionUtil.decrypt(encryptedApiKey);

            // Set the decrypted API key in the request
            request.setApiKey(decryptedApiKey);
            log.info("Received payment request: {}", request);
            TransactionResponse response = paymentRequestService.processPaymentRequest(request);

            ApiResponse<TransactionResponse> apiResponse = new ApiResponse<>(HttpStatus.OK);
            apiResponse.setMessage("Payment request processed successfully");
            apiResponse.setData(response);

            return new ResponseEntity<>(apiResponse, apiResponse.getStatus());


        } catch (RuntimeException e) {
            if (e.getMessage().contains("Decryption failed")) {
                log.error("Failed to decrypt API key: {}", encryptedApiKey);
                throw new CustomException("Invalid encrypted API key", HttpStatus.UNAUTHORIZED);
            }
            throw e;
        }

    }

    @GetMapping("/status/{transactionReference}")
    @Operation(summary = "Get payment status", description = "Get the status of a payment request")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment status retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TransactionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment request not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> getPaymentStatus(
            @PathVariable String transactionReference) {
        
        log.info("Getting payment status for transaction reference: {}", transactionReference);
        TransactionResponse response = paymentRequestService.getPaymentStatus(transactionReference);
        
        ApiResponse<TransactionResponse> apiResponse = new ApiResponse<>(HttpStatus.OK);
        apiResponse.setMessage("Payment status retrieved successfully");
        apiResponse.setData(response);
        
        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
    }

//    @GetMapping("/returndata/{transactionReference}")
//    @Operation(summary = "Get payment return data", description = "Get the return data for a payment request")
//    @ApiResponses(value = {
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment return data retrieved successfully",
//                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentReturnDataResponseDto.class))),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment request not found",
//                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Payment not successful",
//                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class)))
//    })
//    public ResponseEntity<ApiResponse<PaymentReturnDataResponseDto>> getPaymentReturnData(
//            @PathVariable String transactionReference) {
//
//        log.info("Getting payment return data for transaction reference: {}", transactionReference);
//        PaymentReturnDataResponseDto response = paymentRequestService.getPaymentReturnData(transactionReference);
//
//        ApiResponse<PaymentReturnDataResponseDto> apiResponse = new ApiResponse<>(HttpStatus.OK);
//        apiResponse.setMessage("Payment return data retrieved successfully");
//        apiResponse.setData(response);
//
//        return new ResponseEntity<>(apiResponse, apiResponse.getStatus());
//    }

    @GetMapping("/vend-mach/{serial}")
    @Operation(summary = "Get Vending machine by serial number", description = "Get a device by its serial number.")
    public ResponseEntity< ApiResponse<DeviceResponse>> getVendingMachineBySerial(@PathVariable String serial) {
        DeviceResponse response = deviceService.getDeviceByVendingMachineId(serial);
        ApiResponse<DeviceResponse> responseApi = new ApiResponse<>(HttpStatus.OK);
        responseApi.setMessage("Vending machine data retrieved successfully");
        responseApi.setData(response);
        return new ResponseEntity<>(responseApi, responseApi.getStatus());
    }
}