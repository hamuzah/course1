package ug.stleoskyegobe.g06.clubs;

import ug.stleoskyegobe.g06.core.FileStore;
import ug.stleoskyegobe.g06.staff.Staff;
import ug.stleoskyegobe.g06.staff.StaffService;
import ug.stleoskyegobe.g06.students.Student;
import ug.stleoskyegobe.g06.students.StudentService;

import java.time.LocalDate;
import java.util.*;

/**
 * Service class — manages clubs and memberships in ArrayLists.
 * Loads from file at start-up and saves after every change.
 *
 * @author Nume Allan
 */
public class ClubService {

    private static final String CLUB_FILE = "clubs.txt";
    private static final String MEM_FILE  = "memberships.txt";

    private final List<Club>       clubs = new ArrayList<>();
    private final List<Membership> mems  = new ArrayList<>();
    private final StudentService students;
    private final StaffService   staff;
    private int nextClubSeq = 1, nextMemSeq = 1;

    public ClubService(StudentService students, StaffService staff) {
        this.students = students;
        this.staff = staff;
        loadClubs();
        loadMemberships();
        students.preventRemovalWhile(st -> {
            for (Membership m : mems)
                if (m.getStudent().getId().equalsIgnoreCase(st.getId())) return "a club membership";
            return null;
        });
        staff.preventRemovalWhile(sf -> {
            for (Club c : clubs)
                if (c.getPatron().getId().equalsIgnoreCase(sf.getId())) return "a club (as patron)";
            return null;
        });
    }

    public String nextClubId()       { return String.format("G06-C%03d", nextClubSeq++); }
    public String nextMembershipId() { return String.format("G06-J%03d", nextMemSeq++); }

    public Club addClub(String name, String patronStaffId, int maxMembers) {
        for (Club c : clubs)
            if (c.getName().equalsIgnoreCase(name.trim()))
                throw new IllegalArgumentException("A club called '" + name.trim() + "' already exists");
        Staff patron = staff.findById(patronStaffId);     // throws NoSuchElementException if missing
        Club c = new Club(nextClubId(), name, patron, maxMembers);
        clubs.add(c); saveClubs(); return c;
    }

    public Club findClub(String id) {
        for (Club c : clubs) if (c.getId().equalsIgnoreCase(id)) return c;
        throw new NoSuchElementException("No club with ID: " + id);
    }

    /** Update a club. Pass null for anything that should stay as it is. */
    public Club updateClub(String id, String newName, String newPatronStaffId, Integer newMaxMembers) {
        Club c = findClub(id);
        if (newName != null) {
            for (Club other : clubs)
                if (other != c && other.getName().equalsIgnoreCase(newName.trim()))
                    throw new IllegalArgumentException("A club called '" + newName.trim() + "' already exists");
            c.setName(newName);
        }
        if (newPatronStaffId != null) c.setPatron(staff.findById(newPatronStaffId));
        if (newMaxMembers    != null) c.setMaxMembers(newMaxMembers);
        saveClubs();
        return c;
    }

    /** Remove a club and its old memberships. Refused while it still has active members. */
    public boolean removeClub(String id) {
        for (Club c : clubs) if (c.getId().equalsIgnoreCase(id)) {
            if (c.getMembersCount() > 0)
                throw new IllegalStateException("Cannot remove " + c.getName() + ": " + c.getMembersCount()
                        + " active member(s)");
            mems.removeIf(m -> m.getClub() == c);
            clubs.remove(c);
            saveClubs(); saveMems();
            return true;
        }
        return false;
    }

    public List<Club>       allClubs()       { return Collections.unmodifiableList(clubs); }
    public List<Membership> allMemberships() { return Collections.unmodifiableList(mems); }

    public Membership join(String studentId, String clubId, String position) {
        Student s = students.findById(studentId);     // throws StudentNotFoundException if missing
        Club c = findClub(clubId);
        for (Membership m : mems)
            if (m.isActive() && m.getStudent() == s && m.getClub() == c)
                throw new IllegalStateException(s.getName() + " is already a member of " + c.getName()
                        + " (" + m.getId() + ")");
        // build the membership first: a refused position or a full club uses up no ID number
        Membership m = new Membership(String.format("G06-J%03d", nextMemSeq), s, c, LocalDate.now(), position);
        c.join();                                     // throws ClubFullException if full
        nextMemSeq++;
        mems.add(m);
        saveClubs(); saveMems();
        return m;
    }

    public void leave(String membershipId) {
        for (Membership m : mems) if (m.getId().equalsIgnoreCase(membershipId)) {
            m.leave(LocalDate.now());                 // throws IllegalStateException if already left
            m.getClub().leave();
            saveClubs(); saveMems();
            return;
        }
        throw new NoSuchElementException("No membership with ID: " + membershipId);
    }

    /** Sorted report: clubs with most members first, then by name. */
    public List<Club> reportBySize() {
        List<Club> copy = new ArrayList<>(clubs);
        copy.sort(Comparator.comparingInt(Club::getMembersCount).reversed()
                .thenComparing(Club::getName, String.CASE_INSENSITIVE_ORDER));
        return copy;
    }

    /** Filtered report: active memberships of one club, sorted by student name. */
    public List<Membership> membersOfClub(String clubId) {
        Club c = findClub(clubId);
        List<Membership> out = new ArrayList<>();
        for (Membership m : mems) if (m.isActive() && m.getClub() == c) out.add(m);
        out.sort(Comparator.comparing(m -> m.getStudent().getName(), String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    private void loadClubs() {
        clubs.addAll(FileStore.loadAll(CLUB_FILE, line -> {
            String[] p = line.split("\\|");
            if (p.length != 4) throw new IllegalArgumentException("Bad club record");
            Staff patron = staff.findById(p[2]);
            return new Club(p[0], p[1], patron, Integer.parseInt(p[3]));
        }));
        for (Club c : clubs) {
            try { int n = Integer.parseInt(c.getId().substring(c.getId().lastIndexOf('C') + 1));
                if (n >= nextClubSeq) nextClubSeq = n + 1;
            } catch (Exception ignored) { }
        }
    }
    private void loadMemberships() {
        // memberships are loaded after clubs because each one points to a club and a student
        mems.addAll(FileStore.loadAll(MEM_FILE, line -> {
            String[] p = line.split("\\|");
            if (p.length != 6) throw new IllegalArgumentException("Bad membership record");
            Student s = students.findById(p[1]);
            Club c = findClub(p[2]);
            Membership m = new Membership(p[0], s, c, LocalDate.parse(p[4]), p[3]);
            if (!"-".equals(p[5])) m.leave(LocalDate.parse(p[5]));
            else c.join();                            // rebuild the member count from active memberships
            return m;
        }));
        for (Membership m : mems) {
            try { int n = Integer.parseInt(m.getId().substring(m.getId().lastIndexOf('J') + 1));
                if (n >= nextMemSeq) nextMemSeq = n + 1;
            } catch (Exception ignored) { }
        }
    }
    private void saveClubs() { FileStore.saveAll(CLUB_FILE, clubs, Club::toCsv); }
    private void saveMems()  { FileStore.saveAll(MEM_FILE, mems, Membership::toCsv); }
}
