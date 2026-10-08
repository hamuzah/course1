package ug.stleoskyegobe.g06.students;

import ug.stleoskyegobe.g06.core.Record;
import ug.stleoskyegobe.g06.core.Validation;
import ug.stleoskyegobe.g06.core.Reportable;

/**
 * Model class for a student.
 * Fully encapsulated — fields are private, setters validate and throw.
 *
 * @author Kiggundu Hamuza
 */
public class Student extends Record implements Reportable {

    private String name;
    private int year;          // 1..6 (S1..S6)
    private String stream;     // e.g. "A"
    private String guardianContact;

    public Student(String id, String name, int year, String stream, String guardianContact) {
        super(id);
        setName(name);
        setYear(year);
        setStream(stream);
        setGuardianContact(guardianContact);
    }

    // ─── Getters ───
    public String getName()             { return name; }
    public int    getYear()             { return year; }
    public String getStream()           { return stream; }
    public String getGuardianContact()  { return guardianContact; }

    // ─── Setters (throw on invalid) ───
    public final void setName(String name) {
        this.name = Validation.text(name, "Student name");
    }

    public final void setYear(int year) {
        if (year < 1 || year > 6)
            throw new IllegalArgumentException("Year must be between 1 and 6 (S1..S6)");
        this.year = year;
    }

    public final void setStream(String stream) {
        this.stream = Validation.text(stream, "Stream").toUpperCase();
    }

    public final void setGuardianContact(String guardianContact) {
        this.guardianContact = Validation.text(guardianContact, "Guardian contact");
    }

    public boolean promote() {
        if (year >= 6) return false;
        year++;
        return true;
    }

    // ─── Polymorphism ───
    @Override public String describe() {
        return getId() + "  " + name + "  S" + year + stream;
    }

    @Override public String reportLine() {
        return String.format("%-10s %-25s S%d%-2s   Guardian: %s",
                getId(), name, year, stream, guardianContact);
    }

    // ─── Serialization for FileStore ───
    public String toCsv() {
        return String.join("|", getId(), name, String.valueOf(year), stream, guardianContact);
    }

    public static Student fromCsv(String line) {
        String[] p = line.split("\\|");
        if (p.length != 5) throw new IllegalArgumentException("Bad student record");
        return new Student(p[0], p[1], Integer.parseInt(p[2]), p[3], p[4]);
    }
}
