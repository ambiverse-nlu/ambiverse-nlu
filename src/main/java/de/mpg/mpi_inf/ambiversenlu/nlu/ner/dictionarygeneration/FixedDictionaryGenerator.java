package de.mpg.mpi_inf.ambiversenlu.nlu.ner.dictionarygeneration;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.EntityMetaData;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FixedDictionaryGenerator {
    private static Pattern yagoTypePattern = Pattern.compile(".*(<[a-z_0-9]+>).*");
    private static Pattern humanRepresentationPattern = Pattern.compile("([\\s\\da-zA-Z]*)(?:[,(].*[)]?)?");

    private static int dictionarySize;
    private static double importanceThreshold;




    public static void main(String[] args) throws IOException, EntityLinkingDataAccessException {
        if (args.length < 3) {
            System.out.println("Usage: java " + FixedDictionaryGenerator.class.getCanonicalName() + " <path to the directory, containing files with yagotypes for each dictionary> " +
                    "<location of the new dictionary> <size limit for new dictionaries> <importance threshold>") ;
            return;
        }
        createDictionaryDirectory(args[1]);

        List<Path> files = getDictionaries(args[0]);
        dictionarySize = Integer.valueOf(args[2]);

        importanceThreshold = Double.valueOf(args[3]);

        for (Path dictFile : files) {
            try {
                List<String> lines = Files.readAllLines(dictFile);

                List<String> yagoTypes = new ArrayList<>();
                for (String str : lines) {
                    Matcher matcher = yagoTypePattern.matcher(str);
                    if (matcher.find()){
                        yagoTypes.add(matcher.group(1));
                    }
                }

                if (yagoTypes.isEmpty()) {
                    System.out.println(dictFile + ": no yago types!");
                    continue;
                }

                int[] entityIdArray = getEntityIds(yagoTypes);

                TIntObjectHashMap<EntityMetaData> entitiesMetaData = getEntitiesMetaData(entityIdArray);


                Files.write(Paths.get(args[1], dictFile.getFileName().toString()),
                        entitiesMetaData.valueCollection()
                                .stream()
                                .map(e -> {
                                    Matcher matcher = humanRepresentationPattern.matcher(e.getHumanReadableRepresentation());
                                    if (matcher.find()) {
                                        return matcher.group(1);
                                    }
                                    throw new RuntimeException(e.getHumanReadableRepresentation() + " does not match human representation pattern");
                                })
                                .filter(e -> !e.isEmpty())
                                .sorted()
                                .collect(Collectors.toList()));

                Files.write(Paths.get(args[1], dictFile.getFileName().toString()+".hr.txt"),
                        entitiesMetaData.valueCollection()
                                .stream()
                                .map(EntityMetaData::getHumanReadableRepresentation)
                                .filter(e -> !e.isEmpty())
                                .sorted()
                                .collect(Collectors.toList()));

            } catch (Exception e) {
                System.out.println(dictFile + ": " + e.getMessage());
            }
        }
    }

    private static TIntObjectHashMap<EntityMetaData> getEntitiesMetaData(int[] entityIdArray) throws EntityLinkingDataAccessException {
        TIntDoubleHashMap entitiesImportances = DataAccess.getEntitiesImportances(entityIdArray);
        TIntDoubleIterator eiIterator = entitiesImportances.iterator();
        class Pair {
            private int entityId;
            private double importance;

            public Pair(int entityId, double importance) {
                this.entityId = entityId;
                this.importance = importance;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Pair pair = (Pair) o;

                if (entityId != pair.entityId) return false;
                return Double.compare(pair.importance, importance) == 0;

            }

            @Override
            public int hashCode() {
                int result;
                long temp;
                result = entityId;
                temp = Double.doubleToLongBits(importance);
                result = 31 * result + (int) (temp ^ (temp >>> 32));
                return result;
            }
        }
        Set<Pair> importanceEntitySet = new TreeSet<>((id1, id2) -> (id1.importance - id2.importance == 0)? 0 :
                (id1.importance - id2.importance < 0? -1 : 1));
        while (eiIterator.hasNext()) {
            eiIterator.advance();
            importanceEntitySet.add(new Pair(eiIterator.key(), eiIterator.value()));
        }

        return DataAccess.getEntitiesMetaData(importanceEntitySet
                .stream()
                .filter(emd -> emd.importance < importanceThreshold)
                .limit(dictionarySize)
                .mapToInt(i -> i.entityId)
                .toArray());
    }

    private static int[] getEntityIds(List<String> yagoTypes) throws EntityLinkingDataAccessException {
        TObjectIntHashMap<String> idsForTypeNames = DataAccess.getIdsForTypeNames(yagoTypes);
        TIntObjectIterator<int[]> entityIterator = DataAccess.getEntitiesIdsForTypesIds(idsForTypeNames.values()).iterator();

        Set<Integer> entityIds = new HashSet<>();
        while (entityIterator.hasNext()) {
            entityIterator.advance();
            entityIds.addAll(Arrays.stream(entityIterator.value()).mapToObj(i -> i).collect(Collectors.toSet()));
        }

        int[] entityIdArray = new int[entityIds.size()];
        Iterator<Integer> iterator = entityIds.iterator();
        int j = 0;
        while (iterator.hasNext()){
            entityIdArray[j++] = iterator.next();
        }
        return entityIdArray;
    }

    private static List<Path> getDictionaries(String arg) throws IOException {
        return Files.walk(Paths.get(arg), 1)
                    .filter(p -> {
                        try {
                            return !(Files.isDirectory(p) || Files.isHidden(p));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
    }

    private static void createDictionaryDirectory(String arg) throws IOException, EntityLinkingDataAccessException {
        ConfigUtils.setConfOverride("default");
        EntityLinkingManager.init();

        Path dictDirectory = Paths.get(arg);
        if (Files.exists(dictDirectory)) {
            Files.walkFileTree(dictDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException
                {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e)
                        throws IOException
                {
                    if (e == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        // directory iteration failed
                        throw e;
                    }
                }
            });
            Files.deleteIfExists(dictDirectory);
        }
        Files.createDirectory(dictDirectory);
    }

}
