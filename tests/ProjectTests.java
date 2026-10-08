import ug.stleoskyegobe.g06.boarding.*;
import ug.stleoskyegobe.g06.clubs.*;
import ug.stleoskyegobe.g06.core.InputHelper;
import ug.stleoskyegobe.g06.fees.*;
import ug.stleoskyegobe.g06.library.*;
import ug.stleoskyegobe.g06.marks.*;
import ug.stleoskyegobe.g06.staff.*;
import ug.stleoskyegobe.g06.students.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Runs the 40-case test plan (report Section 5) plus the further checks,
 * and writes every actual result to the console / TEST_LOG.txt.
 * Run it with tests\run_tests.bat - it works in an empty temporary folder,
 * so the school's data folder is never touched.
 */
public class ProjectTests {

    static int pass = 0, fail = 0;
    static final Path DATA = Paths.get("data");

    // ---------------------------------------------------------------- helpers
    static void log(String s) { System.out.println(s); }

    static void test(String id, String what, String expected, String mustContain, Callable<String> action) {
        String actual;
        try { actual = action.call(); }
        catch (Throwable t) { actual = t.getClass().getSimpleName() + ": " + t.getMessage(); }
        boolean ok = actual != null && actual.contains(mustContain);
        if (ok) pass++; else fail++;
        log((ok ? "[PASS] " : "[FAIL] ") + id + "  " + what);
        log("         expected: " + expected);
        log("         actual  : " + actual.replace("\n", " / "));
    }

