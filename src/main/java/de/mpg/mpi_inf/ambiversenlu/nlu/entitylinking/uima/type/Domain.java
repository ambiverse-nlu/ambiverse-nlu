

/* First created by JCasGen Tue Mar 14 16:13:24 CET 2017 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.TOP_Type;



public class Domain extends FSList {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Domain.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected Domain() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Domain(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Domain(JCas jcas) {
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
  //* Feature: domain

  /** getter for domain - gets 
   *
   * @return value of the feature 
   */
  public String getDomain() {
    if (Domain_Type.featOkTst && ((Domain_Type)jcasType).casFeat_domain == null)
      jcasType.jcas.throwFeatMissing("domain", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Domain");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Domain_Type)jcasType).casFeatCode_domain);}
    
  /** setter for domain - sets  
   *
   * @param v value to set into the feature 
   */
  public void setDomain(String v) {
    if (Domain_Type.featOkTst && ((Domain_Type)jcasType).casFeat_domain == null)
      jcasType.jcas.throwFeatMissing("domain", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Domain");
    jcasType.ll_cas.ll_setStringValue(addr, ((Domain_Type)jcasType).casFeatCode_domain, v);}    
  }

    