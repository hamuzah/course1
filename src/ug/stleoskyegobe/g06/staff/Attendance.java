package ug.stleoskyegobe.g06.staff;

import ug.stleoskyegobe.g06.core.Record;
import ug.stleoskyegobe.g06.core.Reportable;

import java.time.LocalDate;

/** @author Namawejje Jovia */
public class Attendance extends Record implements Reportable {

    private final Staff staff;
    private final LocalDate date;
    private final boolean present;

    public Attendance(String id, Staff staff, LocalDate date, boolean present) {
        super(id);
        if (staff == null) throw new IllegalArgumentException("Attendance needs a staff member");
        if (date == null) throw new IllegalArgumentException("Date required");
        this.staff = staff; this.date = date; this.present = present;
    }

    public Staff     getStaff()   { return staff; }
    public LocalDate getDate()    { return date; }
    public boolean   isPresent()  { return present; }

    @Override public String describe() {
        return getId() + "  " + staff.getName() + "  " + date + "  " + (present ? "PRESENT" : "ABSENT");
    }
    @Override public String reportLine() {
        return String.format("%-10s %-25s %s  %s",
                getId(), staff.getName(), date, present ? "PRESENT" : "ABSENT");
    }
    public String toCsv() {
        return String.join("|", getId(), staff.getId(), date.toString(), String.valueOf(present));
    }
}
