package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.disambiguation.CocktailPartyLangaugeModelDefaultDisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.disambiguation.LocalLanguageModelDisambiguationWithNullSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.disambiguation.PriorOnlyDisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EntityEntitySimilarityCombinationsIds;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util.SimilaritySettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Type;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.GraphTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.ByteArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * TrainingSettings for the Disambigutor. Configures the disambiguation process.
 * Pre-configured settings are available in the 
 * {@see mpi.aida.config.settings.disambiguation} package.
 */
public class DisambiguationSettings implements Serializable {

  private static final long serialVersionUID = -3703193764296431133L;

  public enum DISAMBIGUATION_METHOD {
    PRIOR, LM_LOCAL, LM_COHERENCE
  }

  private static Logger logger = LoggerFactory.getLogger(DisambiguationSettings.class);


  private Type[] filteringTypes;

  /**
   * Technique to solve the disambiguation graph with. Most commonly this
   * is LOCAL for mention-entity similarity edges only and 
   * GRAPH to include the entity coherence.
   */
  private Settings.TECHNIQUE disambiguationTechnique = null;

  /**
   * If TECHNIQUE.GRAPH is chosen above, this specifies the algorithm to
   * solve the disambiguation graph. Can be COCKTAIL_PARTY for the full
   * disambiguation graph, COCKTAIL_PARTY_SIZE_CONSTRAINED for a heuristically
   * pruned graph, and RANDOM_WALK for using a PageRank style random walk
   * to find the best entity.
   */
  private Settings.ALGORITHM disambiguationAlgorithm = null;

  /**
   * TrainingSettings to compute the edge-weights of the disambiguation graph.
   */
  private SimilaritySettings similaritySettingsNE = null;
  private SimilaritySettings similaritySettingsC = null;
  
  
  
  /**
   * Maximum (global) rank of the entity according to the entity_rank table.
   * Currently, entities are ranked by number of inlinks. 0.0 means no entity
   * is included, 1.0 means all are included. Default is to include all.
   */
  private double maxEntityRank = 1.0;

  /**
   * Maximum number of candidates to retrieve for a mention, ordered by prior.
   * Set to 0 to retrieve all.
   */
  private int maxCandidatesPerEntityByPrior = 0;

  private double nullMappingThreshold = -1.0;

  private boolean includeNullAsEntityCandidate = false;

  private boolean includeContextMentions = false;

  private String storeFile = null;

  private GraphTracer.TracingTarget tracingTarget = null;

  private String tracingPath = null;
  
  private String trainingCorpus = null;

  public void setTracingPath(String tracingPath) {
    this.tracingPath = tracingPath;
  }

  public String getTracingPath() {
    return tracingPath;
  }

  /**
   * TrainingSettings to use for graph computation.
   */
  private GraphSettings graphSettings;

  /**
   * If true, compute the confidence of the mapping instead of assigning
   * the local (mention-entity) similarity as score.
   */
  private boolean computeConfidence = true;

  /**
   * TrainingSettings to use for confidence computation.
   */
  private ConfidenceSettings confidenceSettings;

  /**
   * Number of chunks to process in parallel.
   */
  private int numChunkThreads = 4;

  private int minMentionOccurrenceCount = 1;

  private DISAMBIGUATION_METHOD disambiguationMethod = DISAMBIGUATION_METHOD.LM_COHERENCE;

  public LanguageSettings getLanguageSettings() {
    return languageSettings;
  }

  public void setLanguageSettings(LanguageSettings languageSettings) {
    this.languageSettings = languageSettings;
  }

  private LanguageSettings languageSettings;

  public DisambiguationSettings() {
    graphSettings = new GraphSettings();
    confidenceSettings = new ConfidenceSettings();
  }

  public void setTracingTarget(GraphTracer.TracingTarget tracingTarget) {
    this.tracingTarget = tracingTarget;
  }

  public GraphTracer.TracingTarget getTracingTarget() {
    return tracingTarget;
  }

  public Settings.TECHNIQUE getDisambiguationTechnique() {
    return disambiguationTechnique;
  }

  public void setDisambiguationTechnique(Settings.TECHNIQUE disambiguationTechnique) {
    this.disambiguationTechnique = disambiguationTechnique;
  }

  public Settings.ALGORITHM getDisambiguationAlgorithm() {
    return disambiguationAlgorithm;
  }

  public void setDisambiguationAlgorithm(Settings.ALGORITHM disambiguationAlgorithm) {
    this.disambiguationAlgorithm = disambiguationAlgorithm;
  }
  
