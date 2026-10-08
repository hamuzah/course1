package ug.stleoskyegobe.g06.staff;

import ug.stleoskyegobe.g06.core.FileStore;

import java.time.LocalDate;
import java.util.*;

/** @author Namawejje Jovia */
public class StaffService {

    private static final String STAFF_FILE = "staff.txt";
    private static final String ATT_FILE   = "attendance.txt";

    private final List<Staff>      staff = new ArrayList<>();
    private final List<Attendance> att   = new ArrayList<>();
    private final List<java.util.function.Function<Staff, String>> removalChecks = new ArrayList<>();
    private int nextStaffSeq = 1, nextAttSeq = 1;

    public StaffService() {
        staff.addAll(FileStore.loadAll(STAFF_FILE, line -> {
            String[] p = line.split("\\|");
            return new Staff(p[0], p[1], p[2], Double.parseDouble(p[3]));
        }));
        for (Staff s : staff) {
            try { int n = Integer.parseInt(s.getId().substring(s.getId().lastIndexOf('T') + 1));
                if (n >= nextStaffSeq) nextStaffSeq = n + 1;
            } catch (Exception ignored) { }
        }
        att.addAll(FileStore.loadAll(ATT_FILE, line -> {
            String[] p = line.split("\\|");
            Staff s = findById(p[1]);
            return new Attendance(p[0], s, LocalDate.parse(p[2]), Boolean.parseBoolean(p[3]));
        }));
        for (Attendance a : att) {
            try { int n = Integer.parseInt(a.getId().substring(a.getId().lastIndexOf('A') + 1));
                if (n >= nextAttSeq) nextAttSeq = n + 1;
            } catch (Exception ignored) { }
        }
    }

    public String nextStaffId() { return String.format("G06-T%03d", nextStaffSeq++); }
    public String nextAttId()   { return String.format("G06-A%03d", nextAttSeq++); }

    public Staff add(String name, String role, double salary) {
        for (Staff s : staff)
            if (s.getName().equalsIgnoreCase(name) && s.getRole().equalsIgnoreCase(role))
                throw new DuplicateStaffException("Already registered: " + name + " / " + role);
        Staff s = new Staff(nextStaffId(), name, role, salary);
        staff.add(s); save(); return s;
    }

    public Staff findById(String id) {
        for (Staff s : staff) if (s.getId().equalsIgnoreCase(id)) return s;
        throw new NoSuchElementException("No staff with ID: " + id);
    }

    public List<Staff> all() { return Collections.unmodifiableList(staff); }
    public List<Attendance> allAttendance() { return Collections.unmodifiableList(att); }

    /** Update a staff member. Pass null for anything that should stay as it is. */
    public Staff update(String id, String newName, String newRole, Double newSalary) {
        Staff s = findById(id);
        // validate on a throw-away copy first so a rejected value changes nothing
        new Staff(s.getId(),
                newName   != null ? newName   : s.getName(),
                newRole   != null ? newRole   : s.getRole(),
                newSalary != null ? newSalary : s.getSalary());
        if (newName   != null) s.setName(newName);
        if (newRole   != null) s.setRole(newRole);
        if (newSalary != null) s.setSalary(newSalary);
        save();
        return s;
    }

    public Attendance mark(String staffId, boolean present) {
        Staff s = findById(staffId);
        Attendance a = new Attendance(nextAttId(), s, LocalDate.now(), present);
        att.add(a); saveAtt();
        return a;
    }

    /** Other modules register a check; it returns a description of what still uses the staff member, or null. */
    public void preventRemovalWhile(java.util.function.Function<Staff, String> check) {
        removalChecks.add(check);
    }

    public boolean remove(String id) {
        Staff target = null;
        for (Staff s : staff) if (s.getId().equalsIgnoreCase(id)) { target = s; break; }
        if (target == null) return false;
        for (java.util.function.Function<Staff, String> check : removalChecks) {
            String reason = check.apply(target);
            if (reason != null)
                throw new IllegalStateException("Cannot remove " + target.getName()
                        + ": still linked to " + reason + ". Remove that record first.");
        }
        boolean r = staff.removeIf(s -> s.getId().equalsIgnoreCase(id));
        if (r) { att.removeIf(a -> a.getStaff().getId().equalsIgnoreCase(id)); save(); saveAtt(); }
        return r;
    }

    /** Sorted report by salary (highest first). */
    public List<Staff> reportBySalary() {
        List<Staff> copy = new ArrayList<>(staff);
        copy.sort(Comparator.comparingDouble(Staff::getSalary).reversed());
        return copy;
    }

    private void save()    { FileStore.saveAll(STAFF_FILE, staff, Staff::toCsv); }
    private void saveAtt() { FileStore.saveAll(ATT_FILE, att, Attendance::toCsv); }
}
