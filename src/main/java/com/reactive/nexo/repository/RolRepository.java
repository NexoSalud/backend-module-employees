package com.reactive.nexo.repository;

import com.reactive.nexo.model.Rol;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RolRepository extends R2dbcRepository<Rol, Integer> {

    Mono<Rol> findByNombre(String nombre);
}
