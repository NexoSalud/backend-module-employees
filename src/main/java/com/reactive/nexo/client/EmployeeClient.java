package com.reactive.nexo.client;

import com.reactive.nexo.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class EmployeeClient {
    private WebClient client = WebClient.create("http://localhost:8080");
    public Mono<Employee> getEmployee(String userId){
       return client.get()
                .uri("/employees/{userId}", userId)
                .retrieve()
                .bodyToMono(Employee.class).log(" Employee fetched ");
    }

    public Mono<Employee> getEmployeeByIdentificationNumber(String identificationNumber){
       return client.get()
                .uri("/employees-by-identification/{identificationNumber}", identificationNumber)
                .retrieve()
                .bodyToMono(Employee.class).log(" Employee fetched ");
    }

    public Flux<Employee> getAllEmployees(){
        return client.get()
                .uri("/employees")
                .retrieve()
                .bodyToFlux(Employee.class)
                .log("Employees Fetched : ");
    }

    public Mono<Employee> createEmployee(Employee employee){
        Mono<Employee> userMono = Mono.just(employee);
        return client.post().uri("/employees").contentType(MediaType.APPLICATION_JSON)
                .body(userMono,Employee.class).retrieve().bodyToMono(Employee.class).log("Created Employee : ");

    }


}
