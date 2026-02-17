package com.reactive.nexo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("rol")
public class Rol {

    @Id
    private Integer id;
    private String name;
    private Boolean asistencial = Boolean.TRUE;

    public Rol(Integer id, String name) {
        this.id = id;
        this.name = name;
        this.asistencial = Boolean.TRUE;
    }
}
