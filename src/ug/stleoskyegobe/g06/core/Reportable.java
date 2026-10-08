package ug.stleoskyegobe.g06.core;

/**
 * Shared interface — anything that can appear on a printed report.
 * Modules implement this on their model classes so one loop can print
 * mixed objects polymorphically.
 *
 * @author G06 Team
 */
public interface Reportable {
    /** One line of plain text suitable for a report row. */
    String reportLine();
}
