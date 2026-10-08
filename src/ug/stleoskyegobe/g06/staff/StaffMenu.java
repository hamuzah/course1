package ug.stleoskyegobe.g06.staff;

import ug.stleoskyegobe.g06.core.InputHelper;
import ug.stleoskyegobe.g06.core.Reportable;

import java.util.ArrayList;
import java.util.List;

/** @author Namawejje Jovia */
public class StaffMenu {

    private final StaffService svc;

    public StaffMenu(StaffService svc) { this.svc = svc; }

    public void show() {
        while (true) {
            System.out.println("\n=== Staff ===");
            System.out.println("1. Add staff");
            System.out.println("2. List all staff");
            System.out.println("3. Mark attendance (today)");
            System.out.println("4. Report: sorted by salary");
            System.out.println("5. Remove staff");
            System.out.println("6. Update staff");
            System.out.println("7. Report: all staff and attendance records");
            System.out.println("0. Back to main menu");

            int c = InputHelper.readInt("Choose: ", 0, 7);
            try {
                switch (c) {
                    case 1 -> {
                        String n = InputHelper.readString("Name: ");
                        String r = InputHelper.readString("Role: ");
                        double sal = InputHelper.readMoney("Monthly salary: ");
                        System.out.println("  Added: " + svc.add(n, r, sal).describe());
                    }
                    case 2 -> {
                        if (svc.all().isEmpty()) System.out.println("  No staff yet.");
                        else svc.all().forEach(s -> System.out.println("  " + s.reportLine()));
                    }
                    case 3 -> {
                        String sid = InputHelper.readString("Staff ID: ");
                        boolean p = InputHelper.readYesNo("Present today?");
                        System.out.println("  " + svc.mark(sid, p).describe());
                    }
                    case 4 -> svc.reportBySalary().forEach(s -> System.out.println("  " + s.reportLine()));
                    case 5 -> {
                        String sid = InputHelper.readString("Staff ID to remove: ");
                        System.out.println(svc.remove(sid) ? "  Removed." : "  Not found.");
                    }
                    case 6 -> {
                        String sid = InputHelper.readString("Staff ID to update: ");
                        System.out.println("  Current: " + svc.findById(sid).reportLine());
                        String n   = InputHelper.readYesNo("Change name?")   ? InputHelper.readString("New name: ") : null;
                        String r   = InputHelper.readYesNo("Change role?")   ? InputHelper.readString("New role: ") : null;
                        Double sal = InputHelper.readYesNo("Change salary?") ? InputHelper.readMoney("New monthly salary: ") : null;
                        System.out.println("  Updated: " + svc.update(sid, n, r, sal).reportLine());
                    }
                    case 7 -> {
                        // Staff and attendance are different classes, but both are Reportable,
                        // so one loop can print them all (polymorphism).
                        List<Reportable> records = new ArrayList<>();
                        records.addAll(svc.all());
                        records.addAll(svc.allAttendance());
                        if (records.isEmpty()) System.out.println("  No records yet.");
                        for (Reportable r : records) System.out.println("  " + r.reportLine());
                    }
                    case 0 -> { return; }
                }
            } catch (RuntimeException ex) { System.out.println("  Error: " + ex.getMessage()); }
        }
    }
}
