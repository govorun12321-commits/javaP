package coursehub;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public final class Task14Importer {
    public record ImportResult(List<Course> courses, int processedRecords) {}

    public static ImportResult importCourses(Reader source) throws CourseImportException {
        List<Course> temp = new ArrayList<>();
        int processed = 0;
        try (BufferedReader reader = new BufferedReader(source)) {
            String line;
            while ((line = reader.readLine()) != null) {
                processed++;
                String[] p = line.split(",", -1);
                if (p.length != 2) throw new CourseImportException(processed, "Malformed record " + processed,
                        new IllegalArgumentException("expected title,duration"));
                try {
                    int duration = Integer.parseInt(p[1].trim());
                    temp.add(new Course(processed, p[0].trim(), duration));
                } catch (IllegalArgumentException e) {
                    throw new CourseImportException(processed, "Malformed record " + processed, e);
                }
            }
            return new ImportResult(List.copyOf(temp), processed);
        } catch (CourseImportException e) {
            throw e;
        } catch (IOException e) {
            throw new CourseImportException(processed, "Reader failed", e);
        }
    }

    public static void main(String[] args) {
        try { System.out.println(importCourses(new StringReader("Java,10\nBad,abc"))); }
        catch (CourseImportException e) { System.out.println(e.getMessage()); }
    }
}
