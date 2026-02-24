package com.eazybrew.vend.dto.request;

import com.eazybrew.vend.model.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceRequest {

    @NotBlank(message = "Device ID (serial number) is required")
    private String deviceId;

    @NotBlank(message = "Device name is required")
    private String deviceName;

    private String vendingMachineId;

    private String vendingMachineName;

    @NotNull(message = "Device type is required")
    private DeviceType deviceType;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    private String location;
    private String locationTest;
    private String nombaTerminalId;
}
