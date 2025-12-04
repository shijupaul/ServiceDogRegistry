package org.policedog.registry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.policedog.registry.dao.SupplierRepository;
import org.policedog.registry.domain.Supplier;
import org.policedog.registry.dto.PageResponse;
import org.policedog.registry.dto.SupplierDetailDto;
import org.policedog.registry.dto.SupplierRequest;
import org.policedog.registry.exception.ResourceNotFoundException;
import org.policedog.registry.mapper.EntityDtoMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final EntityDtoMapper entityDtoMapper;

    @Transactional(readOnly = true)
    public SupplierDetailDto getSupplierById(Long id) {
        Supplier supplier = getSupplier(id);
        return entityDtoMapper.toSupplierDetailDto(supplier);
    }

    @Transactional(readOnly = true)
    public Supplier getSupplierByCode(String supplierCode) {
        return supplierRepository.findByCode(supplierCode)
                .orElseThrow(() -> {
                    log.error("Supplier code {} not found", supplierCode);
                    return new ResourceNotFoundException("Supplier with code " + supplierCode + " not found");
                });
    }

    @Transactional(readOnly = true)
    public PageResponse<SupplierDetailDto> getSuppliers(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        var pageRequest = supplierRepository.findAll(pageable);
        return entityDtoMapper.toSupplierDetailPageResponse(pageRequest);
    }

    @Transactional
    public SupplierDetailDto createSupplier(SupplierRequest createSupplierRequest) {
        raiseErrorIfSupplierCodeExists(createSupplierRequest.getCode());

        var supplierEntity = entityDtoMapper.toSupplier(createSupplierRequest);
        var savedSupplier = supplierRepository.save(supplierEntity);
        log.info("Created new supplier with ID {}", savedSupplier.getId());
        return entityDtoMapper.toSupplierDetailDto(savedSupplier);
    }

    @Transactional
    public SupplierDetailDto updateSupplier(Long id, SupplierRequest supplierRequest) {
        Supplier supplier = getSupplier(id);
        if (!supplier.getCode().equals(supplierRequest.getCode())) {
            raiseErrorIfSupplierCodeExists(supplierRequest.getCode());
        }

        entityDtoMapper.updateSupplierFromDto(supplierRequest, supplier);
        var updatedSupplier = supplierRepository.save(supplier);
        log.info("Updated supplier with ID {}", updatedSupplier.getId());
        return entityDtoMapper.toSupplierDetailDto(updatedSupplier);
    }


    private Supplier getSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Supplier with id {} not found", id);
                    return new ResourceNotFoundException("Supplier with id " + id + " not found");
                });
    }

    private void raiseErrorIfSupplierCodeExists(String supplierCode) {
        if (supplierRepository.existsByCode(supplierCode)) {
            log.error("Supplier with code {} already exists", supplierCode);
            throw new IllegalArgumentException("Supplier with code " + supplierCode + " already exists");
        }
    }
}
