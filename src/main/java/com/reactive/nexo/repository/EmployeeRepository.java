package com.reactive.nexo.repository;

import com.reactive.nexo.model.Employee;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmployeeRepository extends ReactiveCrudRepository<Employee,Integer> {
    @Query("select id,identification_number,identification_type,names,lastnames,password,rol_id,secret from employees where identification_number like $1")
    Flux<Employee> findByIdentificationNumber(String identificationNumber);
    @Query("select id,identification_number,identification_type,names,lastnames,password,rol_id,secret from employees where identification_type = $1 and identification_number = $2 limit 1")
    Mono<Employee> findByIdentificationTypeAndNumber(String identificationType, String identificationNumber);
    @Query("select id,identification_number,identification_type,names,lastnames,password,rol_id,secret from employees order by id asc limit $1 offset $2")
    Flux<Employee> findAllWithPagination(int limit, int offset);
    @Query("select count(*) from employees")
    Mono<Long> countAll();

    // Find any employee by attribute name and value (for uniqueness checks)
    @Query("select e.id, e.identification_number, e.identification_type, e.names, e.lastnames, e.password, e.rol_id, e.secret\n           from employees e\n           join attribute_employee ae on ae.employee_id = e.id\n           join value_attribute_employee vae on vae.attribute_id = ae.id\n           where ae.name_attribute = $1 and vae.value_attribute = $2 limit 1")
    Mono<Employee> findByAttributeNameAndValue(String nameAttribute, String valueAttribute);
}
