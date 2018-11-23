

/* First created by JCasGen Tue Jan 30 13:02:18 CET 2018 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;



public class OpenFact extends Annotation {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(OpenFact.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected OpenFact() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public OpenFact(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public OpenFact(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public OpenFact(JCas jcas, int begin, int end) {
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
  //* Feature: text

  /** getter for text - gets 
   *
   * @return value of the feature 
   */
  public String getText() {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_text == null)
      jcasType.jcas.throwFeatMissing("text", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return jcasType.ll_cas.ll_getStringValue(addr, ((OpenFact_Type)jcasType).casFeatCode_text);}
    
  /** setter for text - sets  
   *
   * @param v value to set into the feature 
   */
  public void setText(String v) {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_text == null)
      jcasType.jcas.throwFeatMissing("text", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    jcasType.ll_cas.ll_setStringValue(addr, ((OpenFact_Type)jcasType).casFeatCode_text, v);}    
   
    
  //*--------------*
  //* Feature: confidence

  /** getter for confidence - gets 
   *
   * @return value of the feature 
   */
  public double getConfidence() {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((OpenFact_Type)jcasType).casFeatCode_confidence);}
    
  /** setter for confidence - sets  
   *
   * @param v value to set into the feature 
   */
  public void setConfidence(double v) {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_confidence == null)
      jcasType.jcas.throwFeatMissing("confidence", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((OpenFact_Type)jcasType).casFeatCode_confidence, v);}    
   
    
  //*--------------*
  //* Feature: annotator

  /** getter for annotator - gets 
   *
   * @return value of the feature 
   */
  public String getAnnotator() {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_annotator == null)
      jcasType.jcas.throwFeatMissing("annotator", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return jcasType.ll_cas.ll_getStringValue(addr, ((OpenFact_Type)jcasType).casFeatCode_annotator);}
    
  /** setter for annotator - sets  
   *
   * @param v value to set into the feature 
   */
  public void setAnnotator(String v) {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_annotator == null)
      jcasType.jcas.throwFeatMissing("annotator", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    jcasType.ll_cas.ll_setStringValue(addr, ((OpenFact_Type)jcasType).casFeatCode_annotator, v);}    
   
    
  //*--------------*
  //* Feature: uri

  /** getter for uri - gets 
   *
   * @return value of the feature 
   */
  public String getUri() {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_uri == null)
      jcasType.jcas.throwFeatMissing("uri", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return jcasType.ll_cas.ll_getStringValue(addr, ((OpenFact_Type)jcasType).casFeatCode_uri);}
    
  /** setter for uri - sets  
   *
   * @param v value to set into the feature 
   */
  public void setUri(String v) {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_uri == null)
      jcasType.jcas.throwFeatMissing("uri", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    jcasType.ll_cas.ll_setStringValue(addr, ((OpenFact_Type)jcasType).casFeatCode_uri, v);}    
   
    
  //*--------------*
  //* Feature: subject

  /** getter for subject - gets 
   *
   * @return value of the feature 
   */
  public Subject getSubject() {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_subject == null)
      jcasType.jcas.throwFeatMissing("subject", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return (Subject)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((OpenFact_Type)jcasType).casFeatCode_subject)));}
    
  /** setter for subject - sets  
   *
   * @param v value to set into the feature 
   */
  public void setSubject(Subject v) {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_subject == null)
      jcasType.jcas.throwFeatMissing("subject", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    jcasType.ll_cas.ll_setRefValue(addr, ((OpenFact_Type)jcasType).casFeatCode_subject, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: object

  /** getter for object - gets 
   *
   * @return value of the feature 
   */
  public ObjectF getObject() {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_object == null)
      jcasType.jcas.throwFeatMissing("object", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return (ObjectF)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((OpenFact_Type)jcasType).casFeatCode_object)));}
    
  /** setter for object - sets  
   *
   * @param v value to set into the feature 
   */
  public void setObject(ObjectF v) {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_object == null)
      jcasType.jcas.throwFeatMissing("object", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    jcasType.ll_cas.ll_setRefValue(addr, ((OpenFact_Type)jcasType).casFeatCode_object, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: relation

  /** getter for relation - gets 
   *
   * @return value of the feature 
   */
  public Relation getRelation() {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_relation == null)
      jcasType.jcas.throwFeatMissing("relation", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return (Relation)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((OpenFact_Type)jcasType).casFeatCode_relation)));}
    
  /** setter for relation - sets  
   *
   * @param v value to set into the feature 
   */
  public void setRelation(Relation v) {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_relation == null)
      jcasType.jcas.throwFeatMissing("relation", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    jcasType.ll_cas.ll_setRefValue(addr, ((OpenFact_Type)jcasType).casFeatCode_relation, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: value

  /** getter for value - gets 
   *
   * @return value of the feature 
   */
  public String getValue() {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    return jcasType.ll_cas.ll_getStringValue(addr, ((OpenFact_Type)jcasType).casFeatCode_value);}
    
  /** setter for value - sets  
   *
   * @param v value to set into the feature 
   */
  public void setValue(String v) {
    if (OpenFact_Type.featOkTst && ((OpenFact_Type)jcasType).casFeat_value == null)
      jcasType.jcas.throwFeatMissing("value", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact");
    jcasType.ll_cas.ll_setStringValue(addr, ((OpenFact_Type)jcasType).casFeatCode_value, v);}    
  }

    