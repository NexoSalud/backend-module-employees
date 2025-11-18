package com.reactive.nexo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserWithAttributesDTO {
    private Integer id;
    private String names;
    private String lastnames;
    private String identification_type;
    private List<AttributeWithValuesDTO> attributes;
}
