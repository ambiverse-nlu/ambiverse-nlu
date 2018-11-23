

/* First created by JCasGen Wed Feb 21 11:42:35 CET 2018 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;



public class Unknown extends Annotation {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Unknown.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected Unknown() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Unknown(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Unknown(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Unknown(JCas jcas, int begin, int end) {
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
  //* Feature: ID

  /** getter for ID - gets 
   *
   * @return value of the feature 
   */
  public String getID() {
    if (Unknown_Type.featOkTst && ((Unknown_Type)jcasType).casFeat_ID == null)
      jcasType.jcas.throwFeatMissing("ID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Unknown");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Unknown_Type)jcasType).casFeatCode_ID);}
    
  /** setter for ID - sets  
   *
   * @param v value to set into the feature 
   */
  public void setID(String v) {
    if (Unknown_Type.featOkTst && ((Unknown_Type)jcasType).casFeat_ID == null)
      jcasType.jcas.throwFeatMissing("ID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Unknown");
    jcasType.ll_cas.ll_setStringValue(addr, ((Unknown_Type)jcasType).casFeatCode_ID, v);}    
  }

    