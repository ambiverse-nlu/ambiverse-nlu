
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
public class PositionInEntity_Type extends Annotation_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator =
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (PositionInEntity_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = PositionInEntity_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new PositionInEntity(addr, PositionInEntity_Type.this);
  			   PositionInEntity_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new PositionInEntity(addr, PositionInEntity_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = PositionInEntity.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("PositionInEntity");


  final Feature casFeat_positionInEntity;

  final int     casFeatCode_positionInEntity;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value
   */
  public String getPositionInEntity(int addr) {
        if (featOkTst && casFeat_positionInEntity == null)
      jcas.throwFeatMissing("positionInEntity", "PositionInEntity");
    return ll_cas.ll_getStringValue(addr, casFeatCode_positionInEntity);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set
   */
  public void setPositionInEntity(int addr, String v) {
        if (featOkTst && casFeat_positionInEntity == null)
      jcas.throwFeatMissing("positionInEntity", "PositionInEntity");
    ll_cas.ll_setStringValue(addr, casFeatCode_positionInEntity, v);}





  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type
	 */
  public PositionInEntity_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_positionInEntity = jcas.getRequiredFeatureDE(casType, "positionInEntity", "uima.cas.String", featOkTst);
    casFeatCode_positionInEntity  = (null == casFeat_positionInEntity) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_positionInEntity).getCode();

  }
}



    