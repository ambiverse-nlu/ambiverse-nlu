

/* First created by JCasGen Tue Mar 29 15:01:43 CEST 2016 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;



public class NerMention extends Annotation {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(NerMention.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected NerMention() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public NerMention(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public NerMention(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public NerMention(JCas jcas, int begin, int end) {
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
  //* Feature: classType

  /** getter for classType - gets 
   *
   * @return value of the feature 
   */
  public String getClassType() {
    if (NerMention_Type.featOkTst && ((NerMention_Type)jcasType).casFeat_classType == null)
      jcasType.jcas.throwFeatMissing("classType", "NerMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NerMention_Type)jcasType).casFeatCode_classType);}
    
  /** setter for classType - sets  
   *
   * @param v value to set into the feature 
   */
  public void setClassType(String v) {
    if (NerMention_Type.featOkTst && ((NerMention_Type)jcasType).casFeat_classType == null)
      jcasType.jcas.throwFeatMissing("classType", "NerMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((NerMention_Type)jcasType).casFeatCode_classType, v);}    
   
    
  //*--------------*
  //* Feature: bmeowTag

  /** getter for bmeowTag - gets 
   *
   * @return value of the feature 
   */
  public String getBmeowTag() {
    if (NerMention_Type.featOkTst && ((NerMention_Type)jcasType).casFeat_bmeowTag == null)
      jcasType.jcas.throwFeatMissing("bmeowTag", "NerMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((NerMention_Type)jcasType).casFeatCode_bmeowTag);}
    
  /** setter for bmeowTag - sets  
   *
   * @param v value to set into the feature 
   */
  public void setBmeowTag(String v) {
    if (NerMention_Type.featOkTst && ((NerMention_Type)jcasType).casFeat_bmeowTag == null)
      jcasType.jcas.throwFeatMissing("bmeowTag", "NerMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((NerMention_Type)jcasType).casFeatCode_bmeowTag, v);}    
  }

    