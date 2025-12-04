package org.policedog.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequest {

    @Schema(description = "Unique code for the supplier", example = "ELITE_K999", requiredMode = REQUIRED)
    @NotBlank(message = "Supplier code must not be empty")
    private String code;

    @Schema(description = "Name of the supplier", example = "Elite K999 Services", requiredMode = REQUIRED)
    @NotBlank(message = "Supplier name must not be empty")
    private String name;

    @Schema(description = "Contact person at the supplier", example = "John Doe")
    private String contactPerson;

    @Schema(description = "Email address of the supplier", example = "john.doe@elitek9.co.uk")
    private String email;

    @Schema(description = "Phone number of the supplier", example = "+441234567890")
    private String phone;
}
