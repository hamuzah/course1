package ug.stleoskyegobe.g06.marks;

import ug.stleoskyegobe.g06.core.InputHelper;
import ug.stleoskyegobe.g06.core.Reportable;
import ug.stleoskyegobe.g06.students.Student;
import ug.stleoskyegobe.g06.students.StudentService;

import java.util.ArrayList;
import java.util.List;

/** @author Kamagara Lloyd */
public class MarksMenu {

    private final MarksService service;
    private final StudentService students;

    public MarksMenu(MarksService s, StudentService st) { this.service = s; this.students = st; }

    public void show() {
        while (true) {
            System.out.println("\n=== Marks & Report Cards ===");
            System.out.println("1. Record a mark");
            System.out.println("2. Marks for a student");
            System.out.println("3. Update a mark");
            System.out.println("4. Remove a mark");
            System.out.println("5. Report: top scores first");
            System.out.println("6. Report card for a student");
            System.out.println("0. Back to main menu");

            int c = InputHelper.readInt("Choose: ", 0, 6);
            try {
                switch (c) {
                    case 1 -> addMark();
                    case 2 -> listByStudent();
                    case 3 -> updateMark();
                    case 4 -> removeMark();
                    case 5 -> topReport();
                    case 6 -> reportCard();
                    case 0 -> { return; }
                }
            } catch (RuntimeException ex) { System.out.println("  Error: " + ex.getMessage()); }
        }
    }

    private void addMark() {
        String sid = InputHelper.readString("Student ID: ");
        Student s = students.findById(sid);
        String subject = InputHelper.readString("Subject: ");
        double score = InputHelper.readMoney("Score (0-100): ");
        Mark m = service.add(s, subject, score);
        System.out.println("  Recorded: " + m.describe());
    }

    private void listByStudent() {
        String sid = InputHelper.readString("Student ID: ");
        List<Mark> list = service.findByStudent(sid);
        if (list.isEmpty()) { System.out.println("  No marks for " + sid); return; }
        for (Mark m : list) System.out.println("  " + m.reportLine());
    }

    private void updateMark() {
        String mid = InputHelper.readString("Mark ID (e.g. G06-M001): ");
        double sc = InputHelper.readMoney("New score (0-100): ");
        service.updateScore(mid, sc);
        System.out.println("  Updated.");
    }

    private void removeMark() {
        String mid = InputHelper.readString("Mark ID: ");
        System.out.println(service.remove(mid) ? "  Removed." : "  Not found.");
    }

    private void topReport() {
        for (Mark m : service.reportTop()) System.out.println("  " + m.reportLine());
    }

    private void reportCard() {
        String sid = InputHelper.readString("Student ID: ");
        Student s = students.findById(sid);
        String term = InputHelper.readString("Term (e.g. Term 1): ");
        ReportCard card = service.reportCardFor(s, term);
        // The card and its marks are different classes, but both are Reportable,
        // so one loop can print them all (polymorphism).
        List<Reportable> lines = new ArrayList<>();
        lines.add(card);
        lines.addAll(card.getMarks());
        for (Reportable r : lines) System.out.println("  " + r.reportLine());
    }
}
