

/* First created by JCasGen Tue Jan 30 13:02:18 CET 2018 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;



public class Constituent extends Annotation {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Constituent.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected Constituent() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Constituent(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Constituent(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Constituent(JCas jcas, int begin, int end) {
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
  //* Feature: tokens

  /** getter for tokens - gets 
   *
   * @return value of the feature 
   */
  public FSArray getTokens() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_tokens == null)
      jcasType.jcas.throwFeatMissing("tokens", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return (FSArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_tokens)));}
    
  /** setter for tokens - sets  
   *
   * @param v value to set into the feature 
   */
  public void setTokens(FSArray v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_tokens == null)
      jcasType.jcas.throwFeatMissing("tokens", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    jcasType.ll_cas.ll_setRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_tokens, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for tokens - gets an indexed value - 
   *
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public TOP getTokens(int i) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_tokens == null)
      jcasType.jcas.throwFeatMissing("tokens", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_tokens), i);
    return (TOP)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_tokens), i)));}

  /** indexed setter for tokens - sets an indexed value - 
   *
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setTokens(int i, TOP v) { 
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_tokens == null)
      jcasType.jcas.throwFeatMissing("tokens", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_tokens), i);
    jcasType.ll_cas.ll_setRefArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_tokens), i, jcasType.ll_cas.ll_getFSRef(v));}
   
    
  //*--------------*
  //* Feature: text

  /** getter for text - gets 
   *
   * @return value of the feature 
   */
  public String getText() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_text == null)
      jcasType.jcas.throwFeatMissing("text", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_text);}
    
  /** setter for text - sets  
   *
   * @param v value to set into the feature 
   */
  public void setText(String v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_text == null)
      jcasType.jcas.throwFeatMissing("text", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    jcasType.ll_cas.ll_setStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_text, v);}    
   
    
  //*--------------*
  //* Feature: uri

  /** getter for uri - gets 
   *
   * @return value of the feature 
   */
  public String getUri() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_uri == null)
      jcasType.jcas.throwFeatMissing("uri", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_uri);}
    
  /** setter for uri - sets  
   *
   * @param v value to set into the feature 
   */
  public void setUri(String v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_uri == null)
      jcasType.jcas.throwFeatMissing("uri", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    jcasType.ll_cas.ll_setStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_uri, v);}    
   
    
  //*--------------*
  //* Feature: explicit

  /** getter for explicit - gets 
   *
   * @return value of the feature 
   */
  public boolean getExplicit() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_explicit == null)
      jcasType.jcas.throwFeatMissing("explicit", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((Constituent_Type)jcasType).casFeatCode_explicit);}
    
  /** setter for explicit - sets  
   *
   * @param v value to set into the feature 
   */
  public void setExplicit(boolean v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_explicit == null)
      jcasType.jcas.throwFeatMissing("explicit", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((Constituent_Type)jcasType).casFeatCode_explicit, v);}    
   
    
  //*--------------*
  //* Feature: label

  /** getter for label - gets 
   *
   * @return value of the feature 
   */
  public String getLabel() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_label);}
    
  /** setter for label - sets  
   *
   * @param v value to set into the feature 
   */
  public void setLabel(String v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    jcasType.ll_cas.ll_setStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_label, v);}    
   
    
  //*--------------*
  //* Feature: labeled

  /** getter for labeled - gets 
   *
   * @return value of the feature 
   */
  public boolean getLabeled() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_labeled == null)
      jcasType.jcas.throwFeatMissing("labeled", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return jcasType.ll_cas.ll_getBooleanValue(addr, ((Constituent_Type)jcasType).casFeatCode_labeled);}
    
  /** setter for labeled - sets  
   *
   * @param v value to set into the feature 
   */
  public void setLabeled(boolean v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_labeled == null)
      jcasType.jcas.throwFeatMissing("labeled", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    jcasType.ll_cas.ll_setBooleanValue(addr, ((Constituent_Type)jcasType).casFeatCode_labeled, v);}    
   
    
  //*--------------*
  //* Feature: annotator

  /** getter for annotator - gets 
   *
   * @return value of the feature 
   */
  public String getAnnotator() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_annotator == null)
      jcasType.jcas.throwFeatMissing("annotator", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_annotator);}
    
  /** setter for annotator - sets  
   *
   * @param v value to set into the feature 
   */
  public void setAnnotator(String v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_annotator == null)
      jcasType.jcas.throwFeatMissing("annotator", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    jcasType.ll_cas.ll_setStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_annotator, v);}    
   
    
  //*--------------*
  //* Feature: confidence

  /** getter for confidence - gets 
   *
   * @return value of the feature 
   */
  public double getConfidence() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((Constituent_Type)jcasType).casFeatCode_confidence);}
    
  /** setter for confidence - sets  
   *
   * @param v value to set into the feature 
   */
  public void setConfidence(double v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((Constituent_Type)jcasType).casFeatCode_confidence, v);}    
   
    
  //*--------------*
  //* Feature: value

  /** getter for value - gets 
   *
   * @return value of the feature 
   */
  public String getValue() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_value);}
    
  /** setter for value - sets  
   *
   * @param v value to set into the feature 
   */
  public void setValue(String v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    jcasType.ll_cas.ll_setStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_value, v);}    
   
    
  //*--------------*
  //* Feature: head

  /** getter for head - gets 
   *
   * @return value of the feature 
   */
  public Annotation getHead() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_head == null)
      jcasType.jcas.throwFeatMissing("head", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return (Annotation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_head)));}
    
  /** setter for head - sets  
   *
   * @param v value to set into the feature 
   */
  public void setHead(Annotation v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_head == null)
      jcasType.jcas.throwFeatMissing("head", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    jcasType.ll_cas.ll_setRefValue(addr, ((Constituent_Type)jcasType).casFeatCode_head, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: normalizedForm

  /** getter for normalizedForm - gets 
   *
   * @return value of the feature 
   */
  public String getNormalizedForm() {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_normalizedForm == null)
      jcasType.jcas.throwFeatMissing("normalizedForm", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_normalizedForm);}
    
  /** setter for normalizedForm - sets  
   *
   * @param v value to set into the feature 
   */
  public void setNormalizedForm(String v) {
    if (Constituent_Type.featOkTst && ((Constituent_Type)jcasType).casFeat_normalizedForm == null)
      jcasType.jcas.throwFeatMissing("normalizedForm", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Constituent");
    jcasType.ll_cas.ll_setStringValue(addr, ((Constituent_Type)jcasType).casFeatCode_normalizedForm, v);}    
  }

    