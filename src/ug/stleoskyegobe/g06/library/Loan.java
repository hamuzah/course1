package ug.stleoskyegobe.g06.library;

import ug.stleoskyegobe.g06.core.Record;
import ug.stleoskyegobe.g06.core.Reportable;
import ug.stleoskyegobe.g06.students.Student;

import java.time.LocalDate;

/**
 * A Loan links a Student (Students module) to a Book — cross-module reference.
 * @author Mutegeki Isaac Bob
 */
public class Loan extends Record implements Reportable {

    private final Student student;
    private final Book    book;
    private final LocalDate borrowedOn;
    private LocalDate       returnedOn;    // null while still out

    public Loan(String id, Student student, Book book, LocalDate borrowedOn) {
        super(id);
        if (student == null || book == null) throw new IllegalArgumentException("Loan needs student + book");
        if (borrowedOn == null) throw new IllegalArgumentException("Loan date required");
        this.student = student;
        this.book = book;
        this.borrowedOn = borrowedOn;
    }

    public Student   getStudent()    { return student; }
    public Book      getBook()       { return book; }
    public LocalDate getBorrowedOn() { return borrowedOn; }
    public LocalDate getReturnedOn() { return returnedOn; }
    public boolean   isReturned()    { return returnedOn != null; }

    public void returnOn(LocalDate date) {
        if (returnedOn != null) throw new IllegalStateException("Already returned");
        if (date == null) throw new IllegalArgumentException("Return date required");
        this.returnedOn = date;
    }

    @Override public String describe() {
        return getId() + "  " + student.getName() + " -> " + book.getTitle()
             + (isReturned() ? " (returned)" : " (OUT)");
    }

    @Override public String reportLine() {
        return String.format("%-10s %-20s %-25s %s  %s",
                getId(), student.getName(), book.getTitle(), borrowedOn,
                isReturned() ? "returned " + returnedOn : "OUT");
    }

    public String toCsv() {
        return String.join("|", getId(), student.getId(), book.getId(),
                borrowedOn.toString(), returnedOn == null ? "-" : returnedOn.toString());
    }
}
