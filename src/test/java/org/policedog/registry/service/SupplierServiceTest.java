package org.policedog.registry.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.policedog.registry.dao.SupplierRepository;
import org.policedog.registry.domain.Supplier;
import org.policedog.registry.dto.SupplierDetailDto;
import org.policedog.registry.dto.SupplierRequest;
import org.policedog.registry.exception.ResourceNotFoundException;
import org.policedog.registry.mapper.EntityDtoMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    private static final Long SUPPLIER_ID = 1L;
    private static final String SUPPLIER_CODE = "ELITEK9";
    private static final String SUPPLIER_WITH_ID_NOT_FOUND_MESSAGE = "Supplier with id %d not found";
    private static final String SUPPLIER_WITH_CODE_NOT_FOUND_MESSAGE = "Supplier with code %s not found";
    private static final String SUPPLIER_CODE_EXISTS_MESSAGE = "Supplier with code %s already exists";

    @Mock
    private SupplierRepository supplierRepositoryMock;

    @Mock
    private EntityDtoMapper entityDtoMapperMock;

    @InjectMocks
    private SupplierService supplierService;

    @Test
    void shouldErrorWhenSupplierNotFoundForGetSupplierById() {
        givenSupplierByIdReturns(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            supplierService.getSupplierById(SUPPLIER_ID);
        });

        assertEquals(SUPPLIER_WITH_ID_NOT_FOUND_MESSAGE.formatted(SUPPLIER_ID), exception.getMessage());
    }

    @Test
    void shouldReturnSupplierWhenFoundForGetSupplierById() {
        Supplier supplier = createSupplier();
        SupplierDetailDto supplierDetailDto = new SupplierDetailDto();

        givenSupplierByIdReturns(Optional.of(supplier));
        givenSupplierMappedToSupplierDetail(supplier, supplierDetailDto);

        SupplierDetailDto supplierDetailDtoRet = supplierService.getSupplierById(SUPPLIER_ID);

        assertEquals(supplierDetailDto, supplierDetailDtoRet);
    }

    @Test
    void shouldErrorWhenSupplierNotFoundForGetSupplierByCode() {
        when(supplierRepositoryMock.findByCode(SUPPLIER_CODE)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            supplierService.getSupplierByCode(SUPPLIER_CODE);
        });

        assertEquals(SUPPLIER_WITH_CODE_NOT_FOUND_MESSAGE.formatted(SUPPLIER_CODE), exception.getMessage());
    }

    @Test
    void shouldReturnSupplierWhenFoundForGetSupplierByCode() {
        Supplier supplier = createSupplier();

        when(supplierRepositoryMock.findByCode(SUPPLIER_CODE)).thenReturn(Optional.of(supplier));

        Supplier supplierRet = supplierService.getSupplierByCode(SUPPLIER_CODE);

        assertEquals(supplier, supplierRet);
    }

    @Test
    void shouldErrorWhenSupplierCodeExistsForCreateSupplier() {
        givenSupplierCodeExistsReturns(SUPPLIER_CODE, true);

        SupplierRequest createSupplierRequest = createSupplierRequest(SUPPLIER_CODE);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            supplierService.createSupplier(createSupplierRequest);
        });

        assertEquals(SUPPLIER_CODE_EXISTS_MESSAGE.formatted(SUPPLIER_CODE), exception.getMessage());
    }

    @Test
    void shouldCreateSupplierWhenCodeDoesNotExist() {
        SupplierRequest createSupplierRequest = createSupplierRequest(SUPPLIER_CODE);
        Supplier supplierEntity = createSupplier();

        SupplierDetailDto supplierDetailDto = new SupplierDetailDto();

        givenSupplierCodeExistsReturns(SUPPLIER_CODE, false);
        givenCreateSupplierMappedToSupplierEntity(createSupplierRequest, supplierEntity);
        givenSaveCalledOnRepository();
        givenSupplierMappedToSupplierDetail(supplierEntity, supplierDetailDto);

        SupplierDetailDto supplierDetailDtoRet = supplierService.createSupplier(createSupplierRequest);

        assertEquals(supplierDetailDto, supplierDetailDtoRet);
    }

    @Test
    void shouldErrorWhenSupplierNotFoundForUpdateSupplier() {
        givenSupplierByIdReturns(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            supplierService.updateSupplier(SUPPLIER_ID, new SupplierRequest());
        });

        assertEquals(SUPPLIER_WITH_ID_NOT_FOUND_MESSAGE.formatted(SUPPLIER_ID), exception.getMessage());
    }

    @Test
    void shouldErrorWhenSupplierCodeExistsForUpdateSupplier() {
        String supplierCodeChanged = "DIFFERENTCODE";

        Supplier existingSupplier = createSupplier();
        SupplierRequest updateRequest = createSupplierRequest(supplierCodeChanged);

        givenSupplierByIdReturns(Optional.of(existingSupplier));
        givenSupplierCodeExistsReturns(supplierCodeChanged, true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            supplierService.updateSupplier(SUPPLIER_ID, updateRequest);
        });

        assertEquals(SUPPLIER_CODE_EXISTS_MESSAGE.formatted(supplierCodeChanged), exception.getMessage());
    }

    @Test
    void shouldUpdateSupplierWhenCodeDoesNotExist() {
        String supplierCodeChanged = "DIFFERENTCODE";

        Supplier existingSupplier = createSupplier();
        SupplierRequest updateRequest = createSupplierRequest(supplierCodeChanged);
        SupplierDetailDto supplierDetailDto = new SupplierDetailDto();

        givenSupplierByIdReturns(Optional.of(existingSupplier));
        givenSupplierCodeExistsReturns(supplierCodeChanged, false);
        givenUpdateSupplierMappedFromDto(updateRequest, existingSupplier);
        givenSaveCalledOnRepository();
        givenSupplierMappedToSupplierDetail(existingSupplier, supplierDetailDto);

        SupplierDetailDto supplierDetailDtoRet = supplierService.updateSupplier(SUPPLIER_ID, updateRequest);

        assertEquals(supplierDetailDto, supplierDetailDtoRet);
    }


    private void givenSupplierByIdReturns(Optional<Supplier> supplier) {
        when(supplierRepositoryMock.findById(SUPPLIER_ID)).thenReturn(supplier);
    }

    private void givenSupplierCodeExistsReturns(String supplierCode, boolean supplierExists) {
        when(supplierRepositoryMock.existsByCode(supplierCode)).thenReturn(supplierExists);
    }

    private void givenSupplierMappedToSupplierDetail(Supplier supplier, SupplierDetailDto supplierDetailDto) {
        when(entityDtoMapperMock.toSupplierDetailDto(supplier)).thenReturn(supplierDetailDto);
    }

    private void givenCreateSupplierMappedToSupplierEntity(SupplierRequest request, Supplier supplier) {
        when(entityDtoMapperMock.toSupplier(request)).thenReturn(supplier);
    }

    private void givenUpdateSupplierMappedFromDto(SupplierRequest request, Supplier supplier) {
        doNothing().when(entityDtoMapperMock).updateSupplierFromDto(request, supplier);
    }

    private void givenSaveCalledOnRepository() {
        when(supplierRepositoryMock.save(any(Supplier.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    }

    private Supplier createSupplier() {
        Supplier supplier = new Supplier();
        supplier.setName("Elite K9 Supplies");
        supplier.setCode(SUPPLIER_CODE);
        return supplier;
    }

    private SupplierRequest createSupplierRequest(String code) {
        SupplierRequest request = new SupplierRequest();
        request.setName("Supplier Name");
        request.setCode(code);
        return request;
    }


}