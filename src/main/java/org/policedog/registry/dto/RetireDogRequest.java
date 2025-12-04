package org.policedog.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.policedog.registry.domain.LeavingReason;

import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to retire a dog from service")
public class RetireDogRequest {

    @NotNull(message = "Leaving date is required")
    @PastOrPresent(message = "Leaving date cannot be in the future")
    @Schema(description = "Date when the dog left service", example = "2020-12-31", requiredMode = REQUIRED)
    private LocalDate leavingDate;

    @NotNull(message = "Leaving reason is required.")
    @Schema(description = "Reason for the dog leaving service, can be TRANSFERRED, RETIRED_PUT_DOWN, KIA, REJECTED, RETIRED_RE_HOUSED, DIED", example = "TRANSFERRED", required = true,
            allowableValues = {"TRANSFERRED", "RETIRED_PUT_DOWN", "KIA", "REJECTED", "RETIRED_RE_HOUSED", "DIED"}, requiredMode = REQUIRED)
    private LeavingReason leavingReason;
}
