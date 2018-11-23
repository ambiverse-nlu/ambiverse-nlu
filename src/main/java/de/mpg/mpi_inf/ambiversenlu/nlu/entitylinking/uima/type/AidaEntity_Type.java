/* First created by JCasGen Fri Aug 14 20:59:14 CEST 2015 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;

/** Entity Annotated by AIDA in a document.
 * Updated by JCasGen Fri Feb 02 15:57:02 CET 2018
 *  */
public class AidaEntity_Type extends Entity_Type {

  /**
   * @return the generator for this type
   */
  @Override protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (AidaEntity_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = AidaEntity_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new AidaEntity(addr, AidaEntity_Type.this);
  			   AidaEntity_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new AidaEntity(addr, AidaEntity_Type.this);
  	  }
    };


  @SuppressWarnings("hiding") public final static int typeIndexID = AidaEntity.typeIndexID;

    @SuppressWarnings("hiding") public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.AidaEntity");

  /** initialize variables to correspond with Cas Type and Features
   *
   * @param jcas JCas
   * @param casType Type
   */
  public AidaEntity_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    