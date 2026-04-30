-- V1__initial_schema.sql
-- Clinical Decision System — Initial Database Migration

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ─── USERS ────────────────────────────────────────────────────────────────
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    firebase_uid    VARCHAR(128) UNIQUE NOT NULL,
    email           VARCHAR(255) UNIQUE NOT NULL,
    phone           VARCHAR(20),
    role            VARCHAR(20) NOT NULL CHECK (role IN ('PATIENT', 'DOCTOR', 'ADMIN')),
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

-- ─── PATIENT PROFILES ─────────────────────────────────────────────────────
CREATE TABLE patient_profiles (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    full_name         VARCHAR(255),
    dob               DATE,
    blood_type        VARCHAR(5),
    gender            VARCHAR(10),
    profile_image     TEXT,
    address           JSONB,
    emergency_contact JSONB
);

-- ─── DOCTORS ──────────────────────────────────────────────────────────────
CREATE TABLE doctors (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    full_name        VARCHAR(255) NOT NULL,
    bio              TEXT,
    specialty        VARCHAR(100) NOT NULL,
    license_number   VARCHAR(100) UNIQUE,
    experience_yrs   INT,
    consultation_fee DECIMAL(10, 2),
    profile_image    TEXT,
    avg_rating       DECIMAL(3, 2) DEFAULT 0.00,
    total_reviews    INT DEFAULT 0,
    is_available     BOOLEAN DEFAULT TRUE,
    verified         BOOLEAN DEFAULT FALSE,
    created_at       TIMESTAMPTZ DEFAULT NOW(),
    updated_at       TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE doctor_specialties (
    doctor_id UUID NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    specialty  VARCHAR(100) NOT NULL,
    PRIMARY KEY (doctor_id, specialty)
);

CREATE TABLE doctor_favorites (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES patient_profiles(id) ON DELETE CASCADE,
    doctor_id  UUID NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE (patient_id, doctor_id)
);

-- ─── SCHEDULES & SLOTS ────────────────────────────────────────────────────
CREATE TABLE schedules (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id          UUID NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    day_of_week        SMALLINT NOT NULL,
    start_time         TIME NOT NULL,
    end_time           TIME NOT NULL,
    slot_duration_mins INT DEFAULT 30
);

CREATE TABLE time_slots (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id    UUID NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    slot_date    DATE NOT NULL,
    start_time   TIME NOT NULL,
    end_time     TIME NOT NULL,
    status       VARCHAR(20) DEFAULT 'AVAILABLE'
                 CHECK (status IN ('AVAILABLE', 'LOCKED', 'BOOKED', 'CANCELLED')),
    locked_until TIMESTAMPTZ,
    locked_by    UUID,
    UNIQUE (doctor_id, slot_date, start_time)
);

-- ─── APPOINTMENTS ─────────────────────────────────────────────────────────
CREATE TABLE appointments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id      UUID NOT NULL REFERENCES patient_profiles(id),
    doctor_id       UUID NOT NULL REFERENCES doctors(id),
    slot_id         UUID NOT NULL REFERENCES time_slots(id),
    status          VARCHAR(30) DEFAULT 'PENDING'
                    CHECK (status IN ('PENDING','CONFIRMED','IN_PROGRESS','COMPLETED','CANCELLED','NO_SHOW')),
    type            VARCHAR(20) DEFAULT 'ONLINE'
                    CHECK (type IN ('ONLINE', 'IN_PERSON')),
    chief_complaint TEXT,
    notes           TEXT,
    cancelled_by    UUID,
    cancel_reason   TEXT,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE appointment_status_history (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id UUID NOT NULL REFERENCES appointments(id),
    old_status     VARCHAR(30),
    new_status     VARCHAR(30),
    changed_by     UUID REFERENCES users(id),
    changed_at     TIMESTAMPTZ DEFAULT NOW(),
    reason         TEXT
);

-- ─── PAYMENTS ─────────────────────────────────────────────────────────────
CREATE TABLE payments (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id      UUID NOT NULL REFERENCES appointments(id),
    patient_id          UUID NOT NULL REFERENCES patient_profiles(id),
    amount              DECIMAL(10, 2) NOT NULL,
    currency            VARCHAR(10) DEFAULT 'INR',
    status              VARCHAR(20) DEFAULT 'INITIATED'
                        CHECK (status IN ('INITIATED','PENDING','SUCCESS','FAILED','REFUNDED')),
    gateway             VARCHAR(30) DEFAULT 'RAZORPAY',
    gateway_order_id    VARCHAR(255),
    gateway_payment_id  VARCHAR(255),
    gateway_signature   VARCHAR(500),
    retry_count         INT DEFAULT 0,
    last_error          TEXT,
    created_at          TIMESTAMPTZ DEFAULT NOW(),
    updated_at          TIMESTAMPTZ DEFAULT NOW()
);

-- ─── NOTIFICATIONS ────────────────────────────────────────────────────────
CREATE TABLE notifications (
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id  UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title    VARCHAR(255) NOT NULL,
    body     TEXT,
    type     VARCHAR(50) NOT NULL,
    ref_id   UUID,
    is_read  BOOLEAN DEFAULT FALSE,
    sent_at  TIMESTAMPTZ DEFAULT NOW()
);

-- ─── DOCTOR RATINGS ───────────────────────────────────────────────────────
CREATE TABLE doctor_ratings (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id UUID UNIQUE NOT NULL REFERENCES appointments(id),
    patient_id     UUID NOT NULL REFERENCES patient_profiles(id),
    doctor_id      UUID NOT NULL REFERENCES doctors(id),
    rating         SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    review         TEXT,
    created_at     TIMESTAMPTZ DEFAULT NOW()
);

-- ─── AI READY TABLES ──────────────────────────────────────────────────────
CREATE TABLE symptom_logs (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id     UUID NOT NULL REFERENCES patient_profiles(id),
    appointment_id UUID REFERENCES appointments(id),
    symptoms       TEXT[],
    severity       VARCHAR(20),
    logged_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE vital_records (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id   UUID NOT NULL REFERENCES patient_profiles(id),
    heart_rate   INT,
    systolic_bp  INT,
    diastolic_bp INT,
    spo2         DECIMAL(5, 2),
    temperature  DECIMAL(5, 2),
    recorded_at  TIMESTAMPTZ DEFAULT NOW()
);

-- ─── INDEXES ──────────────────────────────────────────────────────────────
CREATE INDEX idx_appointments_patient ON appointments(patient_id, status);
CREATE INDEX idx_appointments_doctor  ON appointments(doctor_id, status);
CREATE INDEX idx_slots_doctor_date    ON time_slots(doctor_id, slot_date, status);
CREATE INDEX idx_notifications_user   ON notifications(user_id, is_read);
CREATE INDEX idx_payments_appointment ON payments(appointment_id, status);
CREATE INDEX idx_payments_order       ON payments(gateway_order_id);
CREATE INDEX idx_doctors_specialty    ON doctors(specialty, is_available);
CREATE INDEX idx_users_firebase       ON users(firebase_uid);
