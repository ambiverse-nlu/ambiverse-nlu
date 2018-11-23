

/* First created by JCasGen Thu Mar 03 11:42:04 CET 2016 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;



public class AidaMention extends Annotation {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(AidaMention.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected AidaMention() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public AidaMention(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public AidaMention(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public AidaMention(JCas jcas, int begin, int end) {
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
  //* Feature: internalId

  /** getter for internalId - gets 
   *
   * @return value of the feature 
   */
  public int getInternalId() {
    if (AidaMention_Type.featOkTst && ((AidaMention_Type)jcasType).casFeat_internalId == null)
      jcasType.jcas.throwFeatMissing("internalId", "AidaMention");
    return jcasType.ll_cas.ll_getIntValue(addr, ((AidaMention_Type)jcasType).casFeatCode_internalId);}
    
  /** setter for internalId - sets  
   *
   * @param v value to set into the feature 
   */
  public void setInternalId(int v) {
    if (AidaMention_Type.featOkTst && ((AidaMention_Type)jcasType).casFeat_internalId == null)
      jcasType.jcas.throwFeatMissing("internalId", "AidaMention");
    jcasType.ll_cas.ll_setIntValue(addr, ((AidaMention_Type)jcasType).casFeatCode_internalId, v);}    
   
    
  //*--------------*
  //* Feature: kbIdentifiedId

  /** getter for kbIdentifiedId - gets 
   *
   * @return value of the feature 
   */
  public String getKbIdentifiedId() {
    if (AidaMention_Type.featOkTst && ((AidaMention_Type)jcasType).casFeat_kbIdentifiedId == null)
      jcasType.jcas.throwFeatMissing("kbIdentifiedId", "AidaMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((AidaMention_Type)jcasType).casFeatCode_kbIdentifiedId);}
    
  /** setter for kbIdentifiedId - sets  
   *
   * @param v value to set into the feature 
   */
  public void setKbIdentifiedId(String v) {
    if (AidaMention_Type.featOkTst && ((AidaMention_Type)jcasType).casFeat_kbIdentifiedId == null)
      jcasType.jcas.throwFeatMissing("kbIdentifiedId", "AidaMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((AidaMention_Type)jcasType).casFeatCode_kbIdentifiedId, v);}    
  }

    