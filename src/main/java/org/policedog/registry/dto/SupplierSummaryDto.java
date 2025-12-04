package org.policedog.registry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplierSummaryDto {

    private Long id;
    private String code;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
}
