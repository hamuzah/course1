package ug.stleoskyegobe.g06.core;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Shared file storage utility.
 * Loads at start-up, saves after every change, never crashes on a
 * missing or damaged file (missing → empty list; corrupt line → skipped).
 *
 * @author G06 Team
 */
public class FileStore {

    private static final Path DATA_DIR = Paths.get("data");

    static {
        try {
            if (!Files.exists(DATA_DIR)) Files.createDirectories(DATA_DIR);
        } catch (IOException ignored) { /* best effort */ }
    }

    private FileStore() { }

    /** Save every record as one line via its serializer. */
    public static <T> void saveAll(String fileName, List<T> items,
                                   java.util.function.Function<T, String> toLine) {
        Path p = DATA_DIR.resolve(fileName);
        try (BufferedWriter bw = Files.newBufferedWriter(p)) {
            for (T item : items) {
                bw.write(toLine.apply(item));
                bw.newLine();
            }
        } catch (IOException e) {
            // Do not hide a failed save: the menus catch this and print "Error: ..."
            throw new IllegalStateException("Could not save " + fileName + ": " + e.getMessage()
                + " (the change is in memory only - check the data folder)");
        }
    }

    /** Load every record from a file. Missing file → empty list. Corrupt lines are skipped. */
    public static <T> List<T> loadAll(String fileName,
                                      java.util.function.Function<String, T> fromLine) {
        List<T> out = new ArrayList<>();
        Path p = DATA_DIR.resolve(fileName);
        if (!Files.exists(p)) return out;
        try (BufferedReader br = Files.newBufferedReader(p)) {
            String line;
            int ln = 0;
            while ((line = br.readLine()) != null) {
                ln++;
                if (line.isBlank()) continue;
                try {
                    T item = fromLine.apply(line);
                    if (item != null) out.add(item);
                } catch (Exception ex) {
                    System.out.println("  [storage] Skipped corrupt line " + ln
                        + " in " + fileName + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("  [storage] Could not read " + fileName + ": " + e.getMessage());
        }
        return out;
    }
}
