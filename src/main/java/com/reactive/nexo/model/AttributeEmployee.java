package com.reactive.nexo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("attribute_employee")
public class AttributeEmployee {
    @Id
    private Integer id;
    private String name_attribute;
    private Boolean multiple;
    @Column("employee_id")
    private Integer employeeId;
}
