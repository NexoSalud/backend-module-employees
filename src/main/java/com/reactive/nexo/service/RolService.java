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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    /*public Mono<RolWithPermissionDTO> getRolWithPermissions(Integer rolId) {
        log.info("getRolWithPermissions - fetching role with permissions, rolId={}", rolId);
        return rolRepository.findById(rolId)
            .flatMap(rol -> 
                permissionRepository.findByRolId(rolId)
                    .map(Permission::getMethod)
                    .collectList()
                    .map(permissions -> new RolWithPermissionDTO(rol.getId(), rol.getNombre(), permissions))
            )
            .doOnSuccess(result -> log.info("getRolWithPermissions - successfully fetched role with {} permissions", 
                result.getPermission() != null ? result.getPermission().size() : 0))
            .doOnError(error -> log.error("getRolWithPermissions - error fetching role: {}", error.getMessage()));
    }*/

    public Mono<RolWithPermissionDTO> getRolWithPermissions(Integer rolId) {
        log.info("getRolWithPermissions - fetching role with permissions, rolId={}", rolId);

        Mono<Rol> rolMono = rolRepository.findById(rolId);
        
        // Creamos un Mono que agrupa todos los permisos por método (GET -> [e1, e2], POST -> [e3])
        Mono<Map<String, List<String>>> permissionsMapMono = permissionRepository.findByRolId(rolId)
            .collectMultimap(Permission::getMethod, Permission::getEndpoint)
            // Convertimos Multimap (que usa Collection) a un Map estándar (que usa List)
            .map(multimap -> multimap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> List.copyOf(entry.getValue())))
            );

        // Combinamos el Rol y el Mapa de Permisos cuando ambos estén listos
        return Mono.zip(rolMono, permissionsMapMono)
            .map(tuple -> {
                Rol rol = tuple.getT1();
                Map<String, List<String>> permissionMap = tuple.getT2();

                // Transformamos el mapa a la lista de objetos JSON [{Method: [Endpoint]}, ...]
                List<Map<String, List<String>>> formattedPermissions = permissionMap.entrySet().stream()
                    .map(entry -> Collections.singletonMap(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

                return new RolWithPermissionDTO(rol.getId(), rol.getName(), formattedPermissions);
            })
            .doOnSuccess(result -> log.info("getRolWithPermissions - successfully fetched role with {} method groups", 
                result.getPermissions() != null ? result.getPermissions().size() : 0))
            .doOnError(error -> log.error("getRolWithPermissions - error fetching role: {}", error.getMessage()));
    }
    /**
     * Crear un nuevo rol
     */
    public Mono<Rol> createRol(Rol rol) {
        log.info("createRol - creating new role with name={}", rol.getName());
        return rolRepository.save(rol)
            .doOnSuccess(saved -> log.info("createRol - role created with id={}", saved.getId()))
            .doOnError(error -> log.error("createRol - error creating role: {}", error.getMessage()));
    }

    /**
     * Crear un permission para un rol
     */
    public Mono<Permission> createPermission(Permission permission) {
        log.info("createPermission - creating permission={} for rolId={}", permission.getRol_id());
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
                existing.setName(rol.getName());
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
