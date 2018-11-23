
/* First created by JCasGen Tue Sep 05 13:31:28 CEST 2017 */
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
 * Updated by JCasGen Tue Sep 05 13:49:44 CEST 2017
 *  */
public class ConstituentEntity_Type extends Annotation_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (ConstituentEntity_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = ConstituentEntity_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new ConstituentEntity(addr, ConstituentEntity_Type.this);
  			   ConstituentEntity_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new ConstituentEntity(addr, ConstituentEntity_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = ConstituentEntity.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConstituentEntity");
 

  final Feature casFeat_wikidataID;

  final int     casFeatCode_wikidataID;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getWikidataID(int addr) {
        if (featOkTst && casFeat_wikidataID == null)
      jcas.throwFeatMissing("wikidataID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConstituentEntity");
    return ll_cas.ll_getStringValue(addr, casFeatCode_wikidataID);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setWikidataID(int addr, String v) {
        if (featOkTst && casFeat_wikidataID == null)
      jcas.throwFeatMissing("wikidataID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConstituentEntity");
    ll_cas.ll_setStringValue(addr, casFeatCode_wikidataID, v);}
    
  
 

  final Feature casFeat_entityID;

  final int     casFeatCode_entityID;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getEntityID(int addr) {
        if (featOkTst && casFeat_entityID == null)
      jcas.throwFeatMissing("entityID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConstituentEntity");
    return ll_cas.ll_getStringValue(addr, casFeatCode_entityID);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setEntityID(int addr, String v) {
        if (featOkTst && casFeat_entityID == null)
      jcas.throwFeatMissing("entityID", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConstituentEntity");
    ll_cas.ll_setStringValue(addr, casFeatCode_entityID, v);}
    
  
 

  final Feature casFeat_mention;

  final int     casFeatCode_mention;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getMention(int addr) {
        if (featOkTst && casFeat_mention == null)
      jcas.throwFeatMissing("mention", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConstituentEntity");
    return ll_cas.ll_getStringValue(addr, casFeatCode_mention);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setMention(int addr, String v) {
        if (featOkTst && casFeat_mention == null)
      jcas.throwFeatMissing("mention", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ConstituentEntity");
    ll_cas.ll_setStringValue(addr, casFeatCode_mention, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public ConstituentEntity_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_wikidataID = jcas.getRequiredFeatureDE(casType, "wikidataID", "uima.cas.String", featOkTst);
    casFeatCode_wikidataID  = (null == casFeat_wikidataID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_wikidataID).getCode();

 
    casFeat_entityID = jcas.getRequiredFeatureDE(casType, "entityID", "uima.cas.String", featOkTst);
    casFeatCode_entityID  = (null == casFeat_entityID) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_entityID).getCode();

 
    casFeat_mention = jcas.getRequiredFeatureDE(casType, "mention", "uima.cas.String", featOkTst);
    casFeatCode_mention  = (null == casFeat_mention) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_mention).getCode();

  }
}



    