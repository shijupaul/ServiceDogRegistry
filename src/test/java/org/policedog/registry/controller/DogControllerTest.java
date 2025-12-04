package org.policedog.registry.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.policedog.registry.domain.Status;
import org.policedog.registry.dto.*;
import org.policedog.registry.service.DogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.policedog.registry.domain.Gender.MALE;
import static org.policedog.registry.domain.Status.TRAINING;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DogController.class)
@TestPropertySource(properties = {"spring.jpa.auditing.enabled=false"})
class DogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DogService dogService;

    @Test
    void shouldReturnBadRequestForCreateDogWhenRequiredFieldsAreMissing() throws Exception {
        CreateDogRequest createDogRequest = new CreateDogRequest();
        mockMvc.perform(post("/api/dogs/dogs")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDogRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.name").value("Name is required"))
                .andExpect(jsonPath("$.errors.breed").value("Breed is required"))
                .andExpect(jsonPath("$.errors.supplierCode").value("Supplier code is required"))
                .andExpect(jsonPath("$.errors.badgeNumber").value("Badge number is required"))
                .andExpect(jsonPath("$.errors.gender").value("Gender is required"))
                .andExpect(jsonPath("$.errors.birthDate").value("Birth date is required"))
                .andExpect(jsonPath("$.errors.status").value("Status is required"));
    }

    @Test
    void shouldReturnBadRequestForCreateDogWhenBirthDateIsNotInPast() throws Exception {
        CreateDogRequest createDogRequest = buildValidCreateDogRequest();
        createDogRequest.setBirthDate(LocalDate.now());

        mockMvc.perform(post("/api/dogs/dogs")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDogRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.birthDate").value("Birth date must be in the past"));
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, mode = EnumSource.Mode.EXCLUDE, names = {"TRAINING", "IN_SERVICE"})
    void shouldReturnBadRequestForCreateDogWhenStatusIsInvalid(Status status) throws Exception {
        CreateDogRequest createDogRequest = buildValidCreateDogRequest();
        createDogRequest.setStatus(status);

        mockMvc.perform(post("/api/dogs/dogs")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDogRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.status").value("Invalid status '%s'. Allowed values are: TRAINING, IN_SERVICE.".formatted(status.name())));
    }

    @Test
    void shouldCreateDogWhenRequestIsValid() throws Exception {
        CreateDogRequest createDogRequest = buildValidCreateDogRequest();
        DogDetailDto expectedDogDetailDto = buildValidDogDetailDto(createDogRequest);

        when(dogService.createDog(createDogRequest)).thenReturn(expectedDogDetailDto);

        String responseJson = mockMvc.perform(post("/api/dogs/dogs")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDogRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        DogDetailDto actualDogDetailDto = objectMapper.readValue(responseJson, DogDetailDto.class);
        assertThat(actualDogDetailDto).isEqualTo(expectedDogDetailDto);
    }

    @Test
    void shouldReturnBadRequestForGetDogsWhenFilterIsInvalidJson() throws Exception {
        String invalidFilter = "{name: 'Rex', breed: 'German Shepherd'";

        mockMvc.perform(get("/api/dogs/dogs")
                        .queryParam("filter", invalidFilter))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invalid filter parameter"));

    }

    @Test
    void shouldGetDogsWhenFilterIsValidJson() throws Exception {
        String validFilter = "{\"name\":\"Rex\",\"breed\":\"German Shepherd\",\"supplierCode\":\"ELITE_K9\"}";
        PageResponse<DogDetailDto> expectedPageResponse = buildValidPageResponse();

        when(dogService.getDogs(objectMapper.readValue(validFilter, SearchFilter.class), 0, 10))
                .thenReturn(expectedPageResponse);

        String actualPageResponseJson = mockMvc.perform(get("/api/dogs/dogs")
                        .queryParam("filter", validFilter))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResponse<DogDetailDto> actualPageResponse = objectMapper.readValue(actualPageResponseJson, new TypeReference<PageResponse<DogDetailDto>>() {
        });
        assertThat(actualPageResponse).isEqualTo(expectedPageResponse);
    }

    @Test
    void shouldGetDogsWhenNoFilterIsProvided() throws Exception {
        PageResponse<DogDetailDto> expectedPageResponse = buildValidPageResponse();

        when(dogService.getDogs(new SearchFilter(), 0, 10))
                .thenReturn(expectedPageResponse);

        String actualPageResponseJson = mockMvc.perform(get("/api/dogs/dogs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResponse<DogDetailDto> actualPageResponse = objectMapper.readValue(actualPageResponseJson, new TypeReference<PageResponse<DogDetailDto>>() {
        });
        assertThat(actualPageResponse).isEqualTo(expectedPageResponse);
    }

    @Test
    void shouldGetDogsWhenPaginationParametersAreProvidedWithoutFilter() throws Exception {
        int pageNo = 1;
        int pageSize = 5;
        PageResponse<DogDetailDto> expectedPageResponse = buildValidPageResponse();

        when(dogService.getDogs(new SearchFilter(), pageNo, pageSize))
                .thenReturn(expectedPageResponse);

        String actualPageResponseJson = mockMvc.perform(get("/api/dogs/dogs")
                        .queryParam("pageNo", String.valueOf(pageNo))
                        .queryParam("pageSize", String.valueOf(pageSize)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResponse<DogDetailDto> actualPageResponse = objectMapper.readValue(actualPageResponseJson, new TypeReference<PageResponse<DogDetailDto>>() {
        });
        assertThat(actualPageResponse).isEqualTo(expectedPageResponse);
    }

    @Test
    void shouldGetDogsWhenPaginationParametersAreProvidedAlongWithFilter() throws Exception {
        int pageNo = 1;
        int pageSize = 5;
        String validFilter = "{\"name\":\"Rex\",\"breed\":\"German Shepherd\",\"supplierCode\":\"ELITE_K9\"}";

        PageResponse<DogDetailDto> expectedPageResponse = buildValidPageResponse();

        when(dogService.getDogs(objectMapper.readValue(validFilter, SearchFilter.class), pageNo, pageSize))
                .thenReturn(expectedPageResponse);

        String actualPageResponseJson = mockMvc.perform(get("/api/dogs/dogs")
                        .queryParam("filter", validFilter)
                        .queryParam("pageNo", String.valueOf(pageNo))
                        .queryParam("pageSize", String.valueOf(pageSize)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResponse<DogDetailDto> actualPageResponse = objectMapper.readValue(actualPageResponseJson, new TypeReference<PageResponse<DogDetailDto>>() {
        });
        assertThat(actualPageResponse).isEqualTo(expectedPageResponse);
    }

    @Test
    void shouldGetDogById() throws Exception {
        Long dogId = 1L;
        DogDetailDto expectedDogDetailDto = buildValidDogDetailDto();

        when(dogService.getDogById(dogId)).thenReturn(expectedDogDetailDto);

        String actualDogDetailJson = mockMvc.perform(get("/api/dogs/dogs/{id}", dogId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        DogDetailDto actualDogDetailDto = objectMapper.readValue(actualDogDetailJson, DogDetailDto.class);
        assertThat(actualDogDetailDto).isEqualTo(expectedDogDetailDto);
    }

    @Test
    void shouldDeleteDogById() throws Exception {
        Long dogId = 1L;

        doNothing().when(dogService).deleteDogById(dogId);

        mockMvc.perform(delete("/api/dogs/dogs/{id}", dogId))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnBadRequestForUpdateDogWhenRequiredFieldsAreMissing() throws Exception {
        Long dogId = 1L;
        UpdateDogRequest updateDogRequest = new UpdateDogRequest();

        mockMvc.perform(put("/api/dogs/dogs/{id}", dogId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDogRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.name").value("Name is required"))
                .andExpect(jsonPath("$.errors.breed").value("Breed is required"))
                .andExpect(jsonPath("$.errors.supplierCode").value("Supplier code is required"))
                .andExpect(jsonPath("$.errors.badgeNumber").value("Badge number is required"))
                .andExpect(jsonPath("$.errors.gender").value("Gender is required"))
                .andExpect(jsonPath("$.errors.status").value("Status is required"));
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, mode = EnumSource.Mode.EXCLUDE, names = {"TRAINING", "IN_SERVICE", "RETIRED"})
    void shouldReturnBadRequestForUpdateDogWhenStatusIsInvalid(Status status) throws Exception {
        Long dogId = 1L;
        UpdateDogRequest updateDogRequest = buildValidUpdateDogRequest();
        updateDogRequest.setStatus(status);

        mockMvc.perform(put("/api/dogs/dogs/{id}", dogId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDogRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.status").value("Invalid status '%s'. Allowed values are: TRAINING, IN_SERVICE, RETIRED.".formatted(status.name())));
    }


    @Test
    void shouldUpdateDogWhenRequestIsValid() throws Exception {
        Long dogId = 1L;
        UpdateDogRequest updateDogRequest = buildValidUpdateDogRequest();
        DogDetailDto expectedDogDetailDto = buildValidDogDetailDto(updateDogRequest);

        when(dogService.updateDog(dogId, updateDogRequest)).thenReturn(expectedDogDetailDto);
        String responseJson = mockMvc.perform(put("/api/dogs/dogs/{id}", dogId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDogRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        DogDetailDto actualDogDetailDto = objectMapper.readValue(responseJson, DogDetailDto.class);
        assertThat(actualDogDetailDto).isEqualTo(expectedDogDetailDto);
    }

    @Test
    void shouldReturnBadRequestForRetireDogWhenRequiredFieldsAreMissing() throws Exception {
        Long dogId = 1L;
        RetireDogRequest retireDogRequest = new RetireDogRequest();

        mockMvc.perform(post("/api/dogs/dogs/{id}/retire", dogId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retireDogRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.leavingDate").value("Leaving date is required"))
                .andExpect(jsonPath("$.errors.leavingReason").value("Leaving reason is required."));
    }

    @Test
    void shouldReturnBadRequestForRetireDogWhenLeavingDateIsInFuture() throws Exception {
        Long dogId = 1L;
        RetireDogRequest retireDogRequest = buildValidRetireDogRequest();
        retireDogRequest.setLeavingDate(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/dogs/dogs/{id}/retire", dogId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retireDogRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.leavingDate").value("Leaving date cannot be in the future"));
    }

    @Test
    void shouldRetireDogWhenRequestIsValid() throws Exception {
        Long dogId = 1L;
        RetireDogRequest retireDogRequest = buildValidRetireDogRequest();
        DogDetailDto expectedDogDetailDto = buildValidDogDetailDto();

        when(dogService.retireDog(dogId, retireDogRequest)).thenReturn(expectedDogDetailDto);

        String responseJson = mockMvc.perform(post("/api/dogs/dogs/{id}/retire", dogId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(retireDogRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        DogDetailDto actualDogDetailDto = objectMapper.readValue(responseJson, DogDetailDto.class);
        assertThat(actualDogDetailDto).isEqualTo(expectedDogDetailDto);
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
        return createDogRequest;
    }

    private DogDetailDto buildValidDogDetailDto(CreateDogRequest createDogRequest) {
        DogDetailDto dogDetailDto = new DogDetailDto();
        dogDetailDto.setId(1L);
        dogDetailDto.setName(createDogRequest.getName());
        dogDetailDto.setBreed(createDogRequest.getBreed());
        dogDetailDto.setBadgeNumber(createDogRequest.getBadgeNumber());
        dogDetailDto.setGender(createDogRequest.getGender());
        dogDetailDto.setBirthDate(createDogRequest.getBirthDate());
        dogDetailDto.setStatus(createDogRequest.getStatus());
        dogDetailDto.setSupplier(buildValidSupplierSummaryDto(createDogRequest.getSupplierCode()));
        return dogDetailDto;
    }

    private DogDetailDto buildValidDogDetailDto() {
        return buildValidDogDetailDto(buildValidCreateDogRequest());
    }

    private SupplierSummaryDto buildValidSupplierSummaryDto(String supplierCode) {
        SupplierSummaryDto supplierSummaryDto = new SupplierSummaryDto();
        supplierSummaryDto.setCode(supplierCode);
        supplierSummaryDto.setName("Elite K9 Supplies");
        return supplierSummaryDto;
    }

    private PageResponse<DogDetailDto> buildValidPageResponse() {
        PageResponse<DogDetailDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(List.of(buildValidDogDetailDto()));
        pageResponse.setMetadata(new PageResponse.PageMetadata(0, 10, 0, 0, true, true));
        return pageResponse;
    }

    private UpdateDogRequest buildValidUpdateDogRequest() {
        UpdateDogRequest updateDogRequest = new UpdateDogRequest();
        updateDogRequest.setName("Rex Updated");
        updateDogRequest.setBreed("Belgian Malinois");
        updateDogRequest.setSupplierCode("PRO_K9");
        updateDogRequest.setBadgeNumber("BDG789");
        updateDogRequest.setGender(MALE);
        updateDogRequest.setStatus(TRAINING);
        return updateDogRequest;
    }

    private DogDetailDto buildValidDogDetailDto(UpdateDogRequest updateDogRequest) {
        DogDetailDto dogDetailDto = new DogDetailDto();
        dogDetailDto.setId(1L);
        dogDetailDto.setName(updateDogRequest.getName());
        dogDetailDto.setBreed(updateDogRequest.getBreed());
        dogDetailDto.setBadgeNumber(updateDogRequest.getBadgeNumber());
        dogDetailDto.setGender(updateDogRequest.getGender());
        dogDetailDto.setStatus(updateDogRequest.getStatus());
        dogDetailDto.setSupplier(buildValidSupplierSummaryDto(updateDogRequest.getSupplierCode()));
        return dogDetailDto;
    }

    private RetireDogRequest buildValidRetireDogRequest() {
        RetireDogRequest retireDogRequest = new RetireDogRequest();
        retireDogRequest.setLeavingDate(LocalDate.now().minusDays(1));
        retireDogRequest.setLeavingReason(org.policedog.registry.domain.LeavingReason.RETIRED_PUT_DOWN);
        return retireDogRequest;
    }
}