package ug.stleoskyegobe.g06.staff;

import ug.stleoskyegobe.g06.core.Record;
import ug.stleoskyegobe.g06.core.Validation;
import ug.stleoskyegobe.g06.core.Reportable;

/** @author Namawejje Jovia */
public class Staff extends Record implements Reportable {

    private String name;
    private String role;         // "Teacher", "Bursar", etc.
    private double salary;

    public Staff(String id, String name, String role, double salary) {
        super(id);
        setName(name); setRole(role); setSalary(salary);
    }

    public String getName()   { return name; }
    public String getRole()   { return role; }
    public double getSalary() { return salary; }

    public final void setName(String n) {
        this.name = Validation.text(n, "Name");
    }
    public final void setRole(String r) {
        this.role = Validation.text(r, "Role");
    }
    public final void setSalary(double s) {
        if (Double.isNaN(s) || Double.isInfinite(s) || s < 0)
            throw new IllegalArgumentException("Salary must be a finite number, not negative");
        this.salary = s;
    }

    @Override public String describe() {
        return getId() + "  " + name + "  (" + role + ")";
    }
    @Override public String reportLine() {
        return String.format("%-10s %-25s %-15s UGX %,10.0f", getId(), name, role, salary);
    }

    public String toCsv() {
        return String.join("|", getId(), name, role, String.valueOf(salary));
    }
}
