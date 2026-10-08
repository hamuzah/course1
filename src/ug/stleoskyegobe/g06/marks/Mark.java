package ug.stleoskyegobe.g06.marks;

import ug.stleoskyegobe.g06.core.Record;
import ug.stleoskyegobe.g06.core.Validation;
import ug.stleoskyegobe.g06.core.Reportable;
import ug.stleoskyegobe.g06.students.Student;

/**
 * One mark for one subject for one student.
 * @author Kamagara Lloyd
 */
public class Mark extends Record implements Reportable {

    private final Student student;        // cross-module link
    private final String subject;
    private double score;                 // 0..100

    public Mark(String id, Student student, String subject, double score) {
        super(id);
        if (student == null) throw new IllegalArgumentException("Mark needs a student");
                this.student = student;
        this.subject = Validation.text(subject, "Subject");
        setScore(score);
    }

    public Student getStudent() { return student; }
    public String  getSubject() { return subject; }
    public double  getScore()   { return score; }

    public final void setScore(double score) {
        if (Double.isNaN(score) || score < 0 || score > 100)
            throw new InvalidMarkException("Score must be between 0 and 100 (got " + score + ")");
        this.score = score;
    }

    public String grade() {
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        if (score >= 50) return "D";
        return "F";
    }

    @Override public String describe() {
        return getId() + "  " + student.getName() + "  " + subject + "  " + score + " (" + grade() + ")";
    }

    @Override public String reportLine() {
        return String.format("%-10s %-20s %-15s %6.1f  %s",
                getId(), student.getName(), subject, score, grade());
    }

    public String toCsv() {
        return String.join("|", getId(), student.getId(), subject, String.valueOf(score));
    }
}
