package ug.stleoskyegobe.g06.clubs;

import ug.stleoskyegobe.g06.core.InputHelper;
import ug.stleoskyegobe.g06.core.Reportable;

import java.util.ArrayList;
import java.util.List;

/**
 * Submenu for the Clubs & Co-curricular Activities module.
 *
 * @author Nume Allan
 */
public class ClubsMenu {

    private final ClubService svc;

    public ClubsMenu(ClubService svc) { this.svc = svc; }

    public void show() {
        while (true) {
            System.out.println("\n=== Clubs & Co-curricular Activities ===");
            System.out.println("1. Add club");
            System.out.println("2. List clubs");
            System.out.println("3. Join a club");
            System.out.println("4. Leave a club");
            System.out.println("5. Report: clubs by size");
            System.out.println("6. Report: members of one club");
            System.out.println("7. Update club");
            System.out.println("8. Remove club");
            System.out.println("9. Report: all clubs and memberships");
            System.out.println("0. Back to main menu");

            int c = InputHelper.readInt("Choose: ", 0, 9);
            try {
                switch (c) {
                    case 1 -> {
                        String n   = InputHelper.readString("Club name: ");
                        String pid = InputHelper.readString("Patron staff ID (e.g. G06-T001): ");
                        int max    = InputHelper.readInt("Maximum members (1-200): ", 1, 200);
                        System.out.println("  Added: " + svc.addClub(n, pid, max).describe());
                    }
                    case 2 -> {
                        if (svc.allClubs().isEmpty()) System.out.println("  No clubs yet.");
                        else svc.allClubs().forEach(cl -> System.out.println("  " + cl.reportLine()));
                    }
                    case 3 -> {
                        String sid = InputHelper.readString("Student ID: ");
                        String cid = InputHelper.readString("Club ID: ");
                        String pos = InputHelper.readString("Position (Member/Secretary/Chairperson/Treasurer): ");
                        System.out.println("  Joined: " + svc.join(sid, cid, pos).describe());
                    }
                    case 4 -> {
                        String mid = InputHelper.readString("Membership ID: ");
                        svc.leave(mid);
                        System.out.println("  Left the club.");
                    }
                    case 5 -> svc.reportBySize().forEach(cl -> System.out.println("  " + cl.reportLine()));
                    case 6 -> {
                        String cid = InputHelper.readString("Club ID: ");
                        List<Membership> list = svc.membersOfClub(cid);
                        if (list.isEmpty()) System.out.println("  No members in " + cid);
                        else list.forEach(m -> System.out.println("  " + m.reportLine()));
                    }
                    case 7 -> {
                        String cid = InputHelper.readString("Club ID to update: ");
                        System.out.println("  Current: " + svc.findClub(cid).reportLine());
                        String n    = InputHelper.readYesNo("Change name?")            ? InputHelper.readString("New name: ") : null;
                        String pid  = InputHelper.readYesNo("Change patron?")          ? InputHelper.readString("New patron staff ID: ") : null;
                        Integer max = InputHelper.readYesNo("Change maximum members?") ? InputHelper.readInt("New maximum (1-200): ", 1, 200) : null;
                        System.out.println("  Updated: " + svc.updateClub(cid, n, pid, max).reportLine());
                    }
                    case 8 -> {
                        String cid = InputHelper.readString("Club ID to remove: ");
                        System.out.println(svc.removeClub(cid) ? "  Removed." : "  Not found.");
                    }
                    case 9 -> {
                        // Clubs and memberships are different classes, but both are Reportable,
                        // so one loop can print them all (polymorphism).
                        List<Reportable> records = new ArrayList<>();
                        records.addAll(svc.allClubs());
                        records.addAll(svc.allMemberships());
                        if (records.isEmpty()) System.out.println("  No records yet.");
                        for (Reportable r : records) System.out.println("  " + r.reportLine());
                    }
                    case 0 -> { return; }
                }
            } catch (RuntimeException ex) { System.out.println("  Error: " + ex.getMessage()); }
        }
    }
}
