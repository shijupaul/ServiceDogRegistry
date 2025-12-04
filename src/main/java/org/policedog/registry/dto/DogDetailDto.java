package org.policedog.registry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.policedog.registry.domain.Gender;
import org.policedog.registry.domain.LeavingReason;
import org.policedog.registry.domain.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DogDetailDto {

    private Long id;
    private String name;
    private String breed;
    private SupplierSummaryDto supplier;
    private String badgeNumber;
    private Gender gender;
    private LocalDate birthDate;
    private LocalDate dateAcquired;
    private Status status;
    private LocalDate leavingDate;
    private LeavingReason leavingReason;
    private CharacteristicsDto characteristics;
    private Boolean deleted;
    private LocalDateTime deletedAt;
}
