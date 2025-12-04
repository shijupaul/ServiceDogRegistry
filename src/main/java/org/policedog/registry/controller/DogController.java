package org.policedog.registry.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.policedog.registry.domain.Gender;
import org.policedog.registry.domain.LeavingReason;
import org.policedog.registry.domain.Status;
import org.policedog.registry.dto.*;
import org.policedog.registry.service.DogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/dogs/dogs")
public class DogController {

    private final DogService dogService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = "application/json", produces = "application/json")
    @Operation(summary = "Create a new police dog",
            description = """
                    Create a new police dog with the provided details.
                    
                    Error will be returned in the following cases:
                    - Supplier code does not correspond to an existing supplier
                    - Badge number is not unique
                    - Required fields are missing or invalid
                    """, tags = {"Dog - Command Operations"}
    )
    public ResponseEntity<DogDetailDto> createDog(@Valid @RequestBody CreateDogRequest createDogRequest) {
        DogDetailDto dog = dogService.createDog(createDogRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(dog);
    }

    @GetMapping(produces = "application/json")
    @Operation(summary = "Get a paginated list of active (non-deleted) police dogs with optional filters",
            description = """
                    Retrieve a paginated list of police dogs in the system that are not marked as deleted.
                    Optional filters can be applied by providing a JSON string in the 'filter' query parameter.
                    
                    ***Filter Parameters:**
                    - name: Filter by dog name (partial match) -- {"name": "Rex"}
                    - breed: Filter by dog breed (partial match) -- {"breed": "German Shepherd"}
                    - supplierCode: Filter by supplier code (partial match) -- {"supplierCode": "ELITE_K9"}
                    
                    """, tags = {"Dog - Query Operations"})
    public ResponseEntity<PageResponse<DogDetailDto>> getDogs(
            @Parameter(
                    name = "filter",
                    description = "Search filters as JSON string",
                    example = "{\"name\":\"Rex\",\"breed\":\"German Shepherd\",\"supplierCode\":\"ELITE_K9\"}",
                    schema = @Schema(
                            type = "string",
                            format = "json",
                            implementation = SearchFilter.class
                    )
            )
            @RequestParam(required = false) String filter, @RequestParam(defaultValue = "0") int pageNo, @RequestParam(defaultValue = "10") int pageSize) {
        if (StringUtils.hasText(filter)) {
            try {
                SearchFilter searchFilter = objectMapper.readValue(filter, SearchFilter.class);
                PageResponse<DogDetailDto> dogs = dogService.getDogs(searchFilter, pageNo, pageSize);
                return ResponseEntity.ok(dogs);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid filter parameter", e);
            }
        }
        PageResponse<DogDetailDto> dogs = dogService.getDogs(new SearchFilter(), pageNo, pageSize);
        return ResponseEntity.ok(dogs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a police dog by ID including deleted dogs",
            description = """
                    Retrieve the details of a specific police dog by its ID.
                    
                    Error will be returned in the following cases:
                    - Dog cannot be found
                    """, tags = {"Dog - Query Operations"})
    public ResponseEntity<DogDetailDto> getDog(@PathVariable Long id) {
        DogDetailDto dog = dogService.getDogById(id);
        return ResponseEntity.ok(dog);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a police dog by ID",
            description = """
                    Mark a dog as deleted without permanently removing it from the database.
                    
                    **Idempotent Behavior:**
                    - If the dog is already deleted, this method returns 204 No Content without error
                    - Multiple DELETE requests on the same dog have the same effect
                    - The dog remains in the database with deleted flag set to true
                    """, tags = {"Dog - Lifecycle Operations"})
    public ResponseEntity<Void> deleteDog(@PathVariable Long id) {
        dogService.deleteDogById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a police dog by ID",
            description = """
                    
                    Error will be returned in the following cases:
                    - Dog cannot be found or is deleted
                    - Dog is LEFT the service
                    - Supplier code does not correspond to an existing supplier
                    - Badge number is not unique
                    """, tags = {"Dog - Command Operations"})
    public ResponseEntity<DogDetailDto> updateDog(@PathVariable Long id, @Valid @RequestBody UpdateDogRequest updateDogRequest) {
        DogDetailDto updatedDog = dogService.updateDog(id, updateDogRequest);
        return ResponseEntity.ok(updatedDog);
    }

    @PostMapping(value = "/{id}/retire", consumes = "application/json")
    @Operation(summary = "Retire a police dog by ID",
            description = """
                    Mark a dog as retired by providing the leaving date and reason.
                    
                    **Idempotent Behavior:**
                    - If the dog is already retired, then this method returns the existing retired dog details without error
                    - Multiple retire requests on the same dog have the same effect
                    - If the dog is deleted, an error is returned
                    
                    """, tags = {"Dog - Lifecycle Operations"})
    public ResponseEntity<DogDetailDto> retireDog(@PathVariable Long id, @Valid @RequestBody RetireDogRequest retireDogRequest) {
        DogDetailDto dog = dogService.retireDog(id, retireDogRequest);
        return ResponseEntity.ok(dog);
    }

    @GetMapping("/search/by-gender")
    @Operation(summary = "Get Dogs by Gender",
            description = """
                    Get Dogs by Gender -- MALE / FEMALE
                    """, tags = {"Dog - Query Operations"})
    public ResponseEntity<List<DogDetailDto>> getDogsByGender(@RequestParam Gender gender) {
        List<DogDetailDto> dogs = dogService.getDogsByGender(gender);
        return ResponseEntity.ok(dogs);
    }

    @GetMapping("/search/by-status")
    @Operation(summary = "Get Dogs by Status",
            description = """
                    Get Dogs by Status -- TRAINING / IN_SERVICE / RETIRED / LEFT
                    """, tags = {"Dog - Query Operations"})
    public ResponseEntity<List<DogDetailDto>> getDogsByStatus(@RequestParam Status status) {
        List<DogDetailDto> dogs = dogService.getDogsByStatus(status);
        return ResponseEntity.ok(dogs);
    }

    @GetMapping("/search/by-leaving-reason")
    @Operation(summary = "Get Dogs by LeavingReason",
            description = """
                    Get Dogs by LeavingReason -- TRANSFERRED / RETIRED_PUT_DOWN / KIA / REJECTED / RETIRED_RE_HOUSED / DIED
                    """, tags = {"Dog - Query Operations"})
    public ResponseEntity<List<DogDetailDto>> getDogsByLeavingReason(@RequestParam LeavingReason leavingReason) {
        List<DogDetailDto> dogs = dogService.getDogsByLeavingReason(leavingReason);
        return ResponseEntity.ok(dogs);
    }
}
