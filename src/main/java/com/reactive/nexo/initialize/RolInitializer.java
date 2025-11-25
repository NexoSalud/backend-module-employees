package com.reactive.nexo.initialize;

import com.reactive.nexo.model.AttributeEmployee;
import com.reactive.nexo.model.Employee;
import com.reactive.nexo.repository.AttributeEmployeeRepository;
import com.reactive.nexo.repository.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

import com.reactive.nexo.model.Permission;
import com.reactive.nexo.model.Rol;
import com.reactive.nexo.repository.PermissionRepository;
import com.reactive.nexo.repository.RolRepository;

@Component
@Profile("!test")
@Slf4j
public class RolInitializer implements CommandLineRunner {

    @Autowired
    private final RolRepository rolRepository;
    @Autowired
    private final PermissionRepository permissionRepository;

    public RolInitializer(RolRepository rolRepository, PermissionRepository permissionRepository) {
        this.rolRepository =  rolRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(String... args) {
        RolinitialDataSetup();
    }

    private void RolinitialDataSetup() {
        rolRepository.deleteAll()
            .thenMany(permissionRepository.deleteAll())
            .thenMany(Flux.fromIterable(createRoles()))            
            .flatMap(rolRepository::save)
            .collectList()
            .flatMap(savedRol -> {              
                List<Permission> permissions = createPermissions(savedRol.get(0).getId());
                return permissionRepository.saveAll(permissions).collectList(); 
            })
            .subscribe(
                null,
                error -> log.error("RolInitializer - error during data setup: {}", error.getMessage()),
                () -> log.info("RolInitializer - data setup completed successfully")
            );
    }

    private List<Rol> createRoles() {
        return Arrays.asList(
            new Rol(null, "ADMIN"),
            new Rol(null, "DOCTOR")
        );
    }

    private List<Permission> createPermissions(Integer id) {
        if(id == 1){
            return Arrays.asList(
                new Permission(null, 1, "GET", "/api/v1/employees/"),
                new Permission(null, 1, "PUT", "/api/v1/employees/"),
                new Permission(null, 1, "POST", "/api/v1/employees/"),
                new Permission(null, 1, "DELETE", "/api/v1/employees/"),
                new Permission(null, 1, "GET", "/api/v1/users/"),
                new Permission(null, 1, "PUT", "/api/v1/users/"),
                new Permission(null, 1, "POST", "/api/v1/users/"),
                new Permission(null, 1, "DELETE", "/api/v1/users/")
            );
        } else if(id == 2){
            return Arrays.asList(
                new Permission(null, 2, "GET", "/api/v1/users/"),
                new Permission(null, 2, "PUT", "/api/v1/users/"),
                new Permission(null, 2, "POST", "/api/v1/users/"),
                new Permission(null, 2, "DELETE", "/api/v1/users/")
            );
        }   
        return Arrays.asList(); 
    }
}