  public SimilaritySettings getSimilaritySettings(boolean isNamedEntity) {
    if (isNamedEntity) {
      return getSimilaritySettingsNE();
    }
    else {
      return getSimilaritySettingsC();
    }
  }

  private SimilaritySettings getSimilaritySettingsNE() {
    return similaritySettingsNE;
  }
  
  private SimilaritySettings getSimilaritySettingsC() {
    return similaritySettingsC;
  }

  public void setSimilaritySettingsNE(SimilaritySettings similaritySettings) {
    this.similaritySettingsNE = similaritySettings;
  }
  
  public void setSimilaritySettingsC(SimilaritySettings similaritySettings) {
    this.similaritySettingsC = similaritySettings;
  }

  public double getNullMappingThreshold() {
    return nullMappingThreshold;
  }

  public void setNullMappingThreshold(double nullMappingThreshold) {
    this.nullMappingThreshold = nullMappingThreshold;
  }

  public boolean isIncludeNullAsEntityCandidate() {
    return includeNullAsEntityCandidate;
  }

  public void setIncludeNullAsEntityCandidate(boolean includeNullAsEntityCandidate) {
    this.includeNullAsEntityCandidate = includeNullAsEntityCandidate;
  }

  public void setIncludeContextMentions(boolean flag) {
    this.includeContextMentions = flag;
  }

  public boolean isIncludeContextMentions() {
    return includeContextMentions;
  }

  public double getMaxEntityRank() {
    return maxEntityRank;
  }

  public void setMaxEntityRank(double maxEntityRank) {
    this.maxEntityRank = maxEntityRank;
  }

  public boolean shouldComputeConfidence() {
    return computeConfidence;
  }

  public void setComputeConfidence(boolean computeConfidence) {
    this.computeConfidence = computeConfidence;
  }

  public ConfidenceSettings getConfidenceSettings() {
    return confidenceSettings;
  }

  public void setConfidenceSettings(ConfidenceSettings confidenceSettings) {
    this.confidenceSettings = confidenceSettings;
  }

  public GraphSettings getGraphSettings() {
    return graphSettings;
  }

  public void setGraphSettings(GraphSettings graphSettings) {
    this.graphSettings = graphSettings;
  }

  public int getNumChunkThreads() {
    return numChunkThreads;
  }

  public void setNumChunkThreads(int numChunkThreads) {
    this.numChunkThreads = numChunkThreads;
  }

  public int getMaxCandidatesPerEntityByPrior() {
    return maxCandidatesPerEntityByPrior;
  }

  public void setMaxCandidatesPerEntityByPrior(int maxCandidatesPerEntityByPrior) {
    this.maxCandidatesPerEntityByPrior = maxCandidatesPerEntityByPrior;
  }

  public int getMinMentionOccurrenceCount() {
    return minMentionOccurrenceCount;
  }

  public void setMinMentionOccurrenceCount(int minMentionOccurrenceCount) {
    this.minMentionOccurrenceCount = minMentionOccurrenceCount;
  }

  public Type[] getFilteringTypes() {
    return filteringTypes;
  }

  public void setFilteringTypes(Type[] filteringTypes) {
    this.filteringTypes = filteringTypes;
  }

  public void setDisambiguationMethod(DISAMBIGUATION_METHOD dm) {
    disambiguationMethod = dm;
  }

  public DISAMBIGUATION_METHOD getDisambiguationMethod() {
    return disambiguationMethod;
  }

  public boolean isMentionLookupPrefix() {
    return EntityLinkingConfig.getBoolean(EntityLinkingConfig.ENTITIES_CANDIADATE_LOOKUP_MENTION_ISPREFIX);
  }
  
  public String getTrainingCorpus() {
    return trainingCorpus;
  }

  public void setTrainingCorpus(String trainingCorpus) {
    this.trainingCorpus = trainingCorpus;
  }

  public Map<String, Object> getAsMap() {
    Map<String, Object> s = new HashMap<String, Object>();
    if (disambiguationTechnique != null) {
      s.put("disambiguationTechnique", disambiguationTechnique.toString());
    }
    if (disambiguationAlgorithm != null) {
      s.put("disambiguationAlgorithm", disambiguationAlgorithm.toString());
    }
    s.put("maxEntityRank", String.valueOf(maxEntityRank));
    s.put("maxCandidatesPerEntityByPrior", String.valueOf(maxCandidatesPerEntityByPrior));
    s.put("nullMappingThreshold", String.valueOf(nullMappingThreshold));
    s.put("includeNullAsEntityCandidate", String.valueOf(includeNullAsEntityCandidate));
    s.put("includeContextMentions", String.valueOf(includeContextMentions));
    s.put("computeConfidence", String.valueOf(computeConfidence));
    if (similaritySettingsNE != null) {
      s.put("similaritySettingsNE", similaritySettingsNE.getAsMap());
    }
    if (similaritySettingsC != null) {
      s.put("similaritySettingsC", similaritySettingsC.getAsMap());
    }
    if (confidenceSettings != null) {
      s.put("confidenceSettings", confidenceSettings.getAsMap());
    }
    if (graphSettings != null) {
      s.put("graphSettings", graphSettings.getAsMap());
    }
    return s;
  }

