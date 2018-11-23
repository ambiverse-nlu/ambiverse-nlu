

/* First created by JCasGen Thu Nov 09 10:43:00 CET 2017 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


/** A document annotation that describes the metadata of a
                newspaper article.
 * Updated by JCasGen Thu Nov 09 10:45:36 CET 2017
 * XML source: /Users/dmilchev/eclipse-workspace/NYTArticleMetaDataAnnotator/desc/NYTArticleMetaData.xml
 *  */
public class NYTArticleMetaData extends Annotation {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(NYTArticleMetaData.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected NYTArticleMetaData() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public NYTArticleMetaData(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public NYTArticleMetaData(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public NYTArticleMetaData(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   *  modifiable
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: Guid

  /** getter for Guid - gets The GUID field specifies a (4-byte) integer that is
                        guaranteed
                        to be unique for every document
                        in the corpus.
   *
   * @return value of the feature 
   */
  public int getGuid() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_Guid == null)
      jcasType.jcas.throwFeatMissing("Guid", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return jcasType.ll_cas.ll_getIntValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_Guid);}
    
  /** setter for Guid - sets The GUID field specifies a (4-byte) integer that is
                        guaranteed
                        to be unique for every document
                        in the corpus. 
   *
   * @param v value to set into the feature 
   */
  public void setGuid(int v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_Guid == null)
      jcasType.jcas.throwFeatMissing("Guid", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setIntValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_Guid, v);}


  //*--------------*
  //* Feature: Abstract

  public String getArticleAbstract() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_alternateUrl == null)
      jcasType.jcas.throwFeatMissing("articleAbstract", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_alternateUrl);}

  /** setter for alternateUrl - sets This field specifies the location on nytimes.com of
   the article. When present, this URL is preferred to the URL field
   on articles published on or after April 02,
   2006, as the linked
   page will have richer content.
   *
   * @param v value to set into the feature
   */
  public void setArticleAbstract(String v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_alternateUrl == null)
      jcasType.jcas.throwFeatMissing("articleAbstract", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_alternateUrl, v);}





  //*--------------*
  //* Feature: alternateUrl

  /** getter for alternateUrl - gets This field specifies the location on nytimes.com of
                        the article. When present, this URL is preferred to the URL field
                        on articles published on or after April 02,
                        2006, as the linked
                        page will have richer content.
   *
   * @return value of the feature 
   */
  public String getAlternateUrl() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_alternateUrl == null)
      jcasType.jcas.throwFeatMissing("alternateUrl", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_alternateUrl);}
    
  /** setter for alternateUrl - sets This field specifies the location on nytimes.com of
                        the article. When present, this URL is preferred to the URL field
                        on articles published on or after April 02,
                        2006, as the linked
                        page will have richer content. 
   *
   * @param v value to set into the feature 
   */
  public void setAlternateUrl(String v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_alternateUrl == null)
      jcasType.jcas.throwFeatMissing("alternateUrl", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_alternateUrl, v);}    
   
    
  //*--------------*
  //* Feature: url

  /** getter for url - gets This field specifies the location on nytimes.com of
                        the article. The ‘Alternative Url’
                        field is preferred to this field
                        on articles published on or after
                        April 02, 2006, as the
                        linked page
                        will have richer content.
   *
   * @return value of the feature 
   */
  public String getUrl() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_url == null)
      jcasType.jcas.throwFeatMissing("url", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_url);}
    
  /** setter for url - sets This field specifies the location on nytimes.com of
                        the article. The ‘Alternative Url’
                        field is preferred to this field
                        on articles published on or after
                        April 02, 2006, as the
                        linked page
                        will have richer content. 
   *
   * @param v value to set into the feature 
   */
  public void setUrl(String v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_url == null)
      jcasType.jcas.throwFeatMissing("url", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_url, v);}    
   
    
  //*--------------*
  //* Feature: publicationDate

  /** getter for publicationDate - gets This field specifies the date of the article’s
                        publication. This field is specified in the
                        format
                        YYYYMMDD’T’HHMMSS where:
                        1. YYYY is the four-digit year.
                        2. MM is
                        the two-digit month [01-12].
                        3. DD is the two-digit day [01-31].
                        4.
                        T is a constant value.
                        5. HH is the two-digit hour [00-23].
                        6. MM is
                        the two-digit minute-past-the hour [00-59]
                        7. SS is the two-digit
                        seconds-past-the-minute [00-59].
                        Please note that values for HH,MM,
                        and SS are not defined for this
                        corpus, that is to day
                        HH,MM, and SS
                        are always defined to be ‘00’.
   *
   * @return value of the feature 
   */
  public String getPublicationDate() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_publicationDate == null)
      jcasType.jcas.throwFeatMissing("publicationDate", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_publicationDate);}
    
  /** setter for publicationDate - sets This field specifies the date of the article’s
                        publication. This field is specified in the
                        format
                        YYYYMMDD’T’HHMMSS where:
                        1. YYYY is the four-digit year.
                        2. MM is
                        the two-digit month [01-12].
                        3. DD is the two-digit day [01-31].
                        4.
                        T is a constant value.
                        5. HH is the two-digit hour [00-23].
                        6. MM is
                        the two-digit minute-past-the hour [00-59]
                        7. SS is the two-digit
                        seconds-past-the-minute [00-59].
                        Please note that values for HH,MM,
                        and SS are not defined for this
                        corpus, that is to day
                        HH,MM, and SS
                        are always defined to be ‘00’. 
   *
   * @param v value to set into the feature 
   */
  public void setPublicationDate(String v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_publicationDate == null)
      jcasType.jcas.throwFeatMissing("publicationDate", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_publicationDate, v);}    
   
    
  //*--------------*
  //* Feature: typesOfMaterial

  /** getter for typesOfMaterial - gets This field specifies a normalized list of terms
                        describing the general editorial category of the article.
                        These
                        tags are algorithmically assigned and
                        manually verified by
                        nytimes.com production staff.
                        Examples Include:
                        • REVIEW
                        • OBITUARY
                        • ANALYSIS
   *
   * @return value of the feature 
   */
  public StringArray getTypesOfMaterial() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_typesOfMaterial == null)
      jcasType.jcas.throwFeatMissing("typesOfMaterial", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_typesOfMaterial)));}
    
  /** setter for typesOfMaterial - sets This field specifies a normalized list of terms
                        describing the general editorial category of the article.
                        These
                        tags are algorithmically assigned and
                        manually verified by
                        nytimes.com production staff.
                        Examples Include:
                        • REVIEW
                        • OBITUARY
                        • ANALYSIS 
   *
   * @param v value to set into the feature 
   */
  public void setTypesOfMaterial(StringArray v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_typesOfMaterial == null)
      jcasType.jcas.throwFeatMissing("typesOfMaterial", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_typesOfMaterial, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for typesOfMaterial - gets an indexed value - This field specifies a normalized list of terms
                        describing the general editorial category of the article.
                        These
                        tags are algorithmically assigned and
                        manually verified by
                        nytimes.com production staff.
                        Examples Include:
                        • REVIEW
                        • OBITUARY
                        • ANALYSIS
   *
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getTypesOfMaterial(int i) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_typesOfMaterial == null)
      jcasType.jcas.throwFeatMissing("typesOfMaterial", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_typesOfMaterial), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_typesOfMaterial), i);}

  /** indexed setter for typesOfMaterial - sets an indexed value - This field specifies a normalized list of terms
                        describing the general editorial category of the article.
                        These
                        tags are algorithmically assigned and
                        manually verified by
                        nytimes.com production staff.
                        Examples Include:
                        • REVIEW
                        • OBITUARY
                        • ANALYSIS
   *
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setTypesOfMaterial(int i, String v) { 
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_typesOfMaterial == null)
      jcasType.jcas.throwFeatMissing("typesOfMaterial", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_typesOfMaterial), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_typesOfMaterial), i, v);}
   
    
  //*--------------*
  //* Feature: headline

  /** getter for headline - gets This field specifies the headline of the article as it
                        appeared in the
                        print edition of the New York
                        Times.
   *
   * @return value of the feature 
   */
  public String getHeadline() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_headline == null)
      jcasType.jcas.throwFeatMissing("headline", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_headline);}
    
  /** setter for headline - sets This field specifies the headline of the article as it
                        appeared in the
                        print edition of the New York
                        Times. 
   *
   * @param v value to set into the feature 
   */
  public void setHeadline(String v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_headline == null)
      jcasType.jcas.throwFeatMissing("headline", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_headline, v);}    
   
    
  //*--------------*
  //* Feature: onlineHeadline

  /** getter for onlineHeadline - gets This field specifies the headline displayed with the
                        article on
                        nytimes.com. Often
                        this differs from the headline used in
                        print.
   *
   * @return value of the feature 
   */
  public String getOnlineHeadline() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_onlineHeadline == null)
      jcasType.jcas.throwFeatMissing("onlineHeadline", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_onlineHeadline);}
    
  /** setter for onlineHeadline - sets This field specifies the headline displayed with the
                        article on
                        nytimes.com. Often
                        this differs from the headline used in
                        print. 
   *
   * @param v value to set into the feature 
   */
  public void setOnlineHeadline(String v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_onlineHeadline == null)
      jcasType.jcas.throwFeatMissing("onlineHeadline", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_onlineHeadline, v);}    
   
    
  //*--------------*
  //* Feature: columnName

  /** getter for columnName - gets If the article is part of a regular column, this field
                        specifies the
                        name of that column.
                        Sample Column Names:
                        1. World News
                        Briefs
                        2. WEDDINGS
                        3. The Accessories Channel
   *
   * @return value of the feature 
   */
  public String getColumnName() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_columnName == null)
      jcasType.jcas.throwFeatMissing("columnName", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_columnName);}
    
  /** setter for columnName - sets If the article is part of a regular column, this field
                        specifies the
                        name of that column.
                        Sample Column Names:
                        1. World News
                        Briefs
                        2. WEDDINGS
                        3. The Accessories Channel 
   *
   * @param v value to set into the feature 
   */
  public void setColumnName(String v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_columnName == null)
      jcasType.jcas.throwFeatMissing("columnName", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_columnName, v);}    
   
    
  //*--------------*
  //* Feature: author

  /** getter for author - gets This field is based on the normalized byline in the
                        original corpus data: "The Normalized Byline field is the byline
                        normalized to the form (last name, first
                        name)".
   *
   * @return value of the feature 
   */
  public String getAuthor() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_author == null)
      jcasType.jcas.throwFeatMissing("author", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_author);}
    
  /** setter for author - sets This field is based on the normalized byline in the
                        original corpus data: "The Normalized Byline field is the byline
                        normalized to the form (last name, first
                        name)". 
   *
   * @param v value to set into the feature 
   */
  public void setAuthor(String v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_author == null)
      jcasType.jcas.throwFeatMissing("author", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_author, v);}    
   
    
  //*--------------*
  //* Feature: descriptors

  /** getter for descriptors - gets The ‘descriptors’ field specifies a list of
                        descriptive terms drawn
                        from a normalized controlled
                        vocabulary
                        corresponding to subjects mentioned in the article. These tags
                        are
                        hand-assigned by
                        a team of library scientists working in the New
                        York Times Indexing
                        service.
                        Examples Include:
                        • ECONOMIC CONDITIONS
                        AND TRENDS
                        • AIRPLANES
                        • VIOLINS
   *
   * @return value of the feature 
   */
  public StringArray getDescriptors() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_descriptors == null)
      jcasType.jcas.throwFeatMissing("descriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_descriptors)));}
    
  /** setter for descriptors - sets The ‘descriptors’ field specifies a list of
                        descriptive terms drawn
                        from a normalized controlled
                        vocabulary
                        corresponding to subjects mentioned in the article. These tags
                        are
                        hand-assigned by
                        a team of library scientists working in the New
                        York Times Indexing
                        service.
                        Examples Include:
                        • ECONOMIC CONDITIONS
                        AND TRENDS
                        • AIRPLANES
                        • VIOLINS 
   *
   * @param v value to set into the feature 
   */
  public void setDescriptors(StringArray v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_descriptors == null)
      jcasType.jcas.throwFeatMissing("descriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_descriptors, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for descriptors - gets an indexed value - The ‘descriptors’ field specifies a list of
                        descriptive terms drawn
                        from a normalized controlled
                        vocabulary
                        corresponding to subjects mentioned in the article. These tags
                        are
                        hand-assigned by
                        a team of library scientists working in the New
                        York Times Indexing
                        service.
                        Examples Include:
                        • ECONOMIC CONDITIONS
                        AND TRENDS
                        • AIRPLANES
                        • VIOLINS
   *
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getDescriptors(int i) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_descriptors == null)
      jcasType.jcas.throwFeatMissing("descriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_descriptors), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_descriptors), i);}

  /** indexed setter for descriptors - sets an indexed value - The ‘descriptors’ field specifies a list of
                        descriptive terms drawn
                        from a normalized controlled
                        vocabulary
                        corresponding to subjects mentioned in the article. These tags
                        are
                        hand-assigned by
                        a team of library scientists working in the New
                        York Times Indexing
                        service.
                        Examples Include:
                        • ECONOMIC CONDITIONS
                        AND TRENDS
                        • AIRPLANES
                        • VIOLINS
   *
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setDescriptors(int i, String v) { 
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_descriptors == null)
      jcasType.jcas.throwFeatMissing("descriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_descriptors), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_descriptors), i, v);}
   
    
  //*--------------*
  //* Feature: onlineDescriptors

  /** getter for onlineDescriptors - gets This field specifies a list of descriptors from a
                        normalized
                        controlled
                        vocabulary that
                        correspond to topics mentioned
                        in the article. These
                        tags are
                        algorithmically
                        assigned and manually
                        verified by
                        nytimes.com production staff.
                        Examples Include:
                        • Marriages
                        • Parks and Other Recreation Areas
                        • Cooking and Cookbooks
   *
   * @return value of the feature 
   */
  public StringArray getOnlineDescriptors() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_onlineDescriptors == null)
      jcasType.jcas.throwFeatMissing("onlineDescriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_onlineDescriptors)));}
    
  /** setter for onlineDescriptors - sets This field specifies a list of descriptors from a
                        normalized
                        controlled
                        vocabulary that
                        correspond to topics mentioned
                        in the article. These
                        tags are
                        algorithmically
                        assigned and manually
                        verified by
                        nytimes.com production staff.
                        Examples Include:
                        • Marriages
                        • Parks and Other Recreation Areas
                        • Cooking and Cookbooks 
   *
   * @param v value to set into the feature 
   */
  public void setOnlineDescriptors(StringArray v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_onlineDescriptors == null)
      jcasType.jcas.throwFeatMissing("onlineDescriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_onlineDescriptors, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for onlineDescriptors - gets an indexed value - This field specifies a list of descriptors from a
                        normalized
                        controlled
                        vocabulary that
                        correspond to topics mentioned
                        in the article. These
                        tags are
                        algorithmically
                        assigned and manually
                        verified by
                        nytimes.com production staff.
                        Examples Include:
                        • Marriages
                        • Parks and Other Recreation Areas
                        • Cooking and Cookbooks
   *
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getOnlineDescriptors(int i) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_onlineDescriptors == null)
      jcasType.jcas.throwFeatMissing("onlineDescriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_onlineDescriptors), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_onlineDescriptors), i);}

  /** indexed setter for onlineDescriptors - sets an indexed value - This field specifies a list of descriptors from a
                        normalized
                        controlled
                        vocabulary that
                        correspond to topics mentioned
                        in the article. These
                        tags are
                        algorithmically
                        assigned and manually
                        verified by
                        nytimes.com production staff.
                        Examples Include:
                        • Marriages
                        • Parks and Other Recreation Areas
                        • Cooking and Cookbooks
   *
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setOnlineDescriptors(int i, String v) { 
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_onlineDescriptors == null)
      jcasType.jcas.throwFeatMissing("onlineDescriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_onlineDescriptors), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_onlineDescriptors), i, v);}
   
    
  //*--------------*
  //* Feature: generalOnlineDescriptors

  /** getter for generalOnlineDescriptors - gets The ‘general online descriptors’ field specifies a
                        list of descriptors that are at a higher level of
                        generality than
                        the other tags associated with the article. These tags are
                        algorithmically
                        assigned and manually verified by nytimes.com
                        production staff.
                        Examples Include:
                        • Surfing
                        • Venice Biennale
                        • Ranches
   *
   * @return value of the feature 
   */
  public String getGeneralOnlineDescriptors() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_generalOnlineDescriptors == null)
      jcasType.jcas.throwFeatMissing("generalOnlineDescriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_generalOnlineDescriptors);}
    
  /** setter for generalOnlineDescriptors - sets The ‘general online descriptors’ field specifies a
                        list of descriptors that are at a higher level of
                        generality than
                        the other tags associated with the article. These tags are
                        algorithmically
                        assigned and manually verified by nytimes.com
                        production staff.
                        Examples Include:
                        • Surfing
                        • Venice Biennale
                        • Ranches 
   *
   * @param v value to set into the feature 
   */
  public void setGeneralOnlineDescriptors(String v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_generalOnlineDescriptors == null)
      jcasType.jcas.throwFeatMissing("generalOnlineDescriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_generalOnlineDescriptors, v);}    
   
    
  //*--------------*
  //* Feature: onlineSection

  /** getter for onlineSection - gets This field specifies the section(s) on nytimes.com in
                        which the
                        article is placed. If
                        the article is placed in multiple
                        sections, this field will be
                        specified as a ‘;’ delineated
                        list.
   *
   * @return value of the feature 
   */
  public String getOnlineSection() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_onlineSection == null)
      jcasType.jcas.throwFeatMissing("onlineSection", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_onlineSection);}
    
  /** setter for onlineSection - sets This field specifies the section(s) on nytimes.com in
                        which the
                        article is placed. If
                        the article is placed in multiple
                        sections, this field will be
                        specified as a ‘;’ delineated
                        list. 
   *
   * @param v value to set into the feature 
   */
  public void setOnlineSection(String v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_onlineSection == null)
      jcasType.jcas.throwFeatMissing("onlineSection", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_onlineSection, v);}    
   
    
  //*--------------*
  //* Feature: section

  /** getter for section - gets This field specifies the section of the paper in which
                        the article
                        appears. This is not
                        the name of the section, but rather
                        a letter or number that indicates
                        the section.
   *
   * @return value of the feature 
   */
  public String getSection() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_section == null)
      jcasType.jcas.throwFeatMissing("section", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_section);}
    
  /** setter for section - sets This field specifies the section of the paper in which
                        the article
                        appears. This is not
                        the name of the section, but rather
                        a letter or number that indicates
                        the section. 
   *
   * @param v value to set into the feature 
   */
  public void setSection(String v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_section == null)
      jcasType.jcas.throwFeatMissing("section", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setStringValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_section, v);}    
   
    
  //*--------------*
  //* Feature: taxonomicClassifiers

  /** getter for taxonomicClassifiers - gets This field specifies a list of taxonomic classifiers
                        that place this
                        article into a
                        hierarchy of articles. The individual
                        terms of each taxonomic classifier
                        are separated with the '/' character.
                        These tags are algorithmically assigned and manually
                        verified
                        by nytimes.com production staff.
                        Examples Include:
                        • Top/Features/Travel/Guides/Destinations/North America/United States/Arizona
                        • Top/News/U.S./Rockies
                        • Top/Opinion
   *
   * @return value of the feature 
   */
  public StringArray getTaxonomicClassifiers() {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_taxonomicClassifiers == null)
      jcasType.jcas.throwFeatMissing("taxonomicClassifiers", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_taxonomicClassifiers)));}
    
  /** setter for taxonomicClassifiers - sets This field specifies a list of taxonomic classifiers
                        that place this
                        article into a
                        hierarchy of articles. The individual
                        terms of each taxonomic classifier
                        are separated with the '/' character.
                        These tags are algorithmically assigned and manually
                        verified
                        by nytimes.com production staff.
                        Examples Include:
                        • Top/Features/Travel/Guides/Destinations/North America/United States/Arizona
                        • Top/News/U.S./Rockies
                        • Top/Opinion 
   *
   * @param v value to set into the feature 
   */
  public void setTaxonomicClassifiers(StringArray v) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_taxonomicClassifiers == null)
      jcasType.jcas.throwFeatMissing("taxonomicClassifiers", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.ll_cas.ll_setRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_taxonomicClassifiers, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for taxonomicClassifiers - gets an indexed value - This field specifies a list of taxonomic classifiers
                        that place this
                        article into a
                        hierarchy of articles. The individual
                        terms of each taxonomic classifier
                        are separated with the '/' character.
                        These tags are algorithmically assigned and manually
                        verified
                        by nytimes.com production staff.
                        Examples Include:
                        • Top/Features/Travel/Guides/Destinations/North America/United States/Arizona
                        • Top/News/U.S./Rockies
                        • Top/Opinion
   *
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getTaxonomicClassifiers(int i) {
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_taxonomicClassifiers == null)
      jcasType.jcas.throwFeatMissing("taxonomicClassifiers", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_taxonomicClassifiers), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_taxonomicClassifiers), i);}

  /** indexed setter for taxonomicClassifiers - sets an indexed value - This field specifies a list of taxonomic classifiers
                        that place this
                        article into a
                        hierarchy of articles. The individual
                        terms of each taxonomic classifier
                        are separated with the '/' character.
                        These tags are algorithmically assigned and manually
                        verified
                        by nytimes.com production staff.
                        Examples Include:
                        • Top/Features/Travel/Guides/Destinations/North America/United States/Arizona
                        • Top/News/U.S./Rockies
                        • Top/Opinion
   *
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setTaxonomicClassifiers(int i, String v) { 
    if (NYTArticleMetaData_Type.featOkTst && ((NYTArticleMetaData_Type)jcasType).casFeat_taxonomicClassifiers == null)
      jcasType.jcas.throwFeatMissing("taxonomicClassifiers", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_taxonomicClassifiers), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((NYTArticleMetaData_Type)jcasType).casFeatCode_taxonomicClassifiers), i, v);}
  }

    