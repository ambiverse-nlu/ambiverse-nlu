package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.AIDASchemaPreparationConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3RelationNames;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.Yago3Util;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.yago3.Yago3EntityDictionary;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.FactComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Yago3AnchorsDictionaryEntriesDataProvider extends DictionaryEntriesDataProvider {

  private static final Logger logger = LoggerFactory.getLogger(Yago3AnchorsDictionaryEntriesDataProvider.class);

  private YAGO3Reader yago3Reader;

  private Map<String, Yago3EntityDictionary> language2EntityDictionaryMap = null;

  public Yago3AnchorsDictionaryEntriesDataProvider(YAGO3Reader yago3Reader) throws IOException {
    this.yago3Reader = yago3Reader;


    language2EntityDictionaryMap = new HashMap<>();
    for (Language language : Language.activeLanguages()) {
      String languageName = language.name();
      language2EntityDictionaryMap.put(languageName, new Yago3EntityDictionary(yago3Reader, languageName));
    }
  }

  private Map<String, List<DictionaryEntity>> getDictionaryEntries() throws IOException {

    Map<String, List<DictionaryEntity>> dictionaryEntries = new HashMap<>();

    Map<String, String> anchorTexts = new HashMap<>();
    logger.info("Reading anchor texts");

    List<Fact> anchorTextsSet = yago3Reader.getFacts(YAGO3RelationNames.hasAnchorText);

    for (Fact entry : anchorTextsSet) {
      String mentionLanguage = FactComponent.getLanguageOfString(entry.getObject());
      if (!Language.isActiveLanguage(mentionLanguage)) {
        continue;
      }
      anchorTexts.put(entry.getSubject(), entry.getObject());
    }

    logger.info("Anchors done");

    logger.info("loading links");
    int count = 0;

    List<Fact> internalWikipediaLinksSet = yago3Reader.getFacts(YAGO3RelationNames.hasInternalWikipediaLinkTo);

    List<Fact> isNamedEntityAll = yago3Reader.getFacts(YAGO3RelationNames.isNamedEntity);
    Map<String,String> isNamedEntity = new HashMap<>();
    for(Fact fact: isNamedEntityAll) {
      isNamedEntity.put(fact.getSubject(), fact.getObject());
    }

    for (Fact entry : internalWikipediaLinksSet) {
      String target = entry.getObject();
      String valueIsNE = isNamedEntity.get(target);
      if(valueIsNE == null) {
        valueIsNE = EntityType.UNKNOWN.name();
      }
      EntityType isNE = EntityType.find(valueIsNE);

      String mention = null;
      if (anchorTexts.containsKey(entry.getId())) {
        //invalid languages are already filtered above
        String mentionAsObject = anchorTexts.get(entry.getId());
        mention = FactComponent.asJavaString(mentionAsObject);
        String mentionLanguage = FactComponent.getLanguageOfString(mentionAsObject);
        if (mentionAsObject.contains("Angela Merkel@")) {
          logger.info(mentionAsObject + " " + entry.getId());
        }
        addCandidate(mention, target, dictionaryEntries, mentionLanguage, isNE);
      } else {
        //try to translate it to all possible valid languages
        for (Language activeLanguage : Language.activeLanguages()) {
          Yago3EntityDictionary dictionary = language2EntityDictionaryMap.get(activeLanguage.name());
          String translatedTarget = dictionary.fromEnglish(target);
          if (translatedTarget == null) {
            continue;
          }
          //if still couldn't be translated to the target language, drop it
          if (isValidEntityLanguage(translatedTarget)) {
            mention = FactComponent.unYagoEntity(translatedTarget);
            addCandidate(mention, target, dictionaryEntries, translatedTarget, isNE);
          }
        }
      }

      if (++count % 10_000_000 == 0) {
        logger.info(count + " links loaded");
      }
    }

    int minAnchorCount = AIDASchemaPreparationConfig.getInteger(AIDASchemaPreparationConfig.DICTIONARY_ANCHORS_MINOCCURRENCE);
    if (minAnchorCount > 0) {
      int dropCount = 0;
      int removedCount = 0;
      int filteredCount = 0;
      Map<String, List<DictionaryEntity>> filteredDictionaryEntries = new HashMap<>(dictionaryEntries.size());

      for (Entry<String, List<DictionaryEntity>> e : dictionaryEntries.entrySet()) {
        String mention = e.getKey();
        List<DictionaryEntity> targets = e.getValue();
        final Map<DictionaryEntity, Long> targetCounts = targets.stream().collect(Collectors.groupingByConcurrent(t -> t, Collectors.counting()));

        int originalTargetCount = targets.size();
        targets = targets.stream().filter(t -> targetCounts.get(t) >= minAnchorCount).collect(Collectors.toList());

        if (targets.size() > 0) {
          filteredDictionaryEntries.put(mention, targets);
        } else {
          ++removedCount;
        }
        dropCount += originalTargetCount - targets.size();

        if (++filteredCount % 1000000 == 0) {
          logger.info(filteredCount + " entries filtered");
        }
      }

      logger.info("Removed " + dropCount + " mentions-entity pairs that occur less than " + minAnchorCount + " times as link anchor.");
      logger.info(removedCount + " mentions were removed completely.");
      dictionaryEntries = filteredDictionaryEntries;
    }

    logger.info("Links done");
    return dictionaryEntries;
  }

  @Override public Iterator<Entry<String, List<DictionaryEntity>>> iterator() {
    try {
      return getDictionaryEntries().entrySet().iterator();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private void addCandidate(String mention, String target, Map<String, List<DictionaryEntity>> dictionaryEntries, String language, EntityType isNe) {
    // Make sure that YAGO issues are handled.
    mention = Yago3Util.cleanEntityMention(mention);

    List<DictionaryEntity> candidates = dictionaryEntries.get(mention);
    if (candidates == null) {
      candidates = new LinkedList<>();
      dictionaryEntries.put(mention, candidates);
    }

    DictionaryEntity dictionaryEntity = DictionaryEntity.getDictionaryEntity(target, Yago3PrepConf.YAGO3, Yago3DictionaryEntriesSources.ANCHORS,
        Language.getLanguageForString(language, "en"), isNe);

    candidates.add(dictionaryEntity);
  }

  private boolean isValidEntityLanguage(String entity) {
    String entityLanguage = "";
    if (!entity.contains("/")) {
      entityLanguage = "en";
    } else {
      entityLanguage = FactComponent.getLanguageOfEntity(entity);
    }
    return Language.isActiveLanguage(entityLanguage);
  }
}
