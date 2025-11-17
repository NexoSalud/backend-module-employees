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

@Component
@Profile("!test")
@Slf4j
public class EmployeeInitializer implements CommandLineRunner {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttributeEmployeeRepository attributeEmployeeRepository;
    @Autowired
    private com.reactive.nexo.repository.ValueAttributeEmployeeRepository valueAttributeEmployeeRepository;

    @Autowired
    private com.reactive.nexo.service.ValueAttributeService valueAttributeService;
    
    @Override
    public void run(String... args) {
            initialDataSetup();
    }

    private List<Employee> getData(){
        return Arrays.asList(new Employee(null,"Juan","Campo","CC","1"),
                             new Employee(null,"Elieser","Banguero","CC","2"),
                             new Employee(null,"Migel","Caicedo","CC","3"),
                             new Employee(null,"Huber","Guaza","CC","4"),
                             new Employee(null,"Kenner","Zambrano","CC","5"),
                             new Employee(null,"Yeider","Caicedo","CC","6"),
                             new Employee(null,"Jhordy","Abonia","CC","7"));
    }

    private void initialDataSetup() {
        employeeRepository.deleteAll()
                .thenMany(Flux.fromIterable(getData()))
                .flatMap(employeeRepository::save)
                .collectList()
                .flatMap(savedEmployees -> {
                    // create attributes for each saved employee
                    List<AttributeEmployee> attrs = new java.util.ArrayList<>();
                    for(com.reactive.nexo.model.Employee e : savedEmployees){
                        attrs.add(new AttributeEmployee(null,"fecha de nacimiento",false, e.getId()));
                        attrs.add(new AttributeEmployee(null,"lugar de nacimiento ciudad",false,  e.getId()));
                        attrs.add(new AttributeEmployee(null,"lugar de nacimiento departamento",false,  e.getId()));
                        attrs.add(new AttributeEmployee(null,"lugar de nacimiento pais",false,  e.getId()));
                        attrs.add(new AttributeEmployee(null,"ubicacion ciudad",false,  e.getId()));
                        attrs.add(new AttributeEmployee(null,"ubicacion departamento",false,  e.getId()));
                        attrs.add(new AttributeEmployee(null,"ubicacion pais",false,  e.getId()));
                        attrs.add(new AttributeEmployee(null,"entidad de salud",false,  e.getId()));
                        attrs.add(new AttributeEmployee(null,"ultima consulta",false,  e.getId()));
                        attrs.add(new AttributeEmployee(null,"telefono",false,  e.getId()));
                        attrs.add(new AttributeEmployee(null,"email",false,  e.getId()));
                        attrs.add(new AttributeEmployee(null,"regimen",false,  e.getId()));
                        // clinical history example attributes
                        attrs.add(new AttributeEmployee(null,"historia_clinica_numero",false,  e.getId()));
                        attrs.add(new AttributeEmployee(null,"diagnostico_principal",false,  e.getId()));
                        attrs.add(new AttributeEmployee(null,"alergias",true,  e.getId()));
                    }
                    return attributeEmployeeRepository.saveAll(Flux.fromIterable(attrs)).collectList();
                })
                .flatMap(savedAttrs -> {
                    // create value entries for each attribute saved
                    List<com.reactive.nexo.model.ValueAttributeEmployee> vals = new java.util.ArrayList<>();
                    for(com.reactive.nexo.model.AttributeEmployee a : savedAttrs){
                        String attr = a.getName_attribute();
                        String val;
                        switch(attr){
                            case "fecha de nacimiento": val = "1992-05-06"; break;
                            case "lugar de nacimiento ciudad": val = "cali"; break;
                            case "lugar de nacimiento departamento": val = "valle"; break;
                            case "lugar de nacimiento pais": val = "colombia"; break;
                            case "ubicacion ciudad": val = "guachene"; break;
                            case "ubicacion departamento": val = "cauca"; break;
                            case "ubicacion pais": val = "colombia"; break;
                            case "entidad de salud": val = "sura"; break;
                            case "ultima consulta": val = "2024-06-06"; break;
                            case "telefono": val = "315-000-0000"; break;
                            case "email": val = "jhon-doe@test.co"; break;
                            case "regimen": val = "subcidiado"; break;
                            case "historia_clinica_numero": val = "HC-1000" + a.getId(); break;
                            case "diagnostico_principal": val = "Hipertension"; break;
                            case "alergias": val = "Ninguna"; break;
                            default: val = "";
                        }
                        vals.add(new com.reactive.nexo.model.ValueAttributeEmployee(null, a.getId(), val));
                    }
                    // use service to enforce 'multiple' rule per attribute
                    return Flux.fromIterable(vals)
                            .flatMap(v -> valueAttributeService.saveValue(v))
                            .collectList();
                })
                .thenMany(employeeRepository.findAll())
                .subscribe(employee -> {
                    log.info("Employee Inserted from CommandLineRunner " + employee);
                });
    }

}
