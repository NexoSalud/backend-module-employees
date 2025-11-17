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
@Table("value_attribute_employee")
public class ValueAttributeEmployee {
    @Id
    private Integer id;
    @Column("attribute_id")
    private Integer attributeId;
    @Column("value_attribute")
    private String valueAttribute;
}
