package com.reactive.nexo.service;

//import com.reactive.nexo.dto.UserDepartmentDTO;
import com.reactive.nexo.model.AttributeEmployee;
import com.reactive.nexo.model.Employee;
import com.reactive.nexo.model.ValueAttributeEmployee;
import com.reactive.nexo.repository.AttributeEmployeeRepository;
import com.reactive.nexo.repository.EmployeeRepository;
import com.reactive.nexo.repository.ValueAttributeEmployeeRepository;
import com.reactive.nexo.dto.AttributeWithValuesDTO;
import com.reactive.nexo.dto.UserWithAttributesDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.function.BiFunction;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;
import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
@Slf4j
@Transactional
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttributeEmployeeRepository attributeEmployeeRepository;

    @Autowired
    private ValueAttributeEmployeeRepository valueAttributeEmployeeRepository;

    @Autowired
    private com.reactive.nexo.service.ValueAttributeService valueAttributeService;

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Mono<Employee> createEmployee(Employee employee){
        // encode password if provided and not already encoded
        if(employee.getPassword() != null && !isBCrypt(employee.getPassword())){
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        }
        // enforce uniqueness of (identification_type, identification_number)
        return employeeRepository.findByIdentificationTypeAndNumber(employee.getIdentification_type(), employee.getIdentification_number())
                .flatMap(existing -> Mono.<Employee>error(new ResponseStatusException(HttpStatus.CONFLICT, "Employee with same identification already exists")))
                .switchIfEmpty(employeeRepository.save(employee));
    }

    public Flux<Employee> getAllEmployees(){
        return employeeRepository.findAll();
    }

    public Mono<Employee> findById(Integer employeeId){
        return employeeRepository.findById(employeeId);
    }

    public Mono<UserWithAttributesDTO> getEmployeeWithAttributes(Integer employeeId){
    return employeeRepository.findById(employeeId)
        .flatMap(employee ->
            attributeEmployeeRepository.findByEmployeeId(employeeId)
                .flatMap(attribute ->
                    valueAttributeEmployeeRepository.findByAttributeId(attribute.getId())
                        .map(ValueAttributeEmployee::getValueAttribute)
                        .collectList()
                        .map(values -> new AttributeWithValuesDTO(attribute.getName_attribute(), values))
                )
                .collectList()
                .map(attrs -> new UserWithAttributesDTO(employee.getId(), employee.getNames(), employee.getLastnames(), employee.getIdentification_type(), employee.getIdentification_number(), attrs))
        );
    }

    public Mono<Employee> updateEmployee(Integer employeeId,  Employee employee){
        return employeeRepository.findById(employeeId)
                .flatMap(dbEmployee ->
                    // check if another employee already has the requested identification pair
                    employeeRepository.findByIdentificationTypeAndNumber(employee.getIdentification_type(), employee.getIdentification_number())
                        .flatMap(conflict -> {
                            if(conflict.getId().equals(employeeId)){
                                // same record — allow; encode password if present
                                if(employee.getPassword() != null && !isBCrypt(employee.getPassword())){
                                    employee.setPassword(passwordEncoder.encode(employee.getPassword()));
                                }
                                return employeeRepository.save(employee);
                            }
                            return Mono.<Employee>error(new ResponseStatusException(HttpStatus.CONFLICT, "Another employee with same identification exists"));
                        })
                        .switchIfEmpty(employeeRepository.save(employee))
                );
    }

    public Mono<Employee> deleteEmployee(Integer employeeId){
        return employeeRepository.findById(employeeId)
                .flatMap(existingEmployee -> employeeRepository.delete(existingEmployee)
                .then(Mono.just(existingEmployee)));
    }

    public Flux<Employee> findEmployeesByIdentificationNumber(String identificationNumber){
        return employeeRepository.findByIdentificationNumber(identificationNumber);
    }

    public Mono<UserWithAttributesDTO> getEmployeeWithAttributesByIdentification(String identificationType, String identificationNumber){
    return employeeRepository.findByIdentificationTypeAndNumber(identificationType, identificationNumber)
        .flatMap(employee ->
            attributeEmployeeRepository.findByEmployeeId(employee.getId())
                .flatMap(attribute ->
                    valueAttributeEmployeeRepository.findByAttributeId(attribute.getId())
                        .map(ValueAttributeEmployee::getValueAttribute)
                        .collectList()
                        .map(values -> new AttributeWithValuesDTO(attribute.getName_attribute(), values))
                )
                .collectList()
                .map(attrs -> new UserWithAttributesDTO(employee.getId(), employee.getNames(), employee.getLastnames(), employee.getIdentification_type(), employee.getIdentification_number(), attrs))
        );
    }

    public Flux<Employee> fetchEmployees(List<Integer> employeeIds) {
        return Flux.fromIterable(employeeIds)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(i -> findById(i))
                .ordered((u1, u2) -> u2.getId() - u1.getId());
    }

    public Mono<Employee> createEmployeeWithAttributes(com.reactive.nexo.dto.CreateUserRequest request){
        Employee toSave = new Employee(null, request.getNames(), request.getLastnames(), request.getIdentification_type(), request.getIdentification_number());
        // include and encode password if provided
        if(request.getPassword() != null){
            if(!isBCrypt(request.getPassword())){
                toSave.setPassword(passwordEncoder.encode(request.getPassword()));
            } else {
                toSave.setPassword(request.getPassword());
            }
        }
        return createEmployee(toSave).flatMap(savedEmployee -> {
            Map<String, List<String>> attrs = request.getAttributes();
            if(attrs == null || attrs.isEmpty()){
                return Mono.just(savedEmployee);
            }
            return Flux.fromIterable(attrs.entrySet())
                    .flatMap(e -> {
                        String attrName = e.getKey();
                        List<String> values = e.getValue() == null ? Collections.emptyList() : e.getValue();
            AttributeEmployee attr = new AttributeEmployee(null, attrName, values.size() > 1, savedEmployee.getId());
            return attributeEmployeeRepository.save(attr)
                .flatMap(savedAttr -> Flux.fromIterable(values)
                    .flatMap(v -> valueAttributeService.saveValue(new ValueAttributeEmployee(null, savedAttr.getId(), v)))
                    .then(Mono.just(savedAttr)));
                    })
                    .collectList()
                    .then(Mono.just(savedEmployee));
        });
    }

        private boolean isBCrypt(String s){
            if(s == null) return false;
            return s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$");
        }

    public Mono<Employee> updateEmployeeWithAttributes(Integer employeeId, com.reactive.nexo.dto.CreateUserRequest request){
        return employeeRepository.findById(employeeId)
                .flatMap(dbEmployee ->
                    // check identification uniqueness
                    employeeRepository.findByIdentificationTypeAndNumber(request.getIdentification_type(), request.getIdentification_number())
                        .flatMap(conflict -> {
                log.info("updateEmployeeWithAttributes - found employee by identification: id={} for type={} number={} (updating employeeId={})",
                    conflict.getId(), request.getIdentification_type(), request.getIdentification_number(), employeeId);
                if(conflict.getId().equals(employeeId)){
                                dbEmployee.setNames(request.getNames());
                                dbEmployee.setLastnames(request.getLastnames());
                                dbEmployee.setIdentification_type(request.getIdentification_type());
                                dbEmployee.setIdentification_number(request.getIdentification_number());
                                return employeeRepository.save(dbEmployee);
                            }
                            log.info("updateEmployeeWithAttributes - conflict with other employee id={}", conflict.getId());
                            return Mono.<Employee>error(new ResponseStatusException(HttpStatus.CONFLICT, "Another employee with same identification exists"));
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            dbEmployee.setNames(request.getNames());
                            dbEmployee.setLastnames(request.getLastnames());
                            dbEmployee.setIdentification_type(request.getIdentification_type());
                            dbEmployee.setIdentification_number(request.getIdentification_number());
                            return employeeRepository.save(dbEmployee);
                        }))
                ).flatMap(savedEmployee -> {
                    Map<String, List<String>> attrs = request.getAttributes();
                    final Map<String, List<String>> attrsLocal = (attrs == null) ? Collections.emptyMap() : attrs;

                            // upsert provided attributes using a single safe MERGE (upsert) then load the attribute id
                            Mono<Void> upserts = Flux.fromIterable(attrsLocal.entrySet())
                                    .concatMap(e -> {
                                        String name = e.getKey();
                                        List<String> values = e.getValue() == null ? Collections.emptyList() : e.getValue();
                                        log.info("updateEmployeeWithAttributes - upserting attribute name='{}' values={} for employeeId={}", name, values, savedEmployee.getId());

                                        // Use repository MERGE to avoid duplicate insert races. After MERGE, fetch the attribute
                                        // and then replace/insert values as required.
                                                                return attributeEmployeeRepository.upsertByEmployeeIdAndName(savedEmployee.getId(), name, values.size() > 1)
                                                                    .then(attributeEmployeeRepository.findByEmployeeIdAndName(savedEmployee.getId(), name))
                                                .flatMap(foundAttr -> {
                                                    log.info("updateEmployeeWithAttributes - attribute id={} ready for values update", foundAttr.getId());
                                                    // delete existing values then insert new ones (replacement semantics for non-multiple)
                            return valueAttributeEmployeeRepository.findByAttributeId(foundAttr.getId())
                                .flatMap(valueAttributeEmployeeRepository::delete)
                                .thenMany(Flux.fromIterable(values))
                                .flatMap(v -> valueAttributeService.saveValue(new ValueAttributeEmployee(null, foundAttr.getId(), v)))
                                .then();
                                                });
                                    })
                                    .then();

                    // delete attributes that are not present in request
    Mono<Void> deletions = attributeEmployeeRepository.findByEmployeeId(savedEmployee.getId())
        .filter(a -> !attrsLocal.containsKey(a.getName_attribute()))
                .flatMap(a -> valueAttributeEmployeeRepository.findByAttributeId(a.getId()).flatMap(valueAttributeEmployeeRepository::delete).then(attributeEmployeeRepository.delete(a)))
                .then();
                    
            // Note: the above deletion chain referenced attributeEmployeeRepository and valueAttributeEmployeeRepository
            // but the inner delete call used the old repository names; fix now by delegating properly below.

                    // Run upserts first, then deletions sequentially to avoid races where
                    // a deletion may remove a just-created attribute and cause a duplicate
                    // insert attempt. Doing them sequentially ensures stable, idempotent
                    // upsert behavior for each provided attribute.
                    return upserts.then(deletions).then(Mono.just(savedEmployee));
                });
    }
}
