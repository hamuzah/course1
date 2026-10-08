package ug.stleoskyegobe.g06.app;

import ug.stleoskyegobe.g06.boarding.BoardingMenu;
import ug.stleoskyegobe.g06.boarding.BoardingService;
import ug.stleoskyegobe.g06.clubs.ClubService;
import ug.stleoskyegobe.g06.clubs.ClubsMenu;
import ug.stleoskyegobe.g06.core.InputHelper;
import ug.stleoskyegobe.g06.fees.FeesMenu;
import ug.stleoskyegobe.g06.fees.FeesService;
import ug.stleoskyegobe.g06.library.LibraryMenu;
import ug.stleoskyegobe.g06.library.LibraryService;
import ug.stleoskyegobe.g06.marks.MarksMenu;
import ug.stleoskyegobe.g06.marks.MarksService;
import ug.stleoskyegobe.g06.staff.StaffMenu;
import ug.stleoskyegobe.g06.staff.StaffService;
import ug.stleoskyegobe.g06.students.StudentMenu;
import ug.stleoskyegobe.g06.students.StudentService;

/**
 * Main entry point — leads to every module's submenu.
 * All services are constructed once at start-up, which also loads data from files.
 *
 * @author G06 Team
 */
public class MainMenu {

    public static void main(String[] args) {

        // Build services in dependency order (Students first because other modules link to it).
        StudentService studentService = new StudentService();
        FeesService    feesService    = new FeesService(studentService);
        MarksService   marksService   = new MarksService(studentService);
        LibraryService libraryService = new LibraryService(studentService);
        StaffService   staffService   = new StaffService();
        BoardingService boardingService = new BoardingService(studentService);
        ClubService     clubService     = new ClubService(studentService, staffService);

        // Build each module's menu.
        StudentMenu studentMenu = new StudentMenu(studentService);
        FeesMenu    feesMenu    = new FeesMenu(feesService, studentService);
        MarksMenu   marksMenu   = new MarksMenu(marksService, studentService);
        LibraryMenu libraryMenu = new LibraryMenu(libraryService);
        StaffMenu   staffMenu   = new StaffMenu(staffService);
        BoardingMenu boardingMenu = new BoardingMenu(boardingService);
        ClubsMenu    clubsMenu    = new ClubsMenu(clubService);

        System.out.println("================================================");
        System.out.println(" G06 SCHOOL MANAGEMENT SYSTEM");
        System.out.println("================================================");

        while (true) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Students & Enrolment");
            System.out.println("2. School Fees");
            System.out.println("3. Marks & Report Cards");
            System.out.println("4. Library");
            System.out.println("5. Staff");
            System.out.println("6. Boarding & Dormitories");
            System.out.println("7. Clubs & Co-curricular");
            System.out.println("0. Exit");

            int c = InputHelper.readInt("Choose: ", 0, 7);
            switch (c) {
                case 1 -> studentMenu.show();
                case 2 -> feesMenu.show();
                case 3 -> marksMenu.show();
                case 4 -> libraryMenu.show();
                case 5 -> staffMenu.show();
                case 6 -> boardingMenu.show();
                case 7 -> clubsMenu.show();
                case 0 -> {
                    System.out.println("\nGoodbye. Data saved.\n");
                    return;
                }
            }
        }
    }
}
