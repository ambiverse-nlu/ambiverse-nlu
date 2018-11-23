
/* First created by JCasGen Mon Nov 13 15:03:39 CET 2017 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/**
 * Updated by JCasGen Mon Nov 20 16:37:00 CET 2017
 *  */
public class SalientEntity_Type extends Entity_Type {

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = SalientEntity.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.SalientEntity");


  final Feature casFeat_label;

  final int     casFeatCode_label;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value
   */
  public double getLabel(int addr) {
    if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.SalientEntity");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_label);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set
   */
  public void setLabel(int addr, double v) {
    if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.SalientEntity");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_label, v);}




  final Feature casFeat_salience;

  final int     casFeatCode_salience;
  /**
   * @param addr low level Feature Structure reference
   * @return the feature value
   */
  public double getSalience(int addr) {
    if (featOkTst && casFeat_salience == null)
      jcas.throwFeatMissing("salience", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.SalientEntity");
    return ll_cas.ll_getDoubleValue(addr, casFeatCode_salience);
  }
  /**
   * @param addr low level Feature Structure reference
   * @param v value to set
   */
  public void setSalience(int addr, double v) {
    if (featOkTst && casFeat_salience == null)
      jcas.throwFeatMissing("salience", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.SalientEntity");
    ll_cas.ll_setDoubleValue(addr, casFeatCode_salience, v);}





  /** initialize variables to correspond with Cas Type and Features
   *
   * @param jcas JCas
   * @param casType Type
   */
  public SalientEntity_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());


    casFeat_label = jcas.getRequiredFeatureDE(casType, "label", "uima.cas.Double", featOkTst);
    casFeatCode_label  = (null == casFeat_label) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_label).getCode();


    casFeat_salience = jcas.getRequiredFeatureDE(casType, "salience", "uima.cas.Double", featOkTst);
    casFeatCode_salience  = (null == casFeat_salience) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_salience).getCode();

  }
}