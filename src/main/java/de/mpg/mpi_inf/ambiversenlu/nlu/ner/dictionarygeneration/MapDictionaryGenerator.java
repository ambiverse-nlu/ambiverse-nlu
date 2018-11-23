package de.mpg.mpi_inf.ambiversenlu.nlu.ner.dictionarygeneration;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.EntityMetaData;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generated dictionaries from aida database based on yago types
  */
public class MapDictionaryGenerator {
    private static Pattern yagoTypePattern = Pattern.compile(".*(<[a-z_0-9]+>).*");
    private static Pattern humanRepresentationPattern = Pattern.compile("([\\s\\da-zA-Z]*)(?:[,(].*[)]?)?");

    private static final Logger logger = LoggerFactory.getLogger(MapDictionaryGenerator.class);
    private static String language;

    private static class DictionaryYagoTypes {
        private final String simpleView;
        private final String dictionary;
        private final Set<String> yagoTypes;

        public DictionaryYagoTypes(String f, Set<String> strings) {

            this.dictionary = f;
            this.yagoTypes = strings;
            this.simpleView = dictionary.replaceAll("[<>]", "");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DictionaryYagoTypes that = (DictionaryYagoTypes) o;

            if (!dictionary.equals(that.dictionary)) return false;
            return yagoTypes.equals(that.yagoTypes);

        }

        @Override
        public int hashCode() {
            int result = dictionary.hashCode();
            result = 31 * result + yagoTypes.hashCode();
            return result;
        }
    }

    public static void main(String[] args) throws EntityLinkingDataAccessException, IOException {
        if (args.length < 3) {
            System.out.println("Usage: java " + MapDictionaryGenerator.class.getCanonicalName() + " <language> <path to the file, containing yago types, or directory, containing files with yagotypes for each dictionary> " +
                    "<location of the new dictionary> <optional: path to a file with dictionary parameters>");
            return;
        }

        language = args[0];

        createDictionaryDirectory(args[2]);

        Set<DictionaryYagoTypes> yagoTypesMap = getDictionaryYagoTypes(args[1]);

        Map<String, double[]> dictionaryParamMap = null;
        if (args.length > 3) {
            dictionaryParamMap = getDictionaryParameters(args[3]);
        }

        for (DictionaryYagoTypes dyt : yagoTypesMap) {
            try {

                int[] entityIdArray = getEntityIds(dyt.yagoTypes);

                if (entityIdArray.length == 0) {
                    logger.warn("No entities found for dictionary {}", dyt.dictionary);
                    continue;
                }

                double[] dictionaryParam;
                if (dictionaryParamMap != null) {
                    dictionaryParam = dictionaryParamMap.get(dyt.dictionary);
                    if (dictionaryParam == null) {
                        continue;
                    }
                } else {
                    dictionaryParam = new double[]{0, 1};
                }
                int[] filteredEntities = filterEntitiesByImportance(entityIdArray, dictionaryParam);

                TIntObjectHashMap<EntityMetaData> entitiesMetaData = DataAccess.getEntitiesMetaData(filteredEntities);

                Collection<String> dictionary;

//                for english language we use the human readable representation for the dictionary
                if (language.equals("en")) {

                    Stream<String> filtered = entitiesMetaData.valueCollection()
                            .stream()
                            .map(e -> {

                                Matcher matcher = humanRepresentationPattern.matcher(e.getHumanReadableRepresentation());
                                if (matcher.find()) {
                                    return matcher.group(1);
                                }
                                throw new RuntimeException(e.getHumanReadableRepresentation() + " does not match human representation pattern");
                            })
                            .distinct()
                            .filter(e -> !e.isEmpty());
                    if (dictionaryParam[0] != 0) {
                        filtered = sample(filtered.collect(Collectors.toList()), (int) dictionaryParam[0])
                                .stream();
                    }
                    dictionary = filtered
                            .sorted()
                            .collect(Collectors.toList());

//              for other languages we use mentions for the entities
                } else {
                    dictionary = getLanguageSpecificMentionsForEntities(entitiesMetaData);
                }

                Files.write(Paths.get(args[2], dyt.simpleView), dictionary);

            } catch (Exception e) {
                logger.info(dyt.dictionary + ": " + e.getMessage());
            }
        }
    }

    static Collection<String> sample(Collection<String> original, int k) {

        final int[] i = {0};
        Map<Integer, String> reservoir = original
                .stream()
                .limit(k)
                .map(e -> new Object[]{i[0]++, e})
                .collect(Collectors.toMap(e -> (int) e[0], e -> (String) e[1]));
        final int[] n = {k};
        original
                .stream()
                .skip(k)
                .forEach(e -> {

                    if (new Random().nextInt(n[0]++) < k) {
                        reservoir.put(new Random().nextInt(k), e);
                    }
                });
        return reservoir.values();
    }

