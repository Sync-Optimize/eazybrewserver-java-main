package com.eazybrew.vend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {
    private Long id;
    private String fullName;
    private String cardSerial;
    private String email;
    private String employeeId;
    private Long parentCompanyId;
    private String companyName;
    private boolean enabled;
}
