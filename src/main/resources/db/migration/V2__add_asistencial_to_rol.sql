-- Ensure rol table has asistencial column with default TRUE
ALTER TABLE IF EXISTS rol
    ADD COLUMN IF NOT EXISTS asistencial BOOLEAN DEFAULT TRUE;