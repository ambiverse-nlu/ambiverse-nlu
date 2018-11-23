

/* First created by JCasGen Tue Jun 27 16:45:15 CEST 2017 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.TOP_Type;



public class WikiType extends FSList {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(WikiType.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected WikiType() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public WikiType(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public WikiType(JCas jcas) {
    super(jcas);
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
  //* Feature: id

  /** getter for id - gets 
   *
   * @return value of the feature 
   */
  public int getId() {
    if (WikiType_Type.featOkTst && ((WikiType_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.WikiType");
    return jcasType.ll_cas.ll_getIntValue(addr, ((WikiType_Type)jcasType).casFeatCode_id);}
    
  /** setter for id - sets  
   *
   * @param v value to set into the feature 
   */
  public void setId(int v) {
    if (WikiType_Type.featOkTst && ((WikiType_Type)jcasType).casFeat_id == null)
      jcasType.jcas.throwFeatMissing("id", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.WikiType");
    jcasType.ll_cas.ll_setIntValue(addr, ((WikiType_Type)jcasType).casFeatCode_id, v);}    
   
    
  //*--------------*
  //* Feature: name

  /** getter for name - gets 
   *
   * @return value of the feature 
   */
  public String getName() {
    if (WikiType_Type.featOkTst && ((WikiType_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.WikiType");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WikiType_Type)jcasType).casFeatCode_name);}
    
  /** setter for name - sets  
   *
   * @param v value to set into the feature 
   */
  public void setName(String v) {
    if (WikiType_Type.featOkTst && ((WikiType_Type)jcasType).casFeat_name == null)
      jcasType.jcas.throwFeatMissing("name", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.WikiType");
    jcasType.ll_cas.ll_setStringValue(addr, ((WikiType_Type)jcasType).casFeatCode_name, v);}    
   
    
  //*--------------*
  //* Feature: knowledgebase

  /** getter for knowledgebase - gets 
   *
   * @return value of the feature 
   */
  public String getKnowledgebase() {
    if (WikiType_Type.featOkTst && ((WikiType_Type)jcasType).casFeat_knowledgebase == null)
      jcasType.jcas.throwFeatMissing("knowledgebase", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.WikiType");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WikiType_Type)jcasType).casFeatCode_knowledgebase);}
    
  /** setter for knowledgebase - sets  
   *
   * @param v value to set into the feature 
   */
  public void setKnowledgebase(String v) {
    if (WikiType_Type.featOkTst && ((WikiType_Type)jcasType).casFeat_knowledgebase == null)
      jcasType.jcas.throwFeatMissing("knowledgebase", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.WikiType");
    jcasType.ll_cas.ll_setStringValue(addr, ((WikiType_Type)jcasType).casFeatCode_knowledgebase, v);}    
  }

    