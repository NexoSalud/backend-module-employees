package com.reactive.nexo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolWithPermissionDTO {

    private Integer id;
    private String nombre;
    
    @JsonProperty("permission")
    private List<Map<String, List<String>>> permissions;
    //private List<String> permission;
}
