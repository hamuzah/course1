package ug.stleoskyegobe.g06.fees;

import ug.stleoskyegobe.g06.core.FileStore;
import ug.stleoskyegobe.g06.core.Payable;
import ug.stleoskyegobe.g06.students.Student;
import ug.stleoskyegobe.g06.students.StudentService;

import java.time.LocalDate;
import java.util.*;

/**
 * Service class managing FeeAccounts and Payments.
 * @author Nabagereka Dorah
 */
public class FeesService {

    private static final String ACC_FILE = "fee_accounts.txt";
    private static final String PAY_FILE = "payments.txt";

    private final List<FeeAccount> accounts = new ArrayList<>();
    private final List<Payment>    payments = new ArrayList<>();
    private final StudentService   studentService;
    private int nextAccSeq = 1, nextPaySeq = 1;

    public FeesService(StudentService studentService) {
        this.studentService = studentService;
        loadAccounts();
        loadPayments();
        studentService.preventRemovalWhile(st -> {
            for (FeeAccount a : accounts)
                if (a.getStudent().getId().equalsIgnoreCase(st.getId())) return "a fee account";
            return null;
        });
    }

    public String nextAccountId() { return String.format("G06-F%03d", nextAccSeq++); }
    public String nextPaymentId() { return String.format("G06-P%03d", nextPaySeq++); }

    public FeeAccount openAccount(Student s, double billed) {
        for (FeeAccount existing : accounts)
            if (existing.getStudent().getId().equalsIgnoreCase(s.getId()))
                throw new IllegalArgumentException(s.getName() + " already has fee account " + existing.getId());
        FeeAccount a = new FeeAccount(nextAccountId(), s, billed);
        accounts.add(a);
        saveAccounts();
        return a;
    }

    public FeeAccount findAccount(String id) {
        for (FeeAccount a : accounts) if (a.getId().equalsIgnoreCase(id)) return a;
        throw new NoSuchElementException("No fee account with ID: " + id);
    }

    public List<FeeAccount> allAccounts() { return Collections.unmodifiableList(accounts); }
    public List<Payment>    allPayments() { return Collections.unmodifiableList(payments); }

    public Payment recordPayment(String accountId, double amount, String method) {
        FeeAccount acc = findAccount(accountId);
        // Build (and so validate) the Payment first, so a rejected amount or
        // method never changes the balance or uses up a payment ID.
        String id = String.format("G06-P%03d", nextPaySeq);   // reserve only on success
        Payment p = new Payment(id, acc, amount, LocalDate.now(), method);
        acc.pay(amount);                   // throws InsufficientPaymentException if bad
        nextPaySeq++;
        payments.add(p);
        saveAccounts();
        savePayments();
        return p;
    }

    /** Update an account by billing it a further amount (e.g. a new term's fees). */
    public FeeAccount addBilling(String accountId, double amount) {
        FeeAccount acc = findAccount(accountId);
        acc.addBilling(amount);            // throws IllegalArgumentException if not positive
        saveAccounts();
        return acc;
    }

    public boolean removeAccount(String id) {
        boolean removed = accounts.removeIf(a -> a.getId().equalsIgnoreCase(id));
        if (removed) {
            payments.removeIf(p -> p.getAccount().getId().equalsIgnoreCase(id));
            saveAccounts();
            savePayments();
        }
        return removed;
    }

    /** Polymorphic loop: compute total owed across any Payable. */
    public double totalOwed() {
        double total = 0;
        for (Payable p : accounts) total += p.outstanding();   // mixed-type-ready loop
        return total;
    }

    /** Sorted report by amount outstanding (biggest debt first). */
    public List<FeeAccount> reportByDebtDesc() {
        List<FeeAccount> copy = new ArrayList<>(accounts);
        copy.sort(Comparator.comparingDouble(FeeAccount::outstanding).reversed());
        return copy;
    }

    private void loadAccounts() {
        List<FeeAccount> loaded = FileStore.loadAll(ACC_FILE, line -> {
            String[] p = line.split("\\|");
            Student s = studentService.findById(p[1]);
            FeeAccount a = new FeeAccount(p[0], s, Double.parseDouble(p[2]));
            double paid = Double.parseDouble(p[3]);
            if (paid > 0) a.pay(paid);          // replay the paid balance
            return a;
        });
        accounts.addAll(loaded);
        for (FeeAccount a : accounts) {
            try {
                int n = Integer.parseInt(a.getId().substring(a.getId().lastIndexOf('F') + 1));
                if (n >= nextAccSeq) nextAccSeq = n + 1;
            } catch (Exception ignored) { }
        }
    }

    private void loadPayments() {
        List<Payment> loaded = FileStore.loadAll(PAY_FILE, line -> {
            String[] p = line.split("\\|");
            FeeAccount acc = findAccount(p[1]);
            return new Payment(p[0], acc, Double.parseDouble(p[2]),
                               LocalDate.parse(p[3]), p[4]);
        });
        payments.addAll(loaded);
        for (Payment p : payments) {
            try {
                int n = Integer.parseInt(p.getId().substring(p.getId().lastIndexOf('P') + 1));
                if (n >= nextPaySeq) nextPaySeq = n + 1;
            } catch (Exception ignored) { }
        }
    }

    private void saveAccounts() { FileStore.saveAll(ACC_FILE, accounts, FeeAccount::toCsv); }
    private void savePayments() { FileStore.saveAll(PAY_FILE, payments, Payment::toCsv); }
}
