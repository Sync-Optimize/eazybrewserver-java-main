package com.eazybrew.vend.paystack.model;


import com.eazybrew.vend.model.Company;
import com.eazybrew.vend.model.Staff;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "paystack_transaction")
public class PaystackTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private BigDecimal amount;
    private String authorizationUrl;
    private String accessCode;
    private String reference;
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private Staff creator;
    private String status;
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private Company company;
}
