package kkkvd.operator.operatorkvd.util;

import kkkvd.operator.operatorkvd.entities.Patient;

public final class NameUtils {

    private NameUtils() {
    }

    public static String buildFullName(Patient patient) {
        if (patient == null) {
            return null;
        }

        String last = safe(patient.getLastName());
        String first = safe(patient.getFirstName());
        String middle = safe(patient.getMiddleName());

        String full = (last + " " +  first + " " + middle).trim();
        return full.replaceAll("\\s+", " ");
    }

    public static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
