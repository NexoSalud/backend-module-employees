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
}
