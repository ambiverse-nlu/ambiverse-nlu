
/* First created by JCasGen Fri Feb 02 17:15:34 CET 2018 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Fri Feb 02 17:15:34 CET 2018
 *  */
public class Concept_Type extends Annotation_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Concept_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Concept_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Concept(addr, Concept_Type.this);
  			   Concept_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Concept(addr, Concept_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Concept.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
 

  final Feature casFeat_score;

  final int     casFeatCode_score;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public double getScore(int addr) {
        if (featOkTst && casFeat_score == null)
      jcas.throwFeatMissing("score", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_score);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setScore(int addr, double v) {
        if (featOkTst && casFeat_score == null)
      jcas.throwFeatMissing("score", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_score, v);}
    
  
 

  final Feature casFeat_ID;

  final int     casFeatCode_ID;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getID(int addr) {
        if (featOkTst && casFeat_ID == null)
      jcas.throwFeatMissing("ID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_ID);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setID(int addr, String v) {
        if (featOkTst && casFeat_ID == null)
      jcas.throwFeatMissing("ID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    ll_cas.ll_setStringValue(addr, casFeatCode_ID, v);}
    
  
 

  final Feature casFeat_types;

  final int     casFeatCode_types;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getTypes(int addr) {
        if (featOkTst && casFeat_types == null)
      jcas.throwFeatMissing("types", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    return ll_cas.ll_getRefValue(addr, casFeatCode_types);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setTypes(int addr, int v) {
        if (featOkTst && casFeat_types == null)
      jcas.throwFeatMissing("types", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    ll_cas.ll_setRefValue(addr, casFeatCode_types, v);}
    
   /**
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public String getTypes(int addr, int i) {
        if (featOkTst && casFeat_types == null)
      jcas.throwFeatMissing("types", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    if (lowLevelTypeChecks)
      return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_types), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_types), i);
	return ll_cas.ll_getStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_types), i);
  }
   
  /**
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */ 
  public void setTypes(int addr, int i, String v) {
        if (featOkTst && casFeat_types == null)
      jcas.throwFeatMissing("types", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Concept");
    if (lowLevelTypeChecks)
      ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_types), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_types), i);
    ll_cas.ll_setStringArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_types), i, v);
  }
 



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Concept_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_score = jcas.getRequiredFeatureDE(casType, "score", "uima.cas.Double", featOkTst);
    casFeatCode_score  = (null == casFeat_score) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_score).getCode();

 
    casFeat_ID = jcas.getRequiredFeatureDE(casType, "ID", "uima.cas.String", featOkTst);
    casFeatCode_ID  = (null == casFeat_ID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_ID).getCode();

 
    casFeat_types = jcas.getRequiredFeatureDE(casType, "types", "uima.cas.StringArray", featOkTst);
    casFeatCode_types  = (null == casFeat_types) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_types).getCode();

  }
}



    