package ug.stleoskyegobe.g06.boarding;

import ug.stleoskyegobe.g06.core.Record;
import ug.stleoskyegobe.g06.core.Validation;
import ug.stleoskyegobe.g06.core.Reportable;

/**
 * Model class for a dormitory (sleeping block) belonging to a house.
 * Fully encapsulated — fields are private, setters validate and throw.
 *
 * @author Atee Joan
 */
public class Dormitory extends Record implements Reportable {

    private String name;
    private String house;        // e.g. "Kiwanuka House"
    private int    capacity;     // 1..300 beds
    private int    bedsTaken;

    public Dormitory(String id, String name, String house, int capacity) {
        super(id);
        setName(name);
        setHouse(house);
        setCapacity(capacity);
        this.bedsTaken = 0;
    }

    public String getName()      { return name; }
    public String getHouse()     { return house; }
    public int    getCapacity()  { return capacity; }
    public int    getBedsTaken() { return bedsTaken; }
    public int    freeBeds()     { return capacity - bedsTaken; }
    public boolean isFull()      { return freeBeds() == 0; }

    public final void setName(String n) {
        this.name = Validation.text(n, "Dormitory name");
    }
    public final void setHouse(String h) {
        this.house = Validation.text(h, "House");
    }
    public final void setCapacity(int c) {
        if (c < 1 || c > 300) throw new IllegalArgumentException("Capacity must be between 1 and 300 beds");
        if (c < bedsTaken)
            throw new IllegalArgumentException("Cannot have fewer beds than are taken (" + bedsTaken + ")");
        this.capacity = c;
    }

    void takeBed() {
        if (isFull())
            throw new DormitoryFullException("Dormitory " + name + " is full (" + capacity + " beds)");
        bedsTaken++;
    }
    void freeBed() { if (bedsTaken > 0) bedsTaken--; }

    @Override public String describe() {
        return getId() + "  " + name + " (" + house + ")  " + freeBeds() + "/" + capacity + " beds free";
    }
    @Override public String reportLine() {
        return String.format("%-10s %-20s %-20s %3d/%3d beds free",
                getId(), name, house, freeBeds(), capacity);
    }

    public String toCsv() {
        return String.join("|", getId(), name, house, String.valueOf(capacity));
    }

    public static Dormitory fromCsv(String line) {
        String[] p = line.split("\\|");
        if (p.length != 4) throw new IllegalArgumentException("Bad dormitory record");
        return new Dormitory(p[0], p[1], p[2], Integer.parseInt(p[3]));
    }
}
