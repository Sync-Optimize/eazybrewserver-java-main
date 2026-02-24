package com.eazybrew.vend.service.impl;

import com.eazybrew.vend.dto.request.DeviceRequest;
import com.eazybrew.vend.dto.response.DeviceResponse;
import com.eazybrew.vend.dto.response.PublicDeviceResponse;
import com.eazybrew.vend.exceptions.CustomException;
import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.model.Device;
import com.eazybrew.vend.model.enums.RecordStatusConstant;
import com.eazybrew.vend.repository.CompanyRepository;
import com.eazybrew.vend.repository.DeviceRepository;
import com.eazybrew.vend.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceServiceImpl implements DeviceService {

        private final DeviceRepository deviceRepository;
        private final CompanyRepository companyRepository;

        @Override
        @Transactional
        public DeviceResponse registerDevice(DeviceRequest request) {
                // Check if device ID already exists
                if (deviceRepository.existsByDeviceIdAndRecordStatus(request.getDeviceId(),
                                RecordStatusConstant.ACTIVE)) {
                        throw new CustomException("Device with ID " + request.getDeviceId() + " already exists",
                                        HttpStatus.BAD_REQUEST);
                }

                // Check if vending machine ID already exists (if provided)
                if (request.getVendingMachineId() != null && !request.getVendingMachineId().isEmpty() &&
                                deviceRepository.existsByVendingMachineIdAndRecordStatus(request.getVendingMachineId(),
                                                RecordStatusConstant.ACTIVE)) {
                        throw new CustomException(
                                        "Device with Vending Machine ID " + request.getVendingMachineId()
                                                        + " already exists",
                                        HttpStatus.BAD_REQUEST);
                }

                // Get company
                Company company = companyRepository
                                .findByIdAndRecordStatus(request.getCompanyId(), RecordStatusConstant.ACTIVE)
                                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

                // Create and save device
                Device device = Device.builder()
                                .deviceId(request.getDeviceId())
                                .deviceName(request.getDeviceName())
                                .deviceType(request.getDeviceType())
                                .enabled(true) // Enable by default
                                .company(company)
                                .location(request.getLocation())
                                .build();
                if (request.getVendingMachineId() != null && !request.getVendingMachineId().isEmpty()) {
                        device.setVendingMachineId(request.getVendingMachineId());
                }

                if (request.getVendingMachineName() != null && !request.getVendingMachineName().isEmpty()) {
                        if (deviceRepository.existsByVendingMachineNameLikeIgnoreCaseAndCompanyAndRecordStatus(
                                        request.getVendingMachineName(),
                                        company,
                                        RecordStatusConstant.ACTIVE)) {
                                throw new CustomException(
                                                "Device with Vending Machine Name " + request.getVendingMachineName() +
                                                                " already exists",
                                                HttpStatus.BAD_REQUEST);
                        }
                        device.setVendingMachineName(request.getVendingMachineName());
                }

                if (request.getNombaTerminalId() != null && !request.getNombaTerminalId().isBlank()) {
                        device.setNombaTerminalId(request.getNombaTerminalId());
                }

                device = deviceRepository.save(device);
                log.info("Device registered: {}", device.getDeviceId());

                return DeviceResponse.fromEntity(device);
        }

        @Override
        @Transactional(readOnly = true)
        public DeviceResponse getDeviceById(Long id) {
                Device device = deviceRepository.findByIdAndRecordStatus(id, RecordStatusConstant.ACTIVE)
                                .orElseThrow(() -> new CustomException("Device not found", HttpStatus.NOT_FOUND));

                return DeviceResponse.fromEntity(device);
        }

        @Override
        @Transactional(readOnly = true)
        public DeviceResponse getDeviceByDeviceId(String deviceId) {
                Device device = deviceRepository.findByDeviceIdAndRecordStatus(deviceId, RecordStatusConstant.ACTIVE)
                                .orElseThrow(() -> new CustomException("Device not found", HttpStatus.NOT_FOUND));

                return DeviceResponse.fromEntity(device);
        }

        @Override
        public DeviceResponse getDeviceByVendingMachineId(String vendingMachineId) {
                Device device = deviceRepository
                                .findByVendingMachineIdAndRecordStatus(vendingMachineId, RecordStatusConstant.ACTIVE)
                                .orElseThrow(() -> new CustomException("Device not found", HttpStatus.NOT_FOUND));

                return DeviceResponse.fromEntity(device);
        }

        @Override
        public PublicDeviceResponse getPublicDeviceByVendingMachineId(String vendingMachineId) {
                Device device = deviceRepository
                                .findByVendingMachineIdAndRecordStatus(vendingMachineId, RecordStatusConstant.ACTIVE)
                                .orElseThrow(() -> new CustomException("Device not found", HttpStatus.NOT_FOUND));

                return PublicDeviceResponse.fromEntity(device);
        }

        @Override
        @Transactional(readOnly = true)
        public List<DeviceResponse> getDevicesByCompany(Long companyId) {
                Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

                return deviceRepository.findByCompanyAndRecordStatus(company, RecordStatusConstant.ACTIVE).stream()
                                .map(DeviceResponse::fromEntity)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public Page<DeviceResponse> getDevicesByCompany(Long companyId, Pageable pageable) {
                Company company = companyRepository.findByIdAndRecordStatus(companyId, RecordStatusConstant.ACTIVE)
                                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));

                return deviceRepository.findByCompanyAndRecordStatus(company, RecordStatusConstant.ACTIVE, pageable)
                                .map(DeviceResponse::fromEntity);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<DeviceResponse> getAllDevices(Pageable pageable) {
                return deviceRepository.findAllWithStatus(RecordStatusConstant.ACTIVE, pageable)
                                .map(DeviceResponse::fromEntity);
        }

        @Override
        @Transactional
        public DeviceResponse updateDeviceStatus(String deviceId, boolean enabled) {
                Device device = deviceRepository.findByDeviceIdAndRecordStatus(deviceId, RecordStatusConstant.ACTIVE)
                                .orElseThrow(() -> new CustomException("Device not found", HttpStatus.NOT_FOUND));

                device.setEnabled(enabled);
                device = deviceRepository.save(device);

                log.info("Device {} status updated to {}", device.getDeviceId(), enabled ? "enabled" : "disabled");

                return DeviceResponse.fromEntity(device);
        }

        @Override
        @Transactional
        public DeviceResponse updateDevice(String deviceId, DeviceRequest request) {
                Company company = companyRepository
                                .findByIdAndRecordStatus(request.getCompanyId(), RecordStatusConstant.ACTIVE)
                                .orElseThrow(() -> new CustomException("Company not found", HttpStatus.NOT_FOUND));
                Device device = deviceRepository.findByDeviceIdAndRecordStatus(deviceId, RecordStatusConstant.ACTIVE)
                                .orElseThrow(() -> new CustomException("Device not found", HttpStatus.NOT_FOUND));

                // Check if device ID is being changed and if it already exists
                if (!device.getDeviceId().equals(request.getDeviceId()) &&
                                deviceRepository.existsByDeviceIdAndRecordStatus(request.getDeviceId(),
                                                RecordStatusConstant.ACTIVE)) {
                        throw new CustomException("Device with ID " + request.getDeviceId() + " already exists",
                                        HttpStatus.BAD_REQUEST);
                }

                // Check if vending machine ID is being changed and if it already exists
                if (request.getVendingMachineId() != null && !request.getVendingMachineId().isEmpty() &&
                                (device.getVendingMachineId() == null
                                                || !device.getVendingMachineId().equals(request.getVendingMachineId()))
                                &&
                                deviceRepository.existsByVendingMachineIdAndRecordStatus(request.getVendingMachineId(),
                                                RecordStatusConstant.ACTIVE)) {
                        throw new CustomException(
                                        "Device with Vending Machine ID " + request.getVendingMachineId()
                                                        + " already exists",
                                        HttpStatus.BAD_REQUEST);
                }

                // Check if company is being changed
                if (!device.getCompany().getId().equals(request.getCompanyId())) {
                        Company newCompany = companyRepository
                                        .findByIdAndRecordStatus(request.getCompanyId(), RecordStatusConstant.ACTIVE)
                                        .orElseThrow(() -> new CustomException("Company not found",
                                                        HttpStatus.NOT_FOUND));
                        device.setCompany(newCompany);
                }

                // Update device properties
                device.setDeviceId(request.getDeviceId());
                device.setDeviceName(request.getDeviceName());
                device.setDeviceType(request.getDeviceType());
                device.setLocation(request.getLocation());

                if (request.getVendingMachineId() != null && !request.getVendingMachineId().isEmpty()) {
                        device.setVendingMachineId(request.getVendingMachineId());
                }
                if (request.getVendingMachineName() != null && !request.getVendingMachineName().isEmpty()) {
                        if (deviceRepository.existsByVendingMachineNameLikeIgnoreCaseAndCompanyAndRecordStatus(
                                        request.getVendingMachineName(),
                                        company,
                                        RecordStatusConstant.ACTIVE)) {
                                throw new CustomException(
                                                "Device with Vending Machine Name " + request.getVendingMachineName() +
                                                                " already exists",
                                                HttpStatus.BAD_REQUEST);
                        }
                        device.setVendingMachineName(request.getVendingMachineName());
                }

                // Update Nomba terminal ID (set to null if explicitly cleared, update if
                // provided)
                device.setNombaTerminalId(request.getNombaTerminalId());

                device = deviceRepository.save(device);
                log.info("Device updated: {}", device.getDeviceId());

                return DeviceResponse.fromEntity(device);
        }

        @Override
        @Transactional
        public Boolean deleteDevice(String deviceId) {
                Device device = deviceRepository.findByDeviceIdAndRecordStatus(deviceId, RecordStatusConstant.ACTIVE)
                                .orElseThrow(() -> new CustomException("Device not found", HttpStatus.NOT_FOUND));
                device.setRecordStatus(RecordStatusConstant.DELETED);

                deviceRepository.save(device);
                log.info("Device deleted: {}", device.getDeviceId());
                return Boolean.TRUE;
        }

        @Override
        @Transactional(readOnly = true)
        public boolean isDeviceRegisteredAndEnabled(String deviceId) {
                return deviceRepository.findByDeviceIdAndRecordStatus(deviceId, RecordStatusConstant.ACTIVE)
                                .map(Device::isEnabled)
                                .orElse(false);
        }
}
