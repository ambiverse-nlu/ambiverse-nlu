
/* First created by JCasGen Tue Mar 14 16:13:24 CET 2017 */
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
import org.apache.uima.jcas.cas.FSList_Type;

/** 
 * Updated by JCasGen Tue Mar 14 16:13:47 CET 2017
 *  */
public class Domain_Type extends FSList_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Domain_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Domain_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Domain(addr, Domain_Type.this);
  			   Domain_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Domain(addr, Domain_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Domain.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Domain");




  final Feature casFeat_domain;

  final int     casFeatCode_domain;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getDomain(int addr) {
        if (featOkTst && casFeat_domain == null)
      jcas.throwFeatMissing("domain", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Domain");
    return ll_cas.ll_getStringValue(addr, casFeatCode_domain);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDomain(int addr, String v) {
        if (featOkTst && casFeat_domain == null)
      jcas.throwFeatMissing("domain", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Domain");
    ll_cas.ll_setStringValue(addr, casFeatCode_domain, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Domain_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_domain = jcas.getRequiredFeatureDE(casType, "domain", "uima.cas.String", featOkTst);
    casFeatCode_domain  = (null == casFeat_domain) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_domain).getCode();

  }
}



    