package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessSQL;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.EntityEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.MentionEntitySimilarity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.MentionEntitySimilarityPackage;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.UnitType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EntitiesContext;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EntitiesContextSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.context.EntitiesContextSettings.EntitiesContextType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.importance.EntityImportance;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.measure.MentionEntitySimilarityMeasure;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Entities;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ExternalEntitiesContext;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TrainingSettings for computing the weights of the disambiguation graph.
 * Preconfigured settings are in the 
 * {@see mpi.aida.config.settings.disambiguation}
 * package.
 *
 * Can be created programmatically or read from a file.
 *
 * File format:
 *
 * - mentionEntitySimilarities
 *   - a list of space-separated mention-entity similarity triples, 
 *   separated by ":". The first one is the SimilarityMeasure, the second the 
 *   EntitiesContext, the third the weight of this mentionEntitySimilarity. 
 *   Note that they need to add up to 1.0, including the number for the 
 *   priorWeight option. The mentionEntitySimilarities option also allows to 
 *   enable or disable the first or second half of the mention-entity 
 *   similarities based on the priorThreshold option. If this is present, 
 *   the first half of the list is used when the prior is disable, 
 *   the second one when it is enabled. Note that still the whole list weights 
 *   need to sum up to 1 with the prior, the EnsembleMentionEntitySimilarity 
 *   class will take care of appropriate re-scaling.
 * - priorWeight
 *   - The weight of the prior probability. Needs to sum up to 1.0 with all 
 *   weights in mentionEntitySimilarities.
 * - priorThreshold
 *   -If set, the first half of mentionEntitySimilarities will be used for 
 *   the mention-entity similarity when the best prior for an entity candidate 
 *   is below the given threshold, otherwise the second half of the list 
 *   together with the prior is used.
 * - entityEntitySimilarity
 *   - The name and the weight of the entity-entity similarity to use, 
 *   ":" separated.
 *
 *
 */
public class SimilaritySettings implements Serializable {

  private static final Logger logger = LoggerFactory.getLogger(SimilaritySettings.class);

  private static final long serialVersionUID = 5712963955706453268L;

  /**
   * Mmention-entity-similarity triples. The first entry is the
   * SimilarityMeasure, the second one the EntitiesContext, the third one the
   * weight.
   */
  private List<MentionEntitySimilarityRaw> mentionEntitySimilaritiesNoPrior = new LinkedList<>();

  private List<MentionEntitySimilarityRaw> mentionEntitySimilaritiesWithPrior = new LinkedList<>();

  /** Entity importance-weight pairs. */
  private List<EntityImportancesRaw> entityImportancesNoPrior = new LinkedList<>();

  private List<EntityImportancesRaw> entityImportancesWithPrior = new LinkedList<>();

  /**
   * Entity-Entity-Similarity tuples. First entry is the EESim id, second the
   * weight.
   */
  private List<String[]> entityEntitySimilarities = new LinkedList<>();

  private List<String[]> mentionEntityKeyphraseSourceWeights = new LinkedList<>();

  private List<String[]> entityEntityKeyphraseSourceWeights = new LinkedList<>();

  /** Weight of the prior probability. */
  private double priorWeight;

  /** Take the log of the prior */
  private boolean priorTakeLog;

  /** Prior damping factor */
  private double priorDampingFactor;

  /** Threshold above which the prior should be considered. */
  private double priorThreshold = -1;

  private int numberOfEntityKeyphrase;

  private double entityCohKeyphraseAlpha;

  private double entityCohKeywordAlpha;

  private boolean normalizeCoherenceWeights;

  private boolean shouldAverageCoherenceWeights;

  private boolean useConfusableMIWeights;

  private double minimumEntityKeyphraseWeight;

  private int maxEntityKeyphraseCount;

  // LanguageModel
  private double[] unitSmoothingParameter = new double[UnitType.values().length];

  private boolean[] unitIgnoreMention = new boolean[UnitType.values().length];

  private int nGramLength;

  private String identifier;

  private String fullPath;

  private ImportanceAggregationStrategy importanceAggregationStrategy = ImportanceAggregationStrategy.LINEAR_COMBINATION;

