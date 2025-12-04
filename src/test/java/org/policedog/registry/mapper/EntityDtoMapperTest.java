package org.policedog.registry.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.policedog.registry.domain.Characteristics;
import org.policedog.registry.domain.PoliceDog;
import org.policedog.registry.domain.Supplier;
import org.policedog.registry.dto.CharacteristicsDto;
import org.policedog.registry.dto.DogDetailDto;
import org.policedog.registry.dto.SupplierSummaryDto;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityDtoMapperTest {

    private EntityDtoMapper mapper;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(EntityDtoMapper.class);
        objectMapper = new ObjectMapper();
        objectMapper.registerModules(new JavaTimeModule());
    }

    @Test
    void shouldMapPoliceDogToDogDetailDto() throws JsonProcessingException {
        PoliceDog policeDog = buildValidPoliceDog();
        DogDetailDto dogDetailDto = mapper.toDogDetailDto(policeDog);
        assertEntityDtoMatches(policeDog, dogDetailDto);
    }


    private void assertEntityDtoMatches(PoliceDog policeDog, DogDetailDto dogDetailDto) {
        assertAll(() -> {
            assertEquals(policeDog.getId(), dogDetailDto.getId());
            assertEquals(policeDog.getName(), dogDetailDto.getName());
            assertEquals(policeDog.getBreed(), dogDetailDto.getBreed());
            assertEquals(policeDog.getBadgeNumber(), dogDetailDto.getBadgeNumber());
            assertEquals(policeDog.getGender(), dogDetailDto.getGender());
            assertEquals(policeDog.getBirthDate(), dogDetailDto.getBirthDate());
            assertEquals(policeDog.getDateAcquired(), dogDetailDto.getDateAcquired());
            assertEquals(policeDog.getStatus(), dogDetailDto.getStatus());
            assertEquals(policeDog.getLeavingDate(), dogDetailDto.getLeavingDate());
            assertEquals(policeDog.getLeavingReason(), dogDetailDto.getLeavingReason());
            assertEquals(policeDog.getDeleted(), dogDetailDto.getDeleted());
            assertEquals(policeDog.getDeletedAt(), dogDetailDto.getDeletedAt());
            assertSupplierEntityDtoMatches(policeDog.getSupplier(), dogDetailDto.getSupplier());
            assertCharacteresticsEntityDtoMatches(policeDog.getCharacteristics(), dogDetailDto.getCharacteristics());
        });
    }

    private void assertSupplierEntityDtoMatches(Supplier supplier, SupplierSummaryDto supplierSummaryDto) {
        assertAll(() -> {
            assertEquals(supplier.getId(), supplierSummaryDto.getId());
            assertEquals(supplier.getName(), supplierSummaryDto.getName());
            assertEquals(supplier.getCode(), supplierSummaryDto.getCode());
            assertEquals(supplier.getContactPerson(), supplierSummaryDto.getContactPerson());
            assertEquals(supplier.getEmail(), supplierSummaryDto.getEmail());
            assertEquals(supplier.getPhone(), supplierSummaryDto.getPhone());
        });
    }

    private void assertCharacteresticsEntityDtoMatches(Characteristics characteristics, CharacteristicsDto characteristicsDto) {
        assertAll(() -> {
            assertEquals(characteristics.getIsAggressive(), characteristicsDto.getIsAggressive());
            assertEquals(characteristics.getRequiresSeparateKennel(), characteristicsDto.getRequiresSeparateKennel());
            assertEquals(characteristics.getIsNoiceTolerant(), characteristicsDto.getIsNoiceTolerant());
            assertEquals(characteristics.getHasSpecialDiet(), characteristicsDto.getHasSpecialDiet());
            assertEquals(characteristics.getDietaryRequirements(), characteristicsDto.getDietaryRequirements());
            assertEquals(characteristics.getRequiresExercise(), characteristicsDto.getRequiresExercise());
            assertEquals(characteristics.getExerciseNotes(), characteristicsDto.getExerciseNotes());
            assertEquals(characteristics.getHasMedicalConditions(), characteristicsDto.getHasMedicalConditions());
            assertEquals(characteristics.getMedicalNotes(), characteristicsDto.getMedicalNotes());
            assertEquals(characteristics.getTemperament(), characteristicsDto.getTemperament());
        });
    }


    private PoliceDog buildValidPoliceDog() throws JsonProcessingException {
        String policeDogJson = """
                                {
                  "id": 5,
                  "name": "Rocky",
                  "breed": "Dutch Shepherd",
                  "supplier": {
                    "id": 3,
                    "code": "BRAVO_CANINES",
                    "name": "Bravo Canines Inc.",
                    "dogs": [],
                    "contactPerson": "Mike Johnson",
                    "email": "mike@bravocanines.com",
                    "phone": "555-0103",
                    "version": 1
                  },
                  "badgeNumber": "K9-007",
                  "gender": "MALE",
                  "birthDate": "2021-01-05",
                  "dateAcquired": "2021-08-12",
                  "status": "TRAINING",
                  "leavingDate": null,
                  "leavingReason": null,
                  "characteristics": {
                    "isAggressive": true,
                    "requiresSeparateKennel": true,
                    "isNoiceTolerant": false,
                    "hasSpecialDiet": true,
                    "dietaryRequirements": "Low-fat diet for weight management.",
                    "requiresExercise": true,
                    "exerciseNotes": "Prone to ear infections; requires regular cleaning.",
                    "hasMedicalConditions": false,
                    "medicalNotes": null,
                    "temperament": "AGGRESSIVE"
                  },
                  "version": 1,
                  "deleted": false,
                  "deletedAt": null
                }
                """;
        return objectMapper.readValue(policeDogJson, PoliceDog.class);
    }

}