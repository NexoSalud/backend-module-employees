-- Ensure employees table has login_enabled column
ALTER TABLE IF EXISTS employees
    ADD COLUMN IF NOT EXISTS login_enabled BOOLEAN DEFAULT FALSE;