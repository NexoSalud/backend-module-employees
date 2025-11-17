package com.reactive.nexo.controller;

import com.reactive.nexo.model.Employee;
import com.reactive.nexo.service.EmployeeService;
import com.reactive.nexo.dto.UserWithAttributesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
@Autowired
private EmployeeService employeeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Employee> create(@RequestBody com.reactive.nexo.dto.CreateUserRequest request){
        // create employee and attributes if provided
        return employeeService.createEmployeeWithAttributes(request);
    }

    @GetMapping
    public Flux<Employee> getAllEmployees(){
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{employeeId}")
    public Mono<ResponseEntity<UserWithAttributesDTO>> getEmployeeById(@PathVariable Integer employeeId){
        Mono<UserWithAttributesDTO> employee = employeeService.getEmployeeWithAttributes(employeeId);
        return employee.map( u -> ResponseEntity.ok(u))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-identification/{identificationType}/{identificationNumber}")
    public Mono<ResponseEntity<UserWithAttributesDTO>> getEmployeeByIdentificationNumber(@PathVariable String identificationType, @PathVariable String identificationNumber){
        Mono<UserWithAttributesDTO> employee = employeeService.getEmployeeWithAttributesByIdentification(identificationType.toUpperCase(), identificationNumber);
        return employee.map( u -> ResponseEntity.ok(u))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{employeeId}")
    public Mono<ResponseEntity<Employee>> updateEmployeeById(@PathVariable Integer employeeId, @RequestBody com.reactive.nexo.dto.CreateUserRequest request){
        return employeeService.updateEmployeeWithAttributes(employeeId, request)
                .map(updatedEmployee -> ResponseEntity.ok(updatedEmployee))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{employeeId}")
    public Mono<ResponseEntity<Void>> deleteEmployeeById(@PathVariable Integer employeeId){
        return employeeService.deleteEmployee(employeeId)
                .map( r -> ResponseEntity.ok().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/search/id")
    public Flux<Employee> fetchEmployeesByIds(@RequestBody List<Integer> ids) {
        return employeeService.fetchEmployees(ids);
    }
}
