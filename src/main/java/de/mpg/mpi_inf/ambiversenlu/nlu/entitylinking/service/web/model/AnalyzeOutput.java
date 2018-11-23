package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL) @Generated("org.jsonschema2pojo") @JsonPropertyOrder({
    "docId", "language", "matches" }) public class AnalyzeOutput {

  @JsonProperty("docId") private String docId;

  @JsonProperty("language") private String language;

  @JsonProperty("matches") private List<Match> matches = new ArrayList<Match>();

  @JsonProperty("facts")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Fact> facts = new ArrayList<>();

  @JsonProperty("entities")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Set<Entity> entities = new HashSet<>();

  @JsonIgnore
  @JsonProperty("text")
  private String text;

  @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  /**
   *
   * @return
   *     The docId
   */
  @JsonProperty("docId") public String getDocId() {
    return docId;
  }

  /**
   *
   * @param docId
   *     The docId
   */
  @JsonProperty("docId") public void setDocId(String docId) {
    this.docId = docId;
  }

  public AnalyzeOutput withDocId(String docId) {
    this.docId = docId;
    return this;
  }

  /**
   *
   * @return
   *     The language
   */
  @JsonProperty("language") public String getLanguage() {
    return language;
  }

  /**
   *
   * @param language
   *     The language
   */
  @JsonProperty("language") public void setLanguage(String language) {
    this.language = language;
  }

  public AnalyzeOutput withLanguage(String language) {
    this.language = language;
    return this;
  }

  /**
   *
   * @return
   *     The matches
   */
  @JsonProperty("matches") public List<Match> getMatches() {
    return matches;
  }

  /**
   *
   * @param matches
   *     The matches
   */
  @JsonProperty("matches") public void setMatches(List<Match> matches) {
    this.matches = matches;
  }

  public AnalyzeOutput withMatches(List<Match> matches) {
    this.matches = matches;
    return this;
  }

  /**
   *
   * @return list of entities
   */
  @JsonProperty("entities")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Set<Entity> getEntities() {
      return entities;
  }

  /**
   *
   * @param entities
   */
  @JsonProperty("entities")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public void setEntities(Set<Entity> entities) {
      this.entities = entities;
  }

  /**
   * Create Analyze Output with Entities
   * @param entities
   * @return
   */
  public AnalyzeOutput withEntities(Set<Entity> entities) {
    this.entities = entities;
    return this;
  }


  @JsonProperty("text")
  @JsonIgnore
  public String getText() {
    return text;
  }

  @JsonProperty("text")
  @JsonIgnore
  public void setText(String text) {
    this.text = text;
  }

  public AnalyzeOutput withText(String text) {
    this.text = text;
    return this;
  }

  @JsonProperty("facts")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public List<Fact> getFacts() {
    return facts;
  }

  @JsonProperty("facts")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public void setFacts(List<Fact> facts) {
    this.facts = facts;
  }

  public AnalyzeOutput withFacts(List<Fact> facts) {
    this.facts = facts;
    return this;
  }

  @JsonAnyGetter public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  public AnalyzeOutput withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override public String toString() {
    return "AnalyzeOutput{" + "docId='" + docId + '\'' + ", language='" + language + '\'' + ", matches=" + matches + ", facts=" + facts
        + ", entities=" + entities + '}';
  }
}
