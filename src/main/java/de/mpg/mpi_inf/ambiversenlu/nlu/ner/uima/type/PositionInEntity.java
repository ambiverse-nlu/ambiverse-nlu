

/* First created by JCasGen Tue Mar 01 11:16:33 CET 2016 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;



public class PositionInEntity extends Annotation {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(PositionInEntity.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}

  /** Never called.  Disable default constructor
   *  */
  protected PositionInEntity() {/* intentionally empty block */}

  /** Internal - constructor used by generator
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure
   */
  public PositionInEntity(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }

  /**
   * @param jcas JCas to which this Feature Structure belongs
   */
  public PositionInEntity(JCas jcas) {
    super(jcas);
    readObject();
  }

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA
  */
  public PositionInEntity(JCas jcas, int begin, int end) {
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
  //* Feature: positionInEntity

  /** getter for positionInEntity - gets 
   *
   * @return value of the feature 
   */
  public String getPositionInEntity() {
    if (PositionInEntity_Type.featOkTst && ((PositionInEntity_Type)jcasType).casFeat_positionInEntity == null)
      jcasType.jcas.throwFeatMissing("positionInEntity", "PositionInEntity");
    return jcasType.ll_cas.ll_getStringValue(addr, ((PositionInEntity_Type)jcasType).casFeatCode_positionInEntity);}
    
  /** setter for positionInEntity - sets  
   *
   * @param v value to set into the feature 
   */
  public void setPositionInEntity(String v) {
    if (PositionInEntity_Type.featOkTst && ((PositionInEntity_Type)jcasType).casFeat_positionInEntity == null)
      jcasType.jcas.throwFeatMissing("positionInEntity", "PositionInEntity");
    jcasType.ll_cas.ll_setStringValue(addr, ((PositionInEntity_Type)jcasType).casFeatCode_positionInEntity, v);}    
  }

    