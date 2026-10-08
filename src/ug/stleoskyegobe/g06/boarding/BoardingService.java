package ug.stleoskyegobe.g06.boarding;

import ug.stleoskyegobe.g06.core.FileStore;
import ug.stleoskyegobe.g06.students.Student;
import ug.stleoskyegobe.g06.students.StudentService;

import java.time.LocalDate;
import java.util.*;

/**
 * Service class — manages dormitories and bed allocations in ArrayLists.
 * Loads from file at start-up and saves after every change.
 *
 * @author Atee Joan
 */
public class BoardingService {

    private static final String DORM_FILE  = "dormitories.txt";
    private static final String ALLOC_FILE = "allocations.txt";

    private final List<Dormitory>     dorms  = new ArrayList<>();
    private final List<BedAllocation> allocs = new ArrayList<>();
    private final StudentService students;
    private int nextDormSeq = 1, nextAllocSeq = 1;

    public BoardingService(StudentService students) {
        this.students = students;
        loadDormitories();
        loadAllocations();
        students.preventRemovalWhile(st -> {
            for (BedAllocation a : allocs)
                if (a.getStudent().getId().equalsIgnoreCase(st.getId())) return "a bed allocation";
            return null;
        });
    }

    public String nextDormitoryId()  { return String.format("G06-D%03d", nextDormSeq++); }
    public String nextAllocationId() { return String.format("G06-H%03d", nextAllocSeq++); }

    public Dormitory addDormitory(String name, String house, int capacity) {
        for (Dormitory d : dorms)
            if (d.getName().equalsIgnoreCase(name.trim()))
                throw new IllegalArgumentException("A dormitory called '" + name.trim() + "' already exists");
        Dormitory d = new Dormitory(nextDormitoryId(), name, house, capacity);
        dorms.add(d); saveDorms(); return d;
    }

    public Dormitory findDormitory(String id) {
        for (Dormitory d : dorms) if (d.getId().equalsIgnoreCase(id)) return d;
        throw new NoSuchElementException("No dormitory with ID: " + id);
    }

    /** Update a dormitory. Pass null for anything that should stay as it is. */
    public Dormitory updateDormitory(String id, String newName, String newHouse, Integer newCapacity) {
        Dormitory d = findDormitory(id);
        if (newName != null) {
            for (Dormitory other : dorms)
                if (other != d && other.getName().equalsIgnoreCase(newName.trim()))
                    throw new IllegalArgumentException("A dormitory called '" + newName.trim() + "' already exists");
            d.setName(newName);
        }
        if (newHouse    != null) d.setHouse(newHouse);
        if (newCapacity != null) d.setCapacity(newCapacity);
        saveDorms();
        return d;
    }

    /** Remove a dormitory and its old allocations. Refused while any bed is still taken. */
    public boolean removeDormitory(String id) {
        for (Dormitory d : dorms) if (d.getId().equalsIgnoreCase(id)) {
            if (d.getBedsTaken() > 0)
                throw new IllegalStateException("Cannot remove " + d.getName() + ": " + d.getBedsTaken()
                        + " bed(s) still taken");
            allocs.removeIf(a -> a.getDormitory() == d);
            dorms.remove(d);
            saveDorms(); saveAllocs();
            return true;
        }
        return false;
    }

    public List<Dormitory>     allDormitories() { return Collections.unmodifiableList(dorms); }
    public List<BedAllocation> allAllocations() { return Collections.unmodifiableList(allocs); }

    public BedAllocation allocate(String studentId, String dormId, String term) {
        Student s = students.findById(studentId);     // throws StudentNotFoundException if missing
        Dormitory d = findDormitory(dormId);
        for (BedAllocation a : allocs)
            if (a.isActive() && a.getStudent() == s)
                throw new IllegalStateException(s.getName() + " already has a bed in "
                        + a.getDormitory().getName() + " (" + a.getId() + ")");
        // build the allocation first: a refused term or a full dormitory uses up no ID number
        BedAllocation a = new BedAllocation(String.format("G06-H%03d", nextAllocSeq), s, d, LocalDate.now(), term);
        d.takeBed();                                  // throws DormitoryFullException if full
        nextAllocSeq++;
        allocs.add(a);
        saveDorms(); saveAllocs();
        return a;
    }

    public void vacate(String allocationId) {
        for (BedAllocation a : allocs) if (a.getId().equalsIgnoreCase(allocationId)) {
            a.vacate(LocalDate.now());                // throws IllegalStateException if already vacated
            a.getDormitory().freeBed();
            saveDorms(); saveAllocs();
            return;
        }
        throw new NoSuchElementException("No allocation with ID: " + allocationId);
    }

    /** Sorted report: dormitories with most free beds first, then by name. */
    public List<Dormitory> reportByFreeBeds() {
        List<Dormitory> copy = new ArrayList<>(dorms);
        copy.sort(Comparator.comparingInt(Dormitory::freeBeds).reversed()
                .thenComparing(Dormitory::getName, String.CASE_INSENSITIVE_ORDER));
        return copy;
    }

    /** Filtered report: active allocations in one dormitory, sorted by student name. */
    public List<BedAllocation> studentsInDormitory(String dormId) {
        Dormitory d = findDormitory(dormId);
        List<BedAllocation> out = new ArrayList<>();
        for (BedAllocation a : allocs) if (a.isActive() && a.getDormitory() == d) out.add(a);
        out.sort(Comparator.comparing(a -> a.getStudent().getName(), String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    private void loadDormitories() {
        dorms.addAll(FileStore.loadAll(DORM_FILE, Dormitory::fromCsv));
        for (Dormitory d : dorms) {
            try { int n = Integer.parseInt(d.getId().substring(d.getId().lastIndexOf('D') + 1));
                if (n >= nextDormSeq) nextDormSeq = n + 1;
            } catch (Exception ignored) { }
        }
    }
    private void loadAllocations() {
        // allocations are loaded after dormitories because each one points to a dormitory and a student
        allocs.addAll(FileStore.loadAll(ALLOC_FILE, line -> {
            String[] p = line.split("\\|");
            if (p.length != 6) throw new IllegalArgumentException("Bad allocation record");
            Student s = students.findById(p[1]);
            Dormitory d = findDormitory(p[2]);
            BedAllocation a = new BedAllocation(p[0], s, d, LocalDate.parse(p[4]), p[3]);
            if (!"-".equals(p[5])) a.vacate(LocalDate.parse(p[5]));
            else d.takeBed();                         // rebuild the bed count from active allocations
            return a;
        }));
        for (BedAllocation a : allocs) {
            try { int n = Integer.parseInt(a.getId().substring(a.getId().lastIndexOf('H') + 1));
                if (n >= nextAllocSeq) nextAllocSeq = n + 1;
            } catch (Exception ignored) { }
        }
    }
    private void saveDorms()  { FileStore.saveAll(DORM_FILE, dorms, Dormitory::toCsv); }
    private void saveAllocs() { FileStore.saveAll(ALLOC_FILE, allocs, BedAllocation::toCsv); }
}
