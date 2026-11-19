package clinic;

import java.util.regex.Pattern;

/**
 * Small, dependency-free guards that every domain class uses to keep invalid
 * data out of its objects.
 *
 * <p>The methods follow one convention: they return the cleaned value when the
 * input is acceptable, and throw {@link IllegalArgumentException} with a
 * message that is safe to show to a user when it is not. Centralising the rules
 * here means {@link HealthProfessional} and {@link Appointment} cannot drift
 * apart in how they validate a phone number or a name.</p>
 *
 * <p>The class is a utility class: it is final, has a private constructor and
 * only static members, so it can never be instantiated or subclassed.</p>
 */
public final class Validation {

    /** Identifiers are digits only, e.g. {@code 1001}. */
    private static final Pattern DIGITS_ONLY = Pattern.compile("\\d+");

    /** Phone numbers are stored without spaces, so the raw digit count is what matters. */
    private static final Pattern PHONE_DIGITS = Pattern.compile("\\d{8,15}");

    private Validation() {
        // No instances: this is a stateless helper.
    }

    /**
     * Ensures that a text field carries real content and trims it.
     *
     * @param value the raw input, possibly {@code null}
     * @param field human readable field name used in the error message
     * @return the trimmed value
     * @throws IllegalArgumentException if the value is {@code null} or blank
     */
    public static String text(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be empty.");
        }
        return value.trim();
    }

    /**
     * Ensures that an identifier is made of digits only.
     *
     * @param value the raw input, possibly {@code null}
     * @param field human readable field name used in the error message
     * @return the trimmed, digits-only identifier
     * @throws IllegalArgumentException if the value is missing or not numeric
     */
    public static String digitsOnly(String value, String field) {
        String cleaned = text(value, field);
        if (!DIGITS_ONLY.matcher(cleaned).matches()) {
            throw new IllegalArgumentException(field + " must contain digits only, but was \"" + cleaned + "\".");
        }
        return cleaned;
    }

    /**
     * Validates a phone number and returns it in its stored form (spaces and
     * hyphens removed, so {@code "0412 345 678"} and {@code "0412345678"} are
     * the same number for search purposes).
     *
     * @param value the raw input, possibly {@code null}
     * @param field human readable field name used in the error message
     * @return the phone number without spaces or hyphens
     * @throws IllegalArgumentException if the value is missing or not a phone number
     */
    public static String phone(String value, String field) {
        String cleaned = text(value, field).replaceAll("[\\s-]", "");
        if (!PHONE_DIGITS.matcher(cleaned).matches()) {
            throw new IllegalArgumentException(
                    field + " must be 8 to 15 digits, but was \"" + value + "\".");
        }
        return cleaned;
    }

    /**
     * Ensures that a required object reference was supplied.
     *
     * @param value the reference to check
     * @param field human readable field name used in the error message
     * @param <T>   the type of the reference
     * @return the same reference, for chaining into a field initialiser
     * @throws IllegalArgumentException if the reference is {@code null}
     */
    public static <T> T required(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required.");
        }
        return value;
    }

    /**
     * Ensures that a numeric field is not negative.
     *
     * @param value the number to check
     * @param field human readable field name used in the error message
     * @return the unchanged value
     * @throws IllegalArgumentException if the value is negative
     */
    public static int nonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " must not be negative, but was " + value + ".");
        }
        return value;
    }
}
