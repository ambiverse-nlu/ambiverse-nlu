
/* First created by JCasGen Tue Mar 29 15:01:43 CEST 2016 */
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
 * Updated by JCasGen Wed Jun 22 11:00:00 CEST 2016
 *  */
public class NerMention_Type extends Annotation_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator =
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (NerMention_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = NerMention_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new NerMention(addr, NerMention_Type.this);
  			   NerMention_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new NerMention(addr, NerMention_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = NerMention.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("NerMention");
 

  final Feature casFeat_classType;

  final int     casFeatCode_classType;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getClassType(int addr) {
        if (featOkTst && casFeat_classType == null)
      jcas.throwFeatMissing("classType", "NerMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_classType);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setClassType(int addr, String v) {
        if (featOkTst && casFeat_classType == null)
      jcas.throwFeatMissing("classType", "NerMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_classType, v);}
    
  
 

  final Feature casFeat_bmeowTag;

  final int     casFeatCode_bmeowTag;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getBmeowTag(int addr) {
        if (featOkTst && casFeat_bmeowTag == null)
      jcas.throwFeatMissing("bmeowTag", "NerMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_bmeowTag);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setBmeowTag(int addr, String v) {
        if (featOkTst && casFeat_bmeowTag == null)
      jcas.throwFeatMissing("bmeowTag", "NerMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_bmeowTag, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public NerMention_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_classType = jcas.getRequiredFeatureDE(casType, "classType", "uima.cas.String", featOkTst);
    casFeatCode_classType  = (null == casFeat_classType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_classType).getCode();

 
    casFeat_bmeowTag = jcas.getRequiredFeatureDE(casType, "bmeowTag", "uima.cas.String", featOkTst);
    casFeatCode_bmeowTag  = (null == casFeat_bmeowTag) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_bmeowTag).getCode();

  }
}



    