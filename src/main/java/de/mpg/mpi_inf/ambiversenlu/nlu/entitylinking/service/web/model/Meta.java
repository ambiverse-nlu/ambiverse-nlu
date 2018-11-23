package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.util.DateDeserializer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.util.DateSerializer;

import javax.annotation.Generated;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL) @Generated("org.jsonschema2pojo") @JsonPropertyOrder({
    "dumpVersion", "languages", "creationDate" }) public class Meta {

  @JsonProperty("dumpVersion") private String dumpVersion;

  @JsonProperty("languages") @JsonDeserialize(as = java.util.LinkedHashSet.class) private Set<String> languages = new LinkedHashSet<String>();

  @JsonProperty("creationDate") @JsonSerialize(using = DateSerializer.class) @JsonDeserialize(using = DateDeserializer.class) private Date creationDate;

  @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  /**
   *
   * @return
   *     The dumpVersion
   */
  @JsonProperty("dumpVersion") public String getDumpVersion() {
    return dumpVersion;
  }

  /**
   *
   * @param dumpVersion
   *     The dumpVersion
   */
  @JsonProperty("dumpVersion") public void setDumpVersion(String dumpVersion) {
    this.dumpVersion = dumpVersion;
  }

  public Meta withDumpVersion(String dumpVersion) {
    this.dumpVersion = dumpVersion;
    return this;
  }

  /**
   *
   * @return
   *     The languages
   */
  @JsonProperty("languages") public Set<String> getLanguages() {
    return languages;
  }

  /**
   *
   * @param languages
   *     The languages
   */
  @JsonProperty("languages") public void setLanguages(Set<String> languages) {
    this.languages = languages;
  }

  public Meta withLanguages(Set<String> languages) {
    this.languages = languages;
    return this;
  }

  /**
   *
   * @return
   *     The creationDate
   */
  @JsonProperty("creationDate") public Date getCreationDate() {
    return creationDate;
  }

  /**
   *
   * @param creationDate
   *     The creationDate
   */
  @JsonProperty("creationDate") public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Meta withCreationDate(Date creationDate) {
    this.creationDate = creationDate;
    return this;
  }

  @JsonAnyGetter public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  public Meta withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

}
