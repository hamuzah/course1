package ug.stleoskyegobe.g06.clubs;

/**
 * Custom exception thrown when a student tries to join a club that has no free places.
 *
 * @author Nume Allan
 */
public class ClubFullException extends RuntimeException {
    public ClubFullException(String msg) { super(msg); }
}
