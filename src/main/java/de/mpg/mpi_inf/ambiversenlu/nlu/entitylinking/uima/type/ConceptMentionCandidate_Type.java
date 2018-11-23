
/* First created by JCasGen Mon Jul 24 18:01:49 CEST 2017 */
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
 * Updated by JCasGen Mon Jul 24 18:02:18 CEST 2017
 *  */
public class ConceptMentionCandidate_Type extends Annotation_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ConceptMentionCandidate_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = ConceptMentionCandidate_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ConceptMentionCandidate(addr, ConceptMentionCandidate_Type.this);
  			   ConceptMentionCandidate_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new ConceptMentionCandidate(addr, ConceptMentionCandidate_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ConceptMentionCandidate.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMentionCandidate");
 

  final Feature casFeat_conceptCandidate;

  final int     casFeatCode_conceptCandidate;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getConceptCandidate(int addr) {
        if (featOkTst && casFeat_conceptCandidate == null)
      jcas.throwFeatMissing("conceptCandidate", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMentionCandidate");
    return ll_cas.ll_getStringValue(addr, casFeatCode_conceptCandidate);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setConceptCandidate(int addr, String v) {
        if (featOkTst && casFeat_conceptCandidate == null)
      jcas.throwFeatMissing("conceptCandidate", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMentionCandidate");
    ll_cas.ll_setStringValue(addr, casFeatCode_conceptCandidate, v);}
    
  
 

  final Feature casFeat_mention;

  final int     casFeatCode_mention;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getMention(int addr) {
        if (featOkTst && casFeat_mention == null)
      jcas.throwFeatMissing("mention", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMentionCandidate");
    return ll_cas.ll_getStringValue(addr, casFeatCode_mention);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setMention(int addr, String v) {
        if (featOkTst && casFeat_mention == null)
      jcas.throwFeatMissing("mention", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConceptMentionCandidate");
    ll_cas.ll_setStringValue(addr, casFeatCode_mention, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ConceptMentionCandidate_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_conceptCandidate = jcas.getRequiredFeatureDE(casType, "conceptCandidate", "uima.cas.String", featOkTst);
    casFeatCode_conceptCandidate  = (null == casFeat_conceptCandidate) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_conceptCandidate).getCode();

 
    casFeat_mention = jcas.getRequiredFeatureDE(casType, "mention", "uima.cas.String", featOkTst);
    casFeatCode_mention  = (null == casFeat_mention) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_mention).getCode();

  }
}



    