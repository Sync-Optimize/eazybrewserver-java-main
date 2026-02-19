package com.eazybrew.vend.model;

import com.eazybrew.vend.model.enums.RecordStatusConstant;
import com.eazybrew.vend.model.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Data
@Entity
@Builder
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "phoneNumber")
        })
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity<Long> {
    @Column(unique = true)
    private String email;
    private String password;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;
    private String staffId;
    private String fullName;
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String phoneNumber;
    private boolean enforcePasswordReset;
    private LocalDateTime passwordExpiryDate;
    private boolean accountNonLocked;
    private int failedAttempt;
    private LocalDateTime lockTime;
    private String avatar;
    private String workEmail;
    @JsonIgnore
    @OneToOne
    private Company company;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}