  public static Logger getLogger() {
    return logger;
  }

  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  public List<String[]> getEntityEntitySimilarities() {
    return entityEntitySimilarities;
  }

  public boolean isNormalizeCoherenceWeights() {
    return normalizeCoherenceWeights;
  }

  public void setNormalizeCoherenceWeights(boolean normalizeCoherenceWeights) {
    this.normalizeCoherenceWeights = normalizeCoherenceWeights;
  }

  public boolean isShouldAverageCoherenceWeights() {
    return shouldAverageCoherenceWeights;
  }

  public double[] getUnitSmoothingParameter() {
    return unitSmoothingParameter;
  }

  public void setUnitSmoothingParameter(double[] unitSmoothingParameter) {
    this.unitSmoothingParameter = unitSmoothingParameter;
  }

  public boolean[] getUnitIgnoreMention() {
    return unitIgnoreMention;
  }

  public void setUnitIgnoreMention(boolean[] unitIgnoreMention) {
    this.unitIgnoreMention = unitIgnoreMention;
  }

  public int getnGramLength() {
    return nGramLength;
  }

  public void setnGramLength(int nGramLength) {
    this.nGramLength = nGramLength;
  }

  public void setFullPath(String fullPath) {
    this.fullPath = fullPath;
  }

  public enum ImportanceAggregationStrategy {AVERGAE, LINEAR_COMBINATION}

  ;

  /**
   * Initializes SimilaritySettings from a Properties object.
   * Be careful, the fullPath will not be set when this is used.
   *
   * @param prop
   * @param identifier
   * @throws ClassNotFoundException
   * @throws NoSuchMethodException
   */
  public SimilaritySettings(Properties prop, String identifier) throws NoSuchMethodException, ClassNotFoundException {
    init(prop, identifier);
  }

  public SimilaritySettings() {
    //bean.
  }

  /**
   * mentionEntitySimilarities in property file need to be in the following format:
   *
   * similarityMeasure:entityContext:weight
   *
   * @param propertiesFile
   */
  public SimilaritySettings(File propertiesFile) {
    String name = propertiesFile.getName();
    String id = name.substring(0, name.lastIndexOf('.'));

    Properties prop = new Properties();
    try {
      if (propertiesFile.exists()) {
        FileReader fr = new FileReader(propertiesFile);
        prop.load(fr);
        fr.close();

        init(prop, id);

        fullPath = propertiesFile.getAbsolutePath();

      } else {
        logger.error("Setings file specified but could not be loaded from '" + fullPath + "'");
        throw new FileNotFoundException("Setings file specified but could not be loaded from '" + fullPath + "'");
      }
    } catch (Exception e) {
      logger.error(e.getLocalizedMessage());
    }
  }

