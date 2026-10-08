package ug.stleoskyegobe.g06.students;

import ug.stleoskyegobe.g06.core.InputHelper;
import ug.stleoskyegobe.g06.core.Reportable;

import java.util.ArrayList;
import java.util.List;

/**
 * Submenu for the Students & Enrolment module.
 * @author Kiggundu Hamuza
 */
public class StudentMenu {

    private final StudentService service;

    public StudentMenu(StudentService service) { this.service = service; }

    public void show() {
        while (true) {
            System.out.println("\n=== Students & Enrolment ===");
            System.out.println("1. Add student");
            System.out.println("2. List all students");
            System.out.println("3. Find student by ID");
            System.out.println("4. Update student");
            System.out.println("5. Remove student");
            System.out.println("6. Report: sorted by name");
            System.out.println("7. Report: filter by year");
            System.out.println("8. Enrol student for an academic year");
            System.out.println("9. Report: all students and enrolments");
            System.out.println("0. Back to main menu");

            int choice = InputHelper.readInt("Choose: ", 0, 9);
            try {
                switch (choice) {
                    case 1 -> addStudent();
                    case 2 -> listAll();
                    case 3 -> findOne();
                    case 4 -> updateOne();
                    case 5 -> removeOne();
                    case 6 -> reportByName();
                    case 7 -> reportByYear();
                    case 8 -> enrolStudent();
                    case 9 -> reportAllRecords();
                    case 0 -> { return; }
                }
            } catch (RuntimeException ex) {
                System.out.println("  Error: " + ex.getMessage());
            }
        }
    }

    private void addStudent() {
        String name    = InputHelper.readString("Name: ");
        int    year    = InputHelper.readInt("Year (1-6): ", 1, 6);
        String stream  = InputHelper.readString("Stream (A/B/C): ");
        String contact = InputHelper.readString("Guardian contact: ");
        Student s = service.add(new Student(service.nextId(), name, year, stream, contact));
        System.out.println("  Added: " + s.describe());
    }

    private void listAll() {
        List<Student> all = service.findAll();
        if (all.isEmpty()) { System.out.println("  No students yet."); return; }
        // polymorphic loop over Reportable
        for (Reportable r : all) System.out.println("  " + r.reportLine());
    }

    private void findOne() {
        String id = InputHelper.readString("Student ID (e.g. G06-S001): ");
        System.out.println("  " + service.findById(id).reportLine());
    }

    private void updateOne() {
        String id = InputHelper.readString("Student ID to update: ");
        Student s = service.findById(id);
        System.out.println("  Current: " + s.reportLine());
        String name   = InputHelper.readYesNo("Change name?")    ? InputHelper.readString("New name: ") : null;
        Integer year  = InputHelper.readYesNo("Change year?")    ? InputHelper.readInt("New year (1-6): ", 1, 6) : null;
        String stream = InputHelper.readYesNo("Change stream?")  ? InputHelper.readString("New stream: ") : null;
        String cont   = InputHelper.readYesNo("Change contact?") ? InputHelper.readString("New contact: ") : null;
        service.update(id, name, year, stream, cont);
        System.out.println("  Updated.");
    }

    private void removeOne() {
        String id = InputHelper.readString("Student ID to remove: ");
        System.out.println(service.remove(id) ? "  Removed." : "  Not found.");
    }

    private void reportByName() {
        for (Student s : service.reportSortedByName()) System.out.println("  " + s.reportLine());
    }

    private void reportByYear() {
        int y = InputHelper.readInt("Which year (1-6)? ", 1, 6);
        List<Student> list = service.reportByYear(y);
        if (list.isEmpty()) System.out.println("  None in S" + y);
        else for (Student s : list) System.out.println("  " + s.reportLine());
    }

    private void enrolStudent() {
        String id   = InputHelper.readString("Student ID: ");
        String year = InputHelper.readString("Academic year (e.g. 2026/2027): ");
        Enrolment e = service.enrol(id, year);
        System.out.println("  Enrolled: " + e.describe());
    }

    private void reportAllRecords() {
        // Students and enrolments are different classes, but both are Reportable,
        // so one loop can print them all (polymorphism).
        List<Reportable> records = new ArrayList<>();
        records.addAll(service.findAll());
        records.addAll(service.allEnrolments());
        if (records.isEmpty()) { System.out.println("  No records yet."); return; }
        for (Reportable r : records) System.out.println("  " + r.reportLine());
    }
}
