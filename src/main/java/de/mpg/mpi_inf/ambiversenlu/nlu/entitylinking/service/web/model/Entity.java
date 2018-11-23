package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model;

import com.fasterxml.jackson.annotation.*;
import com.google.common.base.Objects;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL) @Generated("org.jsonschema2pojo") @JsonPropertyOrder({
    "id",
    "name",
    "url",
    "type",
    "confidence",
    "salience"
}) public class Entity {

  /**
   * The knowledge graph ID of the entity. Query the https://api.ambiverse.com/knowledgegraph/ API for additional knowledge stored about this entity.
   *
   */
  @JsonProperty("id") private String id;

  /**
   * The URL of the entity. Usualy this is Wikipedia URL.
   *
   */
  @JsonProperty("url")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String url;

  /**
   * The confidence score of our system that the entity is correctly identified.
   *
   */
  @JsonProperty("confidence")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Double confidence;

  /**
   * The salience score of the entity in a range of [0,1]. The salience score provides information about the importance of the entity to the text.
   * Score closer to 1 means that the entity is more salient, while score closer to 0 means less salient.
   */
  @JsonProperty("salience")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Double salience;


  @JsonProperty("name")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String name;

  @JsonProperty("type")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Type type;

  @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<String, Object>();


  public enum Type {
    UNKNOWN, PERSON, LOCATION, ORGANIZATION, ARTIFACT, EVENT, CONCEPT, OTHER
  }
  
  public Entity() {
    // Auto-generated constructor stub
  }

  /**
   * The knowledge graph ID of the entity. Query the https://api.ambiverse.com/knowledgegraph/ API for additional knowledge stored about this entity.
   *
   * @return
   *     The id
   */
  @JsonProperty("id")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public String getId() {
    return id;
  }

  /**
   * The URL of the entity. Usualy this is Wikipedia URL.
   *
   * @return
   *  The url
   */
  @JsonProperty("url") public String getUrl() {
    return url;
  }

  /**
   * The knowledge graph ID of the entity. Query the https://api.ambiverse.com/knowledgegraph/ API for additional knowledge stored about this entity.
   *
   * @param id
   *     The id
   */
  @JsonProperty("id") public void setId(String id) {
    this.id = id;
  }

  /**
   * The URL of the entity. Usualy this is Wikipedia URL.
   *
   * @param id
   *     The id
   */
  @JsonProperty("url") public void setUrl(String url) {
    this.url = url;
  }

  public Entity withId(String id) {
    this.id = id;
    return this;
  }

  /**
   * The salience score of the entity in a range of [0,1]. The salience score provides information about the importance of the entity to the text.
   * Score closer to 1 means that the entity is more salient, while score closer to 0 means less salient.
   *
   * @return
   *     The confidence
   */
  @JsonProperty("salience") public Double getSalience() {
    return salience;
  }

  /**
   * The salience score of the entity in a range of [0,1]. The salience score provides information about the importance of the entity to the text.
   * Score closer to 1 means that the entity is more salient, while score closer to 0 means less salient.
   *
   * @param confidence
   *     The confidence
   */
  @JsonProperty("salience") public void setSalience(Double salience) {
    this.salience = salience;
  }

  public Entity withSalience(Double salience) {
    this.salience = salience;
    return this;
  }

  /**
   * The most salient type of the entity.
   *
   * @return
   *     The name
   */
  @JsonProperty("type") public Type getType() {
    return type;
  }

  /**
   * TThe most salient type of the entity.
   *
   * @param name
   *     The name
   */
  @JsonProperty("type") public void setType(Type type) {
    this.type = type;
  }

  public Entity withType(Type type) {
    this.type = type;
    return this;
  }

  /**
   * The name of the entity.
   *
   * @return
   *     The name
   */
  @JsonProperty("name") public String getName() {
    return name;
  }

  /**
   * The name of the entity
   *
   * @param name
   *     The name
   */
  @JsonProperty("name") public void setName(String name) {
    this.name = name;
  }

  public Entity withName(String name) {
    this.name = name;
    return this;
  }

  /**
   * The confidence score of our system that the entity is correctly identified.
   *
   * @return
   *     The confidence
   */
  @JsonProperty("confidence") public Double getConfidence() {
    return confidence;
  }

  /**
   * The confidence score of our system that the entity is correctly identified.
   *
   * @param confidence
   *     The confidence
   */
  @JsonProperty("confidence") public void setConfidence(Double confidence) {
    this.confidence = confidence;
  }

  public Entity withConfidence(Double confidence) {
    this.confidence = confidence;
    return this;
  }


  @JsonAnyGetter public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }

  public Entity withAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
    return this;
  }

  @Override public String toString() {
    return "Entity{" + "id='" + id + '\'' + ", url='" + url + '\'' + ", confidence=" + confidence + ", salience=" + salience + ", name='" + name
        + '\'' + ", type=" + type + '}';
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.id);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final Entity other = (Entity) obj;

    return Objects.equal(this.id, other.id);
  }

  public static Entity.Type getEntityType(Set<String> types) {

    if(types == null || types.isEmpty()) {
      return Entity.Type.UNKNOWN;
    }

    if (types.stream().anyMatch(str -> str.contains("person"))) {
      return Entity.Type.PERSON;
    }
    if (types.stream().anyMatch(str -> str.contains("yagoGeoEntity"))) {
      return Entity.Type.LOCATION;
    }
    if (types.stream().anyMatch(str -> str.contains("organization"))) {
      return Entity.Type.ORGANIZATION;
    }
    if (types.stream().anyMatch(str -> str.contains("artifact"))) {
      return Entity.Type.ARTIFACT;
    }
    if (types.stream().anyMatch(str -> str.contains("event"))) {
      return Entity.Type.EVENT;
    }
    return Entity.Type.OTHER;
  }
}
