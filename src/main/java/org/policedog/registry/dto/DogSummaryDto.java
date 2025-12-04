package org.policedog.registry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.policedog.registry.domain.Gender;
import org.policedog.registry.domain.LeavingReason;
import org.policedog.registry.domain.Status;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DogSummaryDto {

    private Long id;
    private String name;
    private String breed;
    private String badgeNumber;
    private Gender gender;
    private LocalDate birthDate;
}
