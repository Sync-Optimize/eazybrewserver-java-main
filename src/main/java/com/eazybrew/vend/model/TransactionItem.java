package com.eazybrew.vend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Entity
@Table(name = "transaction_items")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionItem extends BaseEntity<Long> {
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "transaction_id", nullable = false)
//    private Transaction transaction;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalPrice;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category;

}
