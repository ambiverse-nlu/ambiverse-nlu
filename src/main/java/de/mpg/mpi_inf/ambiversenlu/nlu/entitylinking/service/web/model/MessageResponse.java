package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL) @Generated("org.jsonschema2pojo") @JsonPropertyOrder({
    "message" }) public class MessageResponse {

  /**
   *
   * (Required)
   *
   */
  @JsonProperty("message") private String message;

  @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  /**
   *
   * (Required)
   *
   * @return
   *     The message
   */
  @JsonProperty("message") public String getMessage() {
    return message;
  }

  /**
   *
   * (Required)
   *
   * @param message
   *     The message
   */
  @JsonProperty("message") public void setMessage(String message) {
    this.message = message;
  }

  public MessageResponse withMessage(String message) {
    this.message = message;
    return this;
  }

  @JsonAnyGetter public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  public MessageResponse withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

}
