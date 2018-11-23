

/* First created by JCasGen Mon Jul 24 18:10:26 CEST 2017 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;



public class ConceptMention extends Annotation {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ConceptMention.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected ConceptMention() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public ConceptMention(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public ConceptMention(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public ConceptMention(JCas jcas, int begin, int end) {
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
  //* Feature: concept

  /** getter for concept - gets 
   *
   * @return value of the feature 
   */
  public String getConcept() {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_concept == null)
      jcasType.jcas.throwFeatMissing("concept", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_concept);}
    
  /** setter for concept - sets  
   *
   * @param v value to set into the feature 
   */
  public void setConcept(String v) {
    if (ConceptMention_Type.featOkTst && ((ConceptMention_Type)jcasType).casFeat_concept == null)
      jcasType.jcas.throwFeatMissing("concept", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConceptMention_Type)jcasType).casFeatCode_concept, v);}    
  }

    