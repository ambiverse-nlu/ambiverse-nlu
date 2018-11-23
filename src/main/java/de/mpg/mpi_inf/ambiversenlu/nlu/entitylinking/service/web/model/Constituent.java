package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "text",
        "normalizedForm",
        "charOffset",
        "charLength",
        "uri",
        "confidence"
})
public class Constituent {

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

    @JsonProperty("entities")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnore
    private List<Entity> entitis;

    @JsonProperty("normalizedForm")
    private String normalizedForm;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Constituent() {
    }

    public String getnormalizedForm() {
        return normalizedForm;
    }

    public void setnormalizedForm(String normalizedForm) {
        this.normalizedForm = normalizedForm;
    }

    public Constituent withNormalizedForm(String normalizedForm) {
        this.normalizedForm = normalizedForm;
        return this;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Constituent withText(String Text) {
        this.text = text;
        return this;
    }

    public Integer getCharOffset() {
        return charOffset;
    }

    public void setCharOffset(Integer charOffset) {
        this.charOffset = charOffset;
    }

    public Constituent withCharOffset(Integer charOffset) {
        this.charOffset = charOffset;
        return this;
    }

    public Integer getCharLength() {
        return charLength;
    }

    public void setCharLength(Integer charLength) {
        this.charLength = charLength;
    }

    public Constituent withCharLength(Integer charLength) {
        this.charLength = charLength;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Constituent withUri(String uri) {
        this.uri = uri;
        return this;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Constituent withConfidence(Double confidence) {
        this.confidence = confidence;
        return this;
    }

    @JsonIgnore
    public List<Entity> getEntitis() {
        return entitis;
    }

    @JsonIgnore
    public void setEntitis(List<Entity> entitis) {
        this.entitis = entitis;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Constituent withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
}