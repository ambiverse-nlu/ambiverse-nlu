
/* First created by JCasGen Mon Mar 07 16:24:05 CET 2016 */
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
public class BmeowType_Type extends Annotation_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator =
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (BmeowType_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = BmeowType_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new BmeowType(addr, BmeowType_Type.this);
  			   BmeowType_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new BmeowType(addr, BmeowType_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = BmeowType.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("BmeowType");
 

  final Feature casFeat_bmeowType;

  final int     casFeatCode_bmeowType;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getBmeowType(int addr) {
        if (featOkTst && casFeat_bmeowType == null)
      jcas.throwFeatMissing("bmeowType", "BmeowType");
    return ll_cas.ll_getStringValue(addr, casFeatCode_bmeowType);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setBmeowType(int addr, String v) {
        if (featOkTst && casFeat_bmeowType == null)
      jcas.throwFeatMissing("bmeowType", "BmeowType");
    ll_cas.ll_setStringValue(addr, casFeatCode_bmeowType, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public BmeowType_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_bmeowType = jcas.getRequiredFeatureDE(casType, "bmeowType", "uima.cas.String", featOkTst);
    casFeatCode_bmeowType  = (null == casFeat_bmeowType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_bmeowType).getCode();

  }
}



    