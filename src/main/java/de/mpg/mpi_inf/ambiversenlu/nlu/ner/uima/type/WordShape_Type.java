
/* First created by JCasGen Tue Mar 01 11:16:33 CET 2016 */
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
public class WordShape_Type extends Annotation_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator =
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (WordShape_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = WordShape_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new WordShape(addr, WordShape_Type.this);
  			   WordShape_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new WordShape(addr, WordShape_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = WordShape.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("WordShape");
 

  final Feature casFeat_wordShape;

  final int     casFeatCode_wordShape;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getWordShape(int addr) {
        if (featOkTst && casFeat_wordShape == null)
      jcas.throwFeatMissing("wordShape", "WordShape");
    return ll_cas.ll_getStringValue(addr, casFeatCode_wordShape);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setWordShape(int addr, String v) {
        if (featOkTst && casFeat_wordShape == null)
      jcas.throwFeatMissing("wordShape", "WordShape");
    ll_cas.ll_setStringValue(addr, casFeatCode_wordShape, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public WordShape_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_wordShape = jcas.getRequiredFeatureDE(casType, "wordShape", "uima.cas.String", featOkTst);
    casFeatCode_wordShape  = (null == casFeat_wordShape) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_wordShape).getCode();

  }
}



    