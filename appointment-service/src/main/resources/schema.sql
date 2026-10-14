CREATE TABLE IF NOT EXISTS appointments (
    appointment_id SERIAL PRIMARY KEY,
    patient_id INTEGER NOT NULL,
    doctor_id INTEGER NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED'
        CHECK (status IN ('SCHEDULED', 'FINISHED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS medical_records (
    record_id SERIAL PRIMARY KEY,
    appointment_id INTEGER NOT NULL UNIQUE,
    diagnosis TEXT NOT NULL,
    treatment_notes TEXT NOT NULL,
    record_date DATE NOT NULL,
    CONSTRAINT fk_medical_records_appointment
        FOREIGN KEY (appointment_id)
        REFERENCES appointments (appointment_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS prescriptions (
    prescription_id SERIAL PRIMARY KEY,
    record_id INTEGER NOT NULL,
    medicine_name VARCHAR(150) NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    duration VARCHAR(100) NOT NULL,
    CONSTRAINT fk_prescriptions_record
        FOREIGN KEY (record_id)
        REFERENCES medical_records (record_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_appointments_doctor_slot_active
    ON appointments (doctor_id, appointment_date, appointment_time)
    WHERE status = 'SCHEDULED';

CREATE UNIQUE INDEX IF NOT EXISTS idx_appointments_patient_slot_active
    ON appointments (patient_id, appointment_date, appointment_time)
    WHERE status = 'SCHEDULED';

CREATE INDEX IF NOT EXISTS idx_appointments_patient
    ON appointments (patient_id, appointment_date);

CREATE INDEX IF NOT EXISTS idx_appointments_doctor
    ON appointments (doctor_id, appointment_date);

CREATE INDEX IF NOT EXISTS idx_prescriptions_record
    ON prescriptions (record_id);
