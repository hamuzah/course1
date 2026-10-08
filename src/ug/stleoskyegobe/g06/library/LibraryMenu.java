package ug.stleoskyegobe.g06.library;

import ug.stleoskyegobe.g06.core.InputHelper;
import ug.stleoskyegobe.g06.core.Reportable;

import java.util.ArrayList;
import java.util.List;

/** @author Mutegeki Isaac Bob */
public class LibraryMenu {

    private final LibraryService lib;

    public LibraryMenu(LibraryService lib) { this.lib = lib; }

    public void show() {
        while (true) {
            System.out.println("\n=== Library ===");
            System.out.println("1. Add book");
            System.out.println("2. List books");
            System.out.println("3. Borrow a book");
            System.out.println("4. Return a book");
            System.out.println("5. Report: low-stock books");
            System.out.println("6. Update book");
            System.out.println("7. Remove book");
            System.out.println("8. Report: all books and loans");
            System.out.println("0. Back to main menu");

            int c = InputHelper.readInt("Choose: ", 0, 8);
            try {
                switch (c) {
                    case 1 -> {
                        String t = InputHelper.readString("Title: ");
                        String a = InputHelper.readString("Author: ");
                        int    n = InputHelper.readInt("Copies: ", 1, 999);
                        System.out.println("  Added: " + lib.addBook(t, a, n).describe());
                    }
                    case 2 -> {
                        if (lib.allBooks().isEmpty()) System.out.println("  No books yet.");
                        else lib.allBooks().forEach(b -> System.out.println("  " + b.reportLine()));
                    }
                    case 3 -> {
                        String sid = InputHelper.readString("Student ID: ");
                        String bid = InputHelper.readString("Book ID: ");
                        System.out.println("  " + lib.borrow(sid, bid).describe());
                    }
                    case 4 -> {
                        String lid = InputHelper.readString("Loan ID: ");
                        lib.returnBook(lid);
                        System.out.println("  Returned.");
                    }
                    case 5 -> {
                        int th = InputHelper.readInt("Threshold (<=): ", 0, 999);
                        lib.reportLowStock(th).forEach(b -> System.out.println("  " + b.reportLine()));
                    }
                    case 6 -> {
                        String bid = InputHelper.readString("Book ID to update: ");
                        System.out.println("  Current: " + lib.findBook(bid).reportLine());
                        String t  = InputHelper.readYesNo("Change title?")  ? InputHelper.readString("New title: ") : null;
                        String a  = InputHelper.readYesNo("Change author?") ? InputHelper.readString("New author: ") : null;
                        Integer n = InputHelper.readYesNo("Change copies?") ? InputHelper.readInt("New total copies: ", 1, 999) : null;
                        System.out.println("  Updated: " + lib.updateBook(bid, t, a, n).reportLine());
                    }
                    case 7 -> {
                        String bid = InputHelper.readString("Book ID to remove: ");
                        System.out.println(lib.removeBook(bid) ? "  Removed." : "  Not found.");
                    }
                    case 8 -> {
                        // Books and loans are different classes, but both are Reportable,
                        // so one loop can print them all (polymorphism).
                        List<Reportable> records = new ArrayList<>();
                        records.addAll(lib.allBooks());
                        records.addAll(lib.allLoans());
                        if (records.isEmpty()) System.out.println("  No records yet.");
                        for (Reportable r : records) System.out.println("  " + r.reportLine());
                    }
                    case 0 -> { return; }
                }
            } catch (RuntimeException ex) { System.out.println("  Error: " + ex.getMessage()); }
        }
    }
}
