package ug.stleoskyegobe.g06.core;

/**
 * Shared interface — anything that can accept a payment (fees, library fines, etc.).
 *
 * @author G06 Team
 */
public interface Payable {
    /** Record a payment and return true if accepted. */
    boolean pay(double amount);

    /** How much is still owed. */
    double outstanding();
}
