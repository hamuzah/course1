package ug.stleoskyegobe.g06.boarding;

/**
 * Custom exception thrown when a bed is requested in a dormitory with no free beds.
 *
 * @author Atee Joan
 */
public class DormitoryFullException extends RuntimeException {
    public DormitoryFullException(String msg) { super(msg); }
}
