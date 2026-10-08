package ug.stleoskyegobe.g06.boarding;

import ug.stleoskyegobe.g06.core.InputHelper;
import ug.stleoskyegobe.g06.core.Reportable;

import java.util.ArrayList;
import java.util.List;

/**
 * Submenu for the Boarding & Dormitories module.
 *
 * @author Atee Joan
 */
public class BoardingMenu {

    private final BoardingService svc;

    public BoardingMenu(BoardingService svc) { this.svc = svc; }

    public void show() {
        while (true) {
            System.out.println("\n=== Boarding & Dormitories ===");
            System.out.println("1. Add dormitory");
            System.out.println("2. List dormitories");
            System.out.println("3. Allocate a bed to a student");
            System.out.println("4. Vacate a bed");
            System.out.println("5. Report: dormitories by free beds");
            System.out.println("6. Report: students in one dormitory");
            System.out.println("7. Update dormitory");
            System.out.println("8. Remove dormitory");
            System.out.println("9. Report: all dormitories and allocations");
            System.out.println("0. Back to main menu");

            int c = InputHelper.readInt("Choose: ", 0, 9);
            try {
                switch (c) {
                    case 1 -> {
                        String n = InputHelper.readString("Dormitory name: ");
                        String h = InputHelper.readString("House: ");
                        int cap  = InputHelper.readInt("Beds (1-300): ", 1, 300);
                        System.out.println("  Added: " + svc.addDormitory(n, h, cap).describe());
                    }
                    case 2 -> {
                        if (svc.allDormitories().isEmpty()) System.out.println("  No dormitories yet.");
                        else svc.allDormitories().forEach(d -> System.out.println("  " + d.reportLine()));
                    }
                    case 3 -> {
                        String sid  = InputHelper.readString("Student ID: ");
                        String did  = InputHelper.readString("Dormitory ID: ");
                        String term = InputHelper.readString("Term (e.g. Term 3 2026): ");
                        System.out.println("  Allocated: " + svc.allocate(sid, did, term).describe());
                    }
                    case 4 -> {
                        String aid = InputHelper.readString("Allocation ID: ");
                        svc.vacate(aid);
                        System.out.println("  Vacated.");
                    }
                    case 5 -> svc.reportByFreeBeds().forEach(d -> System.out.println("  " + d.reportLine()));
                    case 6 -> {
                        String did = InputHelper.readString("Dormitory ID: ");
                        List<BedAllocation> list = svc.studentsInDormitory(did);
                        if (list.isEmpty()) System.out.println("  No students in " + did);
                        else list.forEach(a -> System.out.println("  " + a.reportLine()));
                    }
                    case 7 -> {
                        String did = InputHelper.readString("Dormitory ID to update: ");
                        System.out.println("  Current: " + svc.findDormitory(did).reportLine());
                        String n    = InputHelper.readYesNo("Change name?")     ? InputHelper.readString("New name: ") : null;
                        String h    = InputHelper.readYesNo("Change house?")    ? InputHelper.readString("New house: ") : null;
                        Integer cap = InputHelper.readYesNo("Change capacity?") ? InputHelper.readInt("New capacity (1-300): ", 1, 300) : null;
                        System.out.println("  Updated: " + svc.updateDormitory(did, n, h, cap).reportLine());
                    }
                    case 8 -> {
                        String did = InputHelper.readString("Dormitory ID to remove: ");
                        System.out.println(svc.removeDormitory(did) ? "  Removed." : "  Not found.");
                    }
                    case 9 -> {
                        // Dormitories and allocations are different classes, but both are Reportable,
                        // so one loop can print them all (polymorphism).
                        List<Reportable> records = new ArrayList<>();
                        records.addAll(svc.allDormitories());
                        records.addAll(svc.allAllocations());
                        if (records.isEmpty()) System.out.println("  No records yet.");
                        for (Reportable r : records) System.out.println("  " + r.reportLine());
                    }
                    case 0 -> { return; }
                }
            } catch (RuntimeException ex) { System.out.println("  Error: " + ex.getMessage()); }
        }
    }
}
