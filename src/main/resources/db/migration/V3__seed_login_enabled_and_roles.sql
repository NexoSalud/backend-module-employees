-- ============================================================
-- V3: Seed inicial para ambiente de producción
-- 1. Roles y permisos base (idempotente)
-- 2. loginEnabled=true para todos los empleados con password+rol
-- ============================================================

-- Roles base
INSERT INTO rol (name, asistencial) VALUES
    ('ADMIN',   false),
    ('DOCTOR',  true),
    ('FACTURADOR', false),
    ('ENFERMERO', true),
    ('RECEPCIONISTA', false)
ON CONFLICT (name) DO NOTHING;

-- Permisos base para ADMIN (rol_id=1)
INSERT INTO permission (rol_id, method, endpoint)
SELECT r.id, p.method, p.endpoint
FROM rol r
CROSS JOIN (VALUES
    ('GET',    '/api/v1/tracking'),
    ('GET',    '/api/v1/schedule'),
    ('PUT',    '/api/v1/schedule'),
    ('POST',   '/api/v1/schedule'),
    ('DELETE', '/api/v1/schedule'),
    ('GET',    '/api/v1/medical-agenda'),
    ('PUT',    '/api/v1/medical-agenda'),
    ('PATCH',  '/api/v1/medical-agenda'),
    ('POST',   '/api/v1/medical-agenda'),
    ('DELETE', '/api/v1/medical-agenda'),
    ('GET',    '/api/v1/appointments'),
    ('PUT',    '/api/v1/appointments'),
    ('POST',   '/api/v1/appointments'),
    ('PATCH',  '/api/v1/appointments'),
    ('DELETE', '/api/v1/appointments'),
    ('GET',    '/api/v1/rols'),
    ('PUT',    '/api/v1/rols'),
    ('POST',   '/api/v1/rols'),
    ('DELETE', '/api/v1/rols'),
    ('GET',    '/api/v1/employees'),
    ('PUT',    '/api/v1/employees'),
    ('PATCH',  '/api/v1/employees'),
    ('POST',   '/api/v1/employees'),
    ('DELETE', '/api/v1/employees'),
    ('GET',    '/api/v1/users'),
    ('PUT',    '/api/v1/users'),
    ('PATCH',  '/api/v1/users'),
    ('POST',   '/api/v1/users'),
    ('DELETE', '/api/v1/users'),
    ('GET',    '/api/v1/convenios'),
    ('PUT',    '/api/v1/convenios'),
    ('PATCH',  '/api/v1/convenios'),
    ('POST',   '/api/v1/convenios'),
    ('DELETE', '/api/v1/convenios'),
    ('GET',    '/api/v1/billing'),
    ('PUT',    '/api/v1/billing'),
    ('PATCH',  '/api/v1/billing'),
    ('POST',   '/api/v1/billing'),
    ('DELETE', '/api/v1/billing'),
    ('GET',    '/api/v1/form-builder'),
    ('PUT',    '/api/v1/form-builder'),
    ('POST',   '/api/v1/form-builder'),
    ('DELETE', '/api/v1/form-builder')
) AS p(method, endpoint)
WHERE r.name = 'ADMIN'
ON CONFLICT (rol_id, method, endpoint) DO NOTHING;

-- Permisos base para DOCTOR (rol_id=2)
INSERT INTO permission (rol_id, method, endpoint)
SELECT r.id, p.method, p.endpoint
FROM rol r
CROSS JOIN (VALUES
    ('GET',   '/api/v1/users'),
    ('PATCH', '/api/v1/users'),
    ('GET',   '/api/v1/appointments'),
    ('POST',  '/api/v1/appointments'),
    ('PATCH', '/api/v1/appointments'),
    ('GET',   '/api/v1/medical-agenda'),
    ('GET',   '/api/v1/schedule'),
    ('GET',   '/api/v1/form-builder'),
    ('POST',  '/api/v1/form-builder')
) AS p(method, endpoint)
WHERE r.name = 'DOCTOR'
ON CONFLICT (rol_id, method, endpoint) DO NOTHING;

-- Permisos base para FACTURADOR
INSERT INTO permission (rol_id, method, endpoint)
SELECT r.id, p.method, p.endpoint
FROM rol r
CROSS JOIN (VALUES
    ('GET',   '/api/v1/users'),
    ('GET',   '/api/v1/appointments'),
    ('GET',   '/api/v1/billing'),
    ('POST',  '/api/v1/billing'),
    ('PATCH', '/api/v1/billing'),
    ('GET',   '/api/v1/convenios')
) AS p(method, endpoint)
WHERE r.name = 'FACTURADOR'
ON CONFLICT (rol_id, method, endpoint) DO NOTHING;

-- ============================================================
-- loginEnabled=true para todos los empleados con password y rol
-- ============================================================

-- Insertar atributo loginEnabled donde no exista
INSERT INTO attribute_employee (employee_id, name_attribute, multiple)
SELECT e.id, 'loginEnabled', false
FROM employees e
WHERE e.password IS NOT NULL
  AND e.rol_id IS NOT NULL
ON CONFLICT (employee_id, name_attribute) DO NOTHING;

-- Insertar valor 'true' para el atributo loginEnabled
INSERT INTO value_attribute_employee (attribute_id, value_attribute)
SELECT ae.id, 'true'
FROM attribute_employee ae
JOIN employees e ON ae.employee_id = e.id
WHERE ae.name_attribute = 'loginEnabled'
  AND e.password IS NOT NULL
  AND e.rol_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM value_attribute_employee vae
      WHERE vae.attribute_id = ae.id
  );
