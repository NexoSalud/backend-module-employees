package com.reactive.nexo.repository;

import com.reactive.nexo.model.Permiso;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PermisoRepository extends R2dbcRepository<Permiso, Integer> {

    @Query("SELECT * FROM permiso WHERE rol_id = :rolId")
    Flux<Permiso> findByRolId(Integer rolId);
}
