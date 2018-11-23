package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL) @Generated("org.jsonschema2pojo") @JsonPropertyOrder({
    "charLength", "charOffset", }) public class AnnotatedMention {

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

  public AnnotatedMention withCharLength(Integer charLength) {
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

  public AnnotatedMention withCharOffset(Integer charOffset) {
    this.charOffset = charOffset;
    return this;
  }

  @JsonAnyGetter public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  public AnnotatedMention withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

}
