package com.ltf.loanmanagement.service;

import com.ltf.loanmanagement.dto.CustomerRequest;
import com.ltf.loanmanagement.dto.CustomerResponse;
import com.ltf.loanmanagement.entity.Customer;
import com.ltf.loanmanagement.exception.DuplicateResourceException;
import com.ltf.loanmanagement.exception.ResourceNotFoundException;
import com.ltf.loanmanagement.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A customer with email " + request.getEmail() + " already exists");
        }
        if (customerRepository.existsByPanNumber(request.getPanNumber())) {
            throw new DuplicateResourceException("A customer with PAN " + request.getPanNumber() + " already exists");
        }

        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .panNumber(request.getPanNumber())
                .dateOfBirth(request.getDateOfBirth())
                .build();

        return toResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> list(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::toResponse);
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findOrThrow(id);
        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        // Email / PAN are treated as immutable identity fields once created.
        return toResponse(customerRepository.save(customer));
    }

    public void delete(Long id) {
        Customer customer = findOrThrow(id);
        customerRepository.delete(customer);
    }

    private Customer findOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer " + id + " not found"));
    }

    private CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder()
                .id(c.getId())
                .fullName(c.getFullName())
                .email(c.getEmail())
                .phone(c.getPhone())
                .panNumber(c.getPanNumber())
                .dateOfBirth(c.getDateOfBirth())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