  private void init(Properties prop, String identifier) throws NoSuchMethodException, ClassNotFoundException {
    this.identifier = identifier;

    priorWeight = Double.parseDouble(prop.getProperty("priorWeight", "0.0"));
    priorTakeLog = Boolean.parseBoolean(prop.getProperty("priorTakeLog", "false"));
    priorDampingFactor = Double.parseDouble(prop.getProperty("priorDampingFactor", "1.0"));
    priorThreshold = Double.parseDouble(prop.getProperty("priorThreshold", "-1.0"));

    numberOfEntityKeyphrase = Integer.parseInt(prop.getProperty("numberOfEntityKeyphrase", String.valueOf(Integer.MAX_VALUE)));
    entityCohKeyphraseAlpha = Double
        .parseDouble(prop.getProperty("entityCoherenceKeyphraseAlpha", String.valueOf(EntitiesContextSettings.DEFAULT_KEYPHRASE_ALPHA)));
    entityCohKeywordAlpha = Double
        .parseDouble(prop.getProperty("entityCoherenceKeywordAlpha", String.valueOf(EntitiesContextSettings.DEFAULT_KEYWORD_ALPHA)));
    normalizeCoherenceWeights = Boolean.parseBoolean(prop.getProperty("normalizeCoherenceWeights", "false"));
    shouldAverageCoherenceWeights = Boolean.parseBoolean(prop.getProperty("shouldAverageCoherenceWeights", "false"));
    useConfusableMIWeights = Boolean.parseBoolean(prop.getProperty("useConfusableMIWeights", "false"));
    nGramLength = Integer.parseInt(prop.getProperty("nGramLength", String.valueOf(2)));
    minimumEntityKeyphraseWeight = Double.parseDouble(prop.getProperty("minimumEntityKeyphraseWeight", "0.0"));
    maxEntityKeyphraseCount = Integer.parseInt(prop.getProperty("maxEntityKeyphraseCount", "0"));

    importanceAggregationStrategy = ImportanceAggregationStrategy
        .valueOf(prop.getProperty("importanceAggregationStrategy", ImportanceAggregationStrategy.LINEAR_COMBINATION.toString()));

    // LanguageModel
    for (UnitType unitType : UnitType.values()) {
      unitSmoothingParameter[unitType.ordinal()] = Double
          .parseDouble(prop.getProperty("unitSmoothingParameter." + unitType.getUnitName(), prop.getProperty("unitSmoothingParameter", "1.0")));
    }
    for (UnitType unitType : UnitType.values()) {
      unitIgnoreMention[unitType.ordinal()] = Boolean
          .parseBoolean(prop.getProperty("unitIgnoreMention." + unitType.getUnitName(), prop.getProperty("unitIgnoreMention", "false")));
    }

    String mentionEntitySimilarityString = prop.getProperty("mentionEntitySimilaritiesNoPrior");

    if (mentionEntitySimilarityString != null) {
      for (String sim : mentionEntitySimilarityString.split(" ")) {
        mentionEntitySimilaritiesNoPrior.add(MentionEntitySimilarityRaw.parseFrom(sim));
      }
    }

    String mentionEntitySimilarityWithPriorString = prop.getProperty("mentionEntitySimilaritiesWithPrior");

    if (mentionEntitySimilarityWithPriorString != null) {
      for (String sim : mentionEntitySimilarityWithPriorString.split(" ")) {
        mentionEntitySimilaritiesWithPrior.add(MentionEntitySimilarityRaw.parseFrom(sim));
      }
    } else {
      logger.info("No mention-entity similarity setting for prior given in the settings - this almost always needed!");
    }

    String entityImportanceNoPriorString = prop.getProperty("entityImportanceWeightsNoPrior");

    if (entityImportanceNoPriorString != null) {
      for (String imp : entityImportanceNoPriorString.split(" ")) {
        entityImportancesNoPrior.add(EntityImportancesRaw.parseFrom(imp));
      }
    }

    String entityImportanceWithPriorString = prop.getProperty("entityImportanceWeightsWithPrior");

    if (entityImportanceWithPriorString != null) {
      for (String imp : entityImportanceWithPriorString.split(" ")) {
        entityImportancesWithPrior.add(EntityImportancesRaw.parseFrom(imp));
      }
    }

    String entityEntitySimilarityString = prop.getProperty("entityEntitySimilarity");

    if (entityEntitySimilarityString != null) {
      for (String sim : entityEntitySimilarityString.split(" ")) {
        entityEntitySimilarities.add(sim.split(":"));
      }
    }

    if (prop.containsKey("entityEntityKeyphraseSourceWeights")) {
      entityEntityKeyphraseSourceWeights = Arrays.stream(prop.getProperty("entityEntityKeyphraseSourceWeights").split(" ")).map(src -> src.split(":"))
          .collect(Collectors.toList());
    }
    if (prop.containsKey("mentionEntityKeyphraseSourceWeights")) {
      String mentionEntityKeyphraseSourceWeightsString = prop.getProperty("mentionEntityKeyphraseSourceWeights");
      for (String src : mentionEntityKeyphraseSourceWeightsString.split(" ")) {
        mentionEntityKeyphraseSourceWeights.add(src.split(":"));
      }
    }
  }

  /**
   * Constructor for programmatic access. Format of params see above.
   */
  public SimilaritySettings(List<MentionEntitySimilarityRaw> similarities, List<String[]> eeSimilarities, double priorWeight)
      throws MissingSettingException {
    this(similarities, eeSimilarities, null, priorWeight);
  }

