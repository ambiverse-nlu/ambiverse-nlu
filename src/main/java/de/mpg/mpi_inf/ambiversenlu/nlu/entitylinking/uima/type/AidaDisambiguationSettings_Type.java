package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;
/* First created by JCasGen Wed Sep 07 11:46:10 CEST 2016 */

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
 * Updated by JCasGen Wed Jan 11 14:25:32 CET 2017
 *  */
public class AidaDisambiguationSettings_Type extends Annotation_Type {

  /**
   * @return the generator for this type
   */
  @Override protected FSGenerator getFSGenerator() {
    return fsGenerator;
  }


  private final FSGenerator fsGenerator = new FSGenerator() {

    public FeatureStructure createFS(int addr, CASImpl cas) {
      if (AidaDisambiguationSettings_Type.this.useExistingInstance) {
        // Return eq fs instance if already created
        FeatureStructure fs = AidaDisambiguationSettings_Type.this.jcas.getJfsFromCaddr(addr);
        if (null == fs) {
          fs = new AidaDisambiguationSettings(addr, AidaDisambiguationSettings_Type.this);
          AidaDisambiguationSettings_Type.this.jcas.putJfsFromCaddr(addr, fs);
          return fs;
        }
        return fs;
      } else return new AidaDisambiguationSettings(addr, AidaDisambiguationSettings_Type.this);
    }
  };


  @SuppressWarnings("hiding") public final static int typeIndexID = AidaDisambiguationSettings.typeIndexID;

    @SuppressWarnings("hiding") public final static boolean featOkTst = JCasRegistry
      .getFeatOkTst("de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDisambiguationSettings");


  final Feature casFeat_disambiguationSettingsBytes;


  final int casFeatCode_disambiguationSettingsBytes;

  /**
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */
  public int getDisambiguationSettingsBytes(int addr) {
    if (featOkTst && casFeat_disambiguationSettingsBytes == null)
      jcas.throwFeatMissing("disambiguationSettingsBytes", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDisambiguationSettings");
    return ll_cas.ll_getRefValue(addr, casFeatCode_disambiguationSettingsBytes);
  }

  /**
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */
  public void setDisambiguationSettingsBytes(int addr, int v) {
    if (featOkTst && casFeat_disambiguationSettingsBytes == null)
      jcas.throwFeatMissing("disambiguationSettingsBytes", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDisambiguationSettings");
    ll_cas.ll_setRefValue(addr, casFeatCode_disambiguationSettingsBytes, v);
  }

  /**
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @return value at index i in the array 
   */
  public byte getDisambiguationSettingsBytes(int addr, int i) {
    if (featOkTst && casFeat_disambiguationSettingsBytes == null)
      jcas.throwFeatMissing("disambiguationSettingsBytes", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDisambiguationSettings");
    if (lowLevelTypeChecks) return ll_cas.ll_getByteArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_disambiguationSettingsBytes), i, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_disambiguationSettingsBytes), i);
    return ll_cas.ll_getByteArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_disambiguationSettingsBytes), i);
  }

  /**
   * @param addr low level Feature Structure reference
   * @param i index of item in the array
   * @param v value to set
   */
  public void setDisambiguationSettingsBytes(int addr, int i, byte v) {
    if (featOkTst && casFeat_disambiguationSettingsBytes == null)
      jcas.throwFeatMissing("disambiguationSettingsBytes", "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaDisambiguationSettings");
    if (lowLevelTypeChecks) ll_cas.ll_setByteArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_disambiguationSettingsBytes), i, v, true);
    jcas.checkArrayBounds(ll_cas.ll_getRefValue(addr, casFeatCode_disambiguationSettingsBytes), i);
    ll_cas.ll_setByteArrayValue(ll_cas.ll_getRefValue(addr, casFeatCode_disambiguationSettingsBytes), i, v);
  }

  /** initialize variables to correspond with Cas Type and Features
   *
   * @param jcas JCas
   * @param casType Type
   */
  public AidaDisambiguationSettings_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl) this.casType, getFSGenerator());

    casFeat_disambiguationSettingsBytes = jcas.getRequiredFeatureDE(casType, "disambiguationSettingsBytes", "uima.cas.ByteArray", featOkTst);
    casFeatCode_disambiguationSettingsBytes = (null == casFeat_disambiguationSettingsBytes) ?
        JCas.INVALID_FEATURE_CODE :
        ((FeatureImpl) casFeat_disambiguationSettingsBytes).getCode();

  }
}



    