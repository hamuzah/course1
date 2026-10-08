package ug.stleoskyegobe.g06.marks;

import ug.stleoskyegobe.g06.core.FileStore;
import ug.stleoskyegobe.g06.students.Student;
import ug.stleoskyegobe.g06.students.StudentService;

import java.util.*;

/** @author Kamagara Lloyd */
public class MarksService {

    private static final String FILE = "marks.txt";

    private final List<Mark> marks = new ArrayList<>();
    private final StudentService students;
    private int nextSeq = 1;
    private int nextCardSeq = 1;    // report cards are built on request, not stored

    public MarksService(StudentService students) {
        this.students = students;
        students.preventRemovalWhile(st -> {
            for (Mark m : marks)
                if (m.getStudent().getId().equalsIgnoreCase(st.getId())) return "a mark";
            return null;
        });
        marks.addAll(FileStore.loadAll(FILE, line -> {
            String[] p = line.split("\\|");
            Student s = students.findById(p[1]);
            return new Mark(p[0], s, p[2], Double.parseDouble(p[3]));
        }));
        for (Mark m : marks) {
            try {
                int n = Integer.parseInt(m.getId().substring(m.getId().lastIndexOf('M') + 1));
                if (n >= nextSeq) nextSeq = n + 1;
            } catch (Exception ignored) { }
        }
    }

    public String nextId() { return String.format("G06-M%03d", nextSeq++); }

    public Mark add(Student s, String subject, double score) {
        // build the mark first: if the score is refused, no ID number is used up
        Mark m = new Mark(String.format("G06-M%03d", nextSeq), s, subject, score);
        nextSeq++;
        marks.add(m);
        save();
        return m;
    }

    public List<Mark> findByStudent(String studentId) {
        List<Mark> out = new ArrayList<>();
        for (Mark m : marks) if (m.getStudent().getId().equalsIgnoreCase(studentId)) out.add(m);
        return out;
    }

    public List<Mark> allMarks() { return Collections.unmodifiableList(marks); }

    public boolean remove(String markId) {
        boolean removed = marks.removeIf(m -> m.getId().equalsIgnoreCase(markId));
        if (removed) save();
        return removed;
    }

    public void updateScore(String markId, double newScore) {
        for (Mark m : marks) if (m.getId().equalsIgnoreCase(markId)) {
            m.setScore(newScore);
            save();
            return;
        }
        throw new NoSuchElementException("No mark with ID: " + markId);
    }

    /** Build a report card holding every mark the student has, for the given term. */
    public ReportCard reportCardFor(Student s, String term) {
        ReportCard card = new ReportCard(String.format("G06-R%03d", nextCardSeq++), s, term);
        for (Mark m : findByStudent(s.getId())) card.addMark(m);
        return card;
    }

    /** Sorted report by score descending (top of class first). */
    public List<Mark> reportTop() {
        List<Mark> copy = new ArrayList<>(marks);
        copy.sort(Comparator.comparingDouble(Mark::getScore).reversed());
        return copy;
    }

    private void save() { FileStore.saveAll(FILE, marks, Mark::toCsv); }
}
