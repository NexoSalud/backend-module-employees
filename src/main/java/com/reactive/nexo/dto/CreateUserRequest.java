package com.reactive.nexo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    private String names;
    private String lastnames;
    private String identification_type;
    private String identification_number;
    // attributes: map from attribute name -> list of values
    private Map<String, List<String>> attributes;
    private String password;
}
