

/* First created by JCasGen Mon Jul 24 18:01:49 CEST 2017 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;



public class ConceptMentionCandidate extends Annotation {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(ConceptMentionCandidate.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected ConceptMentionCandidate() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public ConceptMentionCandidate(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public ConceptMentionCandidate(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public ConceptMentionCandidate(JCas jcas, int begin, int end) {
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
  //* Feature: conceptCandidate

  /** getter for conceptCandidate - gets 
   *
   * @return value of the feature 
   */
  public String getConceptCandidate() {
    if (ConceptMentionCandidate_Type.featOkTst && ((ConceptMentionCandidate_Type)jcasType).casFeat_conceptCandidate == null)
      jcasType.jcas.throwFeatMissing("conceptCandidate", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMentionCandidate");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConceptMentionCandidate_Type)jcasType).casFeatCode_conceptCandidate);}
    
  /** setter for conceptCandidate - sets  
   *
   * @param v value to set into the feature 
   */
  public void setConceptCandidate(String v) {
    if (ConceptMentionCandidate_Type.featOkTst && ((ConceptMentionCandidate_Type)jcasType).casFeat_conceptCandidate == null)
      jcasType.jcas.throwFeatMissing("conceptCandidate", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMentionCandidate");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConceptMentionCandidate_Type)jcasType).casFeatCode_conceptCandidate, v);}    
   
    
  //*--------------*
  //* Feature: mention

  /** getter for mention - gets 
   *
   * @return value of the feature 
   */
  public String getMention() {
    if (ConceptMentionCandidate_Type.featOkTst && ((ConceptMentionCandidate_Type)jcasType).casFeat_mention == null)
      jcasType.jcas.throwFeatMissing("mention", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMentionCandidate");
    return jcasType.ll_cas.ll_getStringValue(addr, ((ConceptMentionCandidate_Type)jcasType).casFeatCode_mention);}
    
  /** setter for mention - sets  
   *
   * @param v value to set into the feature 
   */
  public void setMention(String v) {
    if (ConceptMentionCandidate_Type.featOkTst && ((ConceptMentionCandidate_Type)jcasType).casFeat_mention == null)
      jcasType.jcas.throwFeatMissing("mention", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMentionCandidate");
    jcasType.ll_cas.ll_setStringValue(addr, ((ConceptMentionCandidate_Type)jcasType).casFeatCode_mention, v);}    
  }

    