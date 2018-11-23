package de.mpg.mpi_inf.ambiversenlu.nlu.openie;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessInterface;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Fact;
import org.apache.uima.cas.CASException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


public class OpenIEFactRankingByGlobalEntityPrior {

  private DataAccessInterface da = new DataAccessSQL();
  
  public OpenIEFactRankingByGlobalEntityPrior() {
    // Nothing yet.
  }
  
  public List<Fact> rankFacts(Collection<Fact> facts) throws EntityLinkingDataAccessException, CASException {
    Map<Fact, Double> orderedFacts = new HashMap<>();

    for (Fact fact : facts) {
      List<de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity> subjectEntities = fact.getSubject().getEntitis();
      List<de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity> objectEntities = fact.getObject().getEntitis();
      
      // Score entities by their mention-entity priors. Extremely basic for now...
      double subjectScore = computeScore(subjectEntities);
      double objectScore = computeScore(objectEntities);
      double score = subjectScore + objectScore;
      
      orderedFacts.put(fact, score);
    }

    // Filter duplicates as suggested by Johannes: Discount facts where the same entities are
    // already present in previous facts 
    List<Fact> allOrderedFacts = new ArrayList<>(sortByValueInDescendingOrder(orderedFacts).keySet());
    
    Map<Set<de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity>, Integer> discounts = new HashMap<>();
    
    for (Fact f : allOrderedFacts) {
      // Create union set of entities of subject and object
      Set<de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity> entities = new HashSet<>();
      
      List<de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity> subjectEntities = f.getSubject().getEntitis();
      List<de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity> objectEntities = f.getObject().getEntitis();
      
      entities.addAll(subjectEntities);
      entities.addAll(objectEntities);
      
      int discount = discounts.get(entities) != null ? discounts.get(entities) : 1;
      
      double discountedScore = discount > 1 ? orderedFacts.get(f) * (1 - Math.log(discount)) : orderedFacts.get(f);
      
      // Increase the discount 
      discount++;
      discounts.put(entities, discount);
      
      // Modify the original score in the map
      orderedFacts.put(f, discountedScore);
    }
    
    return new ArrayList<>(sortByValueInDescendingOrder(orderedFacts).keySet());
  }
  
  private double computeScore(List<de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity> entities) throws EntityLinkingDataAccessException, CASException {
    double score = 0.0;
    
    // Avoid counting mentions twice if theire entities are the same
    Set<String> wikidataIds = new HashSet<>();
    
    for (de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity e : entities) {
      // If an entity was spotted as a mention, but not linked to the knowledge graph,
      // this could result in no Wikidata ID being present.
      if (e.getId() != null && !wikidataIds.contains(e.getId())) {
        Entity entity = getEntityFromWikidataID(e.getId());
      
        // Mind that 0.0 is the best score!
        double mePrior = da.getGlobalEntityPrior(entity);
      
        score += mePrior;
        
        wikidataIds.add(e.getId());
      }
    }
    
    return score;
  }
  
  private Entity getEntityFromWikidataID(String wikidataID) throws EntityLinkingDataAccessException {
    // If a fully-qualified Wikidata ID has been provided, use only the last path component for the lookup.
    if (wikidataID.startsWith("http://") || wikidataID.startsWith("https://")) {
      wikidataID = wikidataID.substring(wikidataID.lastIndexOf("/") + 1);
    }
    
    // Query the database for the entity ID
    Connection conn = null;
    PreparedStatement pstmt = null;
    
    int entityID = 0;
    
    try {
      conn = EntityLinkingManager.getConnectionForDatabase(EntityLinkingManager.DB_AIDA);
      
      // Prepare the query.
      pstmt = conn.prepareStatement("SELECT entity "
          + "FROM entity_metadata as md "
          + "WHERE md.wikidataid=?");
      
      pstmt.setString(1, wikidataID);

      // Execute the query.
      ResultSet rs = pstmt.executeQuery();
      
      while (rs.next()) {
        entityID = rs.getInt("entity");
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
    
    return new Entity(DataAccess.getKnowlegebaseEntityForInternalId(entityID), entityID);
  }
  
  public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueInDescendingOrder(Map<K, V> map) {
    return map.entrySet()
              .stream()
              .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
              .collect(Collectors.toMap(
                Map.Entry::getKey, 
                Map.Entry::getValue, 
                (e1, e2) -> e1, 
                LinkedHashMap::new
              ));
  }
}