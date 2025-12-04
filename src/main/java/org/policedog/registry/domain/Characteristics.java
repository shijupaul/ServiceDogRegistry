package org.policedog.registry.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Characteristics {

    private Boolean isAggressive;
    private Boolean requiresSeparateKennel;
    private Boolean isNoiceTolerant;
    private Boolean hasSpecialDiet;
    private String dietaryRequirements;
    private Boolean requiresExercise;
    private String exerciseNotes;
    private Boolean hasMedicalConditions;
    private String medicalNotes;
    private String temperament;
}
