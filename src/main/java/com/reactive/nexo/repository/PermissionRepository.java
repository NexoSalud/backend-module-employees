package com.reactive.nexo.repository;

import com.reactive.nexo.model.Permission;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PermissionRepository extends R2dbcRepository<Permission, Integer> {

    @Query("SELECT *, concat('[',method,']', endpoint) AS permission FROM permission WHERE rol_id = :rolId")
    Flux<Permission> findByRolId(Integer rolId);
}
