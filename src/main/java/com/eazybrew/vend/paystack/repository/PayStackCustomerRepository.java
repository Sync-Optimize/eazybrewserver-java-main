package com.eazybrew.vend.paystack.repository;


import com.eazybrew.vend.model.User;
import com.eazybrew.vend.paystack.model.PayStackCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayStackCustomerRepository extends JpaRepository<PayStackCustomer, Long> {

    Optional<PayStackCustomer> findByCreator(User agent);
}
