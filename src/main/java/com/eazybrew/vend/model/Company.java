package com.eazybrew.vend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company extends BaseEntity<Long> {
    @Column(unique = true)
    private String companyEmail;
    @NotBlank(message = "Company name is required")
    @Column(nullable = false)
    private String name;
    @NotBlank(message = "Address is required")
    @Column(nullable = false)
    private String address;
    @Column(nullable = false)
    private boolean enabled = true;
    @Column(unique = true, nullable = false)
    private String apiKey;
    private String companyPhoneNumber;
    private Long licenseNumber;
    private Boolean onTrialPeriod;
    private String website;
    private String description;
    private String industry;

    @Override
    public String toString() {
        return "Company{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
