package com.eazybrew.vend.service;

import com.eazybrew.vend.dto.request.DeviceRequest;
import com.eazybrew.vend.dto.response.DeviceResponse;
import com.eazybrew.vend.model.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DeviceService {

    /**
     * Register a new device
     *
     * @param request The device registration request
     * @return The registered device response
     */
    DeviceResponse registerDevice(DeviceRequest request);

    /**
     * Get a device by its ID
     *
     * @param id The device ID
     * @return The device response
     */
    DeviceResponse getDeviceById(Long id);

    /**
     * Get a device by its device ID (serial number)
     *
     * @param deviceId The device ID (serial number)
     * @return The device response
     */
    DeviceResponse getDeviceByDeviceId(String deviceId);

    /**
     * Get a device by its device ID (serial number)
     *
     * @param deviceId The device ID (serial number)
     * @return The device response
     */
    DeviceResponse getDeviceByVendingMachineId(String vendingMachineId);


    /**
     * Get all devices for a company
     *
     * @param companyId The company ID
     * @return List of device responses
     */
    List<DeviceResponse> getDevicesByCompany(Long companyId);

    /**
     * Get all devices for a company with pagination
     *
     * @param companyId The company ID
     * @param pageable Pagination information
     * @return Page of device responses
     */
    Page<DeviceResponse> getDevicesByCompany(Long companyId, Pageable pageable);

    /**
     * Get all devices with pagination
     *
     * @param pageable Pagination information
     * @return Page of device responses
     */
    Page<DeviceResponse> getAllDevices(Pageable pageable);

    /**
     * Enable or disable a device
     *
     * @param deviceId The deviceID
     * @param enabled The enabled status
     * @return The updated device response
     */
    DeviceResponse updateDeviceStatus(String deviceId, boolean enabled);

    /**
     * Update a device's information
     *
     * @param deviceId The deviceID
     * @param request The device update request
     * @return The updated device response
     */
    DeviceResponse updateDevice(String deviceId, DeviceRequest request);

    /**
     * Delete a device
     *
     * @param id The deviceID
     */
    Boolean deleteDevice(String deviceId);

    /**
     * Check if a device is registered and enabled
     *
     * @param deviceId The device ID (serial number)
     * @return true if the device is registered and enabled, false otherwise
     */
    boolean isDeviceRegisteredAndEnabled(String deviceId);
}