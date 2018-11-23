
/* First created by JCasGen Fri Dec 09 14:05:38 CET 2016 */
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
 * Updated by JCasGen Fri Dec 09 14:05:38 CET 2016
 *  */
public class DictionaryFeatureAnnotation_Type extends Annotation_Type {

  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator =
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (DictionaryFeatureAnnotation_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = DictionaryFeatureAnnotation_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new DictionaryFeatureAnnotation(addr, DictionaryFeatureAnnotation_Type.this);
  			   DictionaryFeatureAnnotation_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new DictionaryFeatureAnnotation(addr, DictionaryFeatureAnnotation_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = DictionaryFeatureAnnotation.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("DictionaryFeatureAnnotation");
 

  final Feature casFeat_dictionary;

  final int     casFeatCode_dictionary;

  public String getDictionary(int addr) {
        if (featOkTst && casFeat_dictionary == null)
      jcas.throwFeatMissing("dictionary", "DictionaryFeatureAnnotation");
    return ll_cas.ll_getStringValue(addr, casFeatCode_dictionary);
  }

  public void setDictionary(int addr, String v) {
        if (featOkTst && casFeat_dictionary == null)
      jcas.throwFeatMissing("dictionary", "DictionaryFeatureAnnotation");
    ll_cas.ll_setStringValue(addr, casFeatCode_dictionary, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	*  */
  public DictionaryFeatureAnnotation_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_dictionary = jcas.getRequiredFeatureDE(casType, "dictionary", "uima.cas.String", featOkTst);
    casFeatCode_dictionary  = (null == casFeat_dictionary) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_dictionary).getCode();

  }
}



    