package com.zidio.keystone.service;

import com.zidio.keystone.domain.Customer;
import com.zidio.keystone.domain.Site;
import com.zidio.keystone.dto.site.SiteRequest;
import com.zidio.keystone.dto.site.SiteResponse;
import com.zidio.keystone.repository.CustomerRepository;
import com.zidio.keystone.repository.SiteRepository;
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
public class SiteService {

    private final SiteRepository siteRepository;
    private final CustomerRepository customerRepository;

    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Transactional
    public SiteResponse create(SiteRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new NoSuchElementException("Customer not found " + request.customerId()));

        Site site = Site.builder()
                .customer(customer)
                .name(request.name())
                .address(request.address())
                .build();

        Site saved = siteRepository.save(site);
        return SiteResponse.from(saved);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Transactional(readOnly = true)
    public SiteResponse getById(UUID id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Site not found: " + id));
        return SiteResponse.from(site);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Transactional(readOnly = true)
    public Page<SiteResponse> list(Pageable pageable) {
        return siteRepository.findAll(pageable)
                .map(SiteResponse::from);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Transactional(readOnly = true)
    public Page<SiteResponse> listByCustomer(UUID customerId, Pageable pageable) {
        return siteRepository.findByCustomerId(customerId, pageable)
                .map(SiteResponse::from);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Transactional(readOnly = true)
    public SiteResponse update(UUID id, SiteRequest request) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Site not found: " + id));

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + request.customerId()));

        site.setCustomer(customer);
        site.setName(request.name());
        site.setAddress(request.address());

        return SiteResponse.from(site);
    }
}
