package ug.stleoskyegobe.g06.boarding;

import ug.stleoskyegobe.g06.core.Record;
import ug.stleoskyegobe.g06.core.Reportable;
import ug.stleoskyegobe.g06.students.Student;

import java.time.LocalDate;

/**
 * A BedAllocation links a Student (Students module) to a Dormitory for one term
 * — cross-module reference.
 *
 * @author Atee Joan
 */
public class BedAllocation extends Record implements Reportable {

    private final Student   student;
    private final Dormitory dormitory;
    private final LocalDate allocatedOn;
    private String          term;          // e.g. "Term 3 2026"
    private LocalDate       vacatedOn;     // null while the student still has the bed

    public BedAllocation(String id, Student student, Dormitory dormitory, LocalDate allocatedOn, String term) {
        super(id);
        if (student == null || dormitory == null)
            throw new IllegalArgumentException("Allocation needs student + dormitory");
        if (allocatedOn == null) throw new IllegalArgumentException("Allocation date required");
        this.student = student;
        this.dormitory = dormitory;
        this.allocatedOn = allocatedOn;
        setTerm(term);
    }

    public Student   getStudent()     { return student; }
    public Dormitory getDormitory()   { return dormitory; }
    public LocalDate getAllocatedOn() { return allocatedOn; }
    public String    getTerm()        { return term; }
    public LocalDate getVacatedOn()   { return vacatedOn; }
    public boolean   isActive()       { return vacatedOn == null; }

    public final void setTerm(String t) {
        if (t == null || !t.trim().matches("Term [1-3] \\d{4}"))
            throw new IllegalArgumentException("Term must look like 'Term 3 2026'");
        this.term = t.trim();
    }

    public void vacate(LocalDate date) {
        if (vacatedOn != null) throw new IllegalStateException("Bed already vacated on " + vacatedOn);
        if (date == null) throw new IllegalArgumentException("Vacate date required");
        this.vacatedOn = date;
    }

    @Override public String describe() {
        return getId() + "  " + student.getName() + " -> " + dormitory.getName() + "  " + term
             + (isActive() ? " (ACTIVE)" : " (vacated)");
    }

    @Override public String reportLine() {
        return String.format("%-10s %-20s %-20s %-12s %s  %s",
                getId(), student.getName(), dormitory.getName(), term, allocatedOn,
                isActive() ? "ACTIVE" : "vacated " + vacatedOn);
    }

    public String toCsv() {
        return String.join("|", getId(), student.getId(), dormitory.getId(), term,
                allocatedOn.toString(), vacatedOn == null ? "-" : vacatedOn.toString());
    }
}
