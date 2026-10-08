package ug.stleoskyegobe.g06.clubs;

import ug.stleoskyegobe.g06.core.Record;
import ug.stleoskyegobe.g06.core.Reportable;
import ug.stleoskyegobe.g06.students.Student;

import java.time.LocalDate;

/**
 * A Membership links a Student (Students module) to a Club — cross-module reference.
 *
 * @author Nume Allan
 */
public class Membership extends Record implements Reportable {

    private static final String[] POSITIONS = { "Member", "Secretary", "Chairperson", "Treasurer" };

    private final Student   student;
    private final Club      club;
    private final LocalDate joinedOn;
    private String          position;     // Member, Secretary, Chairperson or Treasurer
    private LocalDate       leftOn;       // null while the student is still a member

    public Membership(String id, Student student, Club club, LocalDate joinedOn, String position) {
        super(id);
        if (student == null || club == null) throw new IllegalArgumentException("Membership needs student + club");
        if (joinedOn == null) throw new IllegalArgumentException("Join date required");
        this.student = student;
        this.club = club;
        this.joinedOn = joinedOn;
        setPosition(position);
    }

    public Student   getStudent()  { return student; }
    public Club      getClub()     { return club; }
    public LocalDate getJoinedOn() { return joinedOn; }
    public String    getPosition() { return position; }
    public LocalDate getLeftOn()   { return leftOn; }
    public boolean   isActive()    { return leftOn == null; }

    public final void setPosition(String p) {
        if (p != null)
            for (String allowed : POSITIONS)
                if (allowed.equalsIgnoreCase(p.trim())) { this.position = allowed; return; }
        throw new IllegalArgumentException("Position must be Member, Secretary, Chairperson or Treasurer");
    }

    public void leave(LocalDate date) {
        if (leftOn != null) throw new IllegalStateException("Already left the club on " + leftOn);
        if (date == null) throw new IllegalArgumentException("Leave date required");
        this.leftOn = date;
    }

    @Override public String describe() {
        return getId() + "  " + student.getName() + " -> " + club.getName() + "  " + position
             + (isActive() ? " (ACTIVE)" : " (left)");
    }

    @Override public String reportLine() {
        return String.format("%-10s %-20s %-25s %-12s %s  %s",
                getId(), student.getName(), club.getName(), position, joinedOn,
                isActive() ? "ACTIVE" : "left " + leftOn);
    }

    public String toCsv() {
        return String.join("|", getId(), student.getId(), club.getId(), position,
                joinedOn.toString(), leftOn == null ? "-" : leftOn.toString());
    }
}
