package org.policedog.registry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplierDetailDto {

    private Long id;
    private String code;
    private String name;
    private List<DogSummaryDto> dogs;
    private String contactPerson;
    private String email;
    private String phone;
}
