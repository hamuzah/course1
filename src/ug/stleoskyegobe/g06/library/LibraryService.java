package ug.stleoskyegobe.g06.library;

import ug.stleoskyegobe.g06.core.FileStore;
import ug.stleoskyegobe.g06.students.Student;
import ug.stleoskyegobe.g06.students.StudentService;

import java.time.LocalDate;
import java.util.*;

/** @author Mutegeki Isaac Bob */
public class LibraryService {

    private static final String BOOK_FILE = "books.txt";
    private static final String LOAN_FILE = "loans.txt";

    private final List<Book> books = new ArrayList<>();
    private final List<Loan> loans = new ArrayList<>();
    private final StudentService students;
    private int nextBookSeq = 1, nextLoanSeq = 1;

    public LibraryService(StudentService students) {
        this.students = students;
        loadBooks();
        loadLoans();
        students.preventRemovalWhile(st -> {
            for (Loan l : loans)
                if (l.getStudent().getId().equalsIgnoreCase(st.getId())) return "a library loan";
            return null;
        });
    }

    public String nextBookId() { return String.format("G06-B%03d", nextBookSeq++); }
    public String nextLoanId() { return String.format("G06-L%03d", nextLoanSeq++); }

    public Book addBook(String title, String author, int copies) {
        Book b = new Book(nextBookId(), title, author, copies);
        books.add(b); saveBooks(); return b;
    }

    public Book findBook(String id) {
        for (Book b : books) if (b.getId().equalsIgnoreCase(id)) return b;
        throw new NoSuchElementException("No book with ID: " + id);
    }

    /** Update a book. Pass null for anything that should stay as it is. */
    public Book updateBook(String id, String newTitle, String newAuthor, Integer newCopies) {
        Book b = findBook(id);
        if (newTitle  != null) b.setTitle(newTitle);
        if (newAuthor != null) b.setAuthor(newAuthor);
        if (newCopies != null) b.setCopiesTotal(newCopies);
        saveBooks();
        return b;
    }

    /** Remove a book and its finished loans. Refused while any copy is still out. */
    public boolean removeBook(String id) {
        for (Book b : books) if (b.getId().equalsIgnoreCase(id)) {
            if (b.getCopiesOnLoan() > 0)
                throw new BookNotAvailableException("Cannot remove '" + b.getTitle() + "': copies are still on loan");
            loans.removeIf(l -> l.getBook() == b);
            books.remove(b);
            saveBooks(); saveLoans();
            return true;
        }
        return false;
    }

    public List<Book> allBooks() { return Collections.unmodifiableList(books); }
    public List<Loan> allLoans() { return Collections.unmodifiableList(loans); }

    public Loan borrow(String studentId, String bookId) {
        Student s = students.findById(studentId);
        Book b = findBook(bookId);
        b.checkOut();
        Loan l = new Loan(nextLoanId(), s, b, LocalDate.now());
        loans.add(l);
        saveBooks(); saveLoans();
        return l;
    }

    public void returnBook(String loanId) {
        for (Loan l : loans) if (l.getId().equalsIgnoreCase(loanId)) {
            l.returnOn(LocalDate.now());
            l.getBook().checkIn();
            saveBooks(); saveLoans();
            return;
        }
        throw new NoSuchElementException("No loan with ID: " + loanId);
    }

    /** Report filtered to books with low stock, sorted by availability. */
    public List<Book> reportLowStock(int threshold) {
        List<Book> out = new ArrayList<>();
        for (Book b : books) if (b.copiesAvailable() <= threshold) out.add(b);
        out.sort(Comparator.comparingInt(Book::copiesAvailable));
        return out;
    }

    private void loadBooks() {
        books.addAll(FileStore.loadAll(BOOK_FILE, line -> {
            String[] p = line.split("\\|");
            Book b = new Book(p[0], p[1], p[2], Integer.parseInt(p[3]));
            int on = Integer.parseInt(p[4]);
            for (int i = 0; i < on; i++) b.checkOut();
            return b;
        }));
        for (Book b : books) {
            try { int n = Integer.parseInt(b.getId().substring(b.getId().lastIndexOf('B') + 1));
                if (n >= nextBookSeq) nextBookSeq = n + 1;
            } catch (Exception ignored) { }
        }
    }
    private void loadLoans() {
        loans.addAll(FileStore.loadAll(LOAN_FILE, line -> {
            String[] p = line.split("\\|");
            Student s = students.findById(p[1]);
            Book b = findBook(p[2]);
            Loan l = new Loan(p[0], s, b, LocalDate.parse(p[3]));
            if (!"-".equals(p[4])) l.returnOn(LocalDate.parse(p[4]));
            return l;
        }));
        for (Loan l : loans) {
            try { int n = Integer.parseInt(l.getId().substring(l.getId().lastIndexOf('L') + 1));
                if (n >= nextLoanSeq) nextLoanSeq = n + 1;
            } catch (Exception ignored) { }
        }
    }
    private void saveBooks() { FileStore.saveAll(BOOK_FILE, books, Book::toCsv); }
    private void saveLoans() { FileStore.saveAll(LOAN_FILE, loans, Loan::toCsv); }
}
