package ug.stleoskyegobe.g06.library;

import ug.stleoskyegobe.g06.core.Record;
import ug.stleoskyegobe.g06.core.Validation;
import ug.stleoskyegobe.g06.core.Reportable;

/** @author Mutegeki Isaac Bob */
public class Book extends Record implements Reportable {

    private String title;
    private String author;
    private int    copiesTotal;
    private int    copiesOnLoan;

    public Book(String id, String title, String author, int copiesTotal) {
        super(id);
        setTitle(title);
        setAuthor(author);
        setCopiesTotal(copiesTotal);
        this.copiesOnLoan = 0;
    }

    public String getTitle()        { return title; }
    public String getAuthor()       { return author; }
    public int    getCopiesTotal()  { return copiesTotal; }
    public int    getCopiesOnLoan() { return copiesOnLoan; }
    public int    copiesAvailable() { return copiesTotal - copiesOnLoan; }

    public final void setTitle(String t) {
        this.title = Validation.text(t, "Title");
    }
    public final void setAuthor(String a) {
        this.author = Validation.text(a, "Author");
    }
    public final void setCopiesTotal(int n) {
        if (n < 1) throw new IllegalArgumentException("Must have at least 1 copy");
        if (n < copiesOnLoan)
            throw new IllegalArgumentException("Cannot have fewer copies than are on loan (" + copiesOnLoan + ")");
        this.copiesTotal = n;
    }

    void checkOut() {
        if (copiesAvailable() == 0)
            throw new BookNotAvailableException("No copies of '" + title + "' available");
        copiesOnLoan++;
    }
    void checkIn() { if (copiesOnLoan > 0) copiesOnLoan--; }

    @Override public String describe() {
        return getId() + "  " + title + " (" + copiesAvailable() + "/" + copiesTotal + " free)";
    }
    @Override public String reportLine() {
        return String.format("%-10s %-30s %-20s %d/%d free",
                getId(), title, author, copiesAvailable(), copiesTotal);
    }

    public String toCsv() {
        return String.join("|", getId(), title, author,
                String.valueOf(copiesTotal), String.valueOf(copiesOnLoan));
    }
}
