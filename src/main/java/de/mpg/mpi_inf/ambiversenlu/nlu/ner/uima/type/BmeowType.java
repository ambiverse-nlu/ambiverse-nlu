

/* First created by JCasGen Mon Mar 07 16:24:05 CET 2016 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;



public class BmeowType extends Annotation {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(BmeowType.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected BmeowType() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public BmeowType(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public BmeowType(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public BmeowType(JCas jcas, int begin, int end) {
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
  //* Feature: bmeowType

  /** getter for bmeowType - gets 
   *
   * @return value of the feature 
   */
  public String getBmeowType() {
    if (BmeowType_Type.featOkTst && ((BmeowType_Type)jcasType).casFeat_bmeowType == null)
      jcasType.jcas.throwFeatMissing("bmeowType", "BmeowType");
    return jcasType.ll_cas.ll_getStringValue(addr, ((BmeowType_Type)jcasType).casFeatCode_bmeowType);}
    
  /** setter for bmeowType - sets  
   *
   * @param v value to set into the feature 
   */
  public void setBmeowType(String v) {
    if (BmeowType_Type.featOkTst && ((BmeowType_Type)jcasType).casFeat_bmeowType == null)
      jcasType.jcas.throwFeatMissing("bmeowType", "BmeowType");
    jcasType.ll_cas.ll_setStringValue(addr, ((BmeowType_Type)jcasType).casFeatCode_bmeowType, v);}    
  }

    