

/* First created by JCasGen Tue Mar 01 11:16:33 CET 2016 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;



public class WordShape extends Annotation {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(WordShape.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected WordShape() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public WordShape(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public WordShape(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public WordShape(JCas jcas, int begin, int end) {
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
  //* Feature: wordShape

  /** getter for wordShape - gets 
   *
   * @return value of the feature 
   */
  public String getWordShape() {
    if (WordShape_Type.featOkTst && ((WordShape_Type)jcasType).casFeat_wordShape == null)
      jcasType.jcas.throwFeatMissing("wordShape", "WordShape");
    return jcasType.ll_cas.ll_getStringValue(addr, ((WordShape_Type)jcasType).casFeatCode_wordShape);}
    
  /** setter for wordShape - sets  
   *
   * @param v value to set into the feature 
   */
  public void setWordShape(String v) {
    if (WordShape_Type.featOkTst && ((WordShape_Type)jcasType).casFeat_wordShape == null)
      jcasType.jcas.throwFeatMissing("wordShape", "WordShape");
    jcasType.ll_cas.ll_setStringValue(addr, ((WordShape_Type)jcasType).casFeatCode_wordShape, v);}    
  }

    