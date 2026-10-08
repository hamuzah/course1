package ug.stleoskyegobe.g06.students;

/**
 * Custom exception thrown when a student lookup fails.
 * @author Kiggundu Hamuza
 */
public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException(String id) {
        super("No student found with ID: " + id);
    }
}
