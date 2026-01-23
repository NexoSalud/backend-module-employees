package com.reactive.nexo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("employees")
public class Employee {

    @Id
    private Integer id;
    private String names;
    private String lastnames;
    private String identification_type;
    private String identification_number;
    //@ToString.Exclude
    private String password;
    private Integer rol_id;
    private String secret;
    private Boolean login_enabled;

    // Keep compatibility with existing constructor usages that pass 5 args
    public Employee(Integer id, String names, String lastnames, String identification_type, String identification_number) {
        this.id = id;
        this.names = names;
        this.lastnames = lastnames;
        this.identification_type = identification_type;
        this.identification_number = identification_number;
        this.password = password;
        this.rol_id = rol_id;
        this.secret = null;
    }
}
