
package com.eazybrew.vend.dto.response;

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
public class AdminUserResponseDto {
    private Long id;
    private String email;
    private String fullName;
    private String phoneNumber;

    public static AdminUserResponseDto fromEntity(User user) {
        if (user == null) {
            return null;
        }

        return AdminUserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
