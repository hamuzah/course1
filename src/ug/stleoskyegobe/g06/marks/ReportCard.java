package ug.stleoskyegobe.g06.marks;

import ug.stleoskyegobe.g06.core.Record;
import ug.stleoskyegobe.g06.core.Validation;
import ug.stleoskyegobe.g06.core.Reportable;
import ug.stleoskyegobe.g06.students.Student;

import java.util.ArrayList;
import java.util.List;

/**
 * A report card bundles all marks for a student in one term.
 * @author Kamagara Lloyd
 */
public class ReportCard extends Record implements Reportable {

    private final Student student;
    private final String term;
    private final List<Mark> marks = new ArrayList<>();

    public ReportCard(String id, Student student, String term) {
        super(id);
        if (student == null) throw new IllegalArgumentException("Report card needs a student");
                this.student = student;
        this.term = Validation.text(term, "Term");
    }

    public Student getStudent() { return student; }
    public String  getTerm()    { return term; }
    public List<Mark> getMarks() { return marks; }

    public void addMark(Mark m) {
        if (m == null) throw new IllegalArgumentException("Mark cannot be null");
        marks.add(m);
    }

    public double average() {
        if (marks.isEmpty()) return 0;
        double sum = 0;
        for (Mark m : marks) sum += m.getScore();
        return sum / marks.size();
    }

    @Override public String describe() {
        return getId() + "  " + student.getName() + "  " + term + "  avg " + average();
    }

    @Override public String reportLine() {
        return String.format("%-10s %-20s %-10s  %d subj  avg %6.1f",
                getId(), student.getName(), term, marks.size(), average());
    }
}
