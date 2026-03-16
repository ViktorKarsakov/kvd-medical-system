package kkkvd.operator.operatorkvd.util;

import kkkvd.operator.operatorkvd.entities.Doctor;

public final class DoctorNameFormatter {
    private DoctorNameFormatter() {
    }

    public static String formatShort(Doctor doctor) {
        if (doctor == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        if (doctor.getLastName() != null) {
            sb.append(doctor.getLastName());
        }

        if (doctor.getFirstName() != null && !doctor.getFirstName().isBlank()) {
            sb.append(" ").append(doctor.getFirstName().charAt(0)).append(".");
        }

        if (doctor.getMiddleName() != null && !doctor.getMiddleName().isBlank()) {
            sb.append(doctor.getMiddleName().charAt(0)).append(".");
        }

        return sb.toString().trim();
    }
}
