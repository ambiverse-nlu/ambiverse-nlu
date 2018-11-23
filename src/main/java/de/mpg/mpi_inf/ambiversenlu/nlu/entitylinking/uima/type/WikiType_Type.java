
/* First created by JCasGen Tue Jun 27 16:45:15 CEST 2017 */
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
 * Updated by JCasGen Tue Jun 27 16:45:15 CEST 2017
 *  */
public class WikiType_Type extends FSList_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (WikiType_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = WikiType_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new WikiType(addr, WikiType_Type.this);
  			   WikiType_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new WikiType(addr, WikiType_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = WikiType.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.WikiType");
 

  final Feature casFeat_id;

  final int     casFeatCode_id;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getId(int addr) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.WikiType");
    return ll_cas.ll_getIntValue(addr, casFeatCode_id);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setId(int addr, int v) {
        if (featOkTst && casFeat_id == null)
      jcas.throwFeatMissing("id", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.WikiType");
    ll_cas.ll_setIntValue(addr, casFeatCode_id, v);}
    
  
 

  final Feature casFeat_name;

  final int     casFeatCode_name;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getName(int addr) {
        if (featOkTst && casFeat_name == null)
      jcas.throwFeatMissing("name", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.WikiType");
    return ll_cas.ll_getStringValue(addr, casFeatCode_name);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setName(int addr, String v) {
        if (featOkTst && casFeat_name == null)
      jcas.throwFeatMissing("name", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.WikiType");
    ll_cas.ll_setStringValue(addr, casFeatCode_name, v);}
    
  
 

  final Feature casFeat_knowledgebase;

  final int     casFeatCode_knowledgebase;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getKnowledgebase(int addr) {
        if (featOkTst && casFeat_knowledgebase == null)
      jcas.throwFeatMissing("knowledgebase", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.WikiType");
    return ll_cas.ll_getStringValue(addr, casFeatCode_knowledgebase);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setKnowledgebase(int addr, String v) {
        if (featOkTst && casFeat_knowledgebase == null)
      jcas.throwFeatMissing("knowledgebase", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.WikiType");
    ll_cas.ll_setStringValue(addr, casFeatCode_knowledgebase, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public WikiType_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_id = jcas.getRequiredFeatureDE(casType, "id", "uima.cas.Integer", featOkTst);
    casFeatCode_id  = (null == casFeat_id) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_id).getCode();

 
    casFeat_name = jcas.getRequiredFeatureDE(casType, "name", "uima.cas.String", featOkTst);
    casFeatCode_name  = (null == casFeat_name) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_name).getCode();

 
    casFeat_knowledgebase = jcas.getRequiredFeatureDE(casType, "knowledgebase", "uima.cas.String", featOkTst);
    casFeatCode_knowledgebase  = (null == casFeat_knowledgebase) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_knowledgebase).getCode();

  }
}



    