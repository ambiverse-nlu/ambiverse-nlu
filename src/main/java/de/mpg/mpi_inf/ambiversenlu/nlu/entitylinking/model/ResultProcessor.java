package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.JsonSettings.JSONTYPE;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 *  Generates a JSON representation of the disambiguated results.
 */
public class ResultProcessor {

  public static final String FORMAT_VERSION = "2.3";

  private DisambiguationResults result;

  private Map<KBIdentifiedEntity, EntityMetaData> entitiesMetaData;

  private String inputFile;

  private PreparedInput input;

  private int maxEntities;

  private long time;

  // Reference to final JSON object
  private JSONObject jFinalObj;

  private JSONArray jAllMentions;

  private void init() throws EntityLinkingDataAccessException {
    jFinalObj = new JSONObject();
    jAllMentions = new JSONArray();
    Set<KBIdentifiedEntity> entities = new HashSet<KBIdentifiedEntity>();
    for (ResultMention resultMention : result.getResultMentions()) {
      for (ResultEntity resultEntity : resultMention.getResultEntities()) {
        entities.add(resultEntity.getKbEntity());
      }
    }
    entitiesMetaData = DataAccess.getEntitiesMetaData(entities);
  }

  public ResultProcessor() throws EntityLinkingDataAccessException {
    init();
  }

  public ResultProcessor(DisambiguationResults result, String inputFile, PreparedInput input) throws EntityLinkingDataAccessException {
    this(result, inputFile, input, Integer.MAX_VALUE);
  }

  /**
   * Constructs ResultProcessor object.
   *
   * @param result Reference DisambiguationResult object
   */
  public ResultProcessor(DisambiguationResults result, String inputFile, PreparedInput input, int maxEntities)
      throws EntityLinkingDataAccessException {
    this.result = result;
    this.input = input;
    this.inputFile = inputFile;
    this.maxEntities = maxEntities;

    init();
  }

  public void setInput(PreparedInput input) {
    this.input = input;
  }

  public void setOverallTime(long time) {
    this.time = time;
  }

  /**
   * Constructs a JSON representation for given disambiguationResult object
   *
   * @return A JSON string representation of result
   */
  @SuppressWarnings("unchecked") public JSONObject process(JSONTYPE jMode) throws EntityLinkingDataAccessException {
    // prepare the compact json representation
    jFinalObj.put("docID", input.getDocId());
    if (jMode != JSONTYPE.STICS) {
      jFinalObj.put("originalFileName", inputFile);
    }
    jFinalObj.put("formatVersion", FORMAT_VERSION);

    String text = input.getOriginalText();

    if (jMode != JSONTYPE.STICS && jMode != JSONTYPE.COMPACT) {
      jFinalObj.put("annotatedText", annotateInputText(text, result));
    }

    if (jMode == JSONTYPE.ANNOTATED_TEXT) {
      jFinalObj.put("originalText", text);
      return jFinalObj;
    }

    Entities aidaEntities = DataAccess.getAidaEntitiesForKBEntities(entitiesMetaData.keySet());
    TIntObjectHashMap<Set<Type>> entitiesTypes = DataAccess.getTypes(aidaEntities);
    TIntDoubleHashMap entitiesImportances = DataAccess.getEntitiesImportances(aidaEntities.getUniqueIdsAsArray());

    // Contains all entities that are bestEntity for a mention.
    Set<String> allEntities = new HashSet<>();

    for (ResultMention rm : result.getResultMentions()) {
      JSONObject jMention = generateJSONForMention(rm);
      // retrieve entity types for the best entity and generate json repr
      ResultEntity bestEntity = rm.getBestEntity();
      if (!bestEntity.getEntity().equals(Entity.OOKBE)) {
        JSONObject jsonEntity = new JSONObject();
        String kbId = bestEntity.getKbEntity().getDictionaryKey();
        jsonEntity.put("kbIdentifier", kbId);
        allEntities.add(kbId);
        NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
        df.setMaximumFractionDigits(5);
        jsonEntity.put("disambiguationScore", df.format(bestEntity.getScore()));
        // add best entity element to mention element
        jMention.put("bestEntity", jsonEntity);
      }

      // Generate temporary JSON Object to store other candidate entities but no need to add to mention json
      JSONObject jsonEntity;
      JSONArray jsonEntityArr = new JSONArray();
      NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
      df.setGroupingUsed(false);
      df.setMaximumFractionDigits(5);
      int i = 0;
      for (ResultEntity re : rm.getResultEntities()) {
        if (++i > maxEntities) {
          break;
        }

        if (!re.getEntity().equals(Entity.OOKBE)) {
          // create a json object for entity
          jsonEntity = new JSONObject();
          String kbId = re.getKbEntity().getDictionaryKey();
          jsonEntity.put("kbIdentifier", kbId);
          jsonEntity.put("disambiguationScore", df.format(re.getScore()));
          jsonEntityArr.add(jsonEntity);
        }
      }
      if (jMode != JSONTYPE.STICS) {
        jMention.put("allEntities", jsonEntityArr);
      }
      jAllMentions.add(jMention);
    }
    jFinalObj.put("mentions", jAllMentions);
    jFinalObj.put("originalText", text);
    jFinalObj.put("overallTime", "" + time);
    JSONArray jAllEntities = new JSONArray();
    jAllEntities.addAll(allEntities);
    jFinalObj.put("allEntities", jAllEntities);

    if (jMode == JSONTYPE.COMPACT) {
      return jFinalObj;
    }

    // add all entities metadata (required for both WEB and EXT versions)
    JSONObject jMetadataArray = new JSONObject();
    Set<String> allTypes = new HashSet<>();
    for (Entry<KBIdentifiedEntity, EntityMetaData> e : entitiesMetaData.entrySet()) {
      JSONObject jMetadata = new JSONObject();
      KBIdentifiedEntity kbEntity = e.getKey();
      EntityMetaData eData = e.getValue();
      if (eData != null) {
        jMetadata.put("readableRepr", eData.getHumanReadableRepresentation());
        jMetadata.put("entityId", kbEntity.getIdentifier());
        jMetadata.put("url", eData.getUrl());
        jMetadata.put("knowledgebase", eData.getKnowledgebase());
        jMetadata.put("depictionurl", eData.getDepictionurl());
        jMetadata.put("depictionthumbnailurl", eData.getDepictionthumbnailurl());
        jMetadata.put("importance", entitiesImportances.get(eData.getId()));

        // Temporary JSONArray to store entity types for the best entity
        JSONArray tmpEntityTypes = new JSONArray();
        List<String> entityTypesStrings = new ArrayList<String>();
        Set<Type> hshTypeSet = entitiesTypes.get(eData.getId());
        if (hshTypeSet != null) {
          for (Type type : hshTypeSet) {
            entityTypesStrings.add(type.toString());
          }
          tmpEntityTypes.addAll(entityTypesStrings);
          allTypes.addAll(entityTypesStrings);
        }
        jMetadata.put("type", tmpEntityTypes);
      }
      jMetadataArray.put(kbEntity.getDictionaryKey(), jMetadata);
    }

    // Add all types in document.
    JSONArray jAllTypes = new JSONArray();
    jAllTypes.addAll(allTypes);
    jFinalObj.put("allTypes", jAllTypes);

    if (jMode == JSONTYPE.EXTENDED || jMode == JSONTYPE.STICS) {
      // Add ranges for title and abstract
      if (input.getTitleRange() != null) {
        JSONArray titleRange = new JSONArray();
        titleRange.add(0, input.getTitleRange().lowerEndpoint());
        titleRange.add(1, input.getTitleRange().upperEndpoint());
        jFinalObj.put("titleRange", titleRange);
      }

      if (input.getAbstractRange() != null) {
        JSONArray abstractRange = new JSONArray();
        abstractRange.add(0, input.getAbstractRange().lowerEndpoint());
        abstractRange.add(1, input.getAbstractRange().upperEndpoint());
        jFinalObj.put("abstractRange", abstractRange);
      }
    }

    if (jMode == JSONTYPE.STICS) {
      return jFinalObj;
    }

    jFinalObj.put("entityMetadata", jMetadataArray);
    if (jMode == JSONTYPE.DEFAULT) {
      return jFinalObj;
    }

    if (jMode == JSONTYPE.WEB) {
      jFinalObj.put("gTracerHtml", result.getgTracerHtml());
      jFinalObj.put("tracerHtml", result.getTracer().getHtmlOutputForWebInterface());
      return jFinalObj;
    }

    jFinalObj.put("tokens", loadTokens(jMode));
    return jFinalObj;
  }

