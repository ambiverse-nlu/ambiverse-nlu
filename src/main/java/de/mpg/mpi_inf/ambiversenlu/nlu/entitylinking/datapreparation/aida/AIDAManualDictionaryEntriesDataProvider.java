package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.aida;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.filereading.TsvEntries;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Adds all manually specified dictionary entries, read from 'pathToDictionary'.
 * It assumes that all are named entities (i.e., there is no concepts on the list). See the "false" argument in DictionaryEntry constructor.
 */
public class AIDAManualDictionaryEntriesDataProvider extends DictionaryEntriesDataProvider {

  private final String pathToDictionary = "entity_repository/dictionary.tsv";

  private Map<String, List<DictionaryEntity>> getDictionaryEntries() throws FileNotFoundException, NoSuchMethodException {

    Map<String, List<DictionaryEntity>> dictionaryEntries = new HashMap<String, List<DictionaryEntity>>();

    for (String[] entries : new TsvEntries(pathToDictionary)) {
      if (entries.length > 0 && !entries[0].startsWith("#")) {
        String mention = entries[0];
        List<DictionaryEntity> entities = new ArrayList<>();
        for (int i = 2; i < entries.length; ++i) {
          KBIdentifiedEntity kbEntity = new KBIdentifiedEntity(entries[1]);
          DictionaryEntity entity = DictionaryEntity.getDictionaryEntity(kbEntity.getIdentifier(), kbEntity.getKnowledgebase(), "manual",
              Language.getLanguageForString(entries[i]), EntityType.NAMED_ENTITY);
          entities.add(entity);
        }
        dictionaryEntries.put(mention, entities);
      }
    }

    return dictionaryEntries;
  }

  @Override public Iterator<Entry<String, List<DictionaryEntity>>> iterator() {
    try {
      return getDictionaryEntries().entrySet().iterator();
    } catch (FileNotFoundException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

}
