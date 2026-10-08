package ug.stleoskyegobe.g06.students;

import ug.stleoskyegobe.g06.core.Record;
import ug.stleoskyegobe.g06.core.Reportable;

import java.time.LocalDate;

/**
 * Second model class in the module.
 * An Enrolment records WHEN a student joined and in which academic year.
 *
 * @author Kiggundu Hamuza
 */
public class Enrolment extends Record implements Reportable {

    private final Student student;      // cross-module reference lives inside module here
    private final LocalDate enrolledOn;
    private String academicYear;        // e.g. "2026/2027"

    public Enrolment(String id, Student student, LocalDate enrolledOn, String academicYear) {
        super(id);
        if (student == null) throw new IllegalArgumentException("Enrolment needs a student");
        if (enrolledOn == null) throw new IllegalArgumentException("Enrolment date required");
        this.student = student;
        this.enrolledOn = enrolledOn;
        setAcademicYear(academicYear);
    }

    public Student    getStudent()      { return student; }
    public LocalDate  getEnrolledOn()   { return enrolledOn; }
    public String     getAcademicYear() { return academicYear; }

    public final void setAcademicYear(String academicYear) {
        if (academicYear == null || !academicYear.matches("\\d{4}/\\d{4}"))
            throw new IllegalArgumentException("Academic year must look like 2026/2027");
        this.academicYear = academicYear;
    }

    @Override public String describe() {
        return getId() + "  " + student.getName() + "  " + academicYear;
    }

    @Override public String reportLine() {
        return String.format("%-10s %-25s %s  on %s",
                getId(), student.getName(), academicYear, enrolledOn);
    }

    public String toCsv() {
        return String.join("|", getId(), student.getId(), enrolledOn.toString(), academicYear);
    }
}
