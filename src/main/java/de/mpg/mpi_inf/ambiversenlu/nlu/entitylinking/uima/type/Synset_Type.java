
/* First created by JCasGen Wed Apr 05 12:19:37 CEST 2017 */
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
 * Updated by JCasGen Wed Apr 05 12:19:37 CEST 2017
 *  */
public class Synset_Type extends FSList_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (Synset_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = Synset_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new Synset(addr, Synset_Type.this);
  			   Synset_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new Synset(addr, Synset_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Synset.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Synset");
 

  final Feature casFeat_synset;

  final int     casFeatCode_synset;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSynset(int addr) {
        if (featOkTst && casFeat_synset == null)
      jcas.throwFeatMissing("synset", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Synset");
    return ll_cas.ll_getStringValue(addr, casFeatCode_synset);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSynset(int addr, String v) {
        if (featOkTst && casFeat_synset == null)
      jcas.throwFeatMissing("synset", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Synset");
    ll_cas.ll_setStringValue(addr, casFeatCode_synset, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Synset_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_synset = jcas.getRequiredFeatureDE(casType, "synset", "uima.cas.String", featOkTst);
    casFeatCode_synset  = (null == casFeat_synset) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_synset).getCode();

  }
}



    