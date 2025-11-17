package com.reactive.nexo.repository;

import com.reactive.nexo.model.ValueAttributeEmployee;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ValueAttributeEmployeeRepository extends ReactiveCrudRepository<ValueAttributeEmployee,Integer> {
    Flux<ValueAttributeEmployee> findByAttributeId(Integer attributeId);
}
