package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

/* First created by JCasGen Wed Sep 07 11:46:10 CEST 2016 */

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.ByteArray;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Updated by JCasGen Wed Jan 11 14:25:32 CET 2017
 * XML source: /Users/corrogg/IdeaProjects/entity-linking/src/main/resources/uima/type/AidaSettingsTypeSystemDescriptor.xml
 *  */
public class AidaDisambiguationSettings extends Annotation {

    @SuppressWarnings("hiding") public final static int typeIndexID = JCasRegistry.register(AidaDisambiguationSettings.class);

    @SuppressWarnings("hiding") public final static int type = typeIndexID;

  /**
   * @return index of the type  
   */
  @Override public int getTypeIndexID() {
    return typeIndexID;
  }

  /** Never called.  Disable default constructor
   *  */
  protected AidaDisambiguationSettings() {/* intentionally empty block */}

  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public AidaDisambiguationSettings(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }

  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public AidaDisambiguationSettings(JCas jcas) {
    super(jcas);
    readObject();
  }

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
   */
  public AidaDisambiguationSettings(JCas jcas, int begin, int end) {
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
  //* Feature: disambiguationSettingsBytes

  /** getter for disambiguationSettingsBytes - gets 
   *
   * @return value of the feature 
   */
  public ByteArray getDisambiguationSettingsBytes() {
    if (AidaDisambiguationSettings_Type.featOkTst && ((AidaDisambiguationSettings_Type) jcasType).casFeat_disambiguationSettingsBytes == null)
      jcasType.jcas.throwFeatMissing("disambiguationSettingsBytes", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDisambiguationSettings");
    return (ByteArray) (jcasType.ll_cas
        .ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((AidaDisambiguationSettings_Type) jcasType).casFeatCode_disambiguationSettingsBytes)));
  }

  /** setter for disambiguationSettingsBytes - sets  
   *
   * @param v value to set into the feature 
   */
  public void setDisambiguationSettingsBytes(ByteArray v) {
    if (AidaDisambiguationSettings_Type.featOkTst && ((AidaDisambiguationSettings_Type) jcasType).casFeat_disambiguationSettingsBytes == null)
      jcasType.jcas.throwFeatMissing("disambiguationSettingsBytes", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDisambiguationSettings");
    jcasType.ll_cas
        .ll_setRefValue(addr, ((AidaDisambiguationSettings_Type) jcasType).casFeatCode_disambiguationSettingsBytes, jcasType.ll_cas.ll_getFSRef(v));
  }

  /** indexed getter for disambiguationSettingsBytes - gets an indexed value - 
   *
   * @param i index in the array to get
   * @return value of the element at index i 
   */
  public byte getDisambiguationSettingsBytes(int i) {
    if (AidaDisambiguationSettings_Type.featOkTst && ((AidaDisambiguationSettings_Type) jcasType).casFeat_disambiguationSettingsBytes == null)
      jcasType.jcas.throwFeatMissing("disambiguationSettingsBytes", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDisambiguationSettings");
    jcasType.jcas
        .checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((AidaDisambiguationSettings_Type) jcasType).casFeatCode_disambiguationSettingsBytes),
            i);
    return jcasType.ll_cas.ll_getByteArrayValue(
        jcasType.ll_cas.ll_getRefValue(addr, ((AidaDisambiguationSettings_Type) jcasType).casFeatCode_disambiguationSettingsBytes), i);
  }

  /** indexed setter for disambiguationSettingsBytes - sets an indexed value - 
   *
   * @param i index in the array to set
   * @param v value to set into the array 
   */
  public void setDisambiguationSettingsBytes(int i, byte v) {
    if (AidaDisambiguationSettings_Type.featOkTst && ((AidaDisambiguationSettings_Type) jcasType).casFeat_disambiguationSettingsBytes == null)
      jcasType.jcas.throwFeatMissing("disambiguationSettingsBytes", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDisambiguationSettings");
    jcasType.jcas
        .checkArrayBounds(jcasType.ll_cas.ll_getRefValue(addr, ((AidaDisambiguationSettings_Type) jcasType).casFeatCode_disambiguationSettingsBytes),
            i);
    jcasType.ll_cas.ll_setByteArrayValue(
        jcasType.ll_cas.ll_getRefValue(addr, ((AidaDisambiguationSettings_Type) jcasType).casFeatCode_disambiguationSettingsBytes), i, v);
  }
}
    