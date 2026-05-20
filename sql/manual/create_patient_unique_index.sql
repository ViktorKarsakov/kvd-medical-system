ALTER TABLE patients
DROP CONSTRAINT IF EXISTS patients_last_name_first_name_middle_name_birth_date_key;

CREATE UNIQUE INDEX IF NOT EXISTS ux_patients_fio_birthdate
    ON patients (
    last_name,
    first_name,
    COALESCE(middle_name, ''),
    birth_date
    );