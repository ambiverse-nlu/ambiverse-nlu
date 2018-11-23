package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL) @Generated("org.jsonschema2pojo") @JsonPropertyOrder({
    "charLength", "charOffset", "text", "entity" }) public class Match {

  /**
   * The character length of the match in the text.
   *
   */
  @JsonProperty("charLength") private Integer charLength;

  /**
   * The character offset of the match in the text, starting at 0.
   *
   */
  @JsonProperty("charOffset") private Integer charOffset;

  /**
   * The full text of the match (equivalent to the substring of the text defined by charOffset and charLength).
   *
   */
  @JsonProperty("text") private String text;

  /**
   * NER type of the mention.
   */
  @JsonProperty("type") private String type;

  @JsonProperty("entity") private Entity entity;

  @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  /**
   * The character length of the match in the text.
   *
   * @return
   *     The charLength
   */
  @JsonProperty("charLength") public Integer getCharLength() {
    return charLength;
  }

  /**
   * The character length of the match in the text.
   *
   * @param charLength
   *     The charLength
   */
  @JsonProperty("charLength") public void setCharLength(Integer charLength) {
    this.charLength = charLength;
  }

  public Match withCharLength(Integer charLength) {
    this.charLength = charLength;
    return this;
  }

  /**
   * The character offset of the match in the text, starting at 0.
   *
   * @return
   *     The charOffset
   */
  @JsonProperty("charOffset") public Integer getCharOffset() {
    return charOffset;
  }

  /**
   * The character offset of the match in the text, starting at 0.
   *
   * @param charOffset
   *     The charOffset
   */
  @JsonProperty("charOffset") public void setCharOffset(Integer charOffset) {
    this.charOffset = charOffset;
  }

  public Match withCharOffset(Integer charOffset) {
    this.charOffset = charOffset;
    return this;
  }

  /**
   * NER type of the mention.
   *
   * @return
   *     The type
   */
  @JsonProperty("type") public String getType() {
    return type;
  }

  /**
   * NER type of the mention.
   *
   * @param type
   *     The NER type
   */
  @JsonProperty("type") public void setType(String type) {
    this.type = type;
  }

  public Match withType(String type) {
    this.type = type;
    return this;
  }

  /**
   * The full text of the match (equivalent to the substring of the text defined by charOffset and charLength).
   *
   * @return
   *     The text
   */
  @JsonProperty("text") public String getText() {
    return text;
  }

  /**
   * The full text of the match (equivalent to the substring of the text defined by charOffset and charLength).
   *
   * @param text
   *     The text
   */
  @JsonProperty("text") public void setText(String text) {
    this.text = text;
  }

  public Match withText(String text) {
    this.text = text;
    return this;
  }

  /**
   *
   * @return
   *     The entity
   */
  @JsonProperty("entity") public Entity getEntity() {
    return entity;
  }

  /**
   *
   * @param entity
   *     The entity
   */
  @JsonProperty("entity") public void setEntity(Entity entity) {
    this.entity = entity;
  }

  public Match withEntity(Entity entity) {
    this.entity = entity;
    return this;
  }

  @JsonAnyGetter public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  public Match withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override public String toString() {
    return "Match{" + "charLength=" + charLength + ", charOffset=" + charOffset + ", text='" + text + '\'' + ", entity=" + entity + '}';
  }
}
