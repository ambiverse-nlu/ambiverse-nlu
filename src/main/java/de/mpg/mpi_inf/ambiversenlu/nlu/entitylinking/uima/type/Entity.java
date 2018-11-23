/* First created by JCasGen Wed Jul 08 15:01:05 CEST 2015 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


public class Entity extends Annotation {

    @SuppressWarnings("hiding") public final static int typeIndexID = JCasRegistry.register(Entity.class);

    @SuppressWarnings("hiding") public final static int type = typeIndexID;

  /**
   * @return index of the type  
   */
  @Override public int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected Entity() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Entity(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Entity(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
   */
  public Entity(JCas jcas, int begin, int end) {
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

  /** getter for score - gets The score provided by the disambiguation method.
   *
   * @return value of the feature 
   */
  public double getScore() {
    if (Entity_Type.featOkTst && ((Entity_Type)jcasType).casFeat_score == null)
      jcasType.jcas.throwFeatMissing("score", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Entity");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((Entity_Type)jcasType).casFeatCode_score);}
    
  /** setter for score - sets The score provided by the disambiguation method. 
   *
   * @param v value to set into the feature 
   */
  public void setScore(double v) {
    if (Entity_Type.featOkTst && ((Entity_Type)jcasType).casFeat_score == null)
      jcasType.jcas.throwFeatMissing("score", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Entity");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((Entity_Type)jcasType).casFeatCode_score, v);}    
   
    
  //*--------------*
  //* Feature: ID

  /** getter for ID - gets The unique identifier of the entity in a knowledge base.
   *
   * @return value of the feature 
   */
  public String getID() {
    if (Entity_Type.featOkTst && ((Entity_Type)jcasType).casFeat_ID == null)
      jcasType.jcas.throwFeatMissing("ID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Entity");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Entity_Type)jcasType).casFeatCode_ID);}
    
  /** setter for ID - sets The unique identifier of the entity in a knowledge base. 
   *
   * @param v value to set into the feature 
   */
  public void setID(String v) {
    if (Entity_Type.featOkTst && ((Entity_Type)jcasType).casFeat_ID == null)
      jcasType.jcas.throwFeatMissing("ID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Entity");
    jcasType.ll_cas.ll_setStringValue(addr, ((Entity_Type)jcasType).casFeatCode_ID, v);}    
   
    
  //*--------------*
  //* Feature: types

  /** getter for types - gets Entity types in the knowledge base.
   *
   * @return value of the feature 
   */
  public StringArray getTypes() {
    if (Entity_Type.featOkTst && ((Entity_Type)jcasType).casFeat_types == null)
      jcasType.jcas.throwFeatMissing("types", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Entity");
    return (StringArray)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Entity_Type)jcasType).casFeatCode_types)));}
    
  /** setter for types - sets Entity types in the knowledge base. 
   *
   * @param v value to set into the feature 
   */
  public void setTypes(StringArray v) {
    if (Entity_Type.featOkTst && ((Entity_Type)jcasType).casFeat_types == null)
      jcasType.jcas.throwFeatMissing("types", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Entity");
    jcasType.ll_cas.ll_setRefValue(addr, ((Entity_Type)jcasType).casFeatCode_types, jcasType.ll_cas.ll_getFSRef(v));}    
    
  /** indexed getter for types - gets an indexed value - Entity types in the knowledge base.
   *
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public String getTypes(int i) {
    if (Entity_Type.featOkTst && ((Entity_Type)jcasType).casFeat_types == null)
      jcasType.jcas.throwFeatMissing("types", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Entity");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Entity_Type)jcasType).casFeatCode_types), i);
    return jcasType.ll_cas.ll_getStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Entity_Type)jcasType).casFeatCode_types), i);}

  /** indexed setter for types - sets an indexed value - Entity types in the knowledge base.
   *
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setTypes(int i, String v) { 
    if (Entity_Type.featOkTst && ((Entity_Type)jcasType).casFeat_types == null)
      jcasType.jcas.throwFeatMissing("types", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Entity");
    jcasType.jcas.checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((Entity_Type)jcasType).casFeatCode_types), i);
    jcasType.ll_cas.ll_setStringArrayValue(jcasType.ll_cas.ll_getRefValue(addr, ((Entity_Type)jcasType).casFeatCode_types), i, v);}
  }

    