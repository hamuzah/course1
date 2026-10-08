package ug.stleoskyegobe.g06.fees;

import ug.stleoskyegobe.g06.core.Record;
import ug.stleoskyegobe.g06.core.Validation;
import ug.stleoskyegobe.g06.core.Reportable;

import java.time.LocalDate;

/**
 * Second model class — one real payment recorded against a FeeAccount.
 * @author Nabagereka Dorah
 */
public class Payment extends Record implements Reportable {

    private final FeeAccount account;
    private final double amount;
    private final LocalDate paidOn;
    private final String method;         // "Cash", "MoMo", "Bank"

    public Payment(String id, FeeAccount account, double amount, LocalDate paidOn, String method) {
        super(id);
        if (account == null) throw new IllegalArgumentException("Payment needs an account");
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0)
            throw new InsufficientPaymentException("Payment amount must be positive");
        if (paidOn == null) throw new IllegalArgumentException("Payment date required");
                this.account = account;
        this.amount = amount;
        this.paidOn = paidOn;
        if (method == null) throw new IllegalArgumentException("Payment method required");
        String m = method.trim();
        if (m.equalsIgnoreCase("Cash")) this.method = "Cash";
        else if (m.equalsIgnoreCase("MoMo")) this.method = "MoMo";
        else if (m.equalsIgnoreCase("Bank")) this.method = "Bank";
        else throw new IllegalArgumentException("Payment method must be Cash, MoMo or Bank");
    }

    public FeeAccount getAccount() { return account; }
    public double     getAmount()  { return amount; }
    public LocalDate  getPaidOn()  { return paidOn; }
    public String     getMethod()  { return method; }

    @Override public String describe() {
        return getId() + "  UGX " + amount + " via " + method;
    }

    @Override public String reportLine() {
        return String.format("%-10s %-10s %,10.0f  %-6s  %s",
                getId(), account.getId(), amount, method, paidOn);
    }

    public String toCsv() {
        return String.join("|", getId(), account.getId(),
                String.valueOf(amount), paidOn.toString(), method);
    }
}
