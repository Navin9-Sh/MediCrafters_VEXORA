-- V2__add_auth_fields.sql
-- Add password_hash, full_name, dob to users table and make firebase_uid nullable for direct email/password signups

ALTER TABLE users ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS dob DATE;
ALTER TABLE users ALTER COLUMN firebase_uid DROP NOT NULL;
