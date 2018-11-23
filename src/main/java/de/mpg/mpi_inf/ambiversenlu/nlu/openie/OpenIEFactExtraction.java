package de.mpg.mpi_inf.ambiversenlu.nlu.openie;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.EntityMetaData;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Constituent;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class OpenIEFactExtraction {
  
  private static final String WIKIDATA_PREFIX = "http://www.wikidata.org/entity/";
  
  private DocumentProcessor dp;
  private Language language;
  
  
  public OpenIEFactExtraction(Language language) throws ResourceInitializationException, InvalidXMLException, NoSuchMethodException, ClassNotFoundException, EntityLinkingDataAccessException, IOException, MissingSettingException {
    this.language = language;
    
    switch (language.name()) {
      case "en":
        this.dp = DocumentProcessor.getInstance(PipelineType.OPENFACT_EXTRACTION_EN);
        break;
      default:
        throw new ResourceInitializationException(
            new Throwable("Language " + language + " not supported by fact extraction component."));  
    }
  }
  
  public List<Fact> extractFacts(String document) throws ClassNotFoundException, NoSuchMethodException, IOException, UIMAException, MissingSettingException, EntityLinkingDataAccessException, UnprocessableDocumentException {
    // Construct a new document with the given language settings
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(document);
    dbuilder.withLanguage(language);
    Document doc = dbuilder.build();
    
    // Process the document to generate facts and annotate entities
    JCas jcas = dp.processDev(doc);
    
    // Map subject and object of the open facts to the knowledge graph, if possible.
    List<Fact> linkedFacts = linkFactsToKnowledgeGraph(jcas);
    
    return linkedFacts;
  }
  
  public static List<Fact> linkFactsToKnowledgeGraph(JCas jcas) throws ClassNotFoundException, NoSuchMethodException, MissingSettingException, IOException, UIMAException, EntityLinkingDataAccessException, UnprocessableDocumentException {
    List<Fact> linkedFacts = new ArrayList<>();

    Set<KBIdentifiedEntity> kbIds = new HashSet<>();
    Collection<AidaEntity> entities = JCasUtil.select(jcas, AidaEntity.class);
    for(Entity entity : entities) {
      kbIds.add(new KBIdentifiedEntity(entity.getID()));
    }
    //Set the wikidata id
    Map<KBIdentifiedEntity, EntityMetaData> metadata = DataAccess.getEntitiesMetaData(kbIds);

    for (OpenFact of : JCasUtil.select(jcas, OpenFact.class)) {
      linkedFacts.add(linkFact(of, metadata));
    }
    
    return linkedFacts;
  }

  /**
   * Link facts to the KG entities and construct the webservice object Fact
   * @param of
   * @param metadata
   * @return
   * @throws CASException
   * @throws EntityLinkingDataAccessException
   */
  private static Fact linkFact(OpenFact of, Map<KBIdentifiedEntity, EntityMetaData> metadata) throws CASException, EntityLinkingDataAccessException {
    // Set the texts and begin/end markers
    Subject ofSubject = of.getSubject();
    Constituent subject = new Constituent();
    
    subject.setText(ofSubject.getText());
    subject.setCharOffset(ofSubject.getBegin());
    subject.setCharLength(ofSubject.getEnd() - ofSubject.getBegin());
    
    Relation ofRelation = of.getRelation();
    Constituent relation = new Constituent();
    
    relation.setText(ofRelation.getText());
    relation.setCharOffset(ofRelation.getBegin());
    relation.setCharLength(ofRelation.getEnd() - ofRelation.getBegin());
    relation.setnormalizedForm(ofRelation.getNormalizedForm());
    
    ObjectF ofObject = of.getObject();
    Constituent object = new Constituent();
    
    object.setText(ofObject.getText());
    object.setCharOffset(ofObject.getBegin());
    object.setCharLength(ofObject.getEnd() - ofObject.getBegin());
    
    // Set the entities for subject and object

    List<de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity> entities = new ArrayList<>();
    for (Entity e : JCasUtil.selectCovered(ofSubject.getCAS().getJCas(), AidaEntity.class, ofSubject)) {
      if (ofSubject.getText().contains(e.getCoveredText())) {
        if(metadata.get(new KBIdentifiedEntity(e.getID())) != null) {
          de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity entity = new de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity();
          entity.setId(WIKIDATA_PREFIX + metadata.get(new KBIdentifiedEntity(e.getID())).getWikiData());
          entity.setConfidence(e.getScore());
          entities.add(entity);
        }
      }
    }
    subject.setEntitis(entities);
    
    entities = new ArrayList<>();
    for (Entity e : JCasUtil.selectCovered(ofObject.getCAS().getJCas(), AidaEntity.class, ofObject)) {
      if (ofObject.getText().contains(e.getCoveredText())) {
        if(metadata.get(new KBIdentifiedEntity(e.getID())) != null) {
          de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity entity = new de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity();
          entity.setId(WIKIDATA_PREFIX + metadata.get(new KBIdentifiedEntity(e.getID())).getWikiData());
          entity.setConfidence(e.getScore());
          entities.add(entity);
        }
      }
    }
    object.setEntitis(entities);

    
    // Construct return object
    Fact fact = new Fact();
    fact.setSubject(subject);
    fact.setRelation(relation);
    fact.setObject(object);

    
    return fact;
  }

  @Deprecated
  /**
   * This is slow calling it per entity. There is a method DataAccess.getEntitiesMetaData(kbIds)
   * that returns Map<KbIdentifier, EntityMetadata> object.
   */
  public static String toWikidataID(String entityID) throws EntityLinkingDataAccessException {
    // Query the database for the Wikidata ID
    Connection conn = null;
    PreparedStatement pstmt = null;
    
    String wikidataID = null;
    
    try {
      conn = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      
      // Prepare the query.
      pstmt = conn.prepareStatement("SELECT wikidataid "
          + "FROM entity_ids as ids, entity_metadata as md "
          + "WHERE ids.knowledgebase=? AND ids.entity=? AND md.entity = ids.id");
      
      pstmt.setString(1, entityID.split(":")[0]);
      pstmt.setString(2, entityID.split(":")[1]);

      // Execute the query.
      ResultSet rs = pstmt.executeQuery();
      
      while (rs.next()) {
        wikidataID = WIKIDATA_PREFIX + rs.getString("wikidataid");
      }
      rs.close();
      pstmt.close();
    } catch (Exception e) {
      throw new EntityLinkingDataAccessException(e);
    } finally {
      try {
        EntityLinkingManager.releaseConnection(conn);
      } catch (SQLException e) {
        throw new EntityLinkingDataAccessException(e);
      }
    }
    
    return wikidataID;
  }
  
}
