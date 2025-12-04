package org.policedog.registry.integrationtest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.policedog.registry.domain.Gender;
import org.policedog.registry.domain.LeavingReason;
import org.policedog.registry.domain.Status;
import org.policedog.registry.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.policedog.registry.domain.Gender.MALE;
import static org.policedog.registry.domain.LeavingReason.RETIRED_PUT_DOWN;
import static org.policedog.registry.domain.LeavingReason.TRANSFERRED;
import static org.policedog.registry.domain.Status.RETIRED;
import static org.policedog.registry.domain.Status.TRAINING;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DogControllerIT {

    private static final String DOG_NOT_FOUND_MESSAGE = "Dog with ID %d not found";
    private static final String CANNOT_UPDATE_DELETED_DOG_MESSAGE = "Cannot update deleted dog with ID %d";
    private static final String CANNOT_UPDATE_RETIRED_DOG_MESSAGE = "Cannot update retired dog with ID %d";
    private static final String DOG_BADGE_NUMBER_EXISTS_MESSAGE = "Dog with badge number %s already exists";
    private static final String SUPPLIER_NOT_FOUND_MESSAGE = "Supplier with code %s not found";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnDogById() throws Exception {
        mockMvc.perform(get("/api/dogs/dogs/{id}", 1)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Bella"))
                .andExpect(jsonPath("$.breed").value("Dutch Shepherd"))
                .andExpect(jsonPath("$.deleted").value(true));
    }

    @Test
    void shouldReturnNotFoundForInvalidDogId() throws Exception {
        long invalidDogId = 9999;
        mockMvc.perform(get("/api/dogs/dogs/{id}", 9999)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(DOG_NOT_FOUND_MESSAGE.formatted(invalidDogId)));
    }

    @Test
    void shouldFailToCreateADogWhenBadgeNumberIsNotUnique() throws Exception {
        String existingBadgeNumber = "K9-005"; // Charlie's badge number is K9-005 -- Dog ID 2 -- see data.sql
        CreateDogRequest createDogRequest = buildValidCreateDogRequest();
        createDogRequest.setBadgeNumber(existingBadgeNumber);

        mockMvc.perform(post("/api/dogs/dogs")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDogRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(DOG_BADGE_NUMBER_EXISTS_MESSAGE.formatted(existingBadgeNumber)));
    }

    @Test
    void shouldFailToCreateADogWhenMatchingSupplierNotFound() throws Exception {
        String nonExistentSupplierCode = "NON_EXISTENT_SUPPLIER";
        CreateDogRequest createDogRequest = buildValidCreateDogRequest();
        createDogRequest.setSupplierCode(nonExistentSupplierCode);

        mockMvc.perform(post("/api/dogs/dogs")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDogRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(SUPPLIER_NOT_FOUND_MESSAGE.formatted(nonExistentSupplierCode)));
    }

    @Test
    void shouldCreateADogSuccessfully() throws Exception {
        CreateDogRequest createDogRequest = buildValidCreateDogRequest();
        String dogCreateRespJson = mockMvc.perform(post("/api/dogs/dogs")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDogRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        DogDetailDto createdDog = objectMapper.readValue(dogCreateRespJson, DogDetailDto.class);
        assertDogDetails(createdDog, createDogRequest);
    }

    @Test
    void shouldGetDogsReturnAllActiveDogs() throws Exception {
        String responseJson = mockMvc.perform(get("/api/dogs/dogs")
                        .queryParam("pageNo", "0")
                        .queryParam("pageSize", "15")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();


        PageResponse<DogDetailDto> pageResponse =
                objectMapper.readValue(responseJson, new TypeReference<PageResponse<DogDetailDto>>() {
                });

        assertAll(() -> {
            assertEquals(10, pageResponse.getContent().size());
            assertEquals(0, pageResponse.getMetadata().getPage());
            assertEquals(15, pageResponse.getMetadata().getSize());
            assertEquals(10, pageResponse.getMetadata().getTotalElements());
            assertEquals(1, pageResponse.getMetadata().getTotalPages());
            assertTrue(pageResponse.getMetadata().isFirst());
            assertTrue(pageResponse.getMetadata().isLast());
        });
        assertDeletedDogIsNotInTheList(pageResponse.getContent());
    }

    @Test
    void shouldGetDogsReturnAllActiveDogsMatchedBySupplierCodeFilter() throws Exception {
        String supplierCodeFilter = "ELITE_K9";
        String filterJson = "{\"supplierCode\":\"%s\"}".formatted(supplierCodeFilter);

        String responseJson = mockMvc.perform(get("/api/dogs/dogs")
                        .queryParam("filter", filterJson)
                        .queryParam("pageNo", "0")
                        .queryParam("pageSize", "10")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResponse<DogDetailDto> pageResponse =
                objectMapper.readValue(responseJson, new TypeReference<PageResponse<DogDetailDto>>() {
                });

        assertAll(() -> {
            assertEquals(4, pageResponse.getContent().size());
            assertEquals(0, pageResponse.getMetadata().getPage());
            assertEquals(10, pageResponse.getMetadata().getSize());
            assertEquals(4, pageResponse.getMetadata().getTotalElements());
            assertEquals(1, pageResponse.getMetadata().getTotalPages());
            assertTrue(pageResponse.getMetadata().isFirst());
            assertTrue(pageResponse.getMetadata().isLast());
        });
        assertDeletedDogIsNotInTheList(pageResponse.getContent());
        for (DogDetailDto dog : pageResponse.getContent()) {
            assertTrue(dog.getSupplier().getCode().contains(supplierCodeFilter),
                    "Dog supplier code does not match filter: " + dog.getSupplier().getCode());
        }
    }

    @Test
    void shouldGetDogsReturnAllActiveDogsMatchedByNameFilter() throws Exception {
        String nameFilter = "Max";
        String filterJson = "{\"name\":\"%s\"}".formatted(nameFilter);

        String responseJson = mockMvc.perform(get("/api/dogs/dogs")
                        .queryParam("filter", filterJson)
                        .queryParam("pageNo", "0")
                        .queryParam("pageSize", "10")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResponse<DogDetailDto> pageResponse =
                objectMapper.readValue(responseJson, new TypeReference<PageResponse<DogDetailDto>>() {
                });

        assertAll(() -> {
            assertEquals(1, pageResponse.getContent().size());
            assertEquals(0, pageResponse.getMetadata().getPage());
            assertEquals(10, pageResponse.getMetadata().getSize());
            assertEquals(1, pageResponse.getMetadata().getTotalElements());
            assertEquals(1, pageResponse.getMetadata().getTotalPages());
            assertTrue(pageResponse.getMetadata().isFirst());
            assertTrue(pageResponse.getMetadata().isLast());
        });
        assertDeletedDogIsNotInTheList(pageResponse.getContent());
        for (DogDetailDto dog : pageResponse.getContent()) {
            assertTrue(dog.getName().contains(nameFilter),
                    "Dog name does not match filter: " + dog.getName());
        }
    }

    @Test
    void shouldGetDogsReturnAllActiveDogsMatchedByBreedFilter() throws Exception {
        String breedFilter = "Belgian Malinois";
        String filterJson = "{\"breed\":\"%s\"}".formatted(breedFilter);

        String responseJson = mockMvc.perform(get("/api/dogs/dogs")
                        .queryParam("filter", filterJson)
                        .queryParam("pageNo", "0")
                        .queryParam("pageSize", "10")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResponse<DogDetailDto> pageResponse =
                objectMapper.readValue(responseJson, new TypeReference<PageResponse<DogDetailDto>>() {
                });

        assertAll(() -> {
            assertEquals(4, pageResponse.getContent().size());
            assertEquals(0, pageResponse.getMetadata().getPage());
            assertEquals(10, pageResponse.getMetadata().getSize());
            assertEquals(4, pageResponse.getMetadata().getTotalElements());
            assertEquals(1, pageResponse.getMetadata().getTotalPages());
            assertTrue(pageResponse.getMetadata().isFirst());
            assertTrue(pageResponse.getMetadata().isLast());
        });
        assertDeletedDogIsNotInTheList(pageResponse.getContent());
        for (DogDetailDto dog : pageResponse.getContent()) {
            assertTrue(dog.getBreed().contains(breedFilter),
                    "Dog breed does not match filter: " + dog.getBreed());
        }
    }

    @Test
    void shouldGetDogsReturnAllActiveDogsMatchedByCombinedFilters() throws Exception {
        String nameFilter = "Max";
        String breedFilter = "Belgian Malinois";
        String supplierCodeFilter = "ELITE_K9";
        String filterJson = """
                {
                    "name":"%s",
                    "breed":"%s",
                    "supplierCode":"%s"
                }
                """.formatted(nameFilter, breedFilter, supplierCodeFilter);

        String responseJson = mockMvc.perform(get("/api/dogs/dogs")
                        .queryParam("filter", filterJson)
                        .queryParam("pageNo", "0")
                        .queryParam("pageSize", "10")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResponse<DogDetailDto> pageResponse =
                objectMapper.readValue(responseJson, new TypeReference<PageResponse<DogDetailDto>>() {
                });

        assertAll(() -> {
            assertEquals(1, pageResponse.getContent().size());
            assertEquals(0, pageResponse.getMetadata().getPage());
            assertEquals(10, pageResponse.getMetadata().getSize());
            assertEquals(1, pageResponse.getMetadata().getTotalElements());
            assertEquals(1, pageResponse.getMetadata().getTotalPages());
            assertTrue(pageResponse.getMetadata().isFirst());
            assertTrue(pageResponse.getMetadata().isLast());
        });
        assertDeletedDogIsNotInTheList(pageResponse.getContent());
        for (DogDetailDto dog : pageResponse.getContent()) {
            assertTrue(dog.getName().contains(nameFilter),
                    "Dog name does not match filter: " + dog.getName());
            assertTrue(dog.getBreed().contains(breedFilter),
                    "Dog breed does not match filter: " + dog.getBreed());
            assertTrue(dog.getSupplier().getCode().contains(supplierCodeFilter),
                    "Dog supplier code does not match filter: " + dog.getSupplier().getCode());
        }
    }

    @Test
    void shouldGetDogsReturnEmptyWhenNoActiveDogsMatchFilters() throws Exception {
        String nameFilter = "NonExistentName";
        String breedFilter = "NonExistentBreed";
        String supplierCodeFilter = "NonExistentSupplier";
        String filterJson = """
                {
                    "name":"%s",
                    "breed":"%s",
                    "supplierCode":"%s"
                }
                """.formatted(nameFilter, breedFilter, supplierCodeFilter);

        String responseJson = mockMvc.perform(get("/api/dogs/dogs")
                        .queryParam("filter", filterJson)
                        .queryParam("pageNo", "0")
                        .queryParam("pageSize", "10")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResponse<DogDetailDto> pageResponse =
                objectMapper.readValue(responseJson, new TypeReference<PageResponse<DogDetailDto>>() {
                });

        assertAll(() -> {
            assertEquals(0, pageResponse.getContent().size());
            assertEquals(0, pageResponse.getMetadata().getPage());
            assertEquals(10, pageResponse.getMetadata().getSize());
            assertEquals(0, pageResponse.getMetadata().getTotalElements());
            assertEquals(0, pageResponse.getMetadata().getTotalPages());
            assertTrue(pageResponse.getMetadata().isFirst());
            assertTrue(pageResponse.getMetadata().isLast());
        });
    }

    @Test
    void shouldDeleteAnActiveDogById() throws Exception {
        long dogIdToDelete = 4; // Luna -- Dog with status TRAINING

        mockMvc.perform(get("/api/dogs/dogs/{id}", dogIdToDelete)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dogIdToDelete))
                .andExpect(jsonPath("$.deleted").value(false));

        mockMvc.perform(delete("/api/dogs/dogs/{id}", dogIdToDelete))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/dogs/dogs/{id}", dogIdToDelete)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dogIdToDelete))
                .andExpect(jsonPath("$.deleted").value(true))
                .andExpect(jsonPath("$.deletedAt").isNotEmpty());
    }

    @Test
    void deletingAnAlreadyDeletedDogShouldBeIdempotent() throws Exception {
        long deletedDogId = 1; // Bella -- Already deleted dog
        String deletedAtFixedValue = "2023-01-01T10:15:30";

        mockMvc.perform(get("/api/dogs/dogs/{id}", deletedDogId)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(deletedDogId))
                .andExpect(jsonPath("$.deleted").value(true))
                .andExpect(jsonPath("$.deletedAt").value(deletedAtFixedValue));

        mockMvc.perform(delete("/api/dogs/dogs/{id}", deletedDogId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/dogs/dogs/{id}", deletedDogId)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(deletedDogId))
                .andExpect(jsonPath("$.deleted").value(true))
                .andExpect(jsonPath("$.deletedAt").value(deletedAtFixedValue));
    }

    @Test
    void shouldThrowErrorWhenDeletingNonExistentDog() throws Exception {
        long nonExistentDogId = 9999;

        mockMvc.perform(delete("/api/dogs/dogs/{id}", nonExistentDogId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(DOG_NOT_FOUND_MESSAGE.formatted(nonExistentDogId)));
    }

    @Test
    void shouldThrowErrorWhenUpdatingNonExistentDog() throws Exception {
        long nonExistentDogId = 9999;
        UpdateDogRequest updateDogRequest = buildValidUpdateDogRequest();
        mockMvc.perform(put("/api/dogs/dogs/{id}", nonExistentDogId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDogRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(DOG_NOT_FOUND_MESSAGE.formatted(nonExistentDogId)));
    }

    @Test
    void shouldThrowErrorWhenUpdatingDeletedDog() throws Exception {
        long deletedDogId = 1; // Bella -- Already deleted dog
        UpdateDogRequest updateDogRequest = buildValidUpdateDogRequest();

        mockMvc.perform(put("/api/dogs/dogs/{id}", deletedDogId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDogRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(CANNOT_UPDATE_DELETED_DOG_MESSAGE.formatted(deletedDogId)));
    }

    @Test
    void shouldThrowErrorWhenUpdatingRetiredDog() throws Exception {
        long retiredDogId = 2; // Charlie -- Already retired dog
        UpdateDogRequest updateDogRequest = buildValidUpdateDogRequest();

        mockMvc.perform(put("/api/dogs/dogs/{id}", retiredDogId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDogRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(CANNOT_UPDATE_RETIRED_DOG_MESSAGE.formatted(retiredDogId)));
    }

    @Test
    void shouldThrowErrorWhenUpdatingDogWithNonExistentSupplier() throws Exception {
        long dogIdToUpdate = 4; // Luna -- Active dog
        String nonExistentSupplierCode = "NON_EXISTENT_SUPPLIER";
        UpdateDogRequest updateDogRequest = buildValidUpdateDogRequest();
        updateDogRequest.setSupplierCode(nonExistentSupplierCode);

        mockMvc.perform(put("/api/dogs/dogs/{id}", dogIdToUpdate)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDogRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(SUPPLIER_NOT_FOUND_MESSAGE.formatted(nonExistentSupplierCode)));
    }

    @Test
    void shouldThrowErrorWhenUpdatingDogWithNonUniqueBadgeNumber() throws Exception {
        long dogIdToUpdate = 4; // Luna -- Active dog with badge number K9-003
        String existingBadgeNumber = "K9-007"; // Rocky's badge number is K9-007 -- Dog ID 5 -- see data.sql
        UpdateDogRequest updateDogRequest = buildValidUpdateDogRequest();
        updateDogRequest.setBadgeNumber(existingBadgeNumber);

        mockMvc.perform(put("/api/dogs/dogs/{id}", dogIdToUpdate)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDogRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(DOG_BADGE_NUMBER_EXISTS_MESSAGE.formatted(existingBadgeNumber)));
    }

    @Test
    void shouldThrowErrorWhenUpdatingDogWithNonExistentSupplierCode() throws Exception {
        long dogIdToUpdate = 4; // Luna -- Active dog with badge number K9-003, supplier ALPHA_DOG
        String nonExistentSupplierCode = "NON_EXISTENT_SUPPLIER";
        UpdateDogRequest updateDogRequest = buildValidUpdateDogRequest();
        updateDogRequest.setSupplierCode(nonExistentSupplierCode);

        mockMvc.perform(put("/api/dogs/dogs/{id}", dogIdToUpdate)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDogRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(SUPPLIER_NOT_FOUND_MESSAGE.formatted(nonExistentSupplierCode)));
    }

    @Test
    void shouldUpdateDogSuccessfully() throws Exception {
        long dogIdToUpdate = 4; // Luna -- Active dog
        UpdateDogRequest updateDogRequest = buildValidUpdateDogRequest();

        String dogUpdateRespJson = mockMvc.perform(put("/api/dogs/dogs/{id}", dogIdToUpdate)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDogRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        DogDetailDto updatedDog = objectMapper.readValue(dogUpdateRespJson, DogDetailDto.class);
        assertAll(() -> {
            assertEquals(dogIdToUpdate, updatedDog.getId());
            assertEquals(updateDogRequest.getName(), updatedDog.getName());
            assertEquals(updateDogRequest.getBreed(), updatedDog.getBreed());
            assertEquals(updateDogRequest.getSupplierCode(), updatedDog.getSupplier().getCode());
            assertEquals(updateDogRequest.getBadgeNumber(), updatedDog.getBadgeNumber());
            assertEquals(updateDogRequest.getGender(), updatedDog.getGender());
            assertEquals(updateDogRequest.getStatus(), updatedDog.getStatus());
            assertFalse(updatedDog.getDeleted());
        });
    }

    @Test
    void shouldThrowErrorWhenRetiringNonExistentDog() throws Exception {
        long nonExistentDogId = 9999;
        RetireDogRequest retireDogRequest = new RetireDogRequest();
        retireDogRequest.setLeavingReason(TRANSFERRED);
        retireDogRequest.setLeavingDate(LocalDate.now());

        mockMvc.perform(post("/api/dogs/dogs/{id}/retire", nonExistentDogId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retireDogRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(DOG_NOT_FOUND_MESSAGE.formatted(nonExistentDogId)));
    }

    @Test
    void shouldThrowErrorWhenRetiringDeletedDog() throws Exception {
        long deletedDogId = 1; // Bella -- Already deleted dog
        RetireDogRequest retireDogRequest = new RetireDogRequest();
        retireDogRequest.setLeavingReason(TRANSFERRED);
        retireDogRequest.setLeavingDate(LocalDate.now());

        mockMvc.perform(post("/api/dogs/dogs/{id}/retire", deletedDogId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retireDogRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Cannot retire deleted dog with ID %d".formatted(deletedDogId)));
    }

    @Test
    void shouldRetireActiveDogSuccessfully() throws Exception {
        long dogIdToRetire = 4; // Luna -- Active dog
        RetireDogRequest retireDogRequest = new RetireDogRequest();
        retireDogRequest.setLeavingReason(RETIRED_PUT_DOWN);
        retireDogRequest.setLeavingDate(LocalDate.now());

        String dogRetireRespJson = mockMvc.perform(post("/api/dogs/dogs/{id}/retire", dogIdToRetire)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retireDogRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        DogDetailDto retiredDog = objectMapper.readValue(dogRetireRespJson, DogDetailDto.class);
        assertAll(() -> {
            assertEquals(dogIdToRetire, retiredDog.getId());
            assertEquals(RETIRED_PUT_DOWN, retiredDog.getLeavingReason());
            assertEquals(retireDogRequest.getLeavingDate(), retiredDog.getLeavingDate());
            assertEquals(RETIRED, retiredDog.getStatus());
            assertFalse(retiredDog.getDeleted());
        });
    }

    @ParameterizedTest(name = "Gender {0} should have {1} dogs")
    @CsvSource({
            "MALE, 5",
            "FEMALE, 6"
    })
    void shouldGetDogsByGender(Gender gender, int expectedCount) throws Exception {
        mockMvc.perform(get("/api/dogs/dogs/search/by-gender")
                        .queryParam("gender", gender.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(expectedCount));
    }

    @ParameterizedTest(name = "Status {0} should have {1} dogs")
    @CsvSource({
            "TRAINING, 3",
            "IN_SERVICE, 6",
            "RETIRED, 1",
            "LEFT, 1"
    })
    void shouldGetDogsByStatus(Status status, int expectedCount) throws Exception {
        mockMvc.perform(get("/api/dogs/dogs/search/by-status")
                        .queryParam("status", status.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(expectedCount));
    }

    @ParameterizedTest(name = "LeavingReason {0} should have {1} dogs")
    @CsvSource({
            "TRANSFERRED, 0",
            "RETIRED_PUT_DOWN, 1",
            "KIA, 0",
            "REJECTED, 0",
            "RETIRED_RE_HOUSED, 0",
            "DIED, 0"
    })
    void shouldGetDogsByLeavingReason(LeavingReason leavingReason, int expectedCount) throws Exception {
        mockMvc.perform(get("/api/dogs/dogs/search/by-leaving-reason")
                        .queryParam("leavingReason", leavingReason.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(expectedCount));
    }

    private void assertDeletedDogIsNotInTheList(List<DogDetailDto> content) {
        for (DogDetailDto dog : content) {
            Assertions.assertFalse(dog.getDeleted(), "Deleted dog found in the active dogs list: ID " + dog.getId());
        }
    }

    private void assertDogDetails(DogDetailDto createdDog, CreateDogRequest createDogRequest) {
        assertAll(() -> {
            assertNotNull(createdDog.getId());
            assertEquals(createDogRequest.getName(), createdDog.getName());
            assertEquals(createDogRequest.getBreed(), createdDog.getBreed());
            assertNotNull(createdDog.getSupplier());
            assertEquals(createDogRequest.getSupplierCode(), createdDog.getSupplier().getCode());
            assertEquals(createDogRequest.getBadgeNumber(), createdDog.getBadgeNumber());
            assertEquals(createDogRequest.getGender(), createdDog.getGender());
            assertEquals(createDogRequest.getBirthDate(), createdDog.getBirthDate());
            assertNotNull(createdDog.getDateAcquired());
            assertEquals(createDogRequest.getStatus(), createdDog.getStatus());
            assertEquals(createDogRequest.getCharacteristics(), createdDog.getCharacteristics());
            assertFalse(createdDog.getDeleted());
            assertNull(createdDog.getDeletedAt());
        });
    }

    private CreateDogRequest buildValidCreateDogRequest() {
        CreateDogRequest createDogRequest = new CreateDogRequest();
        createDogRequest.setName("Rex");
        createDogRequest.setBreed("German Shepherd");
        createDogRequest.setSupplierCode("ELITE_K9");
        createDogRequest.setBadgeNumber("BDG456");
        createDogRequest.setGender(MALE);
        createDogRequest.setBirthDate(LocalDate.now().minusDays(1));
        createDogRequest.setStatus(TRAINING);
        createDogRequest.setCharacteristics(buildCharacteristicsDto());
        return createDogRequest;
    }

    private UpdateDogRequest buildValidUpdateDogRequest() {
        UpdateDogRequest updateDogRequest = new UpdateDogRequest();
        updateDogRequest.setName("Rex Updated");
        updateDogRequest.setBreed("German Shepherd Updated");
        updateDogRequest.setSupplierCode("ELITE_K9");
        updateDogRequest.setBadgeNumber("BDG456U");
        updateDogRequest.setGender(MALE);
        updateDogRequest.setStatus(TRAINING);
        return updateDogRequest;
    }

    private CharacteristicsDto buildCharacteristicsDto() {
        CharacteristicsDto characteristicsDto = new CharacteristicsDto();
        characteristicsDto.setIsAggressive(true);
        characteristicsDto.setRequiresSeparateKennel(false);
        characteristicsDto.setIsNoiceTolerant(true);
        characteristicsDto.setHasSpecialDiet(true);
        characteristicsDto.setDietaryRequirements("High-protein diet");
        characteristicsDto.setRequiresExercise(true);
        characteristicsDto.setExerciseNotes("Needs at least 2 hours of exercise daily");
        characteristicsDto.setHasMedicalConditions(true);
        characteristicsDto.setMedicalNotes("Diabetic, requires insulin shots");
        characteristicsDto.setTemperament("Energetic and alert");

        return characteristicsDto;
    }
}