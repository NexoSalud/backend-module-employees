package com.reactive.nexo.controller;

import com.reactive.nexo.dto.RolWithPermissionDTO;
import com.reactive.nexo.model.Permission;
import com.reactive.nexo.model.Rol;
import com.reactive.nexo.service.RolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/rols")
@Slf4j
public class RolController {

    @Autowired
    private RolService rolService;

    /**
     * GET /api/v1/rols — lista todos los roles
     */
    @GetMapping
    public Flux<Rol> getAllRoles() {
        log.info("GET /api/v1/rols - fetching all roles");
        return rolService.getAllRoles();
    }

    /**
     * GET /api/v1/rols/{rolId} — obtiene un rol con todos sus permissions
     * Response:
     * {
     *     "id": 1,
     *     "nombre": "Admin",
     *     "permissions": ["crear", "editar", "eliminar", ...]
     * }
     */
    @GetMapping("/{rolId}")
    public Mono<ResponseEntity<RolWithPermissionDTO>> getRolWithPermissions(@PathVariable Integer rolId) {
        log.info("GET /api/v1/rols/{} - fetching role with permissions", rolId);
        return rolService.getRolWithPermissions(rolId)
            .map(ResponseEntity::ok)
            .onErrorResume(error -> {
                log.error("Error fetching rol: {}", error.getMessage());
                return Mono.just(ResponseEntity.notFound().build());
            });
    }

    /**
     * POST /api/v1/rols — crea un nuevo rol
     */
    @PostMapping
    public Mono<ResponseEntity<Rol>> createRol(@RequestBody Rol rol) {
        log.info("POST /api/v1/rols - creating new rol with name={}", rol.getName());
        return rolService.createRol(rol)
            .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
            .onErrorResume(error -> {
                log.error("Error creating rol: {}", error.getMessage());
                return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
            });
    }

    /**
     * PUT /api/v1/rols/{rolId} — actualiza un rol
     */
    @PutMapping("/{rolId}")
    public Mono<ResponseEntity<Rol>> updateRol(@PathVariable Integer rolId, @RequestBody Rol rol) {
        log.info("PUT /api/v1/rols/{} - updating role", rolId);
        return rolService.updateRol(rolId, rol)
            .map(ResponseEntity::ok)
            .onErrorResume(error -> {
                log.error("Error updating rol: {}", error.getMessage());
                return Mono.just(ResponseEntity.notFound().build());
            });
    }

    /**
     * DELETE /api/v1/rols/{rolId} — elimina un rol
     */
    @DeleteMapping("/{rolId}")
    public Mono<ResponseEntity<Void>> deleteRol(@PathVariable Integer rolId) {
        log.info("DELETE /api/v1/rols/{} - deleting role", rolId);
        return rolService.deleteRol(rolId)
            .then(Mono.just(ResponseEntity.ok().<Void>build()))
            .onErrorResume(error -> {
                log.error("Error deleting rol: {}", error.getMessage());
                return Mono.just(ResponseEntity.notFound().build());
            });
    }

    /**
     * POST /api/v1/rols/{rolId}/permission — crea un permission para un rol
     */
    @PostMapping("/{rolId}/permission")
    public Mono<ResponseEntity<Permission>> createPermission(@PathVariable Integer rolId, @RequestBody Permission permission) {
        log.info("POST /api/v1/rols/{}/permission - creating permission endpoint={} method={}", rolId, permission.getEndpoint(), permission.getMethod());
        permission.setRol_id(rolId);
        return rolService.createPermission(permission)
            .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
            .onErrorResume(error -> {
                log.error("Error creating permission: {}", error.getMessage());
                return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
            });
    }

    /**
     * DELETE /api/v1/rols/{rolId}/permission/{permissionId} — elimina un permission
     */
    @DeleteMapping("/{rolId}/permission/{permissionId}")
    public Mono<ResponseEntity<Void>> deletePermission(@PathVariable Integer rolId, @PathVariable Integer permissionId) {
        log.info("DELETE /api/v1/rols/{}/permission/{} - deleting permission", rolId, permissionId);
        return rolService.deletePermission(permissionId)
            .then(Mono.just(ResponseEntity.ok().<Void>build()))
            .onErrorResume(error -> {
                log.error("Error deleting permission: {}", error.getMessage());
                return Mono.just(ResponseEntity.notFound().build());
            });
    }
}
