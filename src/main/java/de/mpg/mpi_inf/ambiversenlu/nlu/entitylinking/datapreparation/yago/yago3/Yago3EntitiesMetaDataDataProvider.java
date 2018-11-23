package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiesmetadata.EntitiesMetaDataEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiesmetadata.EntityMetaData;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3Reader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.util.YAGO3RelationNames;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.Yago3Util;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.yago3.Yago3EntityDictionary;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.FactComponent;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.parsers.Char17;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class Yago3EntitiesMetaDataDataProvider extends EntitiesMetaDataEntriesDataProvider {

  Logger logger = LoggerFactory.getLogger(Yago3EntitiesMetaDataDataProvider.class);

  private YAGO3Reader yago3Reader;

  public Yago3EntitiesMetaDataDataProvider(YAGO3Reader yago3Reader) {
    this.yago3Reader = yago3Reader;
  }

  private Set<EntityMetaData> run() throws IOException, EntityLinkingDataAccessException {
    List<Fact> shortDescriptions = yago3Reader.getFacts(YAGO3RelationNames.hasShortDescription);
    Map<String, String> shortDescription = new HashMap<>();
    for (Fact fact : shortDescriptions) {
      // There are multiple languages, prefer English.
      String language = FactComponent.getLanguageOfString(fact.getObject());
      String description = shortDescription.get(fact.getSubject());

      if (description == null || language.equals("en") || language.equals("eng")) {
        shortDescription.put(fact.getSubject(), FactComponent.getString(fact.getObject()));
      }
    }

    List<Fact> imageIDs = yago3Reader.getFacts(YAGO3RelationNames.hasImageID);
    Map<String, String> imageID = new HashMap<>();
    for (Fact fact : imageIDs) {
      imageID.put(fact.getSubject(), fact.getObject());
    }

    List<Fact> imageURLs = yago3Reader.getFacts(YAGO3RelationNames.hasImageUrl);
    Map<String, String> imageURL = new HashMap<>();
    for (Fact fact : imageURLs) {
      imageURL.put(fact.getSubject(), fact.getObject());
    }

    List<Fact> wikiPages = yago3Reader.getFacts(YAGO3RelationNames.hasWikiPage);
    Map<String, String> wikiPage = new HashMap<>();
    for (Fact fact : wikiPages) {
      wikiPage.put(fact.getSubject(), fact.getObject());
    }

    List<Fact> licencesFacts = yago3Reader.getFacts(YAGO3RelationNames.hasLicense);
    Map<String, String> licences = new HashMap<>();
    for (Fact fact : licencesFacts) {
      licences.put(fact.getSubject(), fact.getObject());
    }

    List<Fact> licencesUrlsFacts = yago3Reader.getFacts(YAGO3RelationNames.hasUrl);
    Map<String, String> licencesUrls = new HashMap<>();
    for (Fact fact : licencesUrlsFacts) {
      licencesUrls.put(fact.getSubject(), fact.getObject());
    }

    Set<EntityMetaData> entitiesMetaData = new HashSet<>();

    boolean entityTranslationNeeded = Language.activeLanguages().size() == 1 && !Language.isActiveLanguage("en");

    Yago3EntityDictionary entityDictionary = null;
    if (entityTranslationNeeded) {
      entityDictionary = new Yago3EntityDictionary(yago3Reader, Language.activeLanguages().iterator().next().name());
    }

    List<Fact> wikiDataSameAs = yago3Reader.getFacts(YAGO3RelationNames.sameAs);
    Map<String, String> wikiDataMapping = new HashMap<>();
    for (Fact fact : wikiDataSameAs) {
      wikiDataMapping.put(fact.getSubject(), fact.getObject().substring(fact.getObject().lastIndexOf('/') + 1, fact.getObject().length() - 1));
    }

    Entities entities = DataAccess.getAllEntities();
    try {
      for (Entity e : entities) {
        String entityWithLC = e.getIdentifierInKb();
        String localizedEntityWithLC = e.getIdentifierInKb();
        if (entityTranslationNeeded) {
          String localizedEntity = entityDictionary.fromEnglish(entityWithLC);
          if (localizedEntity != null) {
            localizedEntityWithLC = localizedEntity;
          }
        }
        String language = Yago3Util.getEntityLanguage(localizedEntityWithLC);
        String preferredName = preferredName(localizedEntityWithLC);
        String entityUrlPart = Yago3Util.getEntityAsUrlPart(localizedEntityWithLC);
        String url = "http://" + language + ".wikipedia.org/wiki/" + entityUrlPart;

        String id = imageID.get(entityWithLC);
        String depictionUrl = imageURL.get(id);
        if (depictionUrl != null) {
          depictionUrl = depictionUrl.substring(1, depictionUrl.length() - 1);
        }
        String wp = wikiPage.get(id);
        String licenceUrl = null;
        String license = null;
        if (wp != null) {
          license = licences.get(wp);
          if (license != null) {
            licenceUrl = licencesUrls.get(license);
            licenceUrl = licenceUrl.substring(1, licenceUrl.length() - 1);
          }
        }

        String wikiDataID = wikiDataMapping.get(e.getIdentifierInKb());
        String description = shortDescription.get(entityWithLC);
        entitiesMetaData.add(new EntityMetaData(entityWithLC, preferredName, url, depictionUrl, licenceUrl, description, wikiDataID));

      }
    } catch (UnsupportedEncodingException e) {
      logger.error("Should not happen, UTF-8 must be possible..");
    }
    return entitiesMetaData;
  }

  /** returns the preferred name */
  private String preferredName(String titleEntity) {
    return (Char17.decode(FactComponent.stripBracketsAndLanguage(titleEntity).replace('_', ' ')));
  }

  @Override public Iterator<EntityMetaData> iterator() {
    try {
      return run().iterator();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override public String getKnowledgebaseName() {
    return Yago3PrepConf.YAGO3;
  }

}
