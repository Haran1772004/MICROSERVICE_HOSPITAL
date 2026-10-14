-- OPTIONAL. Run by hand ONCE after the service has started and created the users table:
--   psql -U postgres -d auth_db -f seed_admin_receptionist.sql
-- Creates the first admin and receptionist (nobody can create them through the API before that).
-- Passwords (Postman collection defaults): Admin@123 and Reception@123. CHANGE THEM after first login.
-- The hashes are BCrypt. Spring Security accepts the $2a$ prefix.

INSERT INTO users (username, password, role, status) VALUES
    ('admin',        '$2a$10$7jnmZcKOMylozA9Kl7ZF2ueoasRrvmN8T6ngvyC/llmHnuabyOg/u', 'ADMIN',        'ACTIVE'),
    ('receptionist', '$2a$10$iso6.AvCBNGvq2Hsq8NsMuSFfVz082eZGjCp4Tu8Ju7up3gLkkgb2', 'RECEPTIONIST', 'ACTIVE')
ON CONFLICT (username) DO NOTHING;
