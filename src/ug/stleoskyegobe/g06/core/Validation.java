package ug.stleoskyegobe.g06.core;

/**
 * Small shared checks used by model constructors and setters.
 * Text fields may not be blank and may not contain the '|' separator
 * or line breaks, because those would split a saved record into extra
 * fields or lines and make it unreadable at the next start-up.
 *
 * @author G06 Team
 */
public final class Validation {
    private Validation() { }

    public static String text(String value, String field) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(field + " cannot be blank");
        if (value.contains("|") || value.contains("\n") || value.contains("\r"))
            throw new IllegalArgumentException(field + " cannot contain | or line breaks");
        return value.trim();
    }
}