    static void reset() throws IOException {
        Files.createDirectories(DATA);
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(DATA)) {
            for (Path p : ds) Files.deleteIfExists(p);
        }
    }

    static String capture(Runnable r) {
        PrintStream old = System.out;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buf, true, StandardCharsets.UTF_8));
        try { r.run(); } finally { System.setOut(old); }
        return buf.toString(StandardCharsets.UTF_8);
    }

    static Student stu(StudentService ss, String name, int year) {
        return ss.add(new Student(ss.nextId(), name, year, "A", "0700000001"));
    }

    /** Start a separate JVM (so System.in can be piped) and return its console output. */
    static String run(String mainClass, String input, Path cwd, String... args) throws Exception {
        List<String> cmd = new ArrayList<>(List.of(
                Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"), mainClass));
        cmd.addAll(Arrays.asList(args));
        ProcessBuilder pb = new ProcessBuilder(cmd).directory(cwd.toFile()).redirectErrorStream(true);
        Process p = pb.start();
        try (OutputStream os = p.getOutputStream()) { os.write(input.getBytes(StandardCharsets.UTF_8)); }
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int code = p.waitFor();
        return out + "\n[exit code " + code + "]";
    }

    static Path freshDir(String name) throws IOException {
        Path d = Paths.get(System.getProperty("java.io.tmpdir"), "g06_" + name + "_" + System.nanoTime());
        Files.createDirectories(d);
        return d;
    }

    /** Keep only the console lines that contain one of the needles (plus the exit code). */
    static String key(String out, String... needles) {
        StringBuilder sb = new StringBuilder();
        for (String line : out.split("\\R")) {
            String t = line.trim();
            for (String n : needles) if (t.contains(n)) { sb.append(sb.length() > 0 ? " / " : "").append(t); break; }
        }
        int i = out.lastIndexOf("[exit code");
        return sb + (i >= 0 ? " | " + out.substring(i).trim() : "");
    }

    static final String MENU = "ug.stleoskyegobe.g06.app.MainMenu";

    // ---------------------------------------------------------------- main
    public static void main(String[] args) throws Exception {
        log("G06 TEST LOG - " + java.time.LocalDateTime.now().withNano(0));
        log("Java " + System.getProperty("java.version") + " | working folder " + Paths.get("").toAbsolutePath());
        log("================================================================");
        log("PART 1 - THE 40-CASE TEST PLAN (report Section 5)");
        log("================================================================");

        // ---- Shared core and main menu
        test("CORE-01", "Main menu: type the letter x", "Rejected, menu asks again, no crash",
             "That is not a whole number", () -> key(run(MENU, "x\n0\n", freshDir("c1")), "not a whole number"));
        test("CORE-02", "new Student(\"X001\", ...) - ID without the group prefix",
             "IllegalArgumentException from Record", "must start with G06-",
             () -> { new Student("X001", "Test", 1, "A", "0700000001"); return "created"; });
        test("CORE-03", "Add a bad line to students.txt and restart",
             "Line skipped with a message; the three good students still load", "3 students loaded",
             () -> {
                 reset();
                 StudentService a = new StudentService();
                 stu(a, "Nakato Grace", 2); stu(a, "Okello Peter", 3); stu(a, "Auma Sarah", 4);
                 Files.writeString(DATA.resolve("students.txt"),
                         Files.readString(DATA.resolve("students.txt")) + "BAD LINE WITHOUT FIELDS\n");
                 StudentService[] b = new StudentService[1];
                 String out = capture(() -> b[0] = new StudentService());
                 boolean msg = out.contains("Skipped corrupt line 4 in students.txt");
                 return (msg ? "message shown; " : "NO MESSAGE; ") + b[0].findAll().size() + " students loaded";
             });
        test("CORE-04", "Delete data/marks.txt and restart", "Program starts normally with an empty marks list",
             "No marks", () -> {
                 reset();
                 StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                 MarksService m = new MarksService(a); m.add(s, "Mathematics", 78);
                 Files.deleteIfExists(DATA.resolve("marks.txt"));
                 MarksService m2 = new MarksService(new StudentService());
                 return m2.allMarks().isEmpty() ? "No marks after restart" : "marks still present";
             });
        test("CORE-05", "Main menu: type 9 (outside 0-7)", "Rejected, menu asks again",
             "Enter a number between 0 and 7", () -> key(run(MENU, "9\n0\n", freshDir("c5")), "Enter a number between"));
        test("CORE-06", "Damaged line in allocations.txt and memberships.txt, restart",
             "Both lines skipped with a message; all other records load", "both skipped",
             () -> {
                 reset();
                 StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                 BoardingService b = new BoardingService(a);
                 Dormitory d = b.addDormitory("Nile Dorm", "St. Charles House", 2);
                 b.allocate(s.getId(), d.getId(), "Term 3 2026");
                 StaffService st = new StaffService(); Staff t = st.add("Mugisha Robert", "Teacher", 900000);
                 ClubService c = new ClubService(a, st);
                 Club club = c.addClub("Debate Club", t.getId(), 2);
                 c.join(s.getId(), club.getId(), "Member");
                 for (String f : new String[]{"allocations.txt", "memberships.txt"})
                     Files.writeString(DATA.resolve(f), Files.readString(DATA.resolve(f)) + "DAMAGED\n");
                 StudentService a2 = new StudentService();
                 BoardingService[] b2 = new BoardingService[1]; ClubService[] c2 = new ClubService[1];
                 String out = capture(() -> {
                     b2[0] = new BoardingService(a2);
                     c2[0] = new ClubService(a2, new StaffService());
                 });
                 boolean both = out.contains("allocations.txt") && out.contains("memberships.txt");
                 return (both ? "both skipped" : "MISSING MESSAGE") + "; allocations loaded="
                         + b2[0].allAllocations().size() + ", memberships loaded=" + c2[0].allMemberships().size();
             });
        test("CORE-07", "Close the input (end of input) at the main menu", "Program ends with a message, no crash",
             "Input closed", () -> key(run(MENU, "", freshDir("c7")), "Input closed"));
        test("CORE-08", "Add student named \"Evil|Name\"", "Refused, because | would split the saved record",
             "cannot contain", () -> { new Student("G06-S900", "Evil|Name", 1, "A", "0700000001"); return "created"; });

        // ---- Students
        test("STU-01", "Add student Nakato Grace, year 2, stream A", "Student created with the next ID",
             "G06-S001", () -> { reset(); StudentService a = new StudentService(); return "Added: " + stu(a, "Nakato Grace", 2).describe(); });
        test("STU-02", "Year input: abc, then 9, then 3 (invalid input)", "Both bad values rejected; 3 accepted",
             "RESULT=3", () -> {
                 String out = run("InputProbe", "abc\n9\n3\n", freshDir("s2"), "int", "1", "6");
                 boolean ok = out.contains("not a whole number") && out.contains("between 1 and 6");
                 return (ok ? "both rejected; " : "NOT REJECTED; ") + key(out, "not a whole number", "between 1 and 6", "RESULT");
             });
        test("STU-03", "Find student G06-S999 (does not exist)", "StudentNotFoundException shown as an error",
             "StudentNotFoundException", () -> { reset(); new StudentService().findById("G06-S999"); return "found"; });
        test("STU-04", "Remove a student who has a fee account", "Removal refused; nothing lost after restart",
             "still linked to a fee account", () -> {
                 reset();
                 StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                 FeesService f = new FeesService(a); f.openAccount(s, 850000);
                 String r;
                 try { a.remove(s.getId()); r = "REMOVED"; } catch (RuntimeException e) { r = "Error: " + e.getMessage(); }
                 int after = new StudentService().findAll().size();
                 return r + " | students after restart: " + after;
             });

        // ---- Fees
        test("FEE-01", "Payment amount -5", "InputHelper rejects negative and asks again", "RESULT=300.0",
             () -> key(run("InputProbe", "-5\n300\n", freshDir("f1"), "money"), "Enter a finite", "RESULT"));
        test("FEE-02", "Record payment with amount 0", "InsufficientPaymentException", "InsufficientPaymentException",
             () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                     FeesService f = new FeesService(a); FeeAccount acc = f.openAccount(s, 850000);
                     f.recordPayment(acc.getId(), 0, "Cash"); return "accepted"; });
        test("FEE-03", "CR-01: add billing of 200,000 to an account billed 850,000 with 300,000 paid",
             "Billed 1,050,000; owes 750,000", "billed 1050000 owes 750000",
             () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                     FeesService f = new FeesService(a); FeeAccount acc = f.openAccount(s, 850000);
                     f.recordPayment(acc.getId(), 300000, "Cash"); f.addBilling(acc.getId(), 200000);
                     return String.format("billed %.0f owes %.0f", acc.getTotalBilled(), acc.outstanding()); });
        test("FEE-04", "Payment amount NaN, then Infinity", "Both refused by InputHelper", "RESULT=500.0",
             () -> { String out = run("InputProbe", "NaN\nInfinity\n500\n", freshDir("f4"), "money");
                     int refusals = out.split("Enter a finite, non-negative amount", -1).length - 1;
                     return refusals + " refusals; " + key(out, "RESULT"); });
        test("FEE-05", "Record payment of 200 by \"Cheque\"", "Refused; balance unchanged",
             "balance 850000", () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                     FeesService f = new FeesService(a); FeeAccount acc = f.openAccount(s, 850000);
                     String r; try { f.recordPayment(acc.getId(), 200, "Cheque"); r = "ACCEPTED"; }
                     catch (RuntimeException e) { r = "Error: " + e.getMessage(); }
                     return r + " | balance " + String.format("%.0f", acc.outstanding()); });
        test("FEE-06", "Open a second fee account for the same student", "Refused", "already has fee account",
             () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                     FeesService f = new FeesService(a); f.openAccount(s, 850000);
                     f.openAccount(s, 100000); return "second account accepted"; });

        // ---- Marks
        test("MRK-01", "Record Mathematics 78", "Mark saved with grade B", "(B)",
             () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                     return "Recorded: " + new MarksService(a).add(s, "Mathematics", 78).describe(); });
        test("MRK-02", "Record Physics 150 (out of range)", "InvalidMarkException", "InvalidMarkException",
             () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                     new MarksService(a).add(s, "Physics", 150); return "accepted"; });
        test("MRK-03", "Report card, Term 3, marks 78 and 88", "Card with each mark; average 83.0", "avg 83.0",
             () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                     MarksService m = new MarksService(a); m.add(s, "Mathematics", 78); m.add(s, "English", 88);
                     ReportCard rc = m.reportCardFor(s, "Term 3");
                     return rc.getMarks().size() + " subjects, avg " + rc.average() + " | " + rc.describe(); });

        // ---- Library
        test("LIB-01", "Add book \"Fountain Physics for S3\", 2 copies", "Book created, 2/2 free", "2/2 free",
             () -> { reset(); LibraryService l = new LibraryService(new StudentService());
                     Book b = l.addBook("Fountain Physics for S3", "Mwaka P.", 2);
                     return "Added: " + b.getId() + " " + b.getTitle() + " (" + b.copiesAvailable() + "/" + b.getCopiesTotal() + " free)"; });
        test("LIB-02", "Borrow a book that has no free copy", "BookNotAvailableException", "BookNotAvailableException",
             () -> { reset(); StudentService a = new StudentService(); Student s1 = stu(a, "Nakato Grace", 2); Student s2 = stu(a, "Okello Peter", 3);
                     LibraryService l = new LibraryService(a); Book b = l.addBook("Longhorn English", "Mwaka P.", 1);
                     l.borrow(s1.getId(), b.getId()); l.borrow(s2.getId(), b.getId()); return "second loan accepted"; });
        test("LIB-03", "Report low-stock books, threshold 0", "Only the book with 0 copies free is listed", "listed: 1 (Longhorn English)",
             () -> { reset(); StudentService a = new StudentService(); Student s1 = stu(a, "Nakato Grace", 2);
                     LibraryService l = new LibraryService(a); l.addBook("Fountain Physics for S3", "Mwaka P.", 2);
                     Book b = l.addBook("Longhorn English", "Mwaka P.", 1); l.borrow(s1.getId(), b.getId());
                     List<Book> low = l.reportLowStock(0);
                     return "listed: " + low.size() + (low.isEmpty() ? "" : " (" + low.get(0).getTitle() + ")"); });

        // ---- Staff
        test("STF-01", "Add Mugisha Robert, Teacher, 900,000", "Staff created", "G06-T001",
             () -> { reset(); return "Added: " + new StaffService().add("Mugisha Robert", "Teacher", 900000).describe(); });
        test("STF-02", "Add the same name and role again", "DuplicateStaffException", "DuplicateStaffException",
             () -> { reset(); StaffService s = new StaffService(); s.add("Mugisha Robert", "Teacher", 900000);
                     s.add("Mugisha Robert", "Teacher", 900000); return "duplicate accepted"; });
        test("STF-03", "Mark attendance present for the staff member", "Attendance record for today", "PRESENT",
             () -> { reset(); StaffService s = new StaffService(); Staff t = s.add("Mugisha Robert", "Teacher", 900000);
                     return s.mark(t.getId(), true).describe(); });
        test("STF-04", "Remove a staff member who is patron of a club", "Removal refused", "still linked to a club (as patron)",
             () -> { reset(); StudentService a = new StudentService(); StaffService s = new StaffService();
                     Staff t = s.add("Mugisha Robert", "Teacher", 900000);
                     new ClubService(a, s).addClub("Debate Club", t.getId(), 2);
                     try { s.remove(t.getId()); return "REMOVED"; } catch (RuntimeException e) { return "Error: " + e.getMessage(); } });

        // ---- Boarding
        test("BRD-01", "Beds: 0 then 2 (invalid input), then add dormitory", "0 rejected and asked again; dormitory created with 2 beds",
             "2/2 beds free", () -> {
                 String out = run("InputProbe", "0\n2\n", freshDir("b1"), "int", "1", "300");
                 boolean rej = out.contains("between 1 and 300");
                 reset(); Dormitory d = new BoardingService(new StudentService()).addDormitory("Kizito Dorm", "St. Charles House", 2);
                 return (rej ? "0 rejected; " : "0 NOT REJECTED; ") + "Added: " + d.getId() + " " + d.getName() + " " + d.freeBeds() + "/" + d.getCapacity() + " beds free"; });
        test("BRD-02", "Allocate with term \"term three\"", "Refused: term must look like Term 3 2026", "Term must look like",
             () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                     BoardingService b = new BoardingService(a); Dormitory d = b.addDormitory("Nile Dorm", "St. Charles House", 1);
                     b.allocate(s.getId(), d.getId(), "term three"); return "accepted"; });
        test("BRD-03", "Allocate a second student to a 1-bed dormitory", "DormitoryFullException", "DormitoryFullException",
             () -> { reset(); StudentService a = new StudentService(); Student s1 = stu(a, "Nakato Grace", 2); Student s2 = stu(a, "Okello Peter", 3);
                     BoardingService b = new BoardingService(a); Dormitory d = b.addDormitory("Nile Dorm", "St. Charles House", 1);
                     b.allocate(s1.getId(), d.getId(), "Term 3 2026"); b.allocate(s2.getId(), d.getId(), "Term 3 2026"); return "accepted"; });
        test("BRD-04", "Allocate the same student again", "Refused: one active bed per student", "already has a bed",
             () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                     BoardingService b = new BoardingService(a); Dormitory d = b.addDormitory("Nile Dorm", "St. Charles House", 2);
                     b.allocate(s.getId(), d.getId(), "Term 3 2026"); b.allocate(s.getId(), d.getId(), "Term 3 2026"); return "accepted"; });
        test("BRD-05", "Remove a dormitory while a bed is taken", "Removal refused", "still taken",
             () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                     BoardingService b = new BoardingService(a); Dormitory d = b.addDormitory("Nile Dorm", "St. Charles House", 1);
                     b.allocate(s.getId(), d.getId(), "Term 3 2026");
                     try { b.removeDormitory(d.getId()); return "REMOVED"; } catch (RuntimeException e) { return "Error: " + e.getMessage(); } });
        test("BRD-06", "Report: dormitories by free beds", "Most free beds first", "Kizito Dorm, Nile Dorm",
             () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                     BoardingService b = new BoardingService(a);
                     Dormitory k = b.addDormitory("Kizito Dorm", "St. Charles House", 2); Dormitory n = b.addDormitory("Nile Dorm", "St. Charles House", 1);
                     b.allocate(s.getId(), n.getId(), "Term 3 2026");
                     StringBuilder sb = new StringBuilder();
                     for (Dormitory d : b.reportByFreeBeds()) sb.append(sb.length() > 0 ? ", " : "").append(d.getName());
                     return sb.toString(); });

        // ---- Clubs
        test("CLB-01", "Add club with patron G06-T999 (no such staff member)", "Refused", "No staff with ID",
             () -> { reset(); new ClubService(new StudentService(), new StaffService()).addClub("Debate Club", "G06-T999", 2); return "created"; });
        test("CLB-02", "Add Debate Club, patron G06-T001, maximum 2", "Club created", "0/2 members",
             () -> { reset(); StaffService s = new StaffService(); Staff t = s.add("Mugisha Robert", "Teacher", 900000);
                     Club c = new ClubService(new StudentService(), s).addClub("Debate Club", t.getId(), 2);
                     return "Added: " + c.getId() + " " + c.getName() + " (patron " + c.getPatron().getName() + ") " + c.getMembersCount() + "/" + c.getMaxMembers() + " members"; });
        test("CLB-03", "Join with position \"Captain\"", "Refused", "Position must be",
             () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                     StaffService st = new StaffService(); Staff t = st.add("Mugisha Robert", "Teacher", 900000);
                     ClubService c = new ClubService(a, st); Club club = c.addClub("Debate Club", t.getId(), 2);
                     c.join(s.getId(), club.getId(), "Captain"); return "accepted"; });
        test("CLB-04", "A third student joins a club with 2 of 2 places taken", "ClubFullException", "ClubFullException",
             () -> { reset(); StudentService a = new StudentService();
                     Student s1 = stu(a, "Nakato Grace", 2), s2 = stu(a, "Okello Peter", 3), s3 = stu(a, "Auma Sarah", 4);
                     StaffService st = new StaffService(); Staff t = st.add("Mugisha Robert", "Teacher", 900000);
                     ClubService c = new ClubService(a, st); Club club = c.addClub("Debate Club", t.getId(), 2);
                     c.join(s1.getId(), club.getId(), "Member"); c.join(s2.getId(), club.getId(), "Member");
                     c.join(s3.getId(), club.getId(), "Member"); return "accepted"; });
        test("CLB-05", "Remove a club while it has a member", "Removal refused", "active member",
             () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                     StaffService st = new StaffService(); Staff t = st.add("Mugisha Robert", "Teacher", 900000);
                     ClubService c = new ClubService(a, st); Club club = c.addClub("Chess Club", t.getId(), 5);
                     c.join(s.getId(), club.getId(), "Member");
                     try { c.removeClub(club.getId()); return "REMOVED"; } catch (RuntimeException e) { return "Error: " + e.getMessage(); } });
        test("CLB-06", "Restart the program and list clubs", "Clubs reload with member counts rebuilt", "2/2 members",
             () -> { reset(); StudentService a = new StudentService();
                     Student s1 = stu(a, "Nakato Grace", 2), s2 = stu(a, "Okello Peter", 3);
                     StaffService st = new StaffService(); Staff t = st.add("Mugisha Robert", "Teacher", 900000);
                     ClubService c = new ClubService(a, st); Club club = c.addClub("Debate Club", t.getId(), 2);
                     c.join(s1.getId(), club.getId(), "Member"); c.join(s2.getId(), club.getId(), "Member");
                     StudentService a2 = new StudentService(); StaffService st2 = new StaffService();
                     ClubService c2 = new ClubService(a2, st2); Club r = c2.allClubs().get(0);
                     return r.getId() + " " + r.getName() + " " + r.getMembersCount() + "/" + r.getMaxMembers() + " members"; });

        int planPass = pass, planFail = fail;
        log("");
        log("PART 1 RESULT: " + planPass + " passed, " + planFail + " failed (40 cases)");

        // ------------------------------------------------------------ further checks
        log("");
        log("================================================================");
        log("PART 2 - FURTHER CHECKS (constructor-level, atomic update, save failure, robustness)");
        log("================================================================");
        test("X-01", "FeeAccount constructor rejects NaN", "IllegalArgumentException", "IllegalArgumentException",
             () -> { StudentService a = new StudentService(); reset(); Student s = stu(a, "Nakato Grace", 2);
                     new FeeAccount("G06-F900", s, Double.NaN); return "accepted"; });
        test("X-02", "Mark rejects NaN", "InvalidMarkException", "InvalidMarkException",
             () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                     new Mark("G06-M900", s, "Maths", Double.NaN); return "accepted"; });
        test("X-03", "Payment and addBilling reject NaN", "Both refused", "both refused",
             () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                     FeesService f = new FeesService(a); FeeAccount acc = f.openAccount(s, 850000);
                     int refused = 0;
                     try { f.recordPayment(acc.getId(), Double.NaN, "Cash"); } catch (RuntimeException e) { refused++; }
                     try { f.addBilling(acc.getId(), Double.NaN); } catch (RuntimeException e) { refused++; }
                     return refused == 2 ? "both refused" : "only " + refused + " refused"; });
        test("X-04", "Staff salary NaN is rejected", "IllegalArgumentException", "IllegalArgumentException",
             () -> { new Staff("G06-T900", "Test Person", "Teacher", Double.NaN); return "accepted"; });
        test("X-05", "Rejected student update (valid name, invalid year) changes nothing", "Name unchanged", "name unchanged",
             () -> { reset(); StudentService a = new StudentService(); Student s = stu(a, "Okello Peter", 3);
                     try { a.update(s.getId(), "Changed Name", 9, null, null); } catch (RuntimeException e) { }
                     return s.getName().equals("Okello Peter") && s.getYear() == 3 ? "name unchanged" : "CHANGED to " + s.getName(); });
        test("X-06", "Rejected staff update (valid name, negative salary) changes nothing", "Name unchanged", "name unchanged",
             () -> { reset(); StaffService st = new StaffService(); Staff t = st.add("Mugisha Robert", "Teacher", 900000);
                     try { st.update(t.getId(), "Changed", null, -5.0); } catch (RuntimeException e) { }
                     return t.getName().equals("Mugisha Robert") ? "name unchanged" : "CHANGED to " + t.getName(); });
        test("X-07", "Student linked to a mark, a loan, a bed or a club cannot be removed", "All four refused", "4 of 4 refused",
             () -> { int refused = 0;
                     for (int k = 0; k < 4; k++) {
                         reset(); StudentService a = new StudentService(); Student s = stu(a, "Nakato Grace", 2);
                         StaffService st = new StaffService(); Staff t = st.add("Mugisha Robert", "Teacher", 900000);
                         MarksService m = new MarksService(a); LibraryService l = new LibraryService(a);
                         BoardingService b = new BoardingService(a); ClubService c = new ClubService(a, st);
                         if (k == 0) m.add(s, "Mathematics", 78);
                         if (k == 1) { Book bk = l.addBook("Longhorn English", "Mwaka P.", 1); l.borrow(s.getId(), bk.getId()); }
                         if (k == 2) { Dormitory d = b.addDormitory("Nile Dorm", "St. Charles House", 1); b.allocate(s.getId(), d.getId(), "Term 3 2026"); }
                         if (k == 3) { Club cl = c.addClub("Debate Club", t.getId(), 2); c.join(s.getId(), cl.getId(), "Member"); }
                         try { a.remove(s.getId()); } catch (IllegalStateException e) { refused++; }
                     }
                     return refused + " of 4 refused"; });
        test("X-08", "A failed save is reported instead of being ignored", "Error shown, not silent", "Error: Could not save",
             () -> { Path d = freshDir("save"); Files.writeString(d.resolve("data"), "not a folder");
                     return key(run("SaveFailProbe", "", d), "Error", "SILENT"); });
        test("X-09", "300 lines of random invalid input typed into the menus", "No crash, no stack trace", "NO STACK TRACE",
             () -> { Random r = new Random(2026);
                     String[] junk = {"x", "", "-1", "99", "abc", "NaN", "Infinity", "|", "1", "2", "3", "4", "5", "6", "7", "0", "G06-S999", "9999999999", "ü漢字", "   "};
                     StringBuilder in = new StringBuilder();
                     for (int i = 0; i < 300; i++) in.append(junk[r.nextInt(junk.length)]).append('\n');
                     in.append("0\n0\n0\n0\n0\n0\n0\n0\n0\n0\n");
                     String out = run(MENU, in.toString(), freshDir("fuzz"));
                     boolean bad = out.contains("Exception in thread") || out.contains("\tat ug.stleoskyegobe");
                     return bad ? "STACK TRACE FOUND" : "NO STACK TRACE"; });
        test("X-10", "Every data file damaged 4 ways (garbage, truncated, empty, binary): start-up never crashes",
             "All 7 services start each time", "ALL STARTS OK",
             () -> { reset(); StudentService a = new StudentService();
                     Student s1 = stu(a, "Nakato Grace", 2), s2 = stu(a, "Okello Peter", 3);
                     FeesService f = new FeesService(a); FeeAccount acc = f.openAccount(s1, 850000); f.recordPayment(acc.getId(), 300000, "Cash");
                     MarksService m = new MarksService(a); m.add(s1, "Mathematics", 78);
                     LibraryService l = new LibraryService(a); Book bk = l.addBook("Longhorn English", "Mwaka P.", 2); l.borrow(s1.getId(), bk.getId());
                     StaffService st = new StaffService(); Staff t = st.add("Mugisha Robert", "Teacher", 900000); st.mark(t.getId(), true);
                     BoardingService b = new BoardingService(a); Dormitory d = b.addDormitory("Nile Dorm", "St. Charles House", 2); b.allocate(s2.getId(), d.getId(), "Term 3 2026");
                     ClubService c = new ClubService(a, st); Club cl = c.addClub("Debate Club", t.getId(), 3); c.join(s1.getId(), cl.getId(), "Member");
                     Map<Path, byte[]> original = new LinkedHashMap<>();
                     try (DirectoryStream<Path> ds = Files.newDirectoryStream(DATA)) { for (Path p : ds) original.put(p, Files.readAllBytes(p)); }
                     int ok = 0, total = 0;
                     for (int mode = 0; mode < 4; mode++) {
                         for (Map.Entry<Path, byte[]> e : original.entrySet()) { Files.write(e.getKey(), e.getValue()); }
                         for (Path p : original.keySet()) {
                             for (Map.Entry<Path, byte[]> e : original.entrySet()) { Files.write(e.getKey(), e.getValue()); }
                             byte[] bad = switch (mode) {
                                 case 0 -> (new String(original.get(p), StandardCharsets.UTF_8) + "GARBAGE|LINE\n").getBytes(StandardCharsets.UTF_8);
                                 case 1 -> Arrays.copyOf(original.get(p), original.get(p).length / 2);
                                 case 2 -> new byte[0];
                                 default -> new byte[]{(byte) 0xFF, (byte) 0xFE, 0, 1, 2, (byte) 0x80, '\n', 0, 0};
                             };
                             Files.write(p, bad);
                             if (original.size() > 0) total++;
                             try {
                                 capture(() -> {
                                     StudentService a2 = new StudentService(); new FeesService(a2); new MarksService(a2);
                                     new LibraryService(a2); StaffService s3 = new StaffService(); new BoardingService(a2); new ClubService(a2, s3);
                                 });
                                 ok++;
                             } catch (Throwable t2) { log("         start-up crashed after damaging " + p.getFileName() + " (mode " + mode + "): " + t2); }
                         }
                     }
                     for (Map.Entry<Path, byte[]> e : original.entrySet()) Files.write(e.getKey(), e.getValue());
                     return (ok == total ? "ALL STARTS OK: " : "SOME STARTS CRASHED: ") + ok + " of " + total + " (" + original.size() + " files x 4 damage types)"; }
        );

        log("");
        log("================================================================");
        log("TOTAL: " + pass + " passed, " + fail + " failed");
        log("  Part 1 (40-case plan): " + planPass + " passed, " + planFail + " failed");
        log("  Part 2 (further checks): " + (pass - planPass) + " passed, " + (fail - planFail) + " failed");
        log("================================================================");
        System.exit(fail == 0 ? 0 : 1);
    }
}

/** Calls the shared InputHelper in its own JVM so System.in can be piped. */
class InputProbe {
    public static void main(String[] a) {
        if (a[0].equals("int"))
            System.out.println("RESULT=" + InputHelper.readInt("Value: ", Integer.parseInt(a[1]), Integer.parseInt(a[2])));
        else
            System.out.println("RESULT=" + InputHelper.readMoney("Amount: "));
    }
}

/** Tries to save when the data folder cannot be written; the failure must be reported. */
class SaveFailProbe {
    public static void main(String[] a) {
        StudentService ss = new StudentService();
        try {
            ss.add(new Student(ss.nextId(), "Nakato Grace", 2, "A", "0700000001"));
            System.out.println("SILENT: save failed but nothing was reported");
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
