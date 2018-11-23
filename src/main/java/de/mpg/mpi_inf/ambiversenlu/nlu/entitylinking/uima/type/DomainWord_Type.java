
/* First created by JCasGen Fri Jun 23 15:02:48 CEST 2017 */
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
 * Updated by JCasGen Fri Jun 23 15:04:39 CEST 2017
 *  */
public class DomainWord_Type extends FSList_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DomainWord_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DomainWord_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DomainWord(addr, DomainWord_Type.this);
  			   DomainWord_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DomainWord(addr, DomainWord_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = DomainWord.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.DomainWord");




  final Feature casFeat_domainWord;

  final int     casFeatCode_domainWord;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getDomainWord(int addr) {
        if (featOkTst && casFeat_domainWord == null)
      jcas.throwFeatMissing("domainWord", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.DomainWord");
    return ll_cas.ll_getStringValue(addr, casFeatCode_domainWord);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setDomainWord(int addr, String v) {
        if (featOkTst && casFeat_domainWord == null)
      jcas.throwFeatMissing("domainWord", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.DomainWord");
    ll_cas.ll_setStringValue(addr, casFeatCode_domainWord, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public DomainWord_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_domainWord = jcas.getRequiredFeatureDE(casType, "domainWord", "uima.cas.String", featOkTst);
    casFeatCode_domainWord  = (null == casFeat_domainWord) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_domainWord).getCode();

  }
}



    