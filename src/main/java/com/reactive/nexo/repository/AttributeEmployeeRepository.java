package com.reactive.nexo.repository;

import com.reactive.nexo.model.AttributeEmployee;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Mono;

public interface AttributeEmployeeRepository extends ReactiveCrudRepository<AttributeEmployee,Integer> {
    // An employee can have multiple attributes, so return a Flux
    Flux<AttributeEmployee> findByEmployeeId(Integer employeeId);

    @Query("select id,employee_id,name_attribute,multiple from attribute_employee where employee_id = $1 and name_attribute = $2 limit 1")
    Mono<AttributeEmployee> findByEmployeeIdAndName(Integer employeeId, String nameAttribute);

    @Query("MERGE INTO attribute_employee (employee_id, name_attribute, multiple) KEY (employee_id, name_attribute) VALUES ($1, $2, $3)")
    Mono<Integer> upsertByEmployeeIdAndName(Integer employeeId, String nameAttribute, Boolean multiple);
}
