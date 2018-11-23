package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.entitiesmetadata;

public class EntityMetaData {

  private String entity;

  private String humanReadableRepresentation;

  private String URL;

  private String depictionURL;

  private String licenseURL;

  public String getLicenseURL() {
    return licenseURL;
  }

  public void setLicenseURL(String licenseURL) {
    this.licenseURL = licenseURL;
  }

  private String description;

  private String wikiData;

  public EntityMetaData(String entity, String humanReadableRepresentation, String uRL, String depictionURL, String licenseURL, String description,
      String wikiData) {
    super();
    this.entity = entity;
    this.humanReadableRepresentation = humanReadableRepresentation;
    URL = uRL;
    this.depictionURL = depictionURL;
    this.description = description;
    this.wikiData = wikiData;
    this.licenseURL = licenseURL;
  }

  public String getEntity() {
    return entity;
  }

  public void setId(String entity) {
    this.entity = entity;
  }

  public String getHumanReadableRepresentation() {
    return humanReadableRepresentation;
  }

  public void setHumanReadableRepresentation(String humanReadableRepresentation) {
    this.humanReadableRepresentation = humanReadableRepresentation;
  }

  public String getURL() {
    return URL;
  }

  public void setURL(String uRL) {
    URL = uRL;
  }

  public String getDepictionURL() {
    return depictionURL;
  }

  public void setDepictionURL(String depictionURL) {
    this.depictionURL = depictionURL;
  }

  public String getDescription() {
    return description;
  }

  public String getWikiData() {
    return wikiData;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override public int hashCode() {
    return entity.hashCode();
  }

  @Override public boolean equals(Object obj) {

    return entity.equals(((EntityMetaData) obj).entity);
  }

}
