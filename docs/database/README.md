# Database migrations

The project currently uses Hibernate `ddl-auto=update`. Hibernate can create
enum check constraints for a new database, but it does not expand an existing
check constraint when a Java enum receives another value.

Before enabling `CRAZY_SUPER_ADMIN_ENABLED` on a database that already has the
`users` table, run:

```text
psql -d racing_db -f docs/database/2026-09-18-add-super-admin-role.sql
```

The migration is idempotent: it replaces `users_role_check` with a constraint
that accepts `USER`, `ADMIN`, and `SUPER_ADMIN`.
