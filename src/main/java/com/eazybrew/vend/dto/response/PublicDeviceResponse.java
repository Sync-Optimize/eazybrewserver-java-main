package com.eazybrew.vend.dto.response;

import com.eazybrew.vend.model.Device;
import com.eazybrew.vend.model.enums.DeviceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Public-facing device response — omits sensitive internal fields like
 * nombaTerminalId.
 * Used by vending-machine client endpoints under /api/processrequests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicDeviceResponse {

    private Long id;
    private String deviceId;
    private String deviceName;
    private String vendingMachineId;
    private String vendingMachineName;
    private DeviceType deviceType;
    private boolean enabled;
    private Long companyId;
    private boolean companyEnabled;
    private String companyName;
    private String apiKey;
    private LocalDateTime lastActive;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PublicDeviceResponse fromEntity(Device device) {
        return PublicDeviceResponse.builder()
                .id(device.getId())
                .deviceId(device.getDeviceId())
                .deviceName(device.getDeviceName())
                .vendingMachineId(device.getVendingMachineId())
                .vendingMachineName(device.getVendingMachineName())
                .deviceType(device.getDeviceType())
                .enabled(device.isEnabled())
                .companyId(device.getCompany().getId())
                .companyName(device.getCompany().getName())
                .companyEnabled(device.getCompany().isEnabled())
                .apiKey(device.getCompany().getApiKey())
                .lastActive(device.getLastActive())
                .location(device.getLocation())
                .createdAt(device.getDateCreated())
                .updatedAt(device.getDateModified())
                .build();
    }
}