  public SimilaritySettings(List<MentionEntitySimilarityRaw> similarities, List<String[]> eeSimilarities,
      List<EntityImportancesRaw> entityImportances, double priorWeight) throws MissingSettingException {
    if (similarities != null) this.mentionEntitySimilaritiesWithPrior = similarities;
    this.entityEntitySimilarities = eeSimilarities;
    if (entityImportances != null) this.entityImportancesWithPrior = entityImportances;
    this.priorWeight = priorWeight;
  }

  public String getFullPath() {
    return fullPath;
  }

  public void setPriorThreshold(double priorThreshold) {
    this.priorThreshold = priorThreshold;
  }

  public double getPriorThreshold() {
    return priorThreshold;
  }

  public boolean shouldPriorTakeLog() {
    return priorTakeLog;
  }

  public void setPriorTakeLog(boolean priorTakeLog) {
    this.priorTakeLog = priorTakeLog;
  }

  public double getPriorDampingFactor() {
    return priorDampingFactor;
  }

  public void setPriorDampingFactor(double priorDampingFactor) {
    this.priorDampingFactor = priorDampingFactor;
  }

  /**
   * Returns the mention entity similarities (first is without prior, second is with prior).
   * Both are constructed at the same time ti be able to cache the EntitiesContext
   * (this accesses the DB and is very time intensive).
   *
   * @param entities
   * @param externalContext
   * @param tracer
   * @return
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws InstantiationException
   */
  public MentionEntitySimilarityPackage getMentionEntitySimilarities(Entities entities, ExternalEntitiesContext externalContext, Tracer tracer)
      throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, ClassNotFoundException {
    Map<Constructor<?>, Object> createdObjects = new HashMap<>();

    List<MentionEntitySimilarity> simsNoPrior = getMentionEntitiySimilaritiesConditioned(entities, externalContext, tracer, createdObjects,
        mentionEntitySimilaritiesNoPrior);
    List<MentionEntitySimilarity> simsWithPrior = getMentionEntitiySimilaritiesConditioned(entities, externalContext, tracer, createdObjects,
        mentionEntitySimilaritiesWithPrior);

    MentionEntitySimilarityPackage pack = new MentionEntitySimilarityPackage(simsNoPrior, simsWithPrior);
    return pack;
  }

  private List<MentionEntitySimilarity> getMentionEntitiySimilaritiesConditioned(Entities entities, ExternalEntitiesContext externalContext,
      Tracer tracer, Map<Constructor<?>, Object> createdObjects, List<MentionEntitySimilarityRaw> curMentionEntitySimilarities)
      throws InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException {
    List<MentionEntitySimilarity> sims = new ArrayList<>();
    if (curMentionEntitySimilarities != null) {
      for (MentionEntitySimilarityRaw mesr : curMentionEntitySimilarities) {
        Constructor simConstructor = Class.forName(mesr.fullSimClassName).getConstructor(Tracer.class);
        Constructor contextConstructor = Class.forName(mesr.fullContextClassName)
            .getConstructor(Entities.class, ExternalEntitiesContext.class, EntitiesContextSettings.class);

        Object simObj = simObj = simConstructor.newInstance(tracer);
        createdObjects.put(simConstructor, simObj);

        Object contextObj = createdObjects.get(contextConstructor);
        if (contextObj == null) {
          createdObjects
              .put(contextConstructor, contextObj = contextConstructor.newInstance(entities, externalContext, getEntitiesContextSettings(false)));
        }

        sims.add(new MentionEntitySimilarity((MentionEntitySimilarityMeasure) simObj, (EntitiesContext) contextObj, mesr.weight));
      }
    }
    return sims;
  }

  public List<MentionEntitySimilarity> getAllMentionEntitySimilarities(Entities entities, ExternalEntitiesContext externalContext, Tracer tracer)
      throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {

    Map<Constructor<?>, Object> createdObjects = new HashMap<>();

    List<MentionEntitySimilarity> allMentionEntitySimilarities = new LinkedList<>();
    allMentionEntitySimilarities
        .addAll(getMentionEntitiySimilaritiesConditioned(entities, externalContext, tracer, createdObjects, mentionEntitySimilaritiesNoPrior));
    allMentionEntitySimilarities
        .addAll(getMentionEntitiySimilaritiesConditioned(entities, externalContext, tracer, createdObjects, mentionEntitySimilaritiesWithPrior));

    return allMentionEntitySimilarities;
  }

