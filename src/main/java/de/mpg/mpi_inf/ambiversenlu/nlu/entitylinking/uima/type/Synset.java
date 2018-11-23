

/* First created by JCasGen Wed Apr 05 12:19:37 CEST 2017 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.TOP_Type;



public class Synset extends FSList {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Synset.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected Synset() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Synset(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Synset(JCas jcas) {
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
  //* Feature: synset

  /** getter for synset - gets 
   *
   * @return value of the feature 
   */
  public String getSynset() {
    if (Synset_Type.featOkTst && ((Synset_Type)jcasType).casFeat_synset == null)
      jcasType.jcas.throwFeatMissing("synset", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Synset");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Synset_Type)jcasType).casFeatCode_synset);}
    
  /** setter for synset - sets  
   *
   * @param v value to set into the feature 
   */
  public void setSynset(String v) {
    if (Synset_Type.featOkTst && ((Synset_Type)jcasType).casFeat_synset == null)
      jcasType.jcas.throwFeatMissing("synset", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Synset");
    jcasType.ll_cas.ll_setStringValue(addr, ((Synset_Type)jcasType).casFeatCode_synset, v);}    
  }

    