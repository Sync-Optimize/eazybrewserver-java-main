package com.eazybrew.vend.model;

import com.eazybrew.vend.model.enums.DeviceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "devices")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Device extends BaseEntity<Long>{
    @Column(name = "device_id", unique = true, nullable = false)
    private String deviceId; // Device serial number

    @Column(name = "device_name", nullable = false)
    private String deviceName;

    @Column(name = "vending_machine_id", unique = true)
    private String vendingMachineId; // Vending Machine serial number

    @Column(name = "vending_machine_name")
    private String vendingMachineName;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type")
    private DeviceType deviceType; // VENDING_MACHINE or POS

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @Column(name = "location")
    private String location;

}
