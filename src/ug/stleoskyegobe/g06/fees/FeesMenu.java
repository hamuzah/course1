package ug.stleoskyegobe.g06.fees;

import ug.stleoskyegobe.g06.core.InputHelper;
import ug.stleoskyegobe.g06.core.Reportable;
import ug.stleoskyegobe.g06.students.Student;
import ug.stleoskyegobe.g06.students.StudentService;

import java.util.ArrayList;
import java.util.List;

/** @author Nabagereka Dorah */
public class FeesMenu {

    private final FeesService fees;
    private final StudentService students;

    public FeesMenu(FeesService fees, StudentService students) {
        this.fees = fees;
        this.students = students;
    }

    public void show() {
        while (true) {
            System.out.println("\n=== School Fees ===");
            System.out.println("1. Open fee account for a student");
            System.out.println("2. Record a payment");
            System.out.println("3. List all accounts");
            System.out.println("4. Report: biggest debts first");
            System.out.println("5. Show total owed by all students");
            System.out.println("6. Remove account");
            System.out.println("7. Update account: add a new billing");
            System.out.println("8. Report: all accounts and payments");
            System.out.println("0. Back to main menu");

            int choice = InputHelper.readInt("Choose: ", 0, 8);
            try {
                switch (choice) {
                    case 1 -> openAccount();
                    case 2 -> recordPayment();
                    case 3 -> listAll();
                    case 4 -> reportDebt();
                    case 5 -> showTotal();
                    case 6 -> removeAccount();
                    case 7 -> addBilling();
                    case 8 -> reportAllRecords();
                    case 0 -> { return; }
                }
            } catch (RuntimeException ex) {
                System.out.println("  Error: " + ex.getMessage());
            }
        }
    }

    private void openAccount() {
        String sid = InputHelper.readString("Student ID: ");
        Student s = students.findById(sid);
        double billed = InputHelper.readMoney("Total billed (UGX): ");
        FeeAccount a = fees.openAccount(s, billed);
        System.out.println("  Opened: " + a.describe());
    }

    private void recordPayment() {
        String aid    = InputHelper.readString("Fee account ID (e.g. G06-F001): ");
        double amount = InputHelper.readMoney("Amount paid: ");
        String method = InputHelper.readString("Method (Cash/MoMo/Bank): ");
        Payment p = fees.recordPayment(aid, amount, method);
        System.out.println("  Recorded: " + p.describe());
    }

    private void listAll() {
        if (fees.allAccounts().isEmpty()) { System.out.println("  No fee accounts yet."); return; }
        for (FeeAccount a : fees.allAccounts()) System.out.println("  " + a.reportLine());
    }

    private void reportDebt() {
        for (FeeAccount a : fees.reportByDebtDesc()) System.out.println("  " + a.reportLine());
    }

    private void showTotal() {
        System.out.printf("  Total owed across all students: UGX %,.0f%n", fees.totalOwed());
    }

    private void removeAccount() {
        String aid = InputHelper.readString("Account ID to remove: ");
        System.out.println(fees.removeAccount(aid) ? "  Removed." : "  Not found.");
    }

    private void addBilling() {
        String aid    = InputHelper.readString("Fee account ID: ");
        double amount = InputHelper.readMoney("Amount to bill (UGX): ");
        FeeAccount a = fees.addBilling(aid, amount);
        System.out.println("  Updated: " + a.reportLine());
    }

    private void reportAllRecords() {
        // Accounts and payments are different classes, but both are Reportable,
        // so one loop can print them all (polymorphism).
        List<Reportable> records = new ArrayList<>();
        records.addAll(fees.allAccounts());
        records.addAll(fees.allPayments());
        if (records.isEmpty()) { System.out.println("  No records yet."); return; }
        for (Reportable r : records) System.out.println("  " + r.reportLine());
    }
}
