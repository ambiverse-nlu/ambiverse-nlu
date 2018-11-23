
/* First created by JCasGen Mon Jul 24 18:10:26 CEST 2017 */
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
 * Updated by JCasGen Mon Jul 24 18:10:26 CEST 2017
 *  */
public class ConceptMention_Type extends Annotation_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ConceptMention_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = ConceptMention_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ConceptMention(addr, ConceptMention_Type.this);
  			   ConceptMention_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new ConceptMention(addr, ConceptMention_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ConceptMention.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMention");
 

  final Feature casFeat_concept;

  final int     casFeatCode_concept;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getConcept(int addr) {
        if (featOkTst && casFeat_concept == null)
      jcas.throwFeatMissing("concept", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_concept);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setConcept(int addr, String v) {
        if (featOkTst && casFeat_concept == null)
      jcas.throwFeatMissing("concept", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_concept, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ConceptMention_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_concept = jcas.getRequiredFeatureDE(casType, "concept", "uima.cas.String", featOkTst);
    casFeatCode_concept  = (null == casFeat_concept) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_concept).getCode();

  }
}



    