

/* First created by JCasGen Fri Dec 09 14:05:38 CET 2016 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;



public class DictionaryFeatureAnnotation extends Annotation {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(DictionaryFeatureAnnotation.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected DictionaryFeatureAnnotation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *  */
  public DictionaryFeatureAnnotation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  

  public DictionaryFeatureAnnotation(JCas jcas) {
    super(jcas);
    readObject();   
  } 


  public DictionaryFeatureAnnotation(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** <!-- begin-user-doc -->
    * Write your own initialization here
    * <!-- end-user-doc -->
   modifiable */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: dictionary

  /** getter for dictionary - gets 
   *  */
  public String getDictionary() {
    if (DictionaryFeatureAnnotation_Type.featOkTst && ((DictionaryFeatureAnnotation_Type)jcasType).casFeat_dictionary == null)
      jcasType.jcas.throwFeatMissing("dictionary", "DictionaryFeatureAnnotation");
    return jcasType.ll_cas.ll_getStringValue(addr, ((DictionaryFeatureAnnotation_Type)jcasType).casFeatCode_dictionary);}
    
  /** setter for dictionary - sets  
   *  */
  public void setDictionary(String v) {
    if (DictionaryFeatureAnnotation_Type.featOkTst && ((DictionaryFeatureAnnotation_Type)jcasType).casFeat_dictionary == null)
      jcasType.jcas.throwFeatMissing("dictionary", "DictionaryFeatureAnnotation");
    jcasType.ll_cas.ll_setStringValue(addr, ((DictionaryFeatureAnnotation_Type)jcasType).casFeatCode_dictionary, v);}    
  }

    