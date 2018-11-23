

/* First created by JCasGen Tue Sep 05 13:31:28 CEST 2017 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;



public class ConstituentEntity extends Annotation {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ConstituentEntity.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected ConstituentEntity() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public ConstituentEntity(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public ConstituentEntity(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public ConstituentEntity(JCas jcas, int begin, int end) {
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
  //* Feature: wikidataID

  /** getter for wikidataID - gets 
   *
   * @return value of the feature 
   */
  public String getWikidataID() {
    if (ConstituentEntity_Type.featOkTst && ((ConstituentEntity_Type)jcasType).casFeat_wikidataID == null)
      jcasType.jcas.throwFeatMissing("wikidataID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConstituentEntity");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConstituentEntity_Type)jcasType).casFeatCode_wikidataID);}
    
  /** setter for wikidataID - sets  
   *
   * @param v value to set into the feature 
   */
  public void setWikidataID(String v) {
    if (ConstituentEntity_Type.featOkTst && ((ConstituentEntity_Type)jcasType).casFeat_wikidataID == null)
      jcasType.jcas.throwFeatMissing("wikidataID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConstituentEntity");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConstituentEntity_Type)jcasType).casFeatCode_wikidataID, v);}    
   
    
  //*--------------*
  //* Feature: entityID

  /** getter for entityID - gets 
   *
   * @return value of the feature 
   */
  public String getEntityID() {
    if (ConstituentEntity_Type.featOkTst && ((ConstituentEntity_Type)jcasType).casFeat_entityID == null)
      jcasType.jcas.throwFeatMissing("entityID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConstituentEntity");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConstituentEntity_Type)jcasType).casFeatCode_entityID);}
    
  /** setter for entityID - sets  
   *
   * @param v value to set into the feature 
   */
  public void setEntityID(String v) {
    if (ConstituentEntity_Type.featOkTst && ((ConstituentEntity_Type)jcasType).casFeat_entityID == null)
      jcasType.jcas.throwFeatMissing("entityID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConstituentEntity");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConstituentEntity_Type)jcasType).casFeatCode_entityID, v);}    
   
    
  //*--------------*
  //* Feature: mention

  /** getter for mention - gets 
   *
   * @return value of the feature 
   */
  public String getMention() {
    if (ConstituentEntity_Type.featOkTst && ((ConstituentEntity_Type)jcasType).casFeat_mention == null)
      jcasType.jcas.throwFeatMissing("mention", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConstituentEntity");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConstituentEntity_Type)jcasType).casFeatCode_mention);}
    
  /** setter for mention - sets  
   *
   * @param v value to set into the feature 
   */
  public void setMention(String v) {
    if (ConstituentEntity_Type.featOkTst && ((ConstituentEntity_Type)jcasType).casFeat_mention == null)
      jcasType.jcas.throwFeatMissing("mention", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConstituentEntity");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConstituentEntity_Type)jcasType).casFeatCode_mention, v);}    
  }

    