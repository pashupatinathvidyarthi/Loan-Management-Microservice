package com.ltf.loanmanagement.repository;

import com.ltf.loanmanagement.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByPanNumber(String panNumber);
    boolean existsByEmail(String email);
    boolean existsByPanNumber(String panNumber);
}
