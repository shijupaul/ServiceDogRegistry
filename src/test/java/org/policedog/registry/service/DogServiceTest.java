package org.policedog.registry.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.policedog.registry.dao.PoliceDogRepository;
import org.policedog.registry.domain.*;
import org.policedog.registry.dto.*;
import org.policedog.registry.exception.ResourceNotFoundException;
import org.policedog.registry.mapper.EntityDtoMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.policedog.registry.domain.Gender.MALE;
import static org.policedog.registry.domain.LeavingReason.TRANSFERRED;
import static org.policedog.registry.domain.Status.*;

@ExtendWith(MockitoExtension.class)
class DogServiceTest {

    private static final String SUPPLIER_CODE = "SUPPLIER_CODE";
    private static final String SUPPLIER_NOT_FOUND_MESSAGE = "Supplier with code %s not found";
    private static final String BADGE_NUMBER = "BADGE_NUMBER";
    private static final String BADGE_NUMBER_EXISTS_MESSAGE = "Dog with badge number %s already exists";
    private static final String DOG_NOT_FOUND_MESSAGE = "Dog with ID %d not found";
    private static final String CANNOT_UPDATE_DELETED_DOG_MESSAGE = "Cannot update deleted dog with ID %d";
    private static final String CANNOT_UPDATE_LEFT_DOG_MESSAGE = "Cannot update retired dog with ID %d";
    private static final String CANNOT_RETIRE_DELETED_DOG_MESSAGE = "Cannot retire deleted dog with ID %d";

    private static final Long DOG_ID = 1L;
    private static final String DOG_NAME = "Rex";
    private static final String DOG_BREED = "German Shepherd";

    @Mock
    private PoliceDogRepository dogRepositoryMock;
    @Mock
    private SupplierService supplierServiceMock;
    @Mock
    private EntityDtoMapper entityDtoMapperMock;
    @InjectMocks
    private DogService dogService;
    @Captor
    private ArgumentCaptor<PoliceDog> policeDogArgumentCaptor;

    @Test
    void shouldErrorWhenSupplierCodeNotFoundForDogCreate() {
        givenSupplierNotFound(SUPPLIER_CODE, SUPPLIER_NOT_FOUND_MESSAGE.formatted(SUPPLIER_CODE));

        CreateDogRequest createDogRequest = createDogRequest();

        var exception = assertThrows(RuntimeException.class, () -> {
            dogService.createDog(createDogRequest); // Replace null with an appropriate CreateDogRequest object
        });
        assertEquals(SUPPLIER_NOT_FOUND_MESSAGE.formatted(SUPPLIER_CODE), exception.getMessage());
    }

    @Test
    void shouldErrorWhenBadgeNumberIsNotUniqueForDogCreate() {
        CreateDogRequest createDogRequest = createDogRequest();
        Supplier supplier = createSupplier();

        givenSupplierFound(SUPPLIER_CODE, supplier);
        givenBadgeNumberExistsReturns(BADGE_NUMBER, true);

        var exception = assertThrows(IllegalArgumentException.class, () -> {
            dogService.createDog(createDogRequest);
        });
        assertEquals(BADGE_NUMBER_EXISTS_MESSAGE.formatted(BADGE_NUMBER), exception.getMessage());
    }

    @Test
    void shouldCreateDogWhenRequestIsValid() {
        CreateDogRequest createDogRequest = createDogRequest();
        Supplier supplier = createSupplier();
        PoliceDog policeDog = mapCreateDogRequestToEntity(createDogRequest);

        givenSupplierFound(SUPPLIER_CODE, supplier);
        givenBadgeNumberExistsReturns(BADGE_NUMBER, false);
        givenCreateDogRequestMappedToEntity(createDogRequest, policeDog);
        givenSaveCalledOnRepository();
        givenDogEntityMappedToDetailDto();

        dogService.createDog(createDogRequest);

        verify(dogRepositoryMock).save(policeDogArgumentCaptor.capture());

        PoliceDog savedDog = policeDogArgumentCaptor.getValue();

        assertAll(() -> {
            assertEquals(supplier, savedDog.getSupplier());
            assertTrue(supplier.getDogs().contains(savedDog));
            assertDogPropertiesMatches(policeDog, savedDog);
        });
    }

