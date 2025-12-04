package org.policedog.registry.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CharacteristicsDto {

    @Schema(description = "Whether the dog is aggressive", example = "false")
    private Boolean isAggressive;
    @Schema(description = "Whether the dog requires a separate kennel", example = "false")
    private Boolean requiresSeparateKennel;
    @Schema(description = "Whether the dog is tolerant to noise", example = "false")
    private Boolean isNoiceTolerant;
    @Schema(description = "Whether the dog has a special diet", example = "false")
    private Boolean hasSpecialDiet;
    @Schema(description = "Dietary requirements of the dog", example = "Grain-free diet")
    private String dietaryRequirements;
    @Schema(description = "Whether the dog requires regular exercise", example = "false")
    private Boolean requiresExercise;
    @Schema(description = "Exercise notes for the dog", example = "Needs at least 1 hour of exercise daily")
    private String exerciseNotes;
    @Schema(description = "Whether the dog has any medical conditions", example = "false")
    private Boolean hasMedicalConditions;
    @Schema(description = "Medical notes for the dog", example = "Allergic to certain medications")
    private String medicalNotes;
    @Schema(description = "Temperament of the dog", example = "Calm and friendly")
    private String temperament;
}
