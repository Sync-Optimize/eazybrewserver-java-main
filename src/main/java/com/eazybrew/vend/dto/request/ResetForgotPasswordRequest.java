package com.eazybrew.vend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetForgotPasswordRequest {
    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}