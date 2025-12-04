package org.policedog.registry.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.policedog.registry.dto.*;
import org.policedog.registry.service.SupplierService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/dogs/supplier")
public class SupplierController {
    private final SupplierService supplierService;

    @GetMapping(value = "/{id}", produces = "application/json")
    @Operation(summary = "Get a supplier by ID including their dogs",
            description = """
                    Retrieve the details of a specific supplier by its ID, including the list of police dogs supplied by them.
                    """, tags = {"Supplier - Query Operations"})
    public ResponseEntity<SupplierDetailDto> getSupplier(@PathVariable Long id) {
        SupplierDetailDto supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }

    @GetMapping(produces = "application/json")
    @Operation(summary = "Get a paginated list of suppliers",
            description = """
                    Retrieve a paginated list of suppliers in the system.
                    
                    """, tags = {"Supplier - Query Operations"})
    public ResponseEntity<PageResponse<SupplierDetailDto>> getSuppliers(@RequestParam(defaultValue = "0") int pageNo, @RequestParam(defaultValue = "10") int pageSize) {
        PageResponse<SupplierDetailDto> suppliers = supplierService.getSuppliers(pageNo, pageSize);
        return ResponseEntity.ok(suppliers);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    @Operation(summary = "Create a new Supplier",
            description = """
                    Create a new Supplier with the provided details.
                    
                    Error will be returned in the following cases:
                    - Supplier code is not unique
                    - Required fields are missing or invalid
                    """, tags = {"Supplier - Command Operations"}
    )
    public ResponseEntity<SupplierDetailDto> createSupplier(@Valid @RequestBody SupplierRequest createSupplierRequest) {
        SupplierDetailDto supplier = supplierService.createSupplier(createSupplierRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(supplier);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a supplier by ID",
            description = """
                    
                    Error will be returned in the following cases:
                    - Supplier with the given ID does not exist
                    """, tags = {"Supplier - Command Operations"})
    public ResponseEntity<SupplierDetailDto> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierRequest supplierRequest) {
        SupplierDetailDto supplierDetailDto = supplierService.updateSupplier(id, supplierRequest);
        return ResponseEntity.ok(supplierDetailDto);
    }

}