  public String toString() {
    return getAsMap().toString();
  }
  
  public String toStringBeautiful() {
    String res = "";
    for ( Entry<String, Object> entiry: getAsMap().entrySet()) {
      res += (entiry.toString() + "\n");
    }
    return res;
  }

  private static DisambiguationSettings getDisambiguationSettings(Builder builder)
      throws MissingSettingException, NoSuchMethodException, IOException, ClassNotFoundException {
    DisambiguationSettings ds = getDisambiguationSettings(builder.disambiguationMethod);
    ds.setNumChunkThreads(builder.numChunkThreads);
    if (builder.nullMappingThreshold != null) {
      ds.setNullMappingThreshold(builder.nullMappingThreshold);
    }
    ds.setMinMentionOccurrenceCount(builder.minMentionOccurrenceCount);
    ds.setLanguageSettings(LanguageSettings.LanguageSettingsFactory.getLanguageSettingsForLanguage(builder.language));

    if(builder.exhaustiveSearch != null) {
      ds.graphSettings.setUseExhaustiveSearch(builder.exhaustiveSearch);
    }

    if(builder.useNormalizedObjective != null) {
      ds.graphSettings.setUseNormalizedObjective(builder.useNormalizedObjective);
    }

    if(builder.computeConfidence != null) {
      ds.setComputeConfidence(builder.computeConfidence);
    }

    if(builder.cohRobustnessThresholdNE != null) {
      ds.graphSettings.setCohRobustnessThresholdNE(builder.cohRobustnessThresholdNE);
      if(builder.cohRobustnessThresholdNE > 0.0) {
        ds.getGraphSettings().setUseCoherenceRobustnessTestNE(true);
      } else {
        ds.getGraphSettings().setUseCoherenceRobustnessTestNE(false);
      }
    }
    if(builder.cohRobustnessThresholdC != null) {
      ds.graphSettings.setCohRobustnessThresholdC(builder.cohRobustnessThresholdC);
      if(builder.cohRobustnessThresholdC > 0.0) {
        ds.getGraphSettings().setUseCoherenceRobustnessTestC(true);
      } else {
        ds.getGraphSettings().setUseCoherenceRobustnessTestC(false);
      }
    }

    if(builder.graphAlpha != null) {
      ds.graphSettings.setAlpha(builder.graphAlpha);
    }

    if(builder.pruneCandidateEntities != null) {
      ds.graphSettings.setPruneCandidateThreshold(builder.pruneCandidateEntities);
      if(builder.pruneCandidateEntities > 0) {
        ds.graphSettings.setPruneCandidateEntities(true);
      } else {
        ds.graphSettings.setPruneCandidateEntities(false);
      }
    }

    if(builder.coherenceConfidenceThreshold != null) {
      ds.getGraphSettings().setConfidenceTestThreshold(builder.coherenceConfidenceThreshold);
      if(builder.coherenceConfidenceThreshold > 0.0) {
        ds.getGraphSettings().setUseConfidenceThresholdTest(true);
      } else {
        ds.getGraphSettings().setUseConfidenceThresholdTest(false);
      }
    }
    
    if (builder.trainingCorpus != null) {
      ds.setTrainingCorpus(builder.trainingCorpus);
    }
    if (builder.cohConfigsIdNE != null) {
      ds.getSimilaritySettingsNE().setEntityEntitySimilarities(builder.cohConfigsIdNE.getConfig());
    }
    
    if (builder.cohConfigsIdC != null) {
      ds.getSimilaritySettingsC().setEntityEntitySimilarities(builder.cohConfigsIdC.getConfig());
    }
    
    if (builder.similaritySettingNameNE != null) {
      List<String[]> cohConfig = new ArrayList<>(ds.getSimilaritySettingsNE().getEntityEntitySimilarities());
      
      Properties switchedUnitProp = ClassPathUtils.getPropertiesFromClasspath("similarity/" + ds.getTrainingCorpus() + "/" + builder.similaritySettingNameNE + "_NE.properties");
      SimilaritySettings ss = new SimilaritySettings(switchedUnitProp, "SimilarityNE");
      ss.setEntityEntitySimilarities(cohConfig);
      ds.setSimilaritySettingsNE(ss);

      if(ds.getDisambiguationMethod().equals(DISAMBIGUATION_METHOD.LM_COHERENCE)) {
        Properties cohRobProp = ClassPathUtils.getPropertiesFromClasspath("similarity/" + ds.getTrainingCorpus() + "/" + builder.similaritySettingNameNE + "_cohrob_NE.properties");
        SimilaritySettings unnormalizedKPsettings = new SimilaritySettings(cohRobProp, "CoherenceRobustnessTestNE");
        ds.getGraphSettings().setCoherenceSimilaritySettingNE(unnormalizedKPsettings);
      }
    }
    
    if (builder.similaritySettingNameC != null) {
      List<String[]> cohConfig = new ArrayList<>(ds.getSimilaritySettingsC().getEntityEntitySimilarities());
      
      Properties switchedUnitProp = ClassPathUtils.getPropertiesFromClasspath("similarity/" + ds.getTrainingCorpus() + "/" + builder.similaritySettingNameC + "_C.properties");
      SimilaritySettings ss = new SimilaritySettings(switchedUnitProp, "SimilarityC");
      ss.setEntityEntitySimilarities(cohConfig);
      ds.setSimilaritySettingsC(ss);
      
//      Properties cohRobProp = ClassPathUtils.getPropertiesFromClasspath("similarity/" + ds.getTrainingCorpus() + "/" + builder.similaritySettingNameC + "_cohrob_C.properties");
//      SimilaritySettings unnormalizedKPsettings = new SimilaritySettings(cohRobProp, "CoherenceRobustnessTestC");
//      ds.getGraphSettings().setCoherenceSimilaritySettingC(unnormalizedKPsettings);
    }
          
    return ds;
  }

