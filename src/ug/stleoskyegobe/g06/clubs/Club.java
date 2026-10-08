package ug.stleoskyegobe.g06.clubs;

import ug.stleoskyegobe.g06.core.Record;
import ug.stleoskyegobe.g06.core.Validation;
import ug.stleoskyegobe.g06.core.Reportable;
import ug.stleoskyegobe.g06.staff.Staff;

/**
 * Model class for a club or co-curricular activity, run by a Staff patron
 * (Staff module) — cross-module reference.
 *
 * @author Nume Allan
 */
public class Club extends Record implements Reportable {

    private String name;
    private Staff  patron;        // staff member in charge
    private int    maxMembers;    // 1..200
    private int    membersCount;

    public Club(String id, String name, Staff patron, int maxMembers) {
        super(id);
        setName(name);
        setPatron(patron);
        setMaxMembers(maxMembers);
        this.membersCount = 0;
    }

    public String getName()         { return name; }
    public Staff  getPatron()       { return patron; }
    public int    getMaxMembers()   { return maxMembers; }
    public int    getMembersCount() { return membersCount; }
    public int    freePlaces()      { return maxMembers - membersCount; }
    public boolean isFull()         { return freePlaces() == 0; }

    public final void setName(String n) {
        this.name = Validation.text(n, "Club name");
    }
    public final void setPatron(Staff p) {
        if (p == null) throw new IllegalArgumentException("Club needs a staff patron");
        this.patron = p;
    }
    public final void setMaxMembers(int m) {
        if (m < 1 || m > 200) throw new IllegalArgumentException("Maximum members must be between 1 and 200");
        if (m < membersCount)
            throw new IllegalArgumentException("Cannot have fewer places than current members (" + membersCount + ")");
        this.maxMembers = m;
    }

    void join() {
        if (isFull())
            throw new ClubFullException("Club " + name + " is full (" + maxMembers + " members)");
        membersCount++;
    }
    void leave() { if (membersCount > 0) membersCount--; }

    @Override public String describe() {
        return getId() + "  " + name + "  (patron " + patron.getName() + ")  "
             + membersCount + "/" + maxMembers + " members";
    }
    @Override public String reportLine() {
        return String.format("%-10s %-25s %-20s %3d/%3d members",
                getId(), name, patron.getName(), membersCount, maxMembers);
    }

    public String toCsv() {
        return String.join("|", getId(), name, patron.getId(), String.valueOf(maxMembers));
    }
}
