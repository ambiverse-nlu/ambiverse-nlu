package de.mpg.mpi_inf.ambiversenlu.nlu.ner.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Util {

    public static void deleteNonEmptyDirectory(Path dir, Set<String> exceptFileNames) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (exceptFileNames == null || !exceptFileNames.contains(file.getFileName().toString())) {
                    Files.delete(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exceptFileNames == null) {
                    Files.delete(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void copyDir(final Path path, Path sourceDir) throws IOException {
        Files.createDirectory(path);
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path target = Paths.get(path.toString(), file.getFileName().toString());
                Files.copy(file, target);
                return super.visitFile(file, attrs);
            }
        });
    }

    public static String produceAidaSentence(Map<Integer, Object[]> typedSpots, List<Integer> begins, List<Integer> ends,
                                      String sentenceText) {
        StringBuilder sb = new StringBuilder();
        Object[] currTypedSpot = null;
        Object lastWordType = null;
        for (int i = 0; i < begins.size(); i++) {
            Integer begin = begins.get(i);
            Integer end = ends.get(i);
            sb.append(sentenceText.substring(begin, end));

            if (typedSpots.containsKey(begin)) {
                sb.append("\t");
                Object[] typedSpot = typedSpots.get(begin);
                Object spotEnd = typedSpot[0];
                Object spotLabel = typedSpot[1];
                sb.append(lastWordType == null || spotLabel != lastWordType ? "I" : "B");
                sb.append("\t");
                sb.append(spotLabel);
//                sb.append("\t");
//                sb.append(typedSpot[2]);
//                sb.append("\t");
//                sb.append(typedSpot[3]);
                if (end.intValue() != ((Integer) spotEnd).intValue()) {
                    currTypedSpot = typedSpot;
                }
                lastWordType = spotLabel;

            } else if (currTypedSpot != null) {
                Object spotEnd = currTypedSpot[0];
                Object spotLabel = currTypedSpot[1];
                sb.append("\tI\t");
                sb.append(spotLabel);
                if (end.intValue() == ((Integer) spotEnd).intValue()) {
                    currTypedSpot = null;
                }
                lastWordType = spotLabel;

            } else {
                lastWordType = null;
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static void deleteNonEmptyDirectory(Path dir) throws IOException {
        deleteNonEmptyDirectory(dir, null);
    }
}
