-- Existing databases created before SUPER_ADMIN was added still restrict
-- users.role to USER and ADMIN. Run this once before enabling the protected
-- super administrator bootstrap.

BEGIN;

ALTER TABLE users
DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE users
ADD CONSTRAINT users_role_check
CHECK (role IN ('USER', 'ADMIN', 'SUPER_ADMIN'));

COMMIT;
