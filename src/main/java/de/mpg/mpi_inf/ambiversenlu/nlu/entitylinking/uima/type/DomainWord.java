

/* First created by JCasGen Fri Jun 23 15:02:48 CEST 2017 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.TOP_Type;



public class DomainWord extends FSList {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DomainWord.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected DomainWord() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public DomainWord(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public DomainWord(JCas jcas) {
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
  //* Feature: domainWord

  /** getter for domainWord - gets 
   *
   * @return value of the feature 
   */
  public String getDomainWord() {
    if (DomainWord_Type.featOkTst && ((DomainWord_Type)jcasType).casFeat_domainWord == null)
      jcasType.jcas.throwFeatMissing("domainWord", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.DomainWord");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DomainWord_Type)jcasType).casFeatCode_domainWord);}
    
  /** setter for domainWord - sets  
   *
   * @param v value to set into the feature 
   */
  public void setDomainWord(String v) {
    if (DomainWord_Type.featOkTst && ((DomainWord_Type)jcasType).casFeat_domainWord == null)
      jcasType.jcas.throwFeatMissing("domainWord", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.DomainWord");
    jcasType.ll_cas.ll_setStringValue(addr, ((DomainWord_Type)jcasType).casFeatCode_domainWord, v);}    
  }

    