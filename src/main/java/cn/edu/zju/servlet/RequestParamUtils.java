package cn.edu.zju.servlet;

/**
 * Shared request-parameter parsing helpers used by all list servlets.
 */
final class RequestParamUtils {

    private RequestParamUtils() {}

    /**
     * Return the trimmed value, or {@code null} if the string is blank.
     */
    static String trimOrNull(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        return s.trim();
    }

    /**
     * Parse an integer query parameter.
     *
     * @param s          raw string value (may be null)
     * @param defaultVal value to use when s is null, blank, non-numeric, or &lt; 1
     * @param max        upper bound (inclusive) to cap the result
     * @return parsed value clamped to [1, max], or defaultVal
     */
    static int parsePositiveInt(String s, int defaultVal, int max) {
        if (s == null || s.trim().isEmpty()) return defaultVal;
        try {
            int v = Integer.parseInt(s.trim());
            if (v < 1) return defaultVal;
            return Math.min(v, max);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
