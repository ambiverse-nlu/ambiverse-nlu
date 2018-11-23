
/* First created by JCasGen Sat Jun 18 18:05:50 CEST 2016 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Wed Jun 22 11:00:00 CEST 2016
 *  */
public class UiucKnownState_Type extends Annotation_Type {
  /**
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}

  private final FSGenerator fsGenerator =
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (UiucKnownState_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = UiucKnownState_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new UiucKnownState(addr, UiucKnownState_Type.this);
  			   UiucKnownState_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new UiucKnownState(addr, UiucKnownState_Type.this);
  	  }
    };

  @SuppressWarnings ("hiding")
  public final static int typeIndexID = UiucKnownState.typeIndexID;
    @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("UiucKnownState");



  /** initialize variables to correspond with Cas Type and Features
	 *
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public UiucKnownState_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

  }
}



    