  private static DisambiguationSettings getDisambiguationSettings(DISAMBIGUATION_METHOD disambiguationTechniqueSetting)
      throws MissingSettingException, NoSuchMethodException, IOException, ClassNotFoundException {
    DisambiguationSettings ds = null;
    switch (disambiguationTechniqueSetting) {
      case PRIOR:
        ds = new PriorOnlyDisambiguationSettings();
        ds.setDisambiguationMethod(DISAMBIGUATION_METHOD.PRIOR);
        break;
      case LM_LOCAL:
        ds = new LocalLanguageModelDisambiguationWithNullSettings();
        ds.setDisambiguationMethod(DISAMBIGUATION_METHOD.LM_LOCAL);
        break;
      case LM_COHERENCE:
        ds = new CocktailPartyLangaugeModelDefaultDisambiguationSettings();
        ds.setDisambiguationMethod(DISAMBIGUATION_METHOD.LM_COHERENCE);
        break;
      default:
        throw new IllegalArgumentException("Disambiguation method should be set");
    }
    return ds;
  }

  public static class Builder {

    private DISAMBIGUATION_METHOD disambiguationMethod = DISAMBIGUATION_METHOD.LM_COHERENCE;

    private int numChunkThreads = 4;

    private Double nullMappingThreshold;

    private int minMentionOccurrenceCount = 1;

    private Language language;

    private Boolean exhaustiveSearch = null;

    private Boolean useNormalizedObjective = null;

    private Integer pruneCandidateEntities = null;

    private Boolean computeConfidence = null;

    private Double graphAlpha = null;

    private Double cohRobustnessThresholdNE = null;
    private Double cohRobustnessThresholdC = null;

    private Double coherenceConfidenceThreshold = null;
    
    private EntityEntitySimilarityCombinationsIds cohConfigsIdNE = null;
    private EntityEntitySimilarityCombinationsIds cohConfigsIdC = null;
    
    private String similaritySettingNameNE = null;
    private String similaritySettingNameC = null;
    
    private String trainingCorpus = null;
    
    
    public Builder withDisambiguationMethod(DISAMBIGUATION_METHOD disambiguationMethod) {
      this.disambiguationMethod = disambiguationMethod;
      return this;
    }

    public Builder withNumChunkThreads(int numChunkThreads) {
      this.numChunkThreads = numChunkThreads;
      return this;
    }

    public Builder withNullMappingThreshold(double nullMappingThreshold) {
      this.nullMappingThreshold = nullMappingThreshold;
      return this;
    }

    public Builder withMinMentionOcurrenceCount(int minMentionOccurrenceCount) {
      this.minMentionOccurrenceCount = minMentionOccurrenceCount;
      return this;
    }

    public Builder withLanguage(Language language) {
      this.language = language;
      return this;
    }

    public Builder withUseNormalizedObjective(boolean b) {
      this.useNormalizedObjective = b;
      return this;
    }

