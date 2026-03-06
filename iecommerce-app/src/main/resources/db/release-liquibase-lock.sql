-- Release a stuck Liquibase changelog lock (e.g. after a crashed/killed app or IDE run).
-- Run this against your database when you see "Waiting for changelog lock...." at startup.
-- Usage: psql -U admin -d iecommerce -f release-liquibase-lock.sql
-- Or in a SQL client: execute the statement below.

UPDATE databasechangeloglock
SET locked = false,
    lockgranted = NULL,
    lockedby = NULL
WHERE id = 1;
