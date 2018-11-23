package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.KBIdentifiedEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ResultEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Domain;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.filehandlers.FileLines;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DomainExtractor extends JCasAnnotator_ImplBase {

  private static Map<String, String> wordnetSynsetDomainMapping;
  private static TIntObjectHashMap<int[]> taxanomy;
  private static final String WORDNETDOMAINS = "src/main/resources/wordnet_domains/wn-domains-3.2/wn-domains-3.2-20070223_wordnetVersion30";
  private static Pattern wordnetClassPattern = Pattern.compile("^<wordnet_.*_(\\d{9})>$");
  private static Pattern domainFileLinePattern = Pattern.compile("^(\\d{8})-(.)\t(.*)$");
  private static final List<String> EXCLUDE_DOMAINS = Arrays.asList("person", "factotum", "biology");
  
  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    try {
      loadWordnetSynsetDomain();
    } catch (IOException e) {
      System.err.println("Could not load wordnet sysnet domain mapping");
    }
    
    try {
      taxanomy = DataAccess.getTaxonomy();
    } catch (EntityLinkingDataAccessException e) {
      System.err.println("Could not load taxanomy");
    }

  }
  
  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    Integer runningId = RunningTimer.recordStartTime("DomainExtractor");
    List<ResultEntity> nameEntities = ResultEntity.getAidaResultEntitiesFromJCas(jCas);
    Set<String> results = new HashSet<>();

    for(ResultEntity ne:nameEntities) {
      int entityId;
      int[] types = null;
      try {
        entityId = DataAccess.getInternalIdForKBEntity(new KBIdentifiedEntity(ne.getKgId()));
        types = DataAccess.getTypeIdsForEntityId(entityId);
        
      } catch (EntityLinkingDataAccessException e) {
        System.err.println("Could not get entity Id for " + ne.getEntity());
      }
      
      if(types != null) {
        for(int typeId:types) {
          if (typeId==0) continue; // workaround for issue in database. There is not type=0 but there is in entity types. (also entity id=0 the same happens!!!)
          try {
            List<String> domains = getDomains(typeId);
            results.addAll(domains);
          } catch (EntityLinkingDataAccessException | IOException e) {
            System.err.println("Problem in get domain function for " + typeId);
          }
        }
      }
    }
    // Add the results to jCas:
    for(String domain:results) {
      if(EXCLUDE_DOMAINS.contains(domain)) {
        continue;
      }
      Domain jCasDomains = new Domain(jCas);
      jCasDomains.setDomain(domain);
      jCasDomains.addToIndexes();
    }
    RunningTimer.recordEndTime("DomainExtractor", runningId);
  }
  
  
  /**
   * Given a typeId(Wordnet sysnset or Wikipedia category), it will return the domains for that type.
   * If the input is Wikipedia category, it first find its wordnet parents and then find the domains.
   * 
   * @param typeId
   * @return
   * @throws EntityLinkingDataAccessException
   * @throws IOException
   */
  private List<String> getDomains(int typeId) throws EntityLinkingDataAccessException, IOException {
    List<String> domains = new ArrayList<>();
    String typeName = DataAccess.getTypeForId(typeId).getName();
    
    if(typeName.startsWith("<wordnet_")) {
      String domain = getDomainForWordnetClass(typeName);
       if(domain != null) {
        String[] split = domain.split("\\s");
        for(String sp:split) 
          domains.add(sp.toLowerCase());
      }
    }
    else if(typeName.startsWith("<wikicat_")) {
      int[] parents = taxanomy.get(typeId);
      
      for(int parentId:parents) {
        String parentName = DataAccess.getTypeForId(parentId).getName();
        if(parentName.startsWith("<wordnet_")) {
          String domain = getDomainForWordnetClass(parentName);
          if(domain != null) {
           String[] split = domain.split("\\s");
           for(String sp:split) 
             domains.add(sp.toLowerCase());
          }
        }
      }
    }
    
    return domains;
  }

  private static void loadWordnetSynsetDomain() throws IOException {
    FileLines wordnetDomain = new FileLines(WORDNETDOMAINS);
    wordnetSynsetDomainMapping = new HashMap<String, String>();
    
    for(String line:wordnetDomain) {
      Matcher matcher = domainFileLinePattern.matcher(line);
      if(matcher.find()) {
        String pos = "1";
        switch (matcher.group(2)) {
          case "n":
            pos = "1";
            break;
          case "v":
            pos = "2";
            break;
          case "a":
          case "s":
            pos = "3";
            break;
          case "r":
            pos = "4";
            break;
        }
        wordnetSynsetDomainMapping.put(pos + matcher.group(1), matcher.group(3));
      }
    }
    wordnetDomain.close();
  }
  
  private static String getDomainForWordnetClass(String wordnetClass) throws IOException {
    String domain = null;
    
    Matcher matcher = wordnetClassPattern.matcher(wordnetClass);
    if(matcher.find()) {
      domain = wordnetSynsetDomainMapping.get(matcher.group(1));
    }
    else
      System.out.println("Input is not a wordnet class: " + wordnetClass);
      
    
    return domain;
  }

}
