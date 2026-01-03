package com.reactive.nexo.repository;

import com.reactive.nexo.model.Rol;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RolRepository extends ReactiveCrudRepository<Rol, Integer> {

    @Query("SELECT id, name FROM rol WHERE name like $1")
    Mono<Rol> findByName(String name);
}
