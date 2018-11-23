
/* First created by JCasGen Thu Mar 03 11:42:04 CET 2016 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type;

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
 * Updated by JCasGen Fri Apr 01 10:54:35 CEST 2016
 *  */
public class AidaMention_Type extends Annotation_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator =
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (AidaMention_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = AidaMention_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new AidaMention(addr, AidaMention_Type.this);
  			   AidaMention_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new AidaMention(addr, AidaMention_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = AidaMention.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("AidaMention");
 

  final Feature casFeat_internalId;

  final int     casFeatCode_internalId;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getInternalId(int addr) {
        if (featOkTst && casFeat_internalId == null)
      jcas.throwFeatMissing("internalId", "AidaMention");
    return ll_cas.ll_getIntValue(addr, casFeatCode_internalId);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setInternalId(int addr, int v) {
        if (featOkTst && casFeat_internalId == null)
      jcas.throwFeatMissing("internalId", "AidaMention");
    ll_cas.ll_setIntValue(addr, casFeatCode_internalId, v);}
    
  
 

  final Feature casFeat_kbIdentifiedId;

  final int     casFeatCode_kbIdentifiedId;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getKbIdentifiedId(int addr) {
        if (featOkTst && casFeat_kbIdentifiedId == null)
      jcas.throwFeatMissing("kbIdentifiedId", "AidaMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_kbIdentifiedId);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setKbIdentifiedId(int addr, String v) {
        if (featOkTst && casFeat_kbIdentifiedId == null)
      jcas.throwFeatMissing("kbIdentifiedId", "AidaMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_kbIdentifiedId, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public AidaMention_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_internalId = jcas.getRequiredFeatureDE(casType, "internalId", "uima.cas.Integer", featOkTst);
    casFeatCode_internalId  = (null == casFeat_internalId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_internalId).getCode();

 
    casFeat_kbIdentifiedId = jcas.getRequiredFeatureDE(casType, "kbIdentifiedId", "uima.cas.String", featOkTst);
    casFeatCode_kbIdentifiedId  = (null == casFeat_kbIdentifiedId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_kbIdentifiedId).getCode();

  }
}



    