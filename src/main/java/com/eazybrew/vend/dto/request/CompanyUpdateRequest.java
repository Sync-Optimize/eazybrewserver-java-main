package com.eazybrew.vend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUpdateRequest {

    @NotBlank(message = "Company name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;


    // Optional company fields
    private String website;
    private String description;
    private String industry;
}