  public EntityEntitySimilarity getEntityEntitySimilarity(String eeIdentifier, Entities entities, Tracer tracer) throws Exception {
    // Not needed now, but might be useful again in the future in case the EntityContexts need additional settings! Keep!
    EntitiesContextSettings settings = getEntitiesContextSettings(true);

    if (eeIdentifier.equals("MilneWittenEntityEntitySimilarity")) {
      return EntityEntitySimilarity.getMilneWittenSimilarity(entities, tracer);
    } else if (eeIdentifier.equals("InlinkOverlapEntityEntitySimilarity")) {
      return EntityEntitySimilarity.getInlinkOverlapSimilarity(entities, tracer);
     } else {
      throw new IllegalArgumentException("EESimilarity '" + eeIdentifier + "' undefined");
    }
  }

  public List<EntityImportance> getEntityImportances(Entities entities, boolean withPrior)
      throws IllegalAccessException, InvocationTargetException, InstantiationException {
    List<EntityImportance> result = new ArrayList<>();
    for (EntityImportancesRaw entityImportancesRaw : (withPrior ? entityImportancesWithPrior : entityImportancesNoPrior)) {
      EntityImportance entityImportance = (EntityImportance) entityImportancesRaw.getImpConstructor().newInstance(entities);
      entityImportance.setWeight(entityImportancesRaw.getWeight());
      result.add(entityImportance);
    }
    return result;
  }

  public List<EntityImportance> getAllEntityImportances(Entities entities)
      throws IllegalAccessException, InstantiationException, InvocationTargetException {

    List<EntityImportance> allEntityImportances = new LinkedList<>();
    allEntityImportances.addAll(getEntityImportances(entities, false));
    allEntityImportances.addAll(getEntityImportances(entities, true));

    return allEntityImportances;
  }

  public EntitiesContextSettings getEntitiesContextSettings(boolean isEntitiesContext) {
    EntitiesContextSettings settings = new EntitiesContextSettings();
    settings.setEntityCoherenceKeyphraseAlpha(entityCohKeyphraseAlpha);
    settings.setEntityCoherenceKeywordAlpha(entityCohKeywordAlpha);
    settings.setUseConfusableMIWeight(useConfusableMIWeights);
    settings.setShouldAverageWeights(shouldAverageCoherenceWeights);
    settings.setNgramLength(nGramLength);
    settings.setMinimumEntityKeyphraseWeight(minimumEntityKeyphraseWeight);
    settings.setMaxEntityKeyphraseCount(maxEntityKeyphraseCount);
    settings.setUnitSmoothingParameter(unitSmoothingParameter);
    settings.setIgnoreMention(unitIgnoreMention);
    // Some settings only apply to EntityEntityContexts.
    if (isEntitiesContext) {
      settings.setEntitiesContextType(EntitiesContextType.ENTITY_ENTITY);
      settings.setShouldNormalizeWeights(normalizeCoherenceWeights);
      settings.setEntityEntityKeyphraseSourceWeights(CollectionUtils.getWeightStringsAsMap(entityEntityKeyphraseSourceWeights));
    } else {
      settings.setEntitiesContextType(EntitiesContextType.MENTION_ENTITY);
      settings.setMentionEntityKeyphraseSourceWeights(CollectionUtils.getWeightStringsAsMap(mentionEntityKeyphraseSourceWeights));
    }
    return settings;
  }

  /**
   * Can only be called BEFORE the first getEntityEntitySimilarities, otherwise
   * there will be no effect.
   */
  public void setEntityEntitySimilarities(List<String[]> eeSims) {
    entityEntitySimilarities = eeSims;
  }

  public List<EntityEntitySimilarity> getEntityEntitySimilarities(Entities entities, Tracer tracer) throws Exception {
    List<EntityEntitySimilarity> eeSims = new LinkedList<EntityEntitySimilarity>();

    for (String[] s : entityEntitySimilarities) {
      String eeIdentifier = s[0];
      EntityEntitySimilarity eeSim = getEntityEntitySimilarity(eeIdentifier, entities, tracer);

      double weight = Double.parseDouble(s[1]);
      eeSim.setWeight(weight);

      eeSims.add(eeSim);
    }

    return eeSims;
  }

