package com.zidio.keystone.service;

import com.zidio.keystone.domain.Customer;
import com.zidio.keystone.dto.customer.CustomerRequest;
import com.zidio.keystone.dto.customer.CustomerResponse;
import com.zidio.keystone.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        Customer customer = Customer.builder()
                .name(request.name())
                .contactEmail(request.contactEmail())
                .build();

        Customer saved = customerRepository.save(customer);
        return CustomerResponse.from(saved);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    public CustomerResponse getById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + id));
        return CustomerResponse.from(customer);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    public Page<CustomerResponse> list(Pageable pageable) {
        return customerRepository.findAll(pageable)
                .map(CustomerResponse::from);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + id));

        customer.setName(request.name());
        customer.setContactEmail(request.contactEmail());

        return CustomerResponse.from(customer);
    }
}
