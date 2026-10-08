package ug.stleoskyegobe.g06.fees;

import ug.stleoskyegobe.g06.core.Payable;
import ug.stleoskyegobe.g06.core.Record;
import ug.stleoskyegobe.g06.core.Reportable;
import ug.stleoskyegobe.g06.students.Student;

/**
 * A fee account belongs to one Student (cross-module link).
 * Implements Payable (shared interface).
 *
 * @author Nabagereka Dorah
 */
public class FeeAccount extends Record implements Payable, Reportable {

    private final Student student;      // link to Students module
    private double totalBilled;
    private double totalPaid;

    public FeeAccount(String id, Student student, double totalBilled) {
        super(id);
        if (student == null) throw new IllegalArgumentException("Fee account needs a student");
        if (Double.isNaN(totalBilled) || Double.isInfinite(totalBilled) || totalBilled < 0)
            throw new IllegalArgumentException("Billed amount must be a finite number, not negative");
        this.student = student;
        this.totalBilled = totalBilled;
        this.totalPaid = 0;
    }

    public Student getStudent()    { return student; }
    public double  getTotalBilled() { return totalBilled; }
    public double  getTotalPaid()   { return totalPaid; }

    public void addBilling(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0)
            throw new IllegalArgumentException("Billing must be a positive, finite amount");
        totalBilled += amount;
    }

    @Override public boolean pay(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0)
            throw new InsufficientPaymentException("Payment must be greater than zero");
        totalPaid += amount;
        return true;
    }

    @Override public double outstanding() {
        double owed = totalBilled - totalPaid;
        return owed < 0 ? 0 : owed;
    }

    public boolean isCleared() { return outstanding() == 0; }

    @Override public String describe() {
        return getId() + "  " + student.getName() + "  outstanding UGX " + outstanding();
    }

    @Override public String reportLine() {
        return String.format("%-10s %-25s billed %,10.0f  paid %,10.0f  owes %,10.0f",
                getId(), student.getName(), totalBilled, totalPaid, outstanding());
    }

    public String toCsv() {
        return String.join("|", getId(), student.getId(),
                String.valueOf(totalBilled), String.valueOf(totalPaid));
    }
}