  public double getPriorWeight() {
    return priorWeight;
  }

  public String getIdentifier() {
    return identifier;
  }

  public double getEntityCohKeyphraseAlpha() {
    return entityCohKeyphraseAlpha;
  }

  public void setEntityCohKeyphraseAlpha(double entityCohKeyphraseAlpha) {
    this.entityCohKeyphraseAlpha = entityCohKeyphraseAlpha;
  }

  public double getEntityCohKeywordAlpha() {
    return entityCohKeywordAlpha;
  }

  public void setEntityCohKeywordAlpha(double entityCohKeywordAlpha) {
    this.entityCohKeywordAlpha = entityCohKeywordAlpha;
  }

  public List<MentionEntitySimilarityRaw> getMentionEntitySimilaritiesNoPrior() {
    return mentionEntitySimilaritiesNoPrior;
  }

  public void setMentionEntitySimilaritiesNoPrior(List<MentionEntitySimilarityRaw> mentionEntitySimilaritiesNoPrior) {
    if (mentionEntitySimilaritiesNoPrior == null) this.mentionEntitySimilaritiesNoPrior = new LinkedList<>();
    else this.mentionEntitySimilaritiesNoPrior = mentionEntitySimilaritiesNoPrior;
  }

  public List<MentionEntitySimilarityRaw> getMentionEntitySimilaritiesWithPrior() {
    return mentionEntitySimilaritiesWithPrior;
  }

  public void setMentionEntitySimilaritiesWithPrior(List<MentionEntitySimilarityRaw> mentionEntitySimilaritiesWithPrior) {
    if (mentionEntitySimilaritiesWithPrior == null) this.mentionEntitySimilaritiesWithPrior = new LinkedList<>();
    else this.mentionEntitySimilaritiesWithPrior = mentionEntitySimilaritiesWithPrior;
  }

  public List<EntityImportancesRaw> getEntityImportancesNoPrior() {
    return entityImportancesNoPrior;
  }

  public void setEntityImportancesNoPrior(List<EntityImportancesRaw> entityImportancesNoPrior) {
    if (entityImportancesNoPrior == null) this.entityImportancesNoPrior = new LinkedList<>();
    else this.entityImportancesNoPrior = entityImportancesNoPrior;
  }

  public List<EntityImportancesRaw> getEntityImportancesWithPrior() {
    return entityImportancesWithPrior;
  }

  public void setEntityImportancesWithPrior(List<EntityImportancesRaw> entityImportancesWithPrior) {
    if (entityImportancesWithPrior == null) this.entityImportancesWithPrior = new LinkedList<>();
    else this.entityImportancesWithPrior = entityImportancesWithPrior;
  }

  public void setPriorWeight(double priorWeight) {
    this.priorWeight = priorWeight;
  }

