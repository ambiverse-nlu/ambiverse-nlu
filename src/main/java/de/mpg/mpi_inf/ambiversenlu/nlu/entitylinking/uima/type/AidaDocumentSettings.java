/* First created by JCasGen Mon Aug 22 15:48:24 CEST 2016 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Updated by JCasGen Wed Jan 11 14:25:32 CET 2017
 * XML source: /Users/corrogg/IdeaProjects/entity-linking/src/main/resources/uima/type/AidaSettingsTypeSystemDescriptor.xml
 *  */
public class AidaDocumentSettings extends Annotation {

    @SuppressWarnings("hiding") public final static int typeIndexID = JCasRegistry.register(AidaDocumentSettings.class);

    @SuppressWarnings("hiding") public final static int type = typeIndexID;

  /**
   * @return index of the type  
   */
  @Override public int getTypeIndexID() {
    return typeIndexID;
  }

  /** Never called.  Disable default constructor
   *  */
  protected AidaDocumentSettings() {/* intentionally empty block */}

  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public AidaDocumentSettings(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }

  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public AidaDocumentSettings(JCas jcas) {
    super(jcas);
    readObject();
  }

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
   */
  public AidaDocumentSettings(JCas jcas, int begin, int end) {
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
  //* Feature: language

  /** getter for language - gets 
   *
   * @return value of the feature 
   */
  public String getLanguage() {
    if (AidaDocumentSettings_Type.featOkTst && ((AidaDocumentSettings_Type) jcasType).casFeat_language == null)
      jcasType.jcas.throwFeatMissing("language", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AidaDocumentSettings_Type) jcasType).casFeatCode_language);
  }

  /** setter for language - sets  
   *
   * @param v value to set into the feature 
   */
  public void setLanguage(String v) {
    if (AidaDocumentSettings_Type.featOkTst && ((AidaDocumentSettings_Type) jcasType).casFeat_language == null)
      jcasType.jcas.throwFeatMissing("language", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    jcasType.ll_cas.ll_setStringValue(addr, ((AidaDocumentSettings_Type) jcasType).casFeatCode_language, v);
  }

  //*--------------*
  //* Feature: docChunkStrategy

  /** getter for docChunkStrategy - gets 
   *
   * @return value of the feature 
   */
  public String getDocChunkStrategy() {
    if (AidaDocumentSettings_Type.featOkTst && ((AidaDocumentSettings_Type) jcasType).casFeat_docChunkStrategy == null)
      jcasType.jcas.throwFeatMissing("docChunkStrategy", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AidaDocumentSettings_Type) jcasType).casFeatCode_docChunkStrategy);
  }

  /** setter for docChunkStrategy - sets  
   *
   * @param v value to set into the feature 
   */
  public void setDocChunkStrategy(String v) {
    if (AidaDocumentSettings_Type.featOkTst && ((AidaDocumentSettings_Type) jcasType).casFeat_docChunkStrategy == null)
      jcasType.jcas.throwFeatMissing("docChunkStrategy", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    jcasType.ll_cas.ll_setStringValue(addr, ((AidaDocumentSettings_Type) jcasType).casFeatCode_docChunkStrategy, v);
  }

  //*--------------*
  //* Feature: documentInputFormat

  /** getter for documentInputFormat - gets 
   *
   * @return value of the feature 
   */
  public String getDocumentInputFormat() {
    if (AidaDocumentSettings_Type.featOkTst && ((AidaDocumentSettings_Type) jcasType).casFeat_documentInputFormat == null)
      jcasType.jcas.throwFeatMissing("documentInputFormat", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AidaDocumentSettings_Type) jcasType).casFeatCode_documentInputFormat);
  }

  /** setter for documentInputFormat - sets  
   *
   * @param v value to set into the feature 
   */
  public void setDocumentInputFormat(String v) {
    if (AidaDocumentSettings_Type.featOkTst && ((AidaDocumentSettings_Type) jcasType).casFeat_documentInputFormat == null)
      jcasType.jcas.throwFeatMissing("documentInputFormat", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    jcasType.ll_cas.ll_setStringValue(addr, ((AidaDocumentSettings_Type) jcasType).casFeatCode_documentInputFormat, v);
  }

  //*--------------*
  //* Feature: encoding

  /** getter for encoding - gets 
   *
   * @return value of the feature 
   */
  public String getEncoding() {
    if (AidaDocumentSettings_Type.featOkTst && ((AidaDocumentSettings_Type) jcasType).casFeat_encoding == null)
      jcasType.jcas.throwFeatMissing("encoding", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AidaDocumentSettings_Type) jcasType).casFeatCode_encoding);
  }

  /** setter for encoding - sets  
   *
   * @param v value to set into the feature 
   */
  public void setEncoding(String v) {
    if (AidaDocumentSettings_Type.featOkTst && ((AidaDocumentSettings_Type) jcasType).casFeat_encoding == null)
      jcasType.jcas.throwFeatMissing("encoding", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    jcasType.ll_cas.ll_setStringValue(addr, ((AidaDocumentSettings_Type) jcasType).casFeatCode_encoding, v);
  }

  //*--------------*
  //* Feature: documentId

  /** getter for documentId - gets 
   *
   * @return value of the feature 
   */
  public String getDocumentId() {
    if (AidaDocumentSettings_Type.featOkTst && ((AidaDocumentSettings_Type) jcasType).casFeat_documentId == null)
      jcasType.jcas.throwFeatMissing("documentId", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AidaDocumentSettings_Type) jcasType).casFeatCode_documentId);
  }

  /** setter for documentId - sets  
   *
   * @param v value to set into the feature 
   */
  public void setDocumentId(String v) {
    if (AidaDocumentSettings_Type.featOkTst && ((AidaDocumentSettings_Type) jcasType).casFeat_documentId == null)
      jcasType.jcas.throwFeatMissing("documentId", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    jcasType.ll_cas.ll_setStringValue(addr, ((AidaDocumentSettings_Type) jcasType).casFeatCode_documentId, v);
  }

  //*--------------*
  //* Feature: disambiguationSettings

  /** getter for disambiguationSettings - gets 
   *
   * @return value of the feature 
   */
  public AidaDisambiguationSettings getDisambiguationSettings() {
    if (AidaDocumentSettings_Type.featOkTst && ((AidaDocumentSettings_Type) jcasType).casFeat_disambiguationSettings == null)
      jcasType.jcas.throwFeatMissing("disambiguationSettings", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    return (AidaDisambiguationSettings) (jcasType.ll_cas
        .ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((AidaDocumentSettings_Type) jcasType).casFeatCode_disambiguationSettings)));
  }

  /** setter for disambiguationSettings - sets  
   *
   * @param v value to set into the feature 
   */
  public void setDisambiguationSettings(AidaDisambiguationSettings v) {
    if (AidaDocumentSettings_Type.featOkTst && ((AidaDocumentSettings_Type) jcasType).casFeat_disambiguationSettings == null)
      jcasType.jcas.throwFeatMissing("disambiguationSettings", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    jcasType.ll_cas.ll_setRefValue(addr, ((AidaDocumentSettings_Type) jcasType).casFeatCode_disambiguationSettings, jcasType.ll_cas.ll_getFSRef(v));
  }
}

    