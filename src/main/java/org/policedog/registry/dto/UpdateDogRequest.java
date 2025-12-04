package org.policedog.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.policedog.registry.domain.Gender;
import org.policedog.registry.domain.Status;
import org.policedog.registry.validator.annotation.ValidStatus;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.policedog.registry.domain.Status.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update an existing police dog")
public class UpdateDogRequest {

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

    @Schema(description = "Status of the dog, can be TRAINING, IN_SERVICE, RETIRED", example = "TRAINING",
            allowableValues = {"TRAINING", "IN_SERVICE", "RETIRED"}, requiredMode = REQUIRED)
    @ValidStatus({TRAINING, IN_SERVICE, RETIRED})
    @NotNull(message = "Status is required")
    private Status status;

    private CharacteristicsDto characteristics;
}
