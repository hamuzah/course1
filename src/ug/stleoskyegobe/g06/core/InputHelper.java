package ug.stleoskyegobe.g06.core;

import java.util.Scanner;

/**
 * Shared input helper used by every module.
 * No input anywhere in the program can crash the application —
 * invalid input is rejected and the user is asked again.
 *
 * @author G06 Team
 */
public class InputHelper {

    private static final Scanner SC = new Scanner(System.in);

    private InputHelper() { /* static helper */ }

    /** Reads one line; if the keyboard/input stream has been closed
     *  (Ctrl+Z on Windows, Ctrl+D elsewhere) the program ends cleanly
     *  instead of throwing NoSuchElementException. Every change has
     *  already been saved, so nothing is lost. */
    private static String nextLine() {
        if (!SC.hasNextLine()) {
            System.out.println("\nInput closed. Last saved data retained. Goodbye.");
            System.exit(0);
        }
        return SC.nextLine();
    }

    public static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = nextLine();
            if (line != null && !line.isBlank()) return line.trim();
            System.out.println("  Value cannot be empty. Try again.");
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = nextLine().trim();
            try {
                int n = Integer.parseInt(line);
                if (n < min || n > max) {
                    System.out.println("  Enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return n;
            } catch (NumberFormatException e) {
                System.out.println("  That is not a whole number. Try again.");
            }
        }
    }

    public static double readMoney(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = nextLine().trim();
            try {
                double n = Double.parseDouble(line);
                if (!Double.isFinite(n) || n < 0) {
                    System.out.println("  Enter a finite, non-negative amount.");
                    continue;
                }
                return n;
            } catch (NumberFormatException e) {
                System.out.println("  That is not a valid amount. Try again.");
            }
        }
    }

    public static boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String line = nextLine().trim().toLowerCase();
            if (line.equals("y") || line.equals("yes")) return true;
            if (line.equals("n") || line.equals("no"))  return false;
            System.out.println("  Please answer y or n.");
        }
    }

    public static void pause() {
        System.out.print("\nPress Enter to continue...");
        nextLine();
    }
}
