package com.reactive.nexo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateEmployeeRequest {
    private String names;
    private String lastnames;
    private String identification_type;
    private String identification_number;
    private String password;
    private Integer rol_id;
    private String secret;
    // attributes: map from attribute name -> list of values
    private Map<String, List<String>> attributes;
}
