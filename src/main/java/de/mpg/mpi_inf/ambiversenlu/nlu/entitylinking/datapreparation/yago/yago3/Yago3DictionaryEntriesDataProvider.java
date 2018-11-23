package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.util.YagoIdsMapper;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3RelationNames;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.Yago3Util;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.FactComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class Yago3DictionaryEntriesDataProvider extends DictionaryEntriesDataProvider {

  private static final Logger logger = LoggerFactory.getLogger(Yago3DictionaryEntriesDataProvider.class);

  private boolean mapYagoIdsToOtherKBIds = false;

  private YagoIdsMapper yagoIdsMapper;

  private String knowledgebaseName = Yago3PrepConf.YAGO3;

  private YAGO3Reader yago3Reader;

  public Yago3DictionaryEntriesDataProvider(YAGO3Reader yago3Reader) {
    this.yago3Reader = yago3Reader;
  }

  public Yago3DictionaryEntriesDataProvider(YAGO3Reader yago3Reader, YagoIdsMapper yagoIdsMapper, String knowledgebaseName) {
    this(yago3Reader);
    this.yagoIdsMapper = yagoIdsMapper;
    this.knowledgebaseName = knowledgebaseName;
    mapYagoIdsToOtherKBIds = true;
  }

  private Map<String, List<DictionaryEntity>> getDictionaryEntries() throws IOException {

    Map<String, List<DictionaryEntity>> dictionaryEntries = new HashMap<>();

    String[] relations = {
        YAGO3RelationNames.prefLabel, YAGO3RelationNames.label, YAGO3RelationNames.hasGivenName, YAGO3RelationNames.hasFamilyName, YAGO3RelationNames.redirectedFrom };

    List<Fact> isNamedEntityAll = yago3Reader.getFacts(YAGO3RelationNames.isNamedEntity);
    Map<String,String> isNamedEntity = new HashMap<>();
    for(Fact fact: isNamedEntityAll) {
      isNamedEntity.put(fact.getSubject(), fact.getObject());
    }


    int count = 0;

    for (String relation : relations) {
      List<Fact> relationPairsSet = yago3Reader.getFacts(relation);
      for (Fact entry : relationPairsSet) {
        String target = entry.getSubject();
        String valueIsNE = isNamedEntity.get(target);
        if(valueIsNE == null) {
          valueIsNE = EntityType.UNKNOWN.name();
        }

        if (mapYagoIdsToOtherKBIds) {
          target = yagoIdsMapper.mapFromYagoId(target);
          if (target == null) continue;
        }

        String mentionLanguage = FactComponent.getLanguageOfString(entry.getObject());

        Language language;
        if (mentionLanguage == null || "".equals(mentionLanguage)) {
          language = Language.getLanguageForString(mentionLanguage, "en");
        } else if (Language.isActiveLanguage(mentionLanguage)) {
          language = Language.getLanguageForString(mentionLanguage);
        } else {
          continue;
        }


        String mention = entry.getObjectAsJavaString();
        //a hack to fix some bug in YAGO extraction, should be remove later
        mention = mention.replace("_", " ");

        if (mention.startsWith("Category:")) {
          continue;
        }

        if (entry.getObject().startsWith("Angela Merkel@")) {
          logger.info(entry.getObject());
        }

        // Make sure that YAGO issues are handled.
        mention = Yago3Util.cleanEntityMention(mention);

        List<DictionaryEntity> candidates = dictionaryEntries.get(mention);
        if (candidates == null) {
          candidates = new LinkedList<DictionaryEntity>();
          dictionaryEntries.put(mention, candidates);
        }

        String source = getSourceName(relation);

        EntityType isNE = EntityType.find(valueIsNE);
        DictionaryEntity dictionaryEntity = DictionaryEntity.getDictionaryEntity(target,
            knowledgebaseName, source, language, isNE);

        candidates.add(dictionaryEntity);
      }
    }
    return dictionaryEntries;
  }

  private String getSourceName(String relation) {
    switch (relation) {
      case YAGO3RelationNames.prefLabel:
        return Yago3DictionaryEntriesSources.PREF_LABEL;
      case YAGO3RelationNames.label:
        return Yago3DictionaryEntriesSources.LABEL;
      case YAGO3RelationNames.hasGivenName:
        return Yago3DictionaryEntriesSources.HAS_GIVEN_NAME;
      case YAGO3RelationNames.hasFamilyName:
        return Yago3DictionaryEntriesSources.HAS_FAMILY_NAME;
      case YAGO3RelationNames.redirectedFrom:
        return Yago3DictionaryEntriesSources.REDIRECT;
      default://the general thing
        return Yago3DictionaryEntriesSources.MEANS;
    }
  }

  @Override public Iterator<Entry<String, List<DictionaryEntity>>> iterator() {
    try {
      return getDictionaryEntries().entrySet().iterator();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
