/* First created by JCasGen Mon Aug 22 15:48:24 CEST 2016 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.tcas.Annotation_Type;

/**
 * Updated by JCasGen Wed Jan 11 14:25:32 CET 2017
 *  */
public class AidaDocumentSettings_Type extends Annotation_Type {

  /**
   * @return the generator for this type
   */
  @Override protected FSGenerator getFSGenerator() {
    return fsGenerator;
  }


  private final FSGenerator fsGenerator = new FSGenerator() {

    public FeatureStructure createFS(int addr, CASImpl cas) {
      if (AidaDocumentSettings_Type.this.useExistingInstance) {
        // Return eq fs instance if already created
        FeatureStructure fs = AidaDocumentSettings_Type.this.jcas.getJfsFromCaddr(addr);
        if (null == fs) {
          fs = new AidaDocumentSettings(addr, AidaDocumentSettings_Type.this);
          AidaDocumentSettings_Type.this.jcas.putJfsFromCaddr(addr, fs);
          return fs;
        }
        return fs;
      } else return new AidaDocumentSettings(addr, AidaDocumentSettings_Type.this);
    }
  };


  @SuppressWarnings("hiding") public final static int typeIndexID = AidaDocumentSettings.typeIndexID;

    @SuppressWarnings("hiding") public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");


  final Feature casFeat_language;


  final int casFeatCode_language;

  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */
  public String getLanguage(int addr) {
    if (featOkTst && casFeat_language == null) jcas.throwFeatMissing("language", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    return ll_cas.ll_getStringValue(addr, casFeatCode_language);
  }

  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */
  public void setLanguage(int addr, String v) {
    if (featOkTst && casFeat_language == null) jcas.throwFeatMissing("language", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    ll_cas.ll_setStringValue(addr, casFeatCode_language, v);
  }


  final Feature casFeat_docChunkStrategy;


  final int casFeatCode_docChunkStrategy;

  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */
  public String getDocChunkStrategy(int addr) {
    if (featOkTst && casFeat_docChunkStrategy == null) jcas.throwFeatMissing("docChunkStrategy", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    return ll_cas.ll_getStringValue(addr, casFeatCode_docChunkStrategy);
  }

  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */
  public void setDocChunkStrategy(int addr, String v) {
    if (featOkTst && casFeat_docChunkStrategy == null) jcas.throwFeatMissing("docChunkStrategy", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    ll_cas.ll_setStringValue(addr, casFeatCode_docChunkStrategy, v);
  }


  final Feature casFeat_documentInputFormat;


  final int casFeatCode_documentInputFormat;

  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */
  public String getDocumentInputFormat(int addr) {
    if (featOkTst && casFeat_documentInputFormat == null)
      jcas.throwFeatMissing("documentInputFormat", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    return ll_cas.ll_getStringValue(addr, casFeatCode_documentInputFormat);
  }

  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */
  public void setDocumentInputFormat(int addr, String v) {
    if (featOkTst && casFeat_documentInputFormat == null)
      jcas.throwFeatMissing("documentInputFormat", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    ll_cas.ll_setStringValue(addr, casFeatCode_documentInputFormat, v);
  }


  final Feature casFeat_encoding;


  final int casFeatCode_encoding;

  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */
  public String getEncoding(int addr) {
    if (featOkTst && casFeat_encoding == null) jcas.throwFeatMissing("encoding", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    return ll_cas.ll_getStringValue(addr, casFeatCode_encoding);
  }

  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */
  public void setEncoding(int addr, String v) {
    if (featOkTst && casFeat_encoding == null) jcas.throwFeatMissing("encoding", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    ll_cas.ll_setStringValue(addr, casFeatCode_encoding, v);
  }


  final Feature casFeat_documentId;


  final int casFeatCode_documentId;

  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */
  public String getDocumentId(int addr) {
    if (featOkTst && casFeat_documentId == null) jcas.throwFeatMissing("documentId", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    return ll_cas.ll_getStringValue(addr, casFeatCode_documentId);
  }

  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */
  public void setDocumentId(int addr, String v) {
    if (featOkTst && casFeat_documentId == null) jcas.throwFeatMissing("documentId", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    ll_cas.ll_setStringValue(addr, casFeatCode_documentId, v);
  }


  final Feature casFeat_disambiguationSettings;


  final int casFeatCode_disambiguationSettings;

  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */
  public int getDisambiguationSettings(int addr) {
    if (featOkTst && casFeat_disambiguationSettings == null)
      jcas.throwFeatMissing("disambiguationSettings", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    return ll_cas.ll_getRefValue(addr, casFeatCode_disambiguationSettings);
  }

  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */
  public void setDisambiguationSettings(int addr, int v) {
    if (featOkTst && casFeat_disambiguationSettings == null)
      jcas.throwFeatMissing("disambiguationSettings", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDocumentSettings");
    ll_cas.ll_setRefValue(addr, casFeatCode_disambiguationSettings, v);
  }

  /** initialize variables to correspond with Cas Type and Features
   *
   * @param jcas JCas
   * @param casType Type
   */
  public AidaDocumentSettings_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl) this.casType, getFSGenerator());

    casFeat_language = jcas.getRequiredFeatureDE(casType, "language", "uima.cas.String", featOkTst);
    casFeatCode_language = (null == casFeat_language) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl) casFeat_language).getCode();

    casFeat_docChunkStrategy = jcas.getRequiredFeatureDE(casType, "docChunkStrategy", "uima.cas.String", featOkTst);
    casFeatCode_docChunkStrategy = (null == casFeat_docChunkStrategy) ?
        JCas.INVALID_FEATURE_CODE :
        ((FeatureImpl) casFeat_docChunkStrategy).getCode();

    casFeat_documentInputFormat = jcas.getRequiredFeatureDE(casType, "documentInputFormat", "uima.cas.String", featOkTst);
    casFeatCode_documentInputFormat = (null == casFeat_documentInputFormat) ?
        JCas.INVALID_FEATURE_CODE :
        ((FeatureImpl) casFeat_documentInputFormat).getCode();

    casFeat_encoding = jcas.getRequiredFeatureDE(casType, "encoding", "uima.cas.String", featOkTst);
    casFeatCode_encoding = (null == casFeat_encoding) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl) casFeat_encoding).getCode();

    casFeat_documentId = jcas.getRequiredFeatureDE(casType, "documentId", "uima.cas.String", featOkTst);
    casFeatCode_documentId = (null == casFeat_documentId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl) casFeat_documentId).getCode();

    casFeat_disambiguationSettings = jcas
        .getRequiredFeatureDE(casType, "disambiguationSettings", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDisambiguationSettings", featOkTst);
    casFeatCode_disambiguationSettings = (null == casFeat_disambiguationSettings) ?
        JCas.INVALID_FEATURE_CODE :
        ((FeatureImpl) casFeat_disambiguationSettings).getCode();

  }
}



    