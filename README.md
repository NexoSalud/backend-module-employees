
# reactive-nexo — Backend (API reactiva con Spring WebFlux y R2DBC)

Proyecto NEXO — módulo de empleados (API reactiva construida con Spring WebFlux y Spring Data R2DBC).

Estado y stack
+- Paquete raíz de la aplicación: `com.reactive.nexo`.
+- Stack: Spring Boot 3.x, Spring WebFlux, Spring Data R2DBC.
+- Base de datos para tests: H2 (R2DBC). En producción puedes usar Postgres u otro driver R2DBC configurando `application.yml`.

Requisitos
+- Java 17 o superior (en CI local se ha usado Java 19 para pruebas). Maven 3.6+.

Documentación OpenAPI / Swagger
+- Swagger UI (si la app se inicia en el puerto 8080):
  - http://localhost:8080/swagger-ui.html
  - http://localhost:8080/swagger-ui/index.html
+- OpenAPI JSON: http://localhost:8080/v3/api-docs
+- OpenAPI YAML:  http://localhost:8080/v3/api-docs.yaml

Cómo compilar, testear y ejecutar

1) Ejecutar la suite de tests (usa H2, perfil `test`):
```bash
mvn clean test
```

2) Empaquetar la aplicación (genera JAR en `target/`):
```bash
mvn clean package
```

3) Ejecutar el JAR generado (ejemplo):
```bash
java -jar target/reactive-nexo-0.0.1-SNAPSHOT.jar
```

Nota: la conexión a BD se configura en `src/main/resources/application.yml`. Ajusta el perfil (por ejemplo a `prod` o `dev`) y las propiedades para usar Postgres u otro R2DBC driver.

Resumen de funcionalidades principales

+- Entidades principales renombradas a `Employee` (antes `User`). Endpoints base ahora expuestos bajo `/api/v1/employees`.
+- Soporte de atributos y valores asociados a empleados (modelo reactivo): atributos con valores múltiples o únicos.
+- Nuevo módulo: roles y permisos
  - Tabla `rol` (id, nombre)
  - Tabla `permiso` (id, rol_id, permiso)
  - Campo `rol_id` añadido a `employees` para asociar rol a empleado
  - Endpoint principal: `GET /api/v1/rols/{rolId}` devuelve el rol con la lista de permisos (array de strings)

Endpoints importantes

+- Empleados:
  - GET  /api/v1/employees            — Listar empleados (Flux)
  - GET  /api/v1/employees/{id}       — Obtener empleado por id (Mono)
  - POST /api/v1/employees            — Crear empleado
  - PUT  /api/v1/employees/{id}       — Actualizar empleado
  - DELETE /api/v1/employees/{id}     — Eliminar empleado
  - GET  /api/v1/employees/by-identification/{type}/{number} — Buscar por identificación

+- Roles / Permisos:
  - GET  /api/v1/rols                 — Listar roles
  - GET  /api/v1/rols/{rolId}         — Obtener rol con permisos (respuesta: { id, nombre, permisos: ["p1","p2"] })
  - POST /api/v1/rols                 — Crear rol
  - PUT  /api/v1/rols/{rolId}         — Actualizar rol
  - DELETE /api/v1/rols/{rolId}       — Eliminar rol
  - POST /api/v1/rols/{rolId}/permisos — Añadir permiso a rol
  - DELETE /api/v1/rols/{rolId}/permisos/{permisoId} — Eliminar permiso

Formato de respuestas (ejemplo rol con permisos)

```json
{
  "id": 1,
  "nombre": "Admin",
  "permisos": ["crear","editar","eliminar"]
}
```

Cómo probar rápidamente (ejemplos)

 - Obtener rol y permisos (curl):
```bash
curl -s http://localhost:8080/api/v1/rols/1 | jq .
```

 - Crear rol:
```bash
curl -s -X POST http://localhost:8080/api/v1/rols \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Admin"}' | jq .
```

Datos de ejemplo y seed
- El inicializador de datos (cuando se ejecuta fuera de `test`) puede insertar empleados de ejemplo, atributos y valores.

Tests y notas de desarrollo
- La suite de tests está configurada para ejecutar con H2 via R2DBC (perfil `test`). Ejecuta `mvn test` para lanzar la suite localmente.
- Durante el desarrollo se han corregido y adaptado tests para mantener un contexto limpio entre tests; la suite de pruebas del repositorio actual pasa (9 tests verdes en el entorno donde se ejecutaron).

Build artefacto
- JAR producido: `target/reactive-nexo-0.0.1-SNAPSHOT.jar` (ver `target/` después de `mvn package`).

Notas rápidas para mantener/expandir
- Si agregas nuevas columnas con `snake_case`, ten en cuenta las convenciones de Spring Data R2DBC al nombrar métodos en repositorios (puede ser necesario usar `@Column` o `@Query`).
- Para semilla/fixtures más robustos, considerar usar Flyway/Liquibase para migraciones y un `data.sql` separado para seeds.

¿Quieres que añada ejemplos de curl más completos, un script de `seed` o un Postman collection? Puedo generarlos y añadirlos al repositorio.
