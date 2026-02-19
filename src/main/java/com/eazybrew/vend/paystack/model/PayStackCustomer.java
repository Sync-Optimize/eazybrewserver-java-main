package com.eazybrew.vend.paystack.model;


import com.eazybrew.vend.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "paystack_customer")
public class PayStackCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    private User creator;


    private BigDecimal payStackCustomerId;
    private String  customerCode;
}
