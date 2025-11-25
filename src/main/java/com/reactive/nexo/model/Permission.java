package com.reactive.nexo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("permission")
public class Permission {

    @Id
    private Integer id;
    @Column("rol_id")
    private Integer rol_id;
    private String method;
    private String endpoint;
    public List<Map<String, List<String>>> getPermissions() {
        return Arrays.asList(Map.of(method, List.of()));
    }   
}
