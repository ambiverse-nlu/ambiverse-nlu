

/* First created by JCasGen Mon Nov 13 15:03:39 CET 2017 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;



/**
 * Updated by JCasGen Mon Nov 20 16:37:00 CET 2017
 * XML source: /Users/dmilchev/eclipse-workspace/NYTArticleMetaDataAnnotator/desc/aidaTypeSystemDescriptor.xml
 *  */
public class SalientEntity extends Entity {
    @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(SalientEntity.class);
    @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /**
   * @return index of the type
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}

  /** Never called.  Disable default constructor
   *  */
  protected SalientEntity() {/* intentionally empty block */}

  /** Internal - constructor used by generator
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure
   */
  public SalientEntity(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }

  /**
   * @param jcas JCas to which this Feature Structure belongs
   */
  public SalientEntity(JCas jcas) {
    super(jcas);
    readObject();
  }

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA
   */
  public SalientEntity(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }

  /**
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   *  modifiable
   */
  private void readObject() {/*default - does nothing empty block */}



  //*--------------*
  //* Feature: label

  /** getter for label - gets
   *
   * @return value of the feature
   */
  public double getLabel() {
    if (SalientEntity_Type.featOkTst && ((SalientEntity_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.SalientEntity");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((SalientEntity_Type)jcasType).casFeatCode_label);}

  /** setter for label - sets
   *
   * @param v value to set into the feature
   */
  public void setLabel(double v) {
    if (SalientEntity_Type.featOkTst && ((SalientEntity_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.SalientEntity");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((SalientEntity_Type)jcasType).casFeatCode_label, v);}


  //*--------------*
  //* Feature: salience

  /** getter for salience - gets
   *
   * @return value of the feature
   */
  public double getSalience() {
    if (SalientEntity_Type.featOkTst && ((SalientEntity_Type)jcasType).casFeat_salience == null)
      jcasType.jcas.throwFeatMissing("salience", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.SalientEntity");
    return jcasType.ll_cas.ll_getDoubleValue(addr, ((SalientEntity_Type)jcasType).casFeatCode_salience);}

  /** setter for salience - sets
   *
   * @param v value to set into the feature
   */
  public void setSalience(double v) {
    if (SalientEntity_Type.featOkTst && ((SalientEntity_Type)jcasType).casFeat_salience == null)
      jcasType.jcas.throwFeatMissing("salience", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.SalientEntity");
    jcasType.ll_cas.ll_setDoubleValue(addr, ((SalientEntity_Type)jcasType).casFeatCode_salience, v);}
}