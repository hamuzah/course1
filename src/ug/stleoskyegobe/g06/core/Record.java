package ug.stleoskyegobe.g06.core;

/**
 * Shared abstract base class for every record in the school system.
 * Guarantees that every record has a unique ID starting with the group number
 * (e.g. G06-S001 for a student, G06-P014 for a payment).
 *
 * @author G06 Team
 */
public abstract class Record {
    public static final String GROUP_PREFIX = "G06";

    private final String id;

    protected Record(String id) {
        if (id == null || id.isBlank() || !id.startsWith(GROUP_PREFIX + "-")) {
            throw new IllegalArgumentException(
                "Record ID must start with " + GROUP_PREFIX + "- (got: " + id + ")");
        }
        this.id = Validation.text(id, "Record ID");
    }

    public String getId() { return id; }

    /** Each subclass describes itself in one short line for menus and reports. */
    public abstract String describe();

    // ─── Object methods overridden (Lesson 5) ───

    @Override
    public String toString() { return describe(); }

    /**
     * Two records are equal when they have the same ID.
     * Follows Lesson 5's pattern: check same-object, then type, then compare key field.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Record)) return false;
        Record r = (Record) other;
        return this.id.equalsIgnoreCase(r.id);
    }

    /** Partner for equals — equal objects must give the same hash code. */
    @Override
    public int hashCode() {
        return id.toLowerCase().hashCode();
    }
}