  public double getNormalizedAverageScore() throws FileNotFoundException, IOException {
    File parentDir = new File(fullPath).getParentFile();
    File avgPropFile = new File(parentDir, "averages.properties");

    if (avgPropFile.exists()) {
      Properties avgProp = new Properties();
      FileReader fr = new FileReader(avgPropFile);
      avgProp.load(fr);
      fr.close();

      if (avgProp.containsKey(identifier)) {
        String[] avgMax = avgProp.getProperty(identifier).split(":");
        double avg = Double.parseDouble(avgMax[0]);
        double max = Double.parseDouble(avgMax[1]);
        double normAvg = avg / max;
        return normAvg;
      } else {
        logger.error("Couldn't load averages for " + identifier + ", run AverageSimilarityScoresCalculator");
        return -1.0;
      }
    } else {
      logger.error("Couldn't load averages.properties from the settings dir, run AverageSimilarityScoresCalculator");
      return -1.0;
    }
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public void setShouldNormalizeCoherenceWeights(boolean flag) {
    normalizeCoherenceWeights = flag;
  }

  public boolean shouldNormalizeCoherenceWeights() {
    return normalizeCoherenceWeights;
  }

  public boolean isUseConfusableMIWeights() {
    return useConfusableMIWeights;
  }

  public void setUseConfusableMIWeights(boolean useConfusableMIWeights) {
    this.useConfusableMIWeights = useConfusableMIWeights;
  }

  public boolean shouldAverageCoherenceWeights() {
    return shouldAverageCoherenceWeights;
  }

  public void setShouldAverageCoherenceWeights(boolean averageCoherenceWeights) {
    this.shouldAverageCoherenceWeights = averageCoherenceWeights;
  }

  public double getMinimumEntityKeyphraseWeight() {
    return minimumEntityKeyphraseWeight;
  }

  public void setMinimumEntityKeyphraseWeight(double minimumEntityKeyphraseWeight) {
    this.minimumEntityKeyphraseWeight = minimumEntityKeyphraseWeight;
  }

  public int getMaxEntityKeyphraseCount() {
    return maxEntityKeyphraseCount;
  }

  public void setMaxEntityKeyphraseCount(int maxEntityKeyphraseCount) {
    this.maxEntityKeyphraseCount = maxEntityKeyphraseCount;
  }

  public void setMentionEntityKeyphraseSourceWeights(List<String[]> mentionEntityKeyphraseSourceWeights) {
    this.mentionEntityKeyphraseSourceWeights = mentionEntityKeyphraseSourceWeights;
  }

  public void setEntityEntityKeyphraseSourceWeights(List<String[]> entityEntityKeyphraseSourceWeights) {
    this.entityEntityKeyphraseSourceWeights = entityEntityKeyphraseSourceWeights;
  }

  public Map<String, Object> getAsMap() {
    Map<String, Object> s = new HashMap<String, Object>();
    if (!mentionEntitySimilaritiesNoPrior.isEmpty()) {
      s.put("mentionEntitySimilaritiesNoPrior",
          mentionEntitySimilaritiesNoPrior.stream().map(MentionEntitySimilarityRaw::toString).collect(Collectors.joining(" ")));
    }
    if (!mentionEntitySimilaritiesNoPrior.isEmpty()) {
      s.put("mentionEntitySimilaritiesWithPrior",
          mentionEntitySimilaritiesWithPrior.stream().map(MentionEntitySimilarityRaw::toString).collect(Collectors.joining(" ")));
    }
    if (!entityImportancesNoPrior.isEmpty()) {
      s.put("entityImportanceWeightsNoPrior", entityImportancesNoPrior.stream().map(EntityImportancesRaw::toString).collect(Collectors.joining(" ")));
    }
    if (!entityImportancesWithPrior.isEmpty()) {
      s.put("entityImportanceWeightsWithPrior",
          entityImportancesWithPrior.stream().map(EntityImportancesRaw::toString).collect(Collectors.joining(" ")));
    }
    if (entityEntitySimilarities != null) {
      s.put("entityEntitySimilarities", entityEntitySimilarities.stream().map(sim -> StringUtils.join(sim, ":")).collect(Collectors.joining(" ")));
    }
    s.put("maxEntityRank", String.valueOf(priorWeight));
    s.put("priorTakeLog", String.valueOf(priorTakeLog));
    s.put("nullMappingThreshold", String.valueOf(priorThreshold));
    s.put("includeNullAsEntityCandidate", String.valueOf(numberOfEntityKeyphrase));
    s.put("includeContextMentions", String.valueOf(entityCohKeyphraseAlpha));
    s.put("entityCohKeywordAlpha", String.valueOf(entityCohKeywordAlpha));
    s.put("normalizeCoherenceWeights", String.valueOf(normalizeCoherenceWeights));
    s.put("shouldAverageCoherenceWeights", String.valueOf(shouldAverageCoherenceWeights));
    s.put("useConfusableMIWeights", String.valueOf(useConfusableMIWeights));
    s.put("nGramLength", String.valueOf(nGramLength));
    s.put("minimumEntityKeyphraseWeight", String.valueOf(minimumEntityKeyphraseWeight));
    if (mentionEntityKeyphraseSourceWeights != null) {
      s.put("mentionEntityKeyphraseSourceWeights",
          mentionEntityKeyphraseSourceWeights.stream().map(sim -> StringUtils.join(sim, ":")).collect(Collectors.joining()));
    }
    if (entityEntityKeyphraseSourceWeights != null) {
      s.put("entityEntityKeyphraseSourceWeights",
          entityEntityKeyphraseSourceWeights.stream().map(sim -> StringUtils.join(sim, ":")).collect(Collectors.joining()));
    }
    if (identifier != null) {
      s.put("identifier", identifier);
    }
    return s;
  }

  public ImportanceAggregationStrategy getImportanceAggregationStrategy() {
    return importanceAggregationStrategy;
  }

  public void setImportanceAggregationStrategy(ImportanceAggregationStrategy importanceAggregationStrategy) {
    this.importanceAggregationStrategy = importanceAggregationStrategy;
  }

  public static class MentionEntitySimilarityRaw implements Serializable {

    public void setUseDistanceDiscount(boolean useDistanceDiscount) {
      this.useDistanceDiscount = useDistanceDiscount;
    }

    private double weight;

    private boolean useDistanceDiscount;

    public MentionEntitySimilarityRaw() {
      // bean.
    }

    String fullSimClassName;

    String fullContextClassName;

    public MentionEntitySimilarityRaw(String simClassName, String contextClassName, double weight, boolean useDistanceDiscount)
        throws ClassNotFoundException, NoSuchMethodException {
      this.fullSimClassName = simClassName;
      this.fullContextClassName = contextClassName;
      this.weight = weight;
      this.useDistanceDiscount = useDistanceDiscount;
    }

    public Constructor<?> getSimConstructor() throws ClassNotFoundException, NoSuchMethodException {
      return Class.forName(fullSimClassName).getConstructor(Tracer.class);
    }

    public Constructor<?> getContextConstructor() throws ClassNotFoundException, NoSuchMethodException {
      return Class.forName(fullContextClassName).getConstructor(Entities.class, ExternalEntitiesContext.class, EntitiesContextSettings.class);
    }

    public boolean isUseDistanceDiscount() {
      return useDistanceDiscount;
    }

    public double getWeight() {
      return weight;
    }

    public void setWeight(double weight) {
      this.weight = weight;
    }

    public static MentionEntitySimilarityRaw parseFrom(String input) throws NoSuchMethodException, ClassNotFoundException {
      String[] s = input.split(":");

      String[] simConfig = s[0].split(",");
      // get flags
      boolean useDistanceDiscount = false;
      for (int i = 1; i < simConfig.length; i++) {
        if (simConfig[i].equals("i")) {
          useDistanceDiscount = true;
        }
      }

      // to support settings files with no weights for training.
      double weight;
      if (s.length < 3) weight = 0d;
      else weight = Double.parseDouble(s[2]);

      return new MentionEntitySimilarityRaw(simConfig[0], s[1], weight, useDistanceDiscount);
    }

    @Override public String toString() {
      return fullSimClassName + (useDistanceDiscount ? ",i:" : ":") + fullContextClassName + ":" + Double.toString(weight);
    }
  }

  public static class EntityImportancesRaw implements Serializable {

    private Constructor<?> impConstructor;

    private double weight;

    public EntityImportancesRaw() {
      // bean.
    }

    public EntityImportancesRaw(String simClassName, double weight) throws ClassNotFoundException, NoSuchMethodException {
      String fullImpClassName = "mpi.aida.graph.similarity.importance." + simClassName;
      this.impConstructor = Class.forName(fullImpClassName).getConstructor(Entities.class);
      this.weight = weight;
    }

    public Constructor<?> getImpConstructor() {
      return impConstructor;
    }

    public void setImpConstructor(Constructor<?> impConstructor) {
      this.impConstructor = impConstructor;
    }

    public double getWeight() {
      return weight;
    }

    public void setWeight(double weight) {
      this.weight = weight;
    }

    public static EntityImportancesRaw parseFrom(String input) throws NoSuchMethodException, ClassNotFoundException {
      String[] s = input.split(":");

      // to support settings files with no weights for training.
      double weight;
      if (s.length < 2) weight = 0d;
      else weight = Double.parseDouble(s[1]);

      return new EntityImportancesRaw(s[0], weight);
    }

    @Override public String toString() {
      return impConstructor.getDeclaringClass().getSimpleName() + ":" + Double.toString(weight);
    }
  }
}
