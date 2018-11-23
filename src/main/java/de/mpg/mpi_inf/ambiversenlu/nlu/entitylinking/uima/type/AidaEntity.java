/* First created by JCasGen Fri Aug 14 20:59:14 CEST 2015 */
package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

/** Entity Annotated by AIDA in a document.
 * Updated by JCasGen Fri Feb 02 15:57:02 CET 2018
 * XML source: /GW/ambiverse/work/students/ghazaleh/entity_linking_eval/entity-linking/src/main/resources/uima/type/aidaTypeSystemDescriptor.xml
 *  */
public class AidaEntity extends Entity {

    @SuppressWarnings("hiding") public final static int typeIndexID = JCasRegistry.register(AidaEntity.class);

    @SuppressWarnings("hiding") public final static int type = typeIndexID;

  /**
   * @return index of the type  
   */
  @Override public int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   *  */
  protected AidaEntity() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   *
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public AidaEntity(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /**
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public AidaEntity(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /**
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
   */
  public AidaEntity(JCas jcas, int begin, int end) {
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

}

    