  @SuppressWarnings("unchecked") private JSONArray loadTokens(JSONTYPE jMode) {
    List<Token> lstToks = input.getTokens().getTokens();
    JSONArray jTokArr = new JSONArray();
    for (Token tk : lstToks) {
      JSONObject jTok = new JSONObject();
      jTok.put("original", tk.getOriginal());
      jTok.put("originalEnd", tk.getOriginalEnd());
      jTok.put("beginIndex", tk.getBeginIndex());
      jTok.put("sentence", tk.getSentence());
      if (jMode != JSONTYPE.STICS) {
        jTok.put("stanfordId", tk.getStandfordId());
        jTok.put("endIndex", tk.getEndIndex());
        jTok.put("paragraph", tk.getParagraph());
        jTok.put("POS", tk.getPOS());
        jTok.put("NE", tk.getNE());
        jTok.put("pageNumber", tk.getPageNumber());
      }
      jTokArr.add(jTok);
    }
    return jTokArr;
  }

  @SuppressWarnings("unchecked") private JSONObject generateJSONForMention(ResultMention rm) {
    JSONObject jObj = new JSONObject();
    // create json representation for given mention
    String mentionName = rm.getMention();
    int offset = rm.getCharacterOffset();
    jObj.put("name", mentionName);
    jObj.put("length", rm.getCharacterLength());
    jObj.put("offset", offset);
    return jObj;
  }

  private String annotateInputText(String text, DisambiguationResults results) {

    Map<Integer, ResultMention> mentionIndex = new HashMap<>();

    for (ResultMention rm : result.getResultMentions()) {
      mentionIndex.put(rm.getCharacterOffset(), rm);
    }
    StringBuffer sBuff = new StringBuffer();
    int len = text.length();
    int start = 0;
    int current = 0;
    while (current < len) {
      if (mentionIndex.containsKey(current)) {
        ResultMention rm = mentionIndex.get(current);
        sBuff.append(text.substring(start, current));
        sBuff.append(constructAnnotation(rm));
        start = current + rm.getCharacterLength();
        current = start;
      } else {
        current++;
      }
    }
    if (current > start) {
      sBuff.append(text.substring(start, current));
    }
    return sBuff.toString();
  }

  private String constructAnnotation(ResultMention rm) {
    StringBuffer sb = new StringBuffer();
    ResultEntity re = rm.getBestEntity();
    //		String url = Entity.OOKBE;
    //		if (!re.isNoMatchingEntity()) {
    //		  EntityMetaData entityMeta = entitiesMetaData.get(re.getKbEntity());
    //		  if(entityMeta != null) {
    //		    url = entityMeta.getUrl();
    //		  } else {
    //		    url = re.getKbEntity().toString();
    //		  }
    //		}    
    sb.append("[[").append(re.getKbEntity().toString()).append("|").append(rm.getMention()).append("]]");
    return sb.toString();
  }
}