    @Test
    void shouldGetDogsWhenCalledWithPagingAndFilters() {
        int pageNo = 0;
        int pageSize = 10;

        SearchFilter searchFilter = new SearchFilter(DOG_NAME, DOG_BREED, SUPPLIER_CODE);

        Page<PoliceDog> policeDogPage = mock(Page.class);
        PageResponse<DogDetailDto> dogDetailPageResponse = mock(PageResponse.class);

        when(dogRepositoryMock.findDogs(DOG_NAME, DOG_BREED, SUPPLIER_CODE, PageRequest.of(pageNo, pageSize)))
                .thenReturn(policeDogPage);
        when(entityDtoMapperMock.toDogDetailPageResponse(policeDogPage))
                .thenReturn(dogDetailPageResponse);

        PageResponse<DogDetailDto> result = dogService.getDogs(searchFilter, pageNo, pageSize);
        assertEquals(dogDetailPageResponse, result);
    }

    @Test
    void shouldErrorWhenDogNotFoundForDogDelete() {

        givenWeExpectDogToBeRetrieved(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            dogService.deleteDogById(DOG_ID);
        });
        assertEquals(DOG_NOT_FOUND_MESSAGE.formatted(DOG_ID), exception.getMessage());
    }

    @Test
    void shouldReturnWithoutErrorWhenDogAlreadyDeletedForDogDelete() {
        PoliceDog policeDog = new PoliceDog();
        policeDog.setDeleted(true);

        givenWeExpectDogToBeRetrieved(Optional.of(policeDog));

        assertDoesNotThrow(() -> {
            dogService.deleteDogById(DOG_ID);
        });

        verify(dogRepositoryMock, never()).save(any(PoliceDog.class));
    }

    @Test
    void shouldSoftDeleteDogWhenValidIdProvidedForDogDelete() {
        PoliceDog policeDog = new PoliceDog();
        policeDog.setDeleted(false);

        givenWeExpectDogToBeRetrieved(Optional.of(policeDog));

        dogService.deleteDogById(DOG_ID);

        verify(dogRepositoryMock).save(policeDogArgumentCaptor.capture());

        PoliceDog deletedDog = policeDogArgumentCaptor.getValue();

        assertAll(() -> {
            assertTrue(deletedDog.getDeleted());
            assertNotNull(deletedDog.getDeletedAt());
        });
    }

    @Test
    void shouldErrorWhenDogNotFoundForDogUpdate() {

        givenWeExpectDogToBeRetrieved(Optional.empty());

        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            dogService.updateDog(DOG_ID, new UpdateDogRequest());
        });
        assertEquals(DOG_NOT_FOUND_MESSAGE.formatted(DOG_ID), exception.getMessage());
    }

    @Test
    void shouldErrorWhenRequestToUpdateADeletedDog() {
        PoliceDog policeDog = new PoliceDog();
        policeDog.setDeleted(true);

        givenWeExpectDogToBeRetrieved(Optional.of(policeDog));

        var exception = assertThrows(IllegalStateException.class, () -> {
            dogService.updateDog(DOG_ID, new UpdateDogRequest());
        });

        assertEquals(CANNOT_UPDATE_DELETED_DOG_MESSAGE.formatted(DOG_ID), exception.getMessage());
    }

    @Test
    void shouldErrorWhenRequestToUpdateDogIsInLeftState() {
        PoliceDog policeDog = new PoliceDog();
        policeDog.setStatus(LEFT);

        givenWeExpectDogToBeRetrieved(Optional.of(policeDog));

        var exception = assertThrows(IllegalStateException.class, () -> {
            dogService.updateDog(DOG_ID, new UpdateDogRequest());
        });

        assertEquals(CANNOT_UPDATE_LEFT_DOG_MESSAGE.formatted(DOG_ID), exception.getMessage());
    }

    @Test
    void shouldErrorWhenBadgeNumberIsNotUniqueForDogUpdate() {
        PoliceDog policeDog = new PoliceDog();
        policeDog.setStatus(TRAINING);
        policeDog.setBadgeNumber(BADGE_NUMBER);

        String newBadgeNumber = "NEW_BADGE_NUMBER";
        UpdateDogRequest updateDogRequest = new UpdateDogRequest();
        updateDogRequest.setBadgeNumber(newBadgeNumber);

        givenWeExpectDogToBeRetrieved(Optional.of(policeDog));
        givenBadgeNumberExistsReturns(newBadgeNumber, true);

        var exception = assertThrows(IllegalArgumentException.class, () -> {
            dogService.updateDog(DOG_ID, updateDogRequest);
        });
        assertEquals(BADGE_NUMBER_EXISTS_MESSAGE.formatted(newBadgeNumber), exception.getMessage());
    }

    @Test
    void shouldReturnErrorWhenSupplierCodeNotFoundForDogUpdate() {
        Supplier supplier = createSupplier();

        PoliceDog policeDog = new PoliceDog();
        policeDog.setStatus(TRAINING);
        policeDog.setSupplier(supplier);
        policeDog.setBadgeNumber(BADGE_NUMBER);

        String newSupplierCode = "NEW_SUPPLIER_CODE";
        UpdateDogRequest updateDogRequest = createUpdateDogRequest(newSupplierCode);
        givenWeExpectDogToBeRetrieved(Optional.of(policeDog));
        givenSupplierNotFound(newSupplierCode, SUPPLIER_NOT_FOUND_MESSAGE.formatted(newSupplierCode));

        var exception = assertThrows(RuntimeException.class, () -> {
            dogService.updateDog(DOG_ID, updateDogRequest);
        });
        assertEquals(SUPPLIER_NOT_FOUND_MESSAGE.formatted(newSupplierCode), exception.getMessage());
    }

    @Test
    void shouldUpdateDogWhenRequestIsValid() {
        Supplier currentSupplier = createSupplier();

        PoliceDog policeDog = new PoliceDog();
        policeDog.setStatus(TRAINING);
        policeDog.setSupplier(currentSupplier);
        policeDog.setBadgeNumber(BADGE_NUMBER);
        currentSupplier.getDogs().add(policeDog);

        String newSupplierCode = "NEW_SUPPLIER_CODE";
        Supplier newSupplier = createSupplierWithGivenCode(newSupplierCode);
        UpdateDogRequest updateDogRequest = createUpdateDogRequest(newSupplierCode);

        givenWeExpectDogToBeRetrieved(Optional.of(policeDog));
        givenSupplierFound(newSupplierCode, newSupplier);
        givenDogEntityUpdatedFromDto(updateDogRequest, policeDog);
        givenSaveCalledOnRepository();

        dogService.updateDog(DOG_ID, updateDogRequest);

        verify(dogRepositoryMock).save(policeDogArgumentCaptor.capture());

        PoliceDog updatedDog = policeDogArgumentCaptor.getValue();

        assertAll(() -> {
            assertEquals(newSupplier, updatedDog.getSupplier());
            assertFalse(currentSupplier.getDogs().contains(updatedDog));
        });
    }

    @Test
    void shouldErrorWhenRequestToRetireADeletedDog() {
        PoliceDog policeDog = new PoliceDog();
        policeDog.setDeleted(true);

        givenWeExpectDogToBeRetrieved(Optional.of(policeDog));

        var exception = assertThrows(IllegalStateException.class, () -> {
            dogService.retireDog(DOG_ID, new RetireDogRequest());
        });

        assertEquals(CANNOT_RETIRE_DELETED_DOG_MESSAGE.formatted(DOG_ID), exception.getMessage());
    }

    @Test
    void shouldReturnWithoutErrorWhenRequestToRetireAlreadyRetiredDog() {
        PoliceDog policeDog = new PoliceDog();
        policeDog.setStatus(RETIRED);

        givenWeExpectDogToBeRetrieved(Optional.of(policeDog));
        givenDogEntityMappedToDetailDto();

        assertDoesNotThrow(() -> {
            dogService.retireDog(DOG_ID, new RetireDogRequest());
        });

        verify(dogRepositoryMock, never()).save(any(PoliceDog.class));
    }

    @Test
    void shouldRetireDogWhenValidRequestProvided() {
        PoliceDog policeDog = new PoliceDog();
        policeDog.setStatus(IN_SERVICE);

        givenWeExpectDogToBeRetrieved(Optional.of(policeDog));
        givenSaveCalledOnRepository();
        givenDogEntityMappedToDetailDto();

        RetireDogRequest retireDogRequest = createRetireDogRequest();
        dogService.retireDog(DOG_ID, retireDogRequest);

        verify(dogRepositoryMock).save(policeDogArgumentCaptor.capture());

        PoliceDog retiredDog = policeDogArgumentCaptor.getValue();

        assertAll(
                () -> assertEquals(retireDogRequest.getLeavingDate(), retiredDog.getLeavingDate()),
                () -> assertEquals(retireDogRequest.getLeavingReason(), retiredDog.getLeavingReason()),
                () -> assertEquals(RETIRED, retiredDog.getStatus())
        );

    }

    @Test
    void shouldErrorWhenDogNotFoundForGetById() {
        givenWeExpectDogToBeRetrieved(Optional.empty());
        var exception = assertThrows(ResourceNotFoundException.class, () -> {
            dogService.getDogById(DOG_ID);
        });
        assertEquals(DOG_NOT_FOUND_MESSAGE.formatted(DOG_ID), exception.getMessage());
    }

    @Test
    void shouldGetDogByIdWhenFound() {
        PoliceDog policeDog = new PoliceDog();
        policeDog.setName(DOG_NAME);

        givenWeExpectDogToBeRetrieved(Optional.of(policeDog));
        givenDogEntityMappedToDetailDto();

        dogService.getDogById(DOG_ID);

        verify(entityDtoMapperMock).toDogDetailDto(policeDog);
    }

    @Test
    void shouldGetDogsByGender() {
        Gender genderToSearch = MALE;
        PoliceDog policeDog = new PoliceDog();
        DogDetailDto dogDetailDto = new DogDetailDto();

        when(dogRepositoryMock.findAllByGender(genderToSearch)).thenReturn(List.of(policeDog));
        when(entityDtoMapperMock.toDogDetailDto(policeDog)).thenReturn(dogDetailDto);

        List<DogDetailDto> response = dogService.getDogsByGender(genderToSearch);
        assertAll(() -> {
            assertEquals(1, response.size());
            assertEquals(dogDetailDto, response.get(0));
        });
    }

    @Test
    void shouldGetDogsByStatus() {
        Status statusToSearch = IN_SERVICE;
        PoliceDog policeDog = new PoliceDog();
        DogDetailDto dogDetailDto = new DogDetailDto();

        when(dogRepositoryMock.findAllByStatus(statusToSearch)).thenReturn(List.of(policeDog));
        when(entityDtoMapperMock.toDogDetailDto(policeDog)).thenReturn(dogDetailDto);

        List<DogDetailDto> response = dogService.getDogsByStatus(statusToSearch);
        assertAll(() -> {
            assertEquals(1, response.size());
            assertEquals(dogDetailDto, response.get(0));
        });
    }

    @Test
    void shouldGetDogsByLeavingReason() {
        LeavingReason leavingReasonToSearch = TRANSFERRED;
        PoliceDog policeDog = new PoliceDog();
        DogDetailDto dogDetailDto = new DogDetailDto();

        when(dogRepositoryMock.findAllByLeavingReason(leavingReasonToSearch)).thenReturn(List.of(policeDog));
        when(entityDtoMapperMock.toDogDetailDto(policeDog)).thenReturn(dogDetailDto);

        List<DogDetailDto> response = dogService.getDogsByLeavingReason(leavingReasonToSearch);
        assertAll(() -> {
            assertEquals(1, response.size());
            assertEquals(dogDetailDto, response.get(0));
        });
    }

    private void givenSupplierNotFound(String supplierCode, String exceptionMessage) {
        when(supplierServiceMock.getSupplierByCode(supplierCode)).thenThrow(new RuntimeException(exceptionMessage));
    }

    private void givenSupplierFound(String supplierCode, Supplier supplier) {
        when(supplierServiceMock.getSupplierByCode(supplierCode)).thenReturn(supplier);
    }

    private void givenBadgeNumberExistsReturns(String badgeNumber, boolean exists) {
        when(dogRepositoryMock.existsByBadgeNumber(badgeNumber)).thenReturn(exists);
    }

    private void givenCreateDogRequestMappedToEntity(CreateDogRequest createDogRequest, PoliceDog policeDog) {
        when(entityDtoMapperMock.toPoliceDog(createDogRequest)).thenReturn(policeDog);
    }

    private void givenSaveCalledOnRepository() {
        when(dogRepositoryMock.save(any(PoliceDog.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
    }

    private void givenDogEntityMappedToDetailDto() {
        when(entityDtoMapperMock.toDogDetailDto(any(PoliceDog.class))).thenReturn(any(DogDetailDto.class));
    }

    private void givenDogEntityUpdatedFromDto(UpdateDogRequest updateDogRequest, PoliceDog policeDog) {
        doNothing().when(entityDtoMapperMock).updatePoliceDogFromDto(updateDogRequest, policeDog);
    }

    private void givenWeExpectDogToBeRetrieved(Optional<PoliceDog> policeDog) {
        when(dogRepositoryMock.findById(DOG_ID)).thenReturn(policeDog);
    }


    private void assertDogPropertiesMatches(PoliceDog policeDog, PoliceDog savedDog) {
        assertEquals(policeDog.getName(), savedDog.getName());
        assertEquals(policeDog.getBreed(), savedDog.getBreed());
        assertEquals(policeDog.getBadgeNumber(), savedDog.getBadgeNumber());
        assertEquals(policeDog.getGender(), savedDog.getGender());
        assertEquals(policeDog.getBirthDate(), savedDog.getBirthDate());
        assertEquals(policeDog.getStatus(), savedDog.getStatus());
    }

    private CreateDogRequest createDogRequestWithGivenSupplierCode(String supplierCode) {
        CreateDogRequest request = createDogRequest();
        request.setSupplierCode(supplierCode);
        return request;
    }

    private CreateDogRequest createDogRequestWithGivenBadgeNumber(String badgeNumber) {
        CreateDogRequest request = createDogRequest();
        request.setBadgeNumber(badgeNumber);
        return request;
    }

    private CreateDogRequest createDogRequest() {
        CreateDogRequest request = new CreateDogRequest();
        request.setName("Rex");
        request.setBreed("German Shepherd");
        request.setSupplierCode(SUPPLIER_CODE);
        request.setBadgeNumber(BADGE_NUMBER);
        request.setGender(MALE);
        request.setBirthDate(LocalDate.now().minusDays(1));
        request.setStatus(TRAINING);
        // Set other required fields as necessary
        return request;
    }

    private UpdateDogRequest createUpdateDogRequest(String supplierCode) {
        UpdateDogRequest request = new UpdateDogRequest();
        request.setName("Rex Updated");
        request.setBreed("Belgian Malinois");
        request.setSupplierCode(supplierCode);
        request.setBadgeNumber(BADGE_NUMBER);
        request.setGender(MALE);
        request.setStatus(TRAINING);
        // Set other required fields as necessary
        return request;
    }

    private Supplier createSupplierWithGivenCode(String supplierCode) {
        Supplier supplier = createSupplier();
        supplier.setCode(supplierCode);
        return supplier;
    }

    private Supplier createSupplier() {
        Supplier supplier = new Supplier();
        supplier.setName("Elite K9 Supplies");
        supplier.setCode(SUPPLIER_CODE);
        return supplier;
    }

    private PoliceDog mapCreateDogRequestToEntity(CreateDogRequest createDogRequest) {
        PoliceDog dog = new PoliceDog();
        dog.setName(createDogRequest.getName());
        dog.setBreed(createDogRequest.getBreed());
        dog.setBadgeNumber(createDogRequest.getBadgeNumber());
        dog.setGender(createDogRequest.getGender());
        dog.setBirthDate(createDogRequest.getBirthDate());
        dog.setStatus(createDogRequest.getStatus());
        // Map other fields as necessary
        return dog;
    }

    private RetireDogRequest createRetireDogRequest() {
        return new RetireDogRequest(LocalDate.now(), LeavingReason.RETIRED_PUT_DOWN);
    }
}