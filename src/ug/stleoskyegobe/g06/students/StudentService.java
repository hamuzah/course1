package ug.stleoskyegobe.g06.students;

import ug.stleoskyegobe.g06.core.FileStore;

import java.time.LocalDate;
import java.util.*;

/**
 * Service class — manages students (and their enrolments) in ArrayLists
 * with add/find/update/remove.
 * Loads from file at start-up and saves after every change.
 *
 * @author Kiggundu Hamuza
 */
public class StudentService {

    private static final String FILE = "students.txt";
    private static final String ENROL_FILE = "enrolments.txt";

    private final List<Student> students = new ArrayList<>();
    private final List<Enrolment> enrolments = new ArrayList<>();
    private final List<java.util.function.Function<Student, String>> removalChecks = new ArrayList<>();
    private int nextSeq = 1;
    private int nextEnrolSeq = 1;

    public StudentService() {
        students.addAll(FileStore.loadAll(FILE, Student::fromCsv));
        for (Student s : students) {
            // keep nextSeq ahead of any ID already in the file
            try {
                int n = Integer.parseInt(s.getId().substring(s.getId().lastIndexOf('S') + 1));
                if (n >= nextSeq) nextSeq = n + 1;
            } catch (Exception ignored) { }
        }
        // enrolments are loaded after students because each one points to a student
        enrolments.addAll(FileStore.loadAll(ENROL_FILE, line -> {
            String[] p = line.split("\\|");
            return new Enrolment(p[0], findById(p[1]), LocalDate.parse(p[2]), p[3]);
        }));
        for (Enrolment e : enrolments) {
            try {
                int n = Integer.parseInt(e.getId().substring(e.getId().lastIndexOf('E') + 1));
                if (n >= nextEnrolSeq) nextEnrolSeq = n + 1;
            } catch (Exception ignored) { }
        }
    }

    public String nextId() { return String.format("G06-S%03d", nextSeq++); }
    public String nextEnrolmentId() { return String.format("G06-E%03d", nextEnrolSeq++); }

    /** Enrol an existing student for an academic year such as 2026/2027. */
    public Enrolment enrol(String studentId, String academicYear) {
        Student s = findById(studentId);       // throws StudentNotFoundException if missing
        if (academicYear == null || !academicYear.matches("\\d{4}/\\d{4}"))
            throw new IllegalArgumentException("Academic year must look like 2026/2027");
        Enrolment e = new Enrolment(nextEnrolmentId(), s, LocalDate.now(), academicYear);
        enrolments.add(e);
        saveEnrolments();
        return e;
    }

    public List<Enrolment> allEnrolments() { return Collections.unmodifiableList(enrolments); }

    public Student add(Student s) {
        students.add(s);
        save();
        return s;
    }

    public Student findById(String id) {
        for (Student s : students) if (s.getId().equalsIgnoreCase(id)) return s;
        throw new StudentNotFoundException(id);
    }

    public List<Student> findAll() { return Collections.unmodifiableList(students); }

    public void update(String id, String newName, Integer newYear, String newStream, String newContact) {
        Student s = findById(id);
        // Validate every new value on a throw-away copy first, so a rejected value
        // never leaves the real record half-changed.
        new Student(s.getId(),
                newName    != null ? newName    : s.getName(),
                newYear    != null ? newYear    : s.getYear(),
                newStream  != null ? newStream  : s.getStream(),
                newContact != null ? newContact : s.getGuardianContact());
        if (newName    != null) s.setName(newName);
        if (newYear    != null) s.setYear(newYear);
        if (newStream  != null) s.setStream(newStream);
        if (newContact != null) s.setGuardianContact(newContact);
        save();
    }

    /**
     * Other modules register a check here. A check returns a short description of the
     * record that still points at the student (for example "a fee account"), or null.
     */
    public void preventRemovalWhile(java.util.function.Function<Student, String> check) {
        removalChecks.add(check);
    }

    public boolean remove(String id) {
        Student target = null;
        for (Student s : students) if (s.getId().equalsIgnoreCase(id)) { target = s; break; }
        if (target == null) return false;
        for (java.util.function.Function<Student, String> check : removalChecks) {
            String reason = check.apply(target);
            if (reason != null)
                throw new IllegalStateException("Cannot remove " + target.getName()
                        + ": still linked to " + reason + ". Remove that record first.");
        }
        boolean removed = students.removeIf(s -> s.getId().equalsIgnoreCase(id));
        if (removed) {
            // a removed student's enrolments go too
            enrolments.removeIf(e -> e.getStudent().getId().equalsIgnoreCase(id));
            save();
            saveEnrolments();
        }
        return removed;
    }

    /** Sorted report by name, using a Comparator. */
    public List<Student> reportSortedByName() {
        List<Student> copy = new ArrayList<>(students);
        copy.sort(Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER));
        return copy;
    }

    /** Filtered report of students in a given year. */
    public List<Student> reportByYear(int year) {
        List<Student> out = new ArrayList<>();
        for (Student s : students) if (s.getYear() == year) out.add(s);
        return out;
    }

    private void save() { FileStore.saveAll(FILE, students, Student::toCsv); }
    private void saveEnrolments() { FileStore.saveAll(ENROL_FILE, enrolments, Enrolment::toCsv); }
}
