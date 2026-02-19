package com.eazybrew.vend.controller;

import com.eazybrew.vend.apiresponse.ApiResponse;
import com.eazybrew.vend.dto.request.DeviceRequest;
import com.eazybrew.vend.dto.response.DeviceResponse;
import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "Device Management", description = "APIs for managing devices (vending machines and POS)")
@SecurityRequirement(name = "bearerAuth")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Register a new device", description = "Register a new device (vending machine or POS). Requires SUPERADMIN role.")
    public ResponseEntity<ApiResponse<DeviceResponse>> registerDevice(@Valid @RequestBody DeviceRequest request) {
        DeviceResponse response = deviceService.registerDevice(request);
        ApiResponse<DeviceResponse> responseApi = new ApiResponse<>(HttpStatus.OK);
        responseApi.setMessage("Device Created successfully");
        responseApi.setData(response);
        return new ResponseEntity<>(responseApi, responseApi.getStatus());
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Get all devices", description = "Get all devices with pagination. Requires SUPERADMIN role.")
    public ResponseEntity<ApiResponse<Page<DeviceResponse>>> getAllDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<DeviceResponse> devicePage = deviceService.getAllDevices(pageable);
        ApiResponse<Page<DeviceResponse>> response = new ApiResponse<>(HttpStatus.OK);
        response.setMessage("Devices retrieved successfully");
        response.setData(devicePage);
        return new ResponseEntity<>(response, response.getStatus());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Get device by ID", description = "Get a device by its ID. Requires SUPERADMIN role.")
    public ResponseEntity<ApiResponse<DeviceResponse>> getDeviceById(@PathVariable Long id) {
        DeviceResponse response = deviceService.getDeviceById(id);
        ApiResponse<DeviceResponse> responseApi = new ApiResponse<>(HttpStatus.OK);
        responseApi.setMessage("Device data retrieved successfully");
        responseApi.setData(response);
        return new ResponseEntity<>(responseApi, responseApi.getStatus());
    }

    @GetMapping("/serial/{deviceId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Get device by serial number", description = "Get a device by its serial number. Requires SUPERADMIN role.")
    public ResponseEntity< ApiResponse<DeviceResponse>> getDeviceByDeviceId(@PathVariable String deviceId) {
        DeviceResponse response = deviceService.getDeviceByDeviceId(deviceId);
        ApiResponse<DeviceResponse> responseApi = new ApiResponse<>(HttpStatus.OK);
        responseApi.setMessage("Device data retrieved successfully");
        responseApi.setData(response);
        return new ResponseEntity<>(responseApi, responseApi.getStatus());
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Get devices by company", description = "Get all devices for a specific company. Requires SUPERADMIN role.")
    public ResponseEntity<ApiResponse<Page<DeviceResponse>>> getDevicesByCompany(@PathVariable Long companyId,
                                                                                 @RequestParam(defaultValue = "0") int page,
                                                                                 @RequestParam(defaultValue = "10") int size,
                                                                                 @RequestParam(defaultValue = "id") String sortBy,
                                                                                 @RequestParam(defaultValue = "asc") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<DeviceResponse> devices = deviceService.getDevicesByCompany(companyId,pageable);
        ApiResponse<Page<DeviceResponse>> responseApi = new ApiResponse<>(HttpStatus.OK);
        responseApi.setMessage("Devices retrieved successfully");
        responseApi.setData(devices);
        return new ResponseEntity<>(responseApi, responseApi.getStatus());
    }

    @PutMapping("/{deviceId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Update device", description = "Update a device's information. Requires SUPERADMIN role.")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDevice(
            @PathVariable String deviceId,
            @Valid @RequestBody DeviceRequest request) {
        DeviceResponse response = deviceService.updateDevice(deviceId, request);
        ApiResponse<DeviceResponse> responseApi = new ApiResponse<>(HttpStatus.OK);
        responseApi.setMessage("Device updated successfully");
        responseApi.setData(response);
        return new ResponseEntity<>(responseApi, responseApi.getStatus());
    }

    @PatchMapping("/{deviceId}/status")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Enable/disable device", description = "Enable or disable a device. Requires SUPERADMIN role.")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateDeviceStatus(
            @PathVariable String deviceId,
            @RequestParam boolean enabled) {
        DeviceResponse response = deviceService.updateDeviceStatus(deviceId, enabled);
        ApiResponse<DeviceResponse> responseApi = new ApiResponse<>(HttpStatus.OK);
        responseApi.setMessage("Device updated successfully");
        responseApi.setData(response);
        return new ResponseEntity<>(responseApi, responseApi.getStatus());
    }

    @DeleteMapping("/{deviceId}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @Operation(summary = "Delete device", description = "Delete a device. Requires SUPERADMIN role.")
    public ResponseEntity<ApiResponse<Boolean>> deleteDevice(@PathVariable String deviceId) {
        Boolean response = deviceService.deleteDevice(deviceId);
        ApiResponse<Boolean> responseApi = new ApiResponse<>(HttpStatus.OK);
        responseApi.setMessage("Device deleted successfully");
        responseApi.setData(response);
        return new ResponseEntity<>(responseApi, responseApi.getStatus());
    }
}