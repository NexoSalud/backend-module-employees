package com.reactive.nexo.service;

import com.reactive.nexo.dto.RolWithPermisosDTO;
import com.reactive.nexo.model.Permiso;
import com.reactive.nexo.model.Rol;
import com.reactive.nexo.repository.PermisoRepository;
import com.reactive.nexo.repository.RolRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PermisoRepository permisoRepository;

    /**
     * Obtener todos los roles
     */
    public Flux<Rol> getAllRoles() {
        log.info("getAllRoles - fetching all roles");
        return rolRepository.findAll();
    }

    /**
     * Obtener un rol por ID
     */
    public Mono<Rol> getRolById(Integer rolId) {
        log.info("getRolById - fetching role with id={}", rolId);
        return rolRepository.findById(rolId);
    }

    /**
     * Obtener un rol con todos sus permisos por ID
     */
    public Mono<RolWithPermisosDTO> getRolWithPermisos(Integer rolId) {
        log.info("getRolWithPermisos - fetching role with permisos, rolId={}", rolId);
        return rolRepository.findById(rolId)
            .flatMap(rol -> 
                permisoRepository.findByRolId(rolId)
                    .map(Permiso::getPermiso)
                    .collectList()
                    .map(permisos -> new RolWithPermisosDTO(rol.getId(), rol.getNombre(), permisos))
            )
            .doOnSuccess(result -> log.info("getRolWithPermisos - successfully fetched role with {} permisos", 
                result.getPermisos() != null ? result.getPermisos().size() : 0))
            .doOnError(error -> log.error("getRolWithPermisos - error fetching role: {}", error.getMessage()));
    }

    /**
     * Crear un nuevo rol
     */
    public Mono<Rol> createRol(Rol rol) {
        log.info("createRol - creating new role with nombre={}", rol.getNombre());
        return rolRepository.save(rol)
            .doOnSuccess(saved -> log.info("createRol - role created with id={}", saved.getId()))
            .doOnError(error -> log.error("createRol - error creating role: {}", error.getMessage()));
    }

    /**
     * Crear un permiso para un rol
     */
    public Mono<Permiso> createPermiso(Permiso permiso) {
        log.info("createPermiso - creating permiso={} for rolId={}", permiso.getPermiso(), permiso.getRol_id());
        return permisoRepository.save(permiso)
            .doOnSuccess(saved -> log.info("createPermiso - permiso created with id={}", saved.getId()))
            .doOnError(error -> log.error("createPermiso - error creating permiso: {}", error.getMessage()));
    }

    /**
     * Actualizar un rol
     */
    public Mono<Rol> updateRol(Integer rolId, Rol rol) {
        log.info("updateRol - updating role id={}", rolId);
        return rolRepository.findById(rolId)
            .flatMap(existing -> {
                existing.setNombre(rol.getNombre());
                return rolRepository.save(existing);
            })
            .doOnSuccess(updated -> log.info("updateRol - role updated"))
            .doOnError(error -> log.error("updateRol - error updating role: {}", error.getMessage()));
    }

    /**
     * Eliminar un rol
     */
    public Mono<Void> deleteRol(Integer rolId) {
        log.info("deleteRol - deleting role id={}", rolId);
        return rolRepository.deleteById(rolId)
            .doOnSuccess(v -> log.info("deleteRol - role deleted"))
            .doOnError(error -> log.error("deleteRol - error deleting role: {}", error.getMessage()));
    }

    /**
     * Eliminar un permiso
     */
    public Mono<Void> deletePermiso(Integer permisoId) {
        log.info("deletePermiso - deleting permiso id={}", permisoId);
        return permisoRepository.deleteById(permisoId)
            .doOnSuccess(v -> log.info("deletePermiso - permiso deleted"))
            .doOnError(error -> log.error("deletePermiso - error deleting permiso: {}", error.getMessage()));
    }
}
