package org.policedog.registry.integrationtest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.policedog.registry.dao.PoliceDogRepository;
import org.policedog.registry.domain.Gender;
import org.policedog.registry.domain.LeavingReason;
import org.policedog.registry.domain.PoliceDog;
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
class DogControllerITUpdated {

    private static final String DOG_NOT_FOUND_MESSAGE = "Dog with ID %d not found";
    private static final String CANNOT_UPDATE_DELETED_DOG_MESSAGE = "Cannot update deleted dog with ID %d";
    private static final String CANNOT_UPDATE_RETIRED_DOG_MESSAGE = "Cannot update retired dog with ID %d";
    private static final String DOG_BADGE_NUMBER_EXISTS_MESSAGE = "Dog with badge number %s already exists";
    private static final String SUPPLIER_NOT_FOUND_MESSAGE = "Supplier with code %s not found";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PoliceDogRepository dogRepository;

    @Test
    void shouldReturnDogById() throws Exception {
        PoliceDog policeDog = dogRepository.findById(5L)
                .orElseThrow(() -> new RuntimeException(DOG_NOT_FOUND_MESSAGE));
        policeDog.getSupplier().setDogs(List.of());
        String dogString = objectMapper.writeValueAsString(policeDog);
        System.out.println(dogString);
    }

}