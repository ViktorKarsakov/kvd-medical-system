package kkkvd.operator.operatorkvd.util;

public final class StringUtils {
    private StringUtils() {
    }

    public static String normalizeBlankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
