
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
 * Updated by JCasGen Tue Jan 30 16:46:25 CET 2018
 *  */
public class Constituent_Type extends Annotation_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Constituent_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Constituent_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Constituent(addr, Constituent_Type.this);
  			   Constituent_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Constituent(addr, Constituent_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Constituent.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
 

  final Feature casFeat_tokens;

  final int     casFeatCode_tokens;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getTokens(int addr) {
        if (featOkTst && casFeat_tokens == null)
      jcas.throwFeatMissing("tokens", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return ll_cas.ll_getRefValue(addr, casFeatCode_tokens);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTokens(int addr, int v) {
        if (featOkTst && casFeat_tokens == null)
      jcas.throwFeatMissing("tokens", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    ll_cas.ll_setRefValue(addr, casFeatCode_tokens, v);}
    
   /**
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public int getTokens(int addr, int i) {
        if (featOkTst && casFeat_tokens == null)
      jcas.throwFeatMissing("tokens", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_tokens), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_tokens), i);
  return ll_cas.ll_getRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_tokens), i);
  }
   
  /**
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setTokens(int addr, int i, int v) {
        if (featOkTst && casFeat_tokens == null)
      jcas.throwFeatMissing("tokens", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    if (lowLevelTypeChecks)
      ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_tokens), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_tokens), i);
    ll_cas.ll_setRefArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_tokens), i, v);
  }
 
 

  final Feature casFeat_text;

  final int     casFeatCode_text;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getText(int addr) {
        if (featOkTst && casFeat_text == null)
      jcas.throwFeatMissing("text", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return ll_cas.ll_getStringValue(addr, casFeatCode_text);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setText(int addr, String v) {
        if (featOkTst && casFeat_text == null)
      jcas.throwFeatMissing("text", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    ll_cas.ll_setStringValue(addr, casFeatCode_text, v);}
    
  
 

  final Feature casFeat_uri;

  final int     casFeatCode_uri;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getUri(int addr) {
        if (featOkTst && casFeat_uri == null)
      jcas.throwFeatMissing("uri", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return ll_cas.ll_getStringValue(addr, casFeatCode_uri);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setUri(int addr, String v) {
        if (featOkTst && casFeat_uri == null)
      jcas.throwFeatMissing("uri", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    ll_cas.ll_setStringValue(addr, casFeatCode_uri, v);}
    
  
 

  final Feature casFeat_explicit;

  final int     casFeatCode_explicit;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public boolean getExplicit(int addr) {
        if (featOkTst && casFeat_explicit == null)
      jcas.throwFeatMissing("explicit", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_explicit);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setExplicit(int addr, boolean v) {
        if (featOkTst && casFeat_explicit == null)
      jcas.throwFeatMissing("explicit", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_explicit, v);}
    
  
 

  final Feature casFeat_label;

  final int     casFeatCode_label;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getLabel(int addr) {
        if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return ll_cas.ll_getStringValue(addr, casFeatCode_label);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLabel(int addr, String v) {
        if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    ll_cas.ll_setStringValue(addr, casFeatCode_label, v);}
    
  
 

  final Feature casFeat_labeled;

  final int     casFeatCode_labeled;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public boolean getLabeled(int addr) {
        if (featOkTst && casFeat_labeled == null)
      jcas.throwFeatMissing("labeled", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return ll_cas.ll_getBooleanValue(addr, casFeatCode_labeled);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLabeled(int addr, boolean v) {
        if (featOkTst && casFeat_labeled == null)
      jcas.throwFeatMissing("labeled", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    ll_cas.ll_setBooleanValue(addr, casFeatCode_labeled, v);}
    
  
 

  final Feature casFeat_annotator;

  final int     casFeatCode_annotator;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getAnnotator(int addr) {
        if (featOkTst && casFeat_annotator == null)
      jcas.throwFeatMissing("annotator", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return ll_cas.ll_getStringValue(addr, casFeatCode_annotator);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setAnnotator(int addr, String v) {
        if (featOkTst && casFeat_annotator == null)
      jcas.throwFeatMissing("annotator", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    ll_cas.ll_setStringValue(addr, casFeatCode_annotator, v);}
    
  
 

  final Feature casFeat_confidence;

  final int     casFeatCode_confidence;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public double getConfidence(int addr) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_confidence);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setConfidence(int addr, double v) {
        if (featOkTst && casFeat_confidence == null)
      jcas.throwFeatMissing("confidence", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_confidence, v);}
    
  
 

  final Feature casFeat_value;

  final int     casFeatCode_value;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getValue(int addr) {
        if (featOkTst && casFeat_value == null)
      jcas.throwFeatMissing("value", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return ll_cas.ll_getStringValue(addr, casFeatCode_value);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setValue(int addr, String v) {
        if (featOkTst && casFeat_value == null)
      jcas.throwFeatMissing("value", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    ll_cas.ll_setStringValue(addr, casFeatCode_value, v);}
    
  
 

  final Feature casFeat_head;

  final int     casFeatCode_head;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getHead(int addr) {
        if (featOkTst && casFeat_head == null)
      jcas.throwFeatMissing("head", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return ll_cas.ll_getRefValue(addr, casFeatCode_head);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setHead(int addr, int v) {
        if (featOkTst && casFeat_head == null)
      jcas.throwFeatMissing("head", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    ll_cas.ll_setRefValue(addr, casFeatCode_head, v);}
    
  
 

  final Feature casFeat_normalizedForm;

  final int     casFeatCode_normalizedForm;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getNormalizedForm(int addr) {
        if (featOkTst && casFeat_normalizedForm == null)
      jcas.throwFeatMissing("normalizedForm", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return ll_cas.ll_getStringValue(addr, casFeatCode_normalizedForm);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setNormalizedForm(int addr, String v) {
        if (featOkTst && casFeat_normalizedForm == null)
      jcas.throwFeatMissing("normalizedForm", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    ll_cas.ll_setStringValue(addr, casFeatCode_normalizedForm, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Constituent_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_tokens = jcas.getRequiredFeatureDE(casType, "tokens", "uima.cas.FSArray", featOkTst);
    casFeatCode_tokens  = (null == casFeat_tokens) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_tokens).getCode();

 
    casFeat_text = jcas.getRequiredFeatureDE(casType, "text", "uima.cas.String", featOkTst);
    casFeatCode_text  = (null == casFeat_text) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_text).getCode();

 
    casFeat_uri = jcas.getRequiredFeatureDE(casType, "uri", "uima.cas.String", featOkTst);
    casFeatCode_uri  = (null == casFeat_uri) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_uri).getCode();

 
    casFeat_explicit = jcas.getRequiredFeatureDE(casType, "explicit", "uima.cas.Boolean", featOkTst);
    casFeatCode_explicit  = (null == casFeat_explicit) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_explicit).getCode();

 
    casFeat_label = jcas.getRequiredFeatureDE(casType, "label", "uima.cas.String", featOkTst);
    casFeatCode_label  = (null == casFeat_label) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_label).getCode();

 
    casFeat_labeled = jcas.getRequiredFeatureDE(casType, "labeled", "uima.cas.Boolean", featOkTst);
    casFeatCode_labeled  = (null == casFeat_labeled) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_labeled).getCode();

 
    casFeat_annotator = jcas.getRequiredFeatureDE(casType, "annotator", "uima.cas.String", featOkTst);
    casFeatCode_annotator  = (null == casFeat_annotator) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_annotator).getCode();

 
    casFeat_confidence = jcas.getRequiredFeatureDE(casType, "confidence", "uima.cas.Double", featOkTst);
    casFeatCode_confidence  = (null == casFeat_confidence) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_confidence).getCode();

 
    casFeat_value = jcas.getRequiredFeatureDE(casType, "value", "uima.cas.String", featOkTst);
    casFeatCode_value  = (null == casFeat_value) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_value).getCode();

 
    casFeat_head = jcas.getRequiredFeatureDE(casType, "head", "uima.tcas.Annotation", featOkTst);
    casFeatCode_head  = (null == casFeat_head) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_head).getCode();

 
    casFeat_normalizedForm = jcas.getRequiredFeatureDE(casType, "normalizedForm", "uima.cas.String", featOkTst);
    casFeatCode_normalizedForm  = (null == casFeat_normalizedForm) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_normalizedForm).getCode();

  }
}



    