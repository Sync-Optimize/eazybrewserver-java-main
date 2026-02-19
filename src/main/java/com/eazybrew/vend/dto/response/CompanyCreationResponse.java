
package com.eazybrew.vend.dto.response;

import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyCreationResponse {
    private CompanyResponseDto company;
    private AdminUserResponseDto adminUser;
    private String temporaryPassword;
    private String apiKey;

    public static CompanyCreationResponse fromEntities(Company company, User adminUser, String temporaryPassword) {
        return CompanyCreationResponse.builder()
                .company(CompanyResponseDto.fromEntity(company))
                .adminUser(AdminUserResponseDto.fromEntity(adminUser))
                .temporaryPassword(temporaryPassword)
                .apiKey(company != null ? company.getApiKey() : null)
                .build();
    }
}
