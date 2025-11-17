package com.reactive.nexo.controller;

import com.reactive.nexo.dto.RolWithPermisosDTO;
import com.reactive.nexo.model.Permiso;
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
     * GET /api/v1/rols/{rolId} — obtiene un rol con todos sus permisos
     * Response:
     * {
     *     "id": 1,
     *     "nombre": "Admin",
     *     "permisos": ["crear", "editar", "eliminar", ...]
     * }
     */
    @GetMapping("/{rolId}")
    public Mono<ResponseEntity<RolWithPermisosDTO>> getRolWithPermisos(@PathVariable Integer rolId) {
        log.info("GET /api/v1/rols/{} - fetching role with permisos", rolId);
        return rolService.getRolWithPermisos(rolId)
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
        log.info("POST /api/v1/rols - creating new rol with nombre={}", rol.getNombre());
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
     * POST /api/v1/rols/{rolId}/permisos — crea un permiso para un rol
     */
    @PostMapping("/{rolId}/permisos")
    public Mono<ResponseEntity<Permiso>> createPermiso(@PathVariable Integer rolId, @RequestBody Permiso permiso) {
        log.info("POST /api/v1/rols/{}/permisos - creating permiso={}", rolId, permiso.getPermiso());
        permiso.setRol_id(rolId);
        return rolService.createPermiso(permiso)
            .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
            .onErrorResume(error -> {
                log.error("Error creating permiso: {}", error.getMessage());
                return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
            });
    }

    /**
     * DELETE /api/v1/rols/{rolId}/permisos/{permisoId} — elimina un permiso
     */
    @DeleteMapping("/{rolId}/permisos/{permisoId}")
    public Mono<ResponseEntity<Void>> deletePermiso(@PathVariable Integer rolId, @PathVariable Integer permisoId) {
        log.info("DELETE /api/v1/rols/{}/permisos/{} - deleting permiso", rolId, permisoId);
        return rolService.deletePermiso(permisoId)
            .then(Mono.just(ResponseEntity.ok().<Void>build()))
            .onErrorResume(error -> {
                log.error("Error deleting permiso: {}", error.getMessage());
                return Mono.just(ResponseEntity.notFound().build());
            });
    }
}
