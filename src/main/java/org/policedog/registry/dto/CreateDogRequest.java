package org.policedog.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.policedog.registry.domain.Gender;
import org.policedog.registry.domain.Status;
import org.policedog.registry.validator.annotation.ValidStatus;

import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.policedog.registry.domain.Status.IN_SERVICE;
import static org.policedog.registry.domain.Status.TRAINING;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDogRequest {

    @Schema(description = "Name of the dog", example = "Rex", requiredMode = REQUIRED)
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Breed of the dog", example = "German Shepherd", requiredMode = REQUIRED)
    @NotBlank(message = "Breed is required")
    private String breed;

    @Schema(description = "Supplier code, should be of an existing supplier", example = "ELITE_K9", requiredMode = REQUIRED)
    @NotBlank(message = "Supplier code is required")
    private String supplierCode;

    @Schema(description = "Badge number of the dog, should be unique.", example = "BDG456", requiredMode = REQUIRED)
    @NotBlank(message = "Badge number is required")
    private String badgeNumber;

    @Schema(description = "Gender of the dog", example = "MALE", requiredMode = REQUIRED)
    @NotNull(message = "Gender is required")
    private Gender gender;

    @Schema(description = "Birth date of the dog, should be in past.", example = "2020-01-15", requiredMode = REQUIRED)
    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @Schema(description = "Status of the dog, can be TRAINING, IN_SERVICE", example = "TRAINING",
            allowableValues = {"TRAINING", "IN_SERVICE"}, requiredMode = REQUIRED)
    @ValidStatus({TRAINING, IN_SERVICE})
    @NotNull(message = "Status is required")
    private Status status;

    private CharacteristicsDto characteristics;
}
