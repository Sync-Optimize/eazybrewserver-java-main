package com.eazybrew.vend.model;

import com.eazybrew.vend.model.enums.PaymentMethod;
import com.eazybrew.vend.model.enums.TransactionStatus;
import com.eazybrew.vend.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Builder
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity<Long> {
    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Column(name = "reference_number", nullable = false)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @Column(name = "notes")
    private String notes;

    @Column(name = "paystack_reference")
    private String paystackReference;

//    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<TransactionItem> items = new ArrayList<>();
//
//    // Helper method to add an item
//    public void addItem(TransactionItem item) {
//        items.add(item);
//        item.setTransaction(this);
//    }
//
//    // Helper method to remove an item
//    public void removeItem(TransactionItem item) {
//        items.remove(item);
//        item.setTransaction(null);
//    }

}
