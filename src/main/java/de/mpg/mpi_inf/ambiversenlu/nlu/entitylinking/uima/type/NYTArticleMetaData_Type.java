
/* First created by JCasGen Thu Nov 09 10:43:00 CET 2017 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** A document annotation that describes the metadata of a
                newspaper article.
 * Updated by JCasGen Thu Nov 09 10:45:36 CET 2017
 *  */
public class NYTArticleMetaData_Type extends Annotation_Type {

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = NYTArticleMetaData.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
 

  final Feature casFeat_Guid;

  final int     casFeatCode_Guid;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getGuid(int addr) {
        if (featOkTst && casFeat_Guid == null)
      jcas.throwFeatMissing("Guid", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getIntValue(addr, casFeatCode_Guid);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setGuid(int addr, int v) {
        if (featOkTst && casFeat_Guid == null)
      jcas.throwFeatMissing("Guid", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setIntValue(addr, casFeatCode_Guid, v);}
    
  
 

  final Feature casFeat_alternateUrl;

  final int     casFeatCode_alternateUrl;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getAlternateUrl(int addr) {
        if (featOkTst && casFeat_alternateUrl == null)
      jcas.throwFeatMissing("alternateUrl", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_alternateUrl);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setAlternateUrl(int addr, String v) {
        if (featOkTst && casFeat_alternateUrl == null)
      jcas.throwFeatMissing("alternateUrl", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_alternateUrl, v);}
    
  
 

  final Feature casFeat_url;

  final int     casFeatCode_url;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getUrl(int addr) {
        if (featOkTst && casFeat_url == null)
      jcas.throwFeatMissing("url", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_url);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setUrl(int addr, String v) {
        if (featOkTst && casFeat_url == null)
      jcas.throwFeatMissing("url", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_url, v);}



  final Feature casFeat_articleAbstract;

  final int     casFeatCode_articleAbstract;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value
   */
  public String getArticleAbstract(int addr) {
    if (featOkTst && casFeat_articleAbstract == null)
      jcas.throwFeatMissing("articleAbstract", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_articleAbstract);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set
   */
  public void setArticleAbstract(int addr, String v) {
    if (featOkTst && casFeat_articleAbstract == null)
      jcas.throwFeatMissing("articleAbstract", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_articleAbstract, v);}
    
  
 

  final Feature casFeat_publicationDate;

  final int     casFeatCode_publicationDate;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getPublicationDate(int addr) {
        if (featOkTst && casFeat_publicationDate == null)
      jcas.throwFeatMissing("publicationDate", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_publicationDate);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPublicationDate(int addr, String v) {
        if (featOkTst && casFeat_publicationDate == null)
      jcas.throwFeatMissing("publicationDate", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_publicationDate, v);}
    
  
 

  final Feature casFeat_typesOfMaterial;

  final int     casFeatCode_typesOfMaterial;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getTypesOfMaterial(int addr) {
        if (featOkTst && casFeat_typesOfMaterial == null)
      jcas.throwFeatMissing("typesOfMaterial", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getRefValue(addr, casFeatCode_typesOfMaterial);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTypesOfMaterial(int addr, int v) {
        if (featOkTst && casFeat_typesOfMaterial == null)
      jcas.throwFeatMissing("typesOfMaterial", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setRefValue(addr, casFeatCode_typesOfMaterial, v);}
    
   /**
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getTypesOfMaterial(int addr, int i) {
        if (featOkTst && casFeat_typesOfMaterial == null)
      jcas.throwFeatMissing("typesOfMaterial", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_typesOfMaterial), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_typesOfMaterial), i);
  return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_typesOfMaterial), i);
  }
   
  /**
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setTypesOfMaterial(int addr, int i, String v) {
        if (featOkTst && casFeat_typesOfMaterial == null)
      jcas.throwFeatMissing("typesOfMaterial", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_typesOfMaterial), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_typesOfMaterial), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_typesOfMaterial), i, v);
  }
 
 

  final Feature casFeat_headline;

  final int     casFeatCode_headline;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getHeadline(int addr) {
        if (featOkTst && casFeat_headline == null)
      jcas.throwFeatMissing("headline", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_headline);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setHeadline(int addr, String v) {
        if (featOkTst && casFeat_headline == null)
      jcas.throwFeatMissing("headline", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_headline, v);}
    
  
 

  final Feature casFeat_onlineHeadline;

  final int     casFeatCode_onlineHeadline;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getOnlineHeadline(int addr) {
        if (featOkTst && casFeat_onlineHeadline == null)
      jcas.throwFeatMissing("onlineHeadline", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_onlineHeadline);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setOnlineHeadline(int addr, String v) {
        if (featOkTst && casFeat_onlineHeadline == null)
      jcas.throwFeatMissing("onlineHeadline", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_onlineHeadline, v);}
    
  
 

  final Feature casFeat_columnName;

  final int     casFeatCode_columnName;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getColumnName(int addr) {
        if (featOkTst && casFeat_columnName == null)
      jcas.throwFeatMissing("columnName", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_columnName);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setColumnName(int addr, String v) {
        if (featOkTst && casFeat_columnName == null)
      jcas.throwFeatMissing("columnName", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_columnName, v);}
    
  
 

  final Feature casFeat_author;

  final int     casFeatCode_author;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getAuthor(int addr) {
        if (featOkTst && casFeat_author == null)
      jcas.throwFeatMissing("author", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_author);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setAuthor(int addr, String v) {
        if (featOkTst && casFeat_author == null)
      jcas.throwFeatMissing("author", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_author, v);}
    
  
 

  final Feature casFeat_descriptors;

  final int     casFeatCode_descriptors;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getDescriptors(int addr) {
        if (featOkTst && casFeat_descriptors == null)
      jcas.throwFeatMissing("descriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getRefValue(addr, casFeatCode_descriptors);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDescriptors(int addr, int v) {
        if (featOkTst && casFeat_descriptors == null)
      jcas.throwFeatMissing("descriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setRefValue(addr, casFeatCode_descriptors, v);}
    
   /**
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getDescriptors(int addr, int i) {
        if (featOkTst && casFeat_descriptors == null)
      jcas.throwFeatMissing("descriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_descriptors), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_descriptors), i);
  return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_descriptors), i);
  }
   
  /**
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setDescriptors(int addr, int i, String v) {
        if (featOkTst && casFeat_descriptors == null)
      jcas.throwFeatMissing("descriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_descriptors), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_descriptors), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_descriptors), i, v);
  }
 
 

  final Feature casFeat_onlineDescriptors;

  final int     casFeatCode_onlineDescriptors;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getOnlineDescriptors(int addr) {
        if (featOkTst && casFeat_onlineDescriptors == null)
      jcas.throwFeatMissing("onlineDescriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getRefValue(addr, casFeatCode_onlineDescriptors);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setOnlineDescriptors(int addr, int v) {
        if (featOkTst && casFeat_onlineDescriptors == null)
      jcas.throwFeatMissing("onlineDescriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setRefValue(addr, casFeatCode_onlineDescriptors, v);}
    
   /**
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getOnlineDescriptors(int addr, int i) {
        if (featOkTst && casFeat_onlineDescriptors == null)
      jcas.throwFeatMissing("onlineDescriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_onlineDescriptors), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_onlineDescriptors), i);
  return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_onlineDescriptors), i);
  }
   
  /**
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setOnlineDescriptors(int addr, int i, String v) {
        if (featOkTst && casFeat_onlineDescriptors == null)
      jcas.throwFeatMissing("onlineDescriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_onlineDescriptors), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_onlineDescriptors), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_onlineDescriptors), i, v);
  }
 
 

  final Feature casFeat_generalOnlineDescriptors;

  final int     casFeatCode_generalOnlineDescriptors;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getGeneralOnlineDescriptors(int addr) {
        if (featOkTst && casFeat_generalOnlineDescriptors == null)
      jcas.throwFeatMissing("generalOnlineDescriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_generalOnlineDescriptors);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setGeneralOnlineDescriptors(int addr, String v) {
        if (featOkTst && casFeat_generalOnlineDescriptors == null)
      jcas.throwFeatMissing("generalOnlineDescriptors", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_generalOnlineDescriptors, v);}
    
  
 

  final Feature casFeat_onlineSection;

  final int     casFeatCode_onlineSection;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getOnlineSection(int addr) {
        if (featOkTst && casFeat_onlineSection == null)
      jcas.throwFeatMissing("onlineSection", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_onlineSection);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setOnlineSection(int addr, String v) {
        if (featOkTst && casFeat_onlineSection == null)
      jcas.throwFeatMissing("onlineSection", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_onlineSection, v);}
    
  
 

  final Feature casFeat_section;

  final int     casFeatCode_section;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSection(int addr) {
        if (featOkTst && casFeat_section == null)
      jcas.throwFeatMissing("section", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getStringValue(addr, casFeatCode_section);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSection(int addr, String v) {
        if (featOkTst && casFeat_section == null)
      jcas.throwFeatMissing("section", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setStringValue(addr, casFeatCode_section, v);}
    
  
 

  final Feature casFeat_taxonomicClassifiers;

  final int     casFeatCode_taxonomicClassifiers;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getTaxonomicClassifiers(int addr) {
        if (featOkTst && casFeat_taxonomicClassifiers == null)
      jcas.throwFeatMissing("taxonomicClassifiers", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    return ll_cas.ll_getRefValue(addr, casFeatCode_taxonomicClassifiers);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTaxonomicClassifiers(int addr, int v) {
        if (featOkTst && casFeat_taxonomicClassifiers == null)
      jcas.throwFeatMissing("taxonomicClassifiers", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    ll_cas.ll_setRefValue(addr, casFeatCode_taxonomicClassifiers, v);}
    
   /**
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getTaxonomicClassifiers(int addr, int i) {
        if (featOkTst && casFeat_taxonomicClassifiers == null)
      jcas.throwFeatMissing("taxonomicClassifiers", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_taxonomicClassifiers), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_taxonomicClassifiers), i);
  return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_taxonomicClassifiers), i);
  }
   
  /**
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setTaxonomicClassifiers(int addr, int i, String v) {
        if (featOkTst && casFeat_taxonomicClassifiers == null)
      jcas.throwFeatMissing("taxonomicClassifiers", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_taxonomicClassifiers), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_taxonomicClassifiers), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_taxonomicClassifiers), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public NYTArticleMetaData_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_Guid = jcas.getRequiredFeatureDE(casType, "Guid", "uima.cas.Integer", featOkTst);
    casFeatCode_Guid  = (null == casFeat_Guid) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_Guid).getCode();

 
    casFeat_alternateUrl = jcas.getRequiredFeatureDE(casType, "alternateUrl", "uima.cas.String", featOkTst);
    casFeatCode_alternateUrl  = (null == casFeat_alternateUrl) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_alternateUrl).getCode();

 
    casFeat_url = jcas.getRequiredFeatureDE(casType, "url", "uima.cas.String", featOkTst);
    casFeatCode_url  = (null == casFeat_url) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_url).getCode();


    casFeat_articleAbstract = jcas.getRequiredFeatureDE(casType, "articleAbstract", "uima.cas.String", featOkTst);
    casFeatCode_articleAbstract  = (null == casFeat_articleAbstract) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_articleAbstract).getCode();


    casFeat_publicationDate = jcas.getRequiredFeatureDE(casType, "publicationDate", "uima.cas.String", featOkTst);
    casFeatCode_publicationDate  = (null == casFeat_publicationDate) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_publicationDate).getCode();

 
    casFeat_typesOfMaterial = jcas.getRequiredFeatureDE(casType, "typesOfMaterial", "uima.cas.StringArray", featOkTst);
    casFeatCode_typesOfMaterial  = (null == casFeat_typesOfMaterial) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_typesOfMaterial).getCode();

 
    casFeat_headline = jcas.getRequiredFeatureDE(casType, "headline", "uima.cas.String", featOkTst);
    casFeatCode_headline  = (null == casFeat_headline) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_headline).getCode();

 
    casFeat_onlineHeadline = jcas.getRequiredFeatureDE(casType, "onlineHeadline", "uima.cas.String", featOkTst);
    casFeatCode_onlineHeadline  = (null == casFeat_onlineHeadline) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_onlineHeadline).getCode();

 
    casFeat_columnName = jcas.getRequiredFeatureDE(casType, "columnName", "uima.cas.String", featOkTst);
    casFeatCode_columnName  = (null == casFeat_columnName) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_columnName).getCode();

 
    casFeat_author = jcas.getRequiredFeatureDE(casType, "author", "uima.cas.String", featOkTst);
    casFeatCode_author  = (null == casFeat_author) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_author).getCode();

 
    casFeat_descriptors = jcas.getRequiredFeatureDE(casType, "descriptors", "uima.cas.StringArray", featOkTst);
    casFeatCode_descriptors  = (null == casFeat_descriptors) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_descriptors).getCode();

 
    casFeat_onlineDescriptors = jcas.getRequiredFeatureDE(casType, "onlineDescriptors", "uima.cas.StringArray", featOkTst);
    casFeatCode_onlineDescriptors  = (null == casFeat_onlineDescriptors) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_onlineDescriptors).getCode();

 
    casFeat_generalOnlineDescriptors = jcas.getRequiredFeatureDE(casType, "generalOnlineDescriptors", "uima.cas.String", featOkTst);
    casFeatCode_generalOnlineDescriptors  = (null == casFeat_generalOnlineDescriptors) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_generalOnlineDescriptors).getCode();

 
    casFeat_onlineSection = jcas.getRequiredFeatureDE(casType, "onlineSection", "uima.cas.String", featOkTst);
    casFeatCode_onlineSection  = (null == casFeat_onlineSection) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_onlineSection).getCode();

 
    casFeat_section = jcas.getRequiredFeatureDE(casType, "section", "uima.cas.String", featOkTst);
    casFeatCode_section  = (null == casFeat_section) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_section).getCode();

 
    casFeat_taxonomicClassifiers = jcas.getRequiredFeatureDE(casType, "taxonomicClassifiers", "uima.cas.StringArray", featOkTst);
    casFeatCode_taxonomicClassifiers  = (null == casFeat_taxonomicClassifiers) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_taxonomicClassifiers).getCode();

  }
}



    