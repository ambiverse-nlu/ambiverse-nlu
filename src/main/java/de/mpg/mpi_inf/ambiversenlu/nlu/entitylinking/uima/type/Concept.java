

/* First created by JCasGen Fri Feb 02 17:15:33 CET 2018 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;



public class Concept extends Annotation {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Concept.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected Concept() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Concept(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Concept(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Concept(JCas jcas, int begin, int end) {
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
  //* Feature: score

  /** getter for score - gets 
   *
   * @return value of the feature 
   */
  public double getScore() {
    if (Concept_Type.featOkTst && ((Concept_Type)jcasType).casFeat_score == null)
      jcasType.jcas.throwFeatMissing("score", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((Concept_Type)jcasType).casFeatCode_score);}
    
  /** setter for score - sets  
   *
   * @param v value to set into the feature 
   */
  public void setScore(double v) {
    if (Concept_Type.featOkTst && ((Concept_Type)jcasType).casFeat_score == null)
      jcasType.jcas.throwFeatMissing("score", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((Concept_Type)jcasType).casFeatCode_score, v);}    
   
    
  //*--------------*
  //* Feature: ID

  /** getter for ID - gets 
   *
   * @return value of the feature 
   */
  public String getID() {
    if (Concept_Type.featOkTst && ((Concept_Type)jcasType).casFeat_ID == null)
      jcasType.jcas.throwFeatMissing("ID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Concept_Type)jcasType).casFeatCode_ID);}
    
  /** setter for ID - sets  
   *
   * @param v value to set into the feature 
   */
  public void setID(String v) {
    if (Concept_Type.featOkTst && ((Concept_Type)jcasType).casFeat_ID == null)
      jcasType.jcas.throwFeatMissing("ID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    jcasType.ll_cas.ll_setStringValue(addr, ((Concept_Type)jcasType).casFeatCode_ID, v);}    
   
    
  //*--------------*
  //* Feature: types

  /** getter for types - gets 
   *
   * @return value of the feature 
   */
  public StringArray getTypes() {
    if (Concept_Type.featOkTst && ((Concept_Type)jcasType).casFeat_types == null)
      jcasType.jcas.throwFeatMissing("types", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Concept_Type)jcasType).casFeatCode_types)));}
    
  /** setter for types - sets  
   *
   * @param v value to set into the feature 
   */
  public void setTypes(StringArray v) {
    if (Concept_Type.featOkTst && ((Concept_Type)jcasType).casFeat_types == null)
      jcasType.jcas.throwFeatMissing("types", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    jcasType.ll_cas.ll_setRefValue(addr, ((Concept_Type)jcasType).casFeatCode_types, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for types - gets an indexed value - 
   *
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getTypes(int i) {
    if (Concept_Type.featOkTst && ((Concept_Type)jcasType).casFeat_types == null)
      jcasType.jcas.throwFeatMissing("types", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Concept_Type)jcasType).casFeatCode_types), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Concept_Type)jcasType).casFeatCode_types), i);}

  /** indexed setter for types - sets an indexed value - 
   *
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setTypes(int i, String v) { 
    if (Concept_Type.featOkTst && ((Concept_Type)jcasType).casFeat_types == null)
      jcasType.jcas.throwFeatMissing("types", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Concept_Type)jcasType).casFeatCode_types), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Concept_Type)jcasType).casFeatCode_types), i, v);}
  }

    