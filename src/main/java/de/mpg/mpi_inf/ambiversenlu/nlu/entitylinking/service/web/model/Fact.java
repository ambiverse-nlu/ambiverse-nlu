package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "text",
        "charOffset",
        "charLength",
        "uri",
        "confidence",
        "subject",
        "relation",
        "object"
})
public class Fact {

    @JsonProperty("text")
    private String text;

    @JsonProperty("charOffset")
    private Integer charOffset;

    @JsonProperty("charLength")
    private Integer charLength;

    @JsonProperty("uri")
    private String uri;

    @JsonProperty("confidence")
    private Double confidence;

    @JsonProperty("subject")
    private Constituent subject;

    @JsonProperty("object")
    private Constituent object;

    @JsonProperty("relation")
    private Constituent relation;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Fact() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Fact withText(String text) {
        this.text = text;
        return this;
    }

    public Integer getCharOffset() {
        return charOffset;
    }

    public void setCharOffset(Integer charOffset) {
        this.charOffset = charOffset;
    }

    public Fact withCharOffset(Integer charOffset) {
        this.charOffset = charOffset;
        return this;
    }

    public Integer getCharLength() {
        return charLength;
    }

    public void setCharLength(Integer charLength) {
        this.charLength = charLength;
    }

    public Fact withCharLength(Integer charLength) {
        this.charLength = charLength;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Fact withUri(String uri) {
        this.uri = uri;
        return this;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Fact withConfidence(Double confidence) {
        this.confidence = confidence;
        return this;
    }

    public Constituent getSubject() {
        return subject;
    }

    public void setSubject(Constituent subject) {
        this.subject = subject;
    }

    public Fact withSubject(Constituent subject) {
        this.subject = subject;
        return this;
    }

    public Constituent getObject() {
        return object;
    }

    public void setObject(Constituent object) {
        this.object = object;
    }

    public Fact withObject(Constituent object) {
        this.object = object;
        return this;
    }


    public Constituent getRelation() {
        return relation;
    }

    public void setRelation(Constituent relation) {
        this.relation = relation;
    }

    public Fact withRelation(Constituent relation) {
        this.relation = relation;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Fact withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
}