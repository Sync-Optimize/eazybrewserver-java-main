
package com.eazybrew.vend.dto.response;

import com.eazybrew.vend.model.Company;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyResponseDto {
    private Long id;
    private String name;
    private String address;
    private boolean enabled;
    private String website;
    private String industry;
    private String description;
    private LocalDateTime createdAt;

    public static CompanyResponseDto fromEntity(Company company) {
        if (company == null) {
            return null;
        }

        return CompanyResponseDto.builder()
                .id(company.getId())
                .name(company.getName())
                .address(company.getAddress())
                .enabled(company.isEnabled())
//                .website(company.getWebsite())
//                .industry(company.getIndustry())
//                .description(company.getDescription())
                .createdAt(company.getDateCreated())
                .build();
    }
}
