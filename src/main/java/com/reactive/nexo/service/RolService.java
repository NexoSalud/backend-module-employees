package com.reactive.nexo.service;

import com.reactive.nexo.dto.RolWithPermissionDTO;
import com.reactive.nexo.model.Permission;
import com.reactive.nexo.model.Rol;
import com.reactive.nexo.repository.PermissionRepository;
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
    private PermissionRepository permissionRepository;

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
     * Obtener un rol con todos sus permissions por ID
     */
    public Mono<RolWithPermissionDTO> getRolWithPermissions(Integer rolId) {
        log.info("getRolWithPermissions - fetching role with permissions, rolId={}", rolId);
        return rolRepository.findById(rolId)
            .flatMap(rol -> 
                permissionRepository.findByRolId(rolId)
                    .map(Permission::getPermission)
                    .collectList()
                    .map(permissions -> new RolWithPermissionDTO(rol.getId(), rol.getNombre(), permissions))
            )
            .doOnSuccess(result -> log.info("getRolWithPermissions - successfully fetched role with {} permissions", 
                result.getPermission() != null ? result.getPermission().size() : 0))
            .doOnError(error -> log.error("getRolWithPermissions - error fetching role: {}", error.getMessage()));
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
     * Crear un permission para un rol
     */
    public Mono<Permission> createPermission(Permission permission) {
        log.info("createPermission - creating permission={} for rolId={}", permission.getPermission(), permission.getRol_id());
        return permissionRepository.save(permission)
            .doOnSuccess(saved -> log.info("createPermission - permission created with id={}", saved.getId()))
            .doOnError(error -> log.error("createPermission - error creating permission: {}", error.getMessage()));
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
     * Eliminar un permission
     */
    public Mono<Void> deletePermission(Integer permissionId) {
        log.info("deletePermission - deleting permission id={}", permissionId);
        return permissionRepository.deleteById(permissionId)
            .doOnSuccess(v -> log.info("deletePermission - permission deleted"))
            .doOnError(error -> log.error("deletePermission - error deleting permission: {}", error.getMessage()));
    }
}
