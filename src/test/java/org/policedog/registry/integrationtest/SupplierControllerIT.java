package org.policedog.registry.integrationtest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.policedog.registry.dto.PageResponse;
import org.policedog.registry.dto.SupplierDetailDto;
import org.policedog.registry.dto.SupplierRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SupplierControllerIT {

    private static final Long VALID_SUPPLIER_ID = 1L;
    private static final Long INVALID_SUPPLIER_ID = 999L;
    private static final String SUPPLIER_NOT_FOUND_MESSAGE = "Supplier with id %d not found";
    private static final String SUPPLIER_CODE_EXISTS_MESSAGE = "Supplier with code %s already exists";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void shouldReturnNotFoundForInvalidSupplierId() throws Exception {
        mockMvc.perform(get("/api/dogs/supplier/{id}", INVALID_SUPPLIER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(SUPPLIER_NOT_FOUND_MESSAGE.formatted(INVALID_SUPPLIER_ID)));
    }

    @Test
    void shouldReturnMatchingSupplierForId() throws Exception {
        mockMvc.perform(get("/api/dogs/supplier/{id}", VALID_SUPPLIER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(VALID_SUPPLIER_ID))
                .andExpect(jsonPath("$.name").value("Elite K9 Training Center"))
                .andExpect(jsonPath("$.code").value("ELITE_K9"))
                .andExpect(jsonPath("$.contactPerson").value("John Smith"))
                .andExpect(jsonPath("$.email").value("john@elitek9.com"))
                .andExpect(jsonPath("$.phone").value("555-0101"));
    }

    @Test
    void shouldGetSuppliersReturnAll() throws Exception {
        String responseJson = mockMvc.perform(get("/api/dogs/supplier")
                        .queryParam("pageNo", "0")
                        .queryParam("pageSize", "15")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();


        PageResponse<SupplierDetailDto> pageResponse =
                objectMapper.readValue(responseJson, new TypeReference<PageResponse<SupplierDetailDto>>() {
                });

        assertAll(() -> {
            assertEquals(3, pageResponse.getContent().size());
            assertEquals(0, pageResponse.getMetadata().getPage());
            assertEquals(15, pageResponse.getMetadata().getSize());
            assertEquals(3, pageResponse.getMetadata().getTotalElements());
            assertEquals(1, pageResponse.getMetadata().getTotalPages());
            assertTrue(pageResponse.getMetadata().isFirst());
            assertTrue(pageResponse.getMetadata().isLast());
        });
    }

    @Test
    void shouldFailToCreateSupplierWhenSupplierCodeIsNotUnique() throws Exception {
        String existingSupplierCode = "ELITE_K9"; // Matches to existing supplier code (1)

        SupplierRequest supplierRequest = buildValidSupplierRequest();
        supplierRequest.setCode(existingSupplierCode);

        mockMvc.perform(post("/api/dogs/supplier")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplierRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(SUPPLIER_CODE_EXISTS_MESSAGE.formatted(existingSupplierCode)));
    }

    @Test
    void shouldCreateSupplierWhenRequestIsValid() throws Exception {
        SupplierRequest supplierRequest = buildValidSupplierRequest();

        mockMvc.perform(post("/api/dogs/supplier")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplierRequest)))
                .andExpect(jsonPath("$.name").value(supplierRequest.getName()))
                .andExpect(jsonPath("$.code").value(supplierRequest.getCode()))
                .andExpect(jsonPath("$.contactPerson").value(supplierRequest.getContactPerson()))
                .andExpect(jsonPath("$.email").value(supplierRequest.getEmail()))
                .andExpect(jsonPath("$.phone").value(supplierRequest.getPhone()));
    }

    @Test
    void shouldFailToUpdateSupplierWhenNotFound() throws Exception {
        SupplierRequest supplierRequest = buildValidSupplierRequest();

        mockMvc.perform(put("/api/dogs/supplier/{id}", INVALID_SUPPLIER_ID)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplierRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value(SUPPLIER_NOT_FOUND_MESSAGE.formatted(INVALID_SUPPLIER_ID)));
    }

    @Test
    void shouldFailToUpdateSupplierWhenCodeIsNotUnique() throws Exception {
        String newSupplierCode = "ALPHA_DOG"; // Matches to existing supplier code (2)

        SupplierRequest supplierRequest = buildValidSupplierRequest();
        supplierRequest.setCode(newSupplierCode);

        mockMvc.perform(put("/api/dogs/supplier/{id}", VALID_SUPPLIER_ID)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplierRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value(SUPPLIER_CODE_EXISTS_MESSAGE.formatted(newSupplierCode)));
    }

    @Test
    void shouldUpdateSupplierWhenRequestIsValid() throws Exception {
        SupplierRequest supplierRequest = buildValidSupplierRequest();

        mockMvc.perform(put("/api/dogs/supplier/{id}", VALID_SUPPLIER_ID)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplierRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(VALID_SUPPLIER_ID))
                .andExpect(jsonPath("$.name").value(supplierRequest.getName()))
                .andExpect(jsonPath("$.code").value(supplierRequest.getCode()))
                .andExpect(jsonPath("$.contactPerson").value(supplierRequest.getContactPerson()))
                .andExpect(jsonPath("$.email").value(supplierRequest.getEmail()))
                .andExpect(jsonPath("$.phone").value(supplierRequest.getPhone()))
                .andExpect(jsonPath("$.dogs.size()").value(4));
    }

    private SupplierRequest buildValidSupplierRequest() {
        SupplierRequest supplierRequest = new SupplierRequest();
        supplierRequest.setCode("ELITEK9_OXF");
        supplierRequest.setName("Elite K9 Supplies");
        supplierRequest.setContactPerson("Mike Johnson");
        supplierRequest.setPhone("555-123-4567");
        supplierRequest.setEmail("mike@bravocanines.com");
        return supplierRequest;
    }

}
