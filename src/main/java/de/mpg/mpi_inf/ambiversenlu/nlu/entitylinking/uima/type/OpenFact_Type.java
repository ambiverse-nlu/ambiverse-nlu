
/* First created by JCasGen Tue Jan 30 13:02:18 CET 2018 */
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
 * Updated by JCasGen Tue Jan 30 16:46:26 CET 2018
 *  */
public class OpenFact_Type extends Annotation_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (OpenFact_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = OpenFact_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new OpenFact(addr, OpenFact_Type.this);
  			   OpenFact_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new OpenFact(addr, OpenFact_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = OpenFact.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
 

  final Feature casFeat_text;

  final int     casFeatCode_text;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getText(int addr) {
        if (featOkTst && casFeat_text == null)
      jcas.throwFeatMissing("text", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return ll_cas.ll_getStringValue(addr, casFeatCode_text);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setText(int addr, String v) {
        if (featOkTst && casFeat_text == null)
      jcas.throwFeatMissing("text", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    ll_cas.ll_setStringValue(addr, casFeatCode_text, v);}
    
  
 

  final Feature casFeat_confidence;

  final int     casFeatCode_confidence;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public double getConfidence(int addr) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_confidence);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setConfidence(int addr, double v) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_confidence, v);}
    
  
 

  final Feature casFeat_annotator;

  final int     casFeatCode_annotator;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getAnnotator(int addr) {
        if (featOkTst && casFeat_annotator == null)
      jcas.throwFeatMissing("annotator", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return ll_cas.ll_getStringValue(addr, casFeatCode_annotator);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setAnnotator(int addr, String v) {
        if (featOkTst && casFeat_annotator == null)
      jcas.throwFeatMissing("annotator", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    ll_cas.ll_setStringValue(addr, casFeatCode_annotator, v);}
    
  
 

  final Feature casFeat_uri;

  final int     casFeatCode_uri;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getUri(int addr) {
        if (featOkTst && casFeat_uri == null)
      jcas.throwFeatMissing("uri", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return ll_cas.ll_getStringValue(addr, casFeatCode_uri);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setUri(int addr, String v) {
        if (featOkTst && casFeat_uri == null)
      jcas.throwFeatMissing("uri", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    ll_cas.ll_setStringValue(addr, casFeatCode_uri, v);}
    
  
 

  final Feature casFeat_subject;

  final int     casFeatCode_subject;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getSubject(int addr) {
        if (featOkTst && casFeat_subject == null)
      jcas.throwFeatMissing("subject", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return ll_cas.ll_getRefValue(addr, casFeatCode_subject);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSubject(int addr, int v) {
        if (featOkTst && casFeat_subject == null)
      jcas.throwFeatMissing("subject", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    ll_cas.ll_setRefValue(addr, casFeatCode_subject, v);}
    
  
 

  final Feature casFeat_object;

  final int     casFeatCode_object;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getObject(int addr) {
        if (featOkTst && casFeat_object == null)
      jcas.throwFeatMissing("object", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return ll_cas.ll_getRefValue(addr, casFeatCode_object);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setObject(int addr, int v) {
        if (featOkTst && casFeat_object == null)
      jcas.throwFeatMissing("object", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    ll_cas.ll_setRefValue(addr, casFeatCode_object, v);}
    
  
 

  final Feature casFeat_relation;

  final int     casFeatCode_relation;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getRelation(int addr) {
        if (featOkTst && casFeat_relation == null)
      jcas.throwFeatMissing("relation", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return ll_cas.ll_getRefValue(addr, casFeatCode_relation);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRelation(int addr, int v) {
        if (featOkTst && casFeat_relation == null)
      jcas.throwFeatMissing("relation", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    ll_cas.ll_setRefValue(addr, casFeatCode_relation, v);}
    
  
 

  final Feature casFeat_value;

  final int     casFeatCode_value;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getValue(int addr) {
        if (featOkTst && casFeat_value == null)
      jcas.throwFeatMissing("value", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return ll_cas.ll_getStringValue(addr, casFeatCode_value);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setValue(int addr, String v) {
        if (featOkTst && casFeat_value == null)
      jcas.throwFeatMissing("value", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    ll_cas.ll_setStringValue(addr, casFeatCode_value, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public OpenFact_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_text = jcas.getRequiredFeatureDE(casType, "text", "uima.cas.String", featOkTst);
    casFeatCode_text  = (null == casFeat_text) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_text).getCode();

 
    casFeat_confidence = jcas.getRequiredFeatureDE(casType, "confidence", "uima.cas.Double", featOkTst);
    casFeatCode_confidence  = (null == casFeat_confidence) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_confidence).getCode();

 
    casFeat_annotator = jcas.getRequiredFeatureDE(casType, "annotator", "uima.cas.String", featOkTst);
    casFeatCode_annotator  = (null == casFeat_annotator) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_annotator).getCode();

 
    casFeat_uri = jcas.getRequiredFeatureDE(casType, "uri", "uima.cas.String", featOkTst);
    casFeatCode_uri  = (null == casFeat_uri) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_uri).getCode();

 
    casFeat_subject = jcas.getRequiredFeatureDE(casType, "subject", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Subject", featOkTst);
    casFeatCode_subject  = (null == casFeat_subject) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_subject).getCode();

 
    casFeat_object = jcas.getRequiredFeatureDE(casType, "object", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ObjectF", featOkTst);
    casFeatCode_object  = (null == casFeat_object) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_object).getCode();

 
    casFeat_relation = jcas.getRequiredFeatureDE(casType, "relation", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Relation", featOkTst);
    casFeatCode_relation  = (null == casFeat_relation) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_relation).getCode();

 
    casFeat_value = jcas.getRequiredFeatureDE(casType, "value", "uima.cas.String", featOkTst);
    casFeatCode_value  = (null == casFeat_value) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_value).getCode();

  }
}



    