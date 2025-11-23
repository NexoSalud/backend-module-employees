package com.reactive.nexo.controller;

import com.reactive.nexo.model.Employee;
import com.reactive.nexo.service.EmployeeService;
import com.reactive.nexo.dto.EmployeeWithAttributesDTO;
import com.reactive.nexo.dto.AuthRequest;
import com.reactive.nexo.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.reactive.nexo.dto.AuthResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
@Autowired
private EmployeeService employeeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Employee> create(@RequestBody com.reactive.nexo.dto.CreateEmployeeRequest request){
        // create employee and attributes if provided
        return employeeService.createEmployeeWithAttributes(request);
    }

    @GetMapping
    public Flux<Employee> getAllEmployees(){
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{employeeId}")
    public Mono<ResponseEntity<EmployeeWithAttributesDTO>> getEmployeeById(@PathVariable Integer employeeId){
        Mono<EmployeeWithAttributesDTO> employee = employeeService.getEmployeeWithAttributes(employeeId);
        return employee.map( u -> {u.setPassword(null); return ResponseEntity.ok(u);})
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-identification/{identificationType}/{identificationNumber}")
    public Mono<ResponseEntity<EmployeeWithAttributesDTO>> getEmployeeByIdentificationNumber(@PathVariable String identificationType, @PathVariable String identificationNumber){
        Mono<EmployeeWithAttributesDTO> employee = employeeService.getEmployeeWithAttributesByIdentification(identificationType.toUpperCase(), identificationNumber);
        return employee.map( u -> {u.setPassword(null); return ResponseEntity.ok(u);})
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{employeeId}")
    public Mono<ResponseEntity<Employee>> updateEmployeeById(@PathVariable Integer employeeId, @RequestBody com.reactive.nexo.dto.CreateEmployeeRequest request){
        return employeeService.updateEmployeeWithAttributes(employeeId, request)
                .map(updatedEmployee -> { updatedEmployee.setPassword(null); return ResponseEntity.ok(updatedEmployee);})
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @PatchMapping("/{employeeId}")
    public Mono<ResponseEntity<Employee>> patchEmployeeById(@PathVariable Integer employeeId, @RequestBody com.reactive.nexo.dto.CreateEmployeeRequest request){
        return employeeService.partialUpdateEmployee(employeeId, request)
                .map(updatedEmployee ->{ updatedEmployee.setPassword(null); return ResponseEntity.ok(updatedEmployee);})
                .onErrorResume(err -> Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
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
    /**
     * POST /api/v1/employees/authenticate - Authenticate an employee
     * Used by the session module to validate credentials and get roles/permissions
     */
    @PostMapping("/authenticate")
    public Mono<ResponseEntity<AuthResponse>> authenticate(@RequestBody AuthRequest request) {
        return employeeService.authenticate(request)
                .map(authResponse -> ResponseEntity.status(HttpStatus.OK).body(authResponse))
                .onErrorResume(err -> {
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                });
    }
}