    private static Collection<String> getLanguageSpecificMentionsForEntities(TIntObjectHashMap<EntityMetaData> entitiesMetaData) throws EntityLinkingDataAccessException {
        int[] collect = entitiesMetaData
                .valueCollection()
				.stream()
				.mapToInt(e -> e.getId())
				.toArray();
        return DataAccess.getEntityMentionsforLanguageAndEntities(collect, Language.getLanguageForString(language)).keySet();
    }

    private static Map<String, double[]> getDictionaryParameters(String arg) throws IOException {
        return Files.readAllLines(Paths.get(arg))
                .stream()
                .map(s -> s.split(","))
                .collect(Collectors.toMap(s -> s[0], s -> new double[]{Integer.valueOf(s[1]), Double.valueOf(s[2])}));
    }

    private static int[] filterEntitiesByImportance(int[] entityIdArray, double[] params) throws EntityLinkingDataAccessException {
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
        Set<Pair> importanceEntitySet = new TreeSet<>((id1, id2) -> (id1.importance - id2.importance == 0) ? 0 :
                (id1.importance - id2.importance < 0 ? -1 : 1));
        while (eiIterator.hasNext()) {
            eiIterator.advance();
            importanceEntitySet.add(new Pair(eiIterator.key(), eiIterator.value()));
        }

        return importanceEntitySet
                .stream()
                .filter(emd -> emd.importance < params[1])
                .mapToInt(i -> i.entityId)
                .toArray();
    }

    private static int[] getEntityIds(Collection<String> yagoTypes) throws EntityLinkingDataAccessException {
        TObjectIntHashMap<String> idsForTypeNames = DataAccess.getIdsForTypeNames(yagoTypes);

        if (idsForTypeNames.isEmpty()) {
            return new int[0];
        }
        TIntObjectIterator<int[]> entityIterator = DataAccess.getEntitiesIdsForTypesIds(idsForTypeNames.values()).iterator();

        Set<Integer> entityIds = new HashSet<>();
        while (entityIterator.hasNext()) {
            entityIterator.advance();
            entityIds.addAll(Arrays
                    .stream(entityIterator.value())
                    .mapToObj(i -> i)
                    .collect(Collectors.toSet()));
        }

        int[] entityIdArray = new int[entityIds.size()];
        Iterator<Integer> iterator = entityIds.iterator();
        int j = 0;
        while (iterator.hasNext()) {
            entityIdArray[j++] = iterator.next();
        }
        return entityIdArray;
    }

    private static Set<DictionaryYagoTypes> getDictionaryYagoTypes(String arg) throws IOException {
        Set<DictionaryYagoTypes> yagoTypesMap;
        if (Files.isDirectory(Paths.get(arg))) {
            List<Path> files = Files.walk(Paths.get(arg), 1)
                    .filter(p -> {
                        try {
                            return !(Files.isDirectory(p) || Files.isHidden(p));
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());

            yagoTypesMap = files.stream()
                    .map(f -> {
                                String name = f.getFileName().toString();
                                try {
                                    return new DictionaryYagoTypes(name, Files.readAllLines(f)
                                            .stream()
                                            .map(l -> {
                                                Matcher matcher = yagoTypePattern.matcher(l);
                                                if (matcher.find()) {
                                                    return matcher.group(1);
                                                } else {
                                                    return null;
                                                }
                                            })
                                            .filter(s -> s != null)
                                            .distinct()
                                            .collect(Collectors.toSet()));
                                } catch (IOException e) {
                                    return new DictionaryYagoTypes(name, Collections.emptySet());
                                }
                            }


                    )
                    .collect(Collectors.toSet());
        } else {
            yagoTypesMap = Files.readAllLines(Paths.get(arg))
                    .stream()
                    .map(l -> {
                        Matcher matcher = yagoTypePattern.matcher(l);
                        if (matcher.find()) {
                            return matcher.group(1);
                        } else {
                            return null;
                        }
                    })
                    .filter(s -> s != null)
                    .map(l -> new DictionaryYagoTypes(l, Collections.singleton(l)))
                    .collect(Collectors.toSet());
        }
        return yagoTypesMap;
    }

    private static void createDictionaryDirectory(String arg) throws EntityLinkingDataAccessException, IOException {
        EntityLinkingManager.init();

        Path dictDirectory = Paths.get(arg);
        if (Files.exists(dictDirectory)) {
            Files.walkFileTree(dictDirectory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e)
                        throws IOException {
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