    public Builder withExhaustiveSearch(boolean b) {
      this.exhaustiveSearch = b;
      return this;
    }

    public Builder withComputeConfidence(boolean b) {
      this.computeConfidence = b;
      return this;
    }

    public Builder withPruneCandidateEntities(int prune) {
      this.pruneCandidateEntities = prune;
      return this;
    }
    
    public DisambiguationSettings build() throws ClassNotFoundException, NoSuchMethodException, IOException, MissingSettingException {
      return getDisambiguationSettings(this);
    }

    public Builder withGraphAlpha(double alpha) {
      this.graphAlpha = alpha;
      return this;
    }

    public Builder withCohRobustnessThresholdNE(double thresh) {
      this.cohRobustnessThresholdNE = thresh;
      return this;
    }
    
    public Builder withCohRobustnessThresholdC(double thresh) {
      this.cohRobustnessThresholdC = thresh;
      return this;
    }

    public Builder withCoherenceConfidenceThreshold(double confCoherenceThresh) {
      this.coherenceConfidenceThreshold = confCoherenceThresh;
      return this;
    }
    
    public Builder withCohConfigsNE(EntityEntitySimilarityCombinationsIds eesimId) {
      this.cohConfigsIdNE = eesimId;
      return this;
    }
    
    public Builder withCohConfigsC(EntityEntitySimilarityCombinationsIds eesimId) {
      this.cohConfigsIdC = eesimId;
      return this;
    }
    
    public Builder withSimilaritySettingNameNE(String similaritySettingName) {
      this.similaritySettingNameNE = similaritySettingName;
      return this;
    }
    
    public Builder withSimilaritySettingNameC(String similaritySettingName) {
      this.similaritySettingNameC = similaritySettingName;
      return this;
    }
    
    public Builder withTrainingCorpus(String trainingCorpus) {
      this.trainingCorpus = trainingCorpus;
      return this;
    }

    public String toStringBeautiful() {
      List<String> results = new ArrayList<>();
      if (trainingCorpus != null) {
        results.add(trainingCorpus+"-TR");
      }
      if (similaritySettingNameNE != null) {
        results.add(similaritySettingNameNE+"-NE");
      }
      if (similaritySettingNameC != null) {
        results.add(similaritySettingNameC+"-C");
      }
      if (cohConfigsIdNE != null) {
        results.add(cohConfigsIdNE+"-NE");
      }
      if (cohConfigsIdC != null) {
        results.add(cohConfigsIdC+"-C");
      }
      if (graphAlpha != null) {
        results.add(graphAlpha+"-a");
      }
      if (cohRobustnessThresholdNE != null) {
        results.add(cohRobustnessThresholdNE+"-hNE");
      }
      if (cohRobustnessThresholdC != null) {
        results.add(cohRobustnessThresholdC+"-hC");
      }
      if (coherenceConfidenceThreshold != null) {
        results.add(coherenceConfidenceThreshold+"-q");
      }
      if (computeConfidence != null) {
        results.add(computeConfidence+"-confCheck");
      }

      return String.join("_", results);
    }
    
  }

  public void addToJCas(AidaDocumentSettings ads, JCas jcas) throws IOException {
    AidaDisambiguationSettings ds = new AidaDisambiguationSettings(jcas);
    byte[] bytes = encode(this);
    ByteArray ba = new ByteArray(jcas, bytes.length);
    ba.copyFromArray(bytes, 0, 0, bytes.length);
    ba.addToIndexes();
    ds.setDisambiguationSettingsBytes(ba);
    ds.addToIndexes();
    ads.setDisambiguationSettings(ds);
  }

  /**
   * Using SerializationUtils.deserialize() causes
   *  java.lang.ClassNotFoundException: de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.disambiguation.CocktailPartyLangaugeModelDefaultDisambiguationSettings
   *  Thats why we are trying with these techniques for java serializing and deserializing.
   *
   * @param serialized
   * @return
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public static DisambiguationSettings decode(byte[] serialized) throws IOException, ClassNotFoundException {
    try (ByteArrayInputStream bais = new ByteArrayInputStream(serialized)) {
      ObjectInputStream ois = new ObjectInputStream(bais) {
        @Override protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
          ClassLoader cl = Thread.currentThread().getContextClassLoader();
          if (cl == null)  return super.resolveClass(desc);
          return Class.forName(desc.getName(), false, cl);
        }
      };
      return (DisambiguationSettings) ois.readObject();
    }
  }

  public static byte[] encode(DisambiguationSettings o) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      new ObjectOutputStream(baos).writeObject(o);
      return baos.toByteArray();
    }
  }


}

