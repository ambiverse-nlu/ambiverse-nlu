package de.mpg.mpi_inf.ambiversenlu.nlu.ner.dictionarygeneration;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.EntityMetaData;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Type;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


/** Extracts lists of yago types for each of the dictionaries
  */
public class YagoTypesExtractor {
    public static void main(String[] args) throws IOException, EntityLinkingDataAccessException {

        if (args.length < 3) {
            System.out.println("Usage: java " + YagoTypesExtractor.class.getCanonicalName() + "<language> <path to the existing dictionary> " +
                    "<location of the extracted yago types>") ;
            return;
        }
//
        ConfigUtils.setConfOverride("default");
        EntityLinkingManager.init();

        List<String> dictionary_paths = Files.readAllLines(Paths.get(args[1]));

        for (String dict : dictionary_paths) {


            try {

                List<String> mentions = Files.readAllLines(Paths.get(dict));
                //contains counts, how many times the type was used in mentions (not counting each entities)
                Map<Type, Float> typeMentionCounts = new HashMap<>();
                Map<Type, Integer> typeEntityIDs = new HashMap<>();

                for (String mention : mentions) {
                    Entities es = DataAccess.getEntitiesForMention(
                            EntityLinkingManager.conflateToken(mention, Language.getLanguageForString(args[0]), true), 1, 0, true);

                    TIntObjectHashMap<Set<Type>> types =  DataAccess.getTypes(es);
                    TIntObjectIterator<Set<Type>> it = types.iterator();

                    Set<Type> mentionTypes = new HashSet<>();

                    //iterate over entities of the mention
                    while(it.hasNext()) {

                        it.advance();

                        int entityID = it.key();
                        EntityMetaData entityMetaData = DataAccess.getEntityMetaData(entityID);
                        if (representationMatch(mention, entityMetaData.getHumanReadableRepresentation())) {

                            mentionTypes.addAll(it.value());
                        }
                    }

                    for (Type t : mentionTypes) {
                        typeMentionCounts.compute(t, (k,v) -> (v == null)? 1 : v + 1);
                    }
                }

                Files.write(Paths.get(args[2], Paths.get(dict).getFileName().toString() + ".csv"),

                        typeMentionCounts.entrySet()
                                .stream()
                                .sorted((t0, t1) -> (int)(t1.getValue() - t0.getValue()))
                                .map(e -> "\"" + e.getKey() + "\", " + e.getValue())
                                .collect(Collectors.toList()));



            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static boolean representationMatch(String mention, String humanReadableRepresentation) {
        return mention.equals(humanReadableRepresentation);
    }
}
