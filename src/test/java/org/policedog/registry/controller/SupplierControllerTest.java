package org.policedog.registry.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.policedog.registry.dto.DogSummaryDto;
import org.policedog.registry.dto.PageResponse;
import org.policedog.registry.dto.SupplierDetailDto;
import org.policedog.registry.dto.SupplierRequest;
import org.policedog.registry.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.policedog.registry.domain.Gender.FEMALE;
import static org.policedog.registry.domain.Gender.MALE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SupplierController.class)
@TestPropertySource(properties = {"spring.jpa.auditing.enabled=false"})
class SupplierControllerTest {

    private static final Long SUPPLIER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SupplierService supplierService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetSupplierById() throws Exception {
        SupplierDetailDto supplierDetailDto = buildValidSupplierDetailDto();
        when(supplierService.getSupplierById(SUPPLIER_ID)).thenReturn(supplierDetailDto);

        String supplierDetailDtoRetJson = mockMvc.perform(get("/api/dogs/supplier/{id}", SUPPLIER_ID))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        SupplierDetailDto supplierDetailDtoRet = objectMapper.readValue(supplierDetailDtoRetJson, SupplierDetailDto.class);
        assertEquals(supplierDetailDto, supplierDetailDtoRet);
    }

    @Test
    void shouldGetSuppliersWhenPaginationParametersAreProvided() throws Exception {
        int pageNo = 1;
        int pageSize = 5;

        PageResponse<SupplierDetailDto> expectedPageResponse = buildValidPageResponse();
        when(supplierService.getSuppliers(pageNo, pageSize)).thenReturn(expectedPageResponse);

        String actualPageResponseJson = mockMvc.perform(get("/api/dogs/supplier")
                        .queryParam("pageNo", String.valueOf(pageNo))
                        .queryParam("pageSize", String.valueOf(pageSize)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResponse<SupplierDetailDto> actualPageResponse = objectMapper.readValue(actualPageResponseJson, new TypeReference<PageResponse<SupplierDetailDto>>() {
        });
        assertEquals(expectedPageResponse, actualPageResponse);
    }

    @Test
    void shouldGetSuppliersWhenPaginationParametersAreNotProvided() throws Exception {

        PageResponse<SupplierDetailDto> expectedPageResponse = buildValidPageResponse();
        when(supplierService.getSuppliers(0, 10)).thenReturn(expectedPageResponse);

        String actualPageResponseJson = mockMvc.perform(get("/api/dogs/supplier"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResponse<SupplierDetailDto> actualPageResponse = objectMapper.readValue(actualPageResponseJson, new TypeReference<PageResponse<SupplierDetailDto>>() {
        });
        assertEquals(expectedPageResponse, actualPageResponse);
    }

    @Test
    void shouldReturnBadRequestWhenMandatoryFieldsAreMissingForSupplierCreate() throws Exception {
        SupplierRequest supplierRequest = new SupplierRequest();

        mockMvc.perform(post("/api/dogs/supplier")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplierRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.code").value("Supplier code must not be empty"))
                .andExpect(jsonPath("$.errors.name").value("Supplier name must not be empty"));
    }

    @Test
    void shouldCreateSupplierWhenRequestIsValid() throws Exception {
        SupplierRequest supplierRequest = buildValidSupplierRequest();
        SupplierDetailDto supplierDetailDto = buildSupplierDetailsFromSupplierRequest(supplierRequest);

        when(supplierService.createSupplier(supplierRequest)).thenReturn(supplierDetailDto);

        mockMvc.perform(post("/api/dogs/supplier")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplierRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(supplierRequest.getCode()))
                .andExpect(jsonPath("$.name").value(supplierRequest.getName()))
                .andExpect(jsonPath("$.contactPerson").value(supplierRequest.getContactPerson()))
                .andExpect(jsonPath("$.email").value(supplierRequest.getEmail()))
                .andExpect(jsonPath("$.phone").value(supplierRequest.getPhone()));
    }

    @Test
    void shouldReturnBadRequestWhenMandatoryFieldsAreMissingForSupplierUpdate() throws Exception {
        SupplierRequest supplierRequest = new SupplierRequest();

        mockMvc.perform(put("/api/dogs/supplier/{id}", SUPPLIER_ID)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplierRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.code").value("Supplier code must not be empty"))
                .andExpect(jsonPath("$.errors.name").value("Supplier name must not be empty"));
    }

    @Test
    void shouldUpdateSupplierWhenRequestIsValid() throws Exception {
        SupplierRequest supplierRequest = buildValidSupplierRequest();
        SupplierDetailDto supplierDetailDto = buildSupplierDetailsFromSupplierRequest(supplierRequest);

        when(supplierService.updateSupplier(SUPPLIER_ID, supplierRequest)).thenReturn(supplierDetailDto);

        mockMvc.perform(put("/api/dogs/supplier/{id}", SUPPLIER_ID)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplierRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(supplierRequest.getCode()))
                .andExpect(jsonPath("$.name").value(supplierRequest.getName()))
                .andExpect(jsonPath("$.contactPerson").value(supplierRequest.getContactPerson()))
                .andExpect(jsonPath("$.email").value(supplierRequest.getEmail()))
                .andExpect(jsonPath("$.phone").value(supplierRequest.getPhone()));
    }

    private SupplierDetailDto buildValidSupplierDetailDto() {
        SupplierDetailDto supplierDetailDto = new SupplierDetailDto();
        supplierDetailDto.setId(1L);
        supplierDetailDto.setCode("ELITEK9");
        supplierDetailDto.setName("Elite K9 Supplies");
        supplierDetailDto.setContactPerson("Mike Johnson");
        supplierDetailDto.setPhone("555-123-4567");
        supplierDetailDto.setEmail("mike@bravocanines.com");
        supplierDetailDto.setDogs(buildDogSummaryDtoList());
        return supplierDetailDto;
    }

    private SupplierDetailDto buildSupplierDetailsFromSupplierRequest(SupplierRequest supplierRequest) {
        SupplierDetailDto supplierDetailDto = new SupplierDetailDto();
        supplierDetailDto.setCode(supplierRequest.getCode());
        supplierDetailDto.setName(supplierRequest.getName());
        supplierDetailDto.setContactPerson(supplierRequest.getContactPerson());
        supplierDetailDto.setEmail(supplierRequest.getEmail());
        supplierDetailDto.setPhone(supplierRequest.getPhone());
        return supplierDetailDto;
    }

    private List<DogSummaryDto> buildDogSummaryDtoList() {
        return List.of(new DogSummaryDto(1L, "Rex", "German Shepherd", "K9-001", MALE, LocalDate.of(2020, 1, 1)),
                new DogSummaryDto(2L, "Bella", "Dutch Shepherd", "K9-005", FEMALE, LocalDate.of(2021, 5, 20)));
    }

    private PageResponse<SupplierDetailDto> buildValidPageResponse() {
        PageResponse<SupplierDetailDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(List.of(buildValidSupplierDetailDto()));
        pageResponse.setMetadata(new PageResponse.PageMetadata(0, 10, 0, 0, true, true));
        return pageResponse;
    }

    private SupplierRequest buildValidSupplierRequest() {
        SupplierRequest supplierRequest = new SupplierRequest();
        supplierRequest.setCode("ELITEK9");
        supplierRequest.setName("Elite K9 Supplies");
        supplierRequest.setContactPerson("Mike Johnson");
        supplierRequest.setPhone("555-123-4567");
        supplierRequest.setEmail("mike@bravocanines.com");
        return supplierRequest;
    }
}