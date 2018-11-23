package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ObjectF;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Relation;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Subject;
import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

@TypeCapability(outputs = {"de.tudarmstadt.ukp.dkpro.core.api.ner.type.Sentence",
    "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact" })

public class ReadFactTSV extends ResourceCollectionReaderBase {

  public static final String PARAM_LANGUAGE = "language";
  @ConfigurationParameter(
      name = "language",
      defaultValue = "en"
  )
  private String language;

  private static ConcurrentLinkedQueue<Fact> facts;

  @Override public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    facts = new ConcurrentLinkedQueue<>();
  }

  private void convert(CAS aCAS) throws IOException, CollectionException, AnalysisEngineProcessException, CASException  {
    JCas jcas;
    try {
      jcas = aCAS.getJCas();
    } catch (CASException e) {
      throw new CollectionException(e);
    }

    JCasBuilder doc = new JCasBuilder(jcas);
    Fact fact = facts.poll();
    if(fact == null) {
      return;
    }
    doc.add(fact.sentence, Sentence.class);
    OpenFact openFact = new OpenFact(jcas);
    if(fact.factBegin != null) {
      openFact.setBegin(fact.factBegin);
      openFact.setBegin(fact.factEnd);
    }
    Subject subject = new Subject(jcas, fact.subjectBegin, fact.subjectEnd);
    subject.setExplicit(true);
    openFact.setSubject(subject);
    subject.addToIndexes();
    ObjectF object = new ObjectF(jcas, fact.objectBegin, fact.objectEnd);
    object.setExplicit(true);
    openFact.setObject(object);
    object.addToIndexes();
    Relation relation = new Relation(jcas);
    if(fact.relationBegin != null) {
      relation.setBegin(fact.relationBegin);
      relation.setEnd(fact.relationEnd);
    }
    openFact.setRelation(relation);
    relation.addToIndexes();
    openFact.addToIndexes();
    doc.close();
  }

  /**
   * Read a single sentence.
   */
  @Override public void getNext(CAS aCas) throws IOException, CollectionException {
    try {
      DocumentMetaData m = DocumentMetaData.create(aCas);
      m.setLanguage(language);
      m.addToIndexes();
    } catch (CASException e) {
      throw new CollectionException (e);
    }
    try {
      convert(aCas);
    } catch (AnalysisEngineProcessException e) {
      throw new RuntimeException(e);
    } catch (CASException e) {
      e.printStackTrace();
    }
  }

  @Override public boolean hasNext() throws IOException, CollectionException {
    if (super.hasNext()) {
      Resource res = nextFile();
      Scanner in = null;
      try {
        in = new Scanner(new InputStreamReader(res.getInputStream(), "UTF-8"));
      } catch (FileNotFoundException e) {
        throw new CollectionException(e);
      }
      while (in.hasNextLine()) {
        String row = in.nextLine();
        String [] values = row.split("\t");
        if(values.length < 5) {
          continue;
        }
        try {
          Fact fact = new Fact();
          fact.sentence = values[0].substring(1,values[0].length()-1);
          fact.subjectBegin = Integer.parseInt(values[1]);
          fact.subjectEnd = Integer.parseInt(values[2]);
          fact.objectBegin = Integer.parseInt(values[3]);
          fact.objectEnd = Integer.parseInt(values[4]);
          if(values.length>5) {
            fact.relationBegin = Integer.parseInt(values[5]);
            fact.relationEnd = Integer.parseInt(values[6]);
          }
          if(values.length>7) {
            fact.factBegin = Integer.parseInt(values[7]);
            fact.factEnd = Integer.parseInt(values[8]);
          }
          facts.add(fact);
        } catch (NumberFormatException e) {
          continue;
        }

      }
    }
    if (!facts.isEmpty()) {
      return true;
    } else {
      return false;
    }
  }

  public class Fact {
    String sentence;
    Integer subjectBegin;
    Integer subjectEnd;
    Integer objectBegin;
    Integer objectEnd;
    Integer relationBegin;
    Integer relationEnd;
    Integer factBegin;
    Integer factEnd;


    @Override public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Fact facts = (Fact) o;

      if (!sentence.equals(facts.sentence)) return false;
      if (subjectBegin != null ? !subjectBegin.equals(facts.subjectBegin) : facts.subjectBegin != null) return false;
      if (subjectEnd != null ? !subjectEnd.equals(facts.subjectEnd) : facts.subjectEnd != null) return false;
      if (objectBegin != null ? !objectBegin.equals(facts.objectBegin) : facts.objectBegin != null) return false;
      if (objectEnd != null ? !objectEnd.equals(facts.objectEnd) : facts.objectEnd != null) return false;
      if (relationBegin != null ? !relationBegin.equals(facts.relationBegin) : facts.relationBegin != null) return false;
      if (relationEnd != null ? !relationEnd.equals(facts.relationEnd) : facts.relationEnd != null) return false;
      if (factBegin != null ? !factBegin.equals(facts.factBegin) : facts.factBegin != null) return false;
      return factEnd != null ? factEnd.equals(facts.factEnd) : facts.factEnd == null;

    }

    @Override public int hashCode() {
      int result = sentence.hashCode();
      result = 31 * result + (subjectBegin != null ? subjectBegin.hashCode() : 0);
      result = 31 * result + (subjectEnd != null ? subjectEnd.hashCode() : 0);
      result = 31 * result + (objectBegin != null ? objectBegin.hashCode() : 0);
      result = 31 * result + (objectEnd != null ? objectEnd.hashCode() : 0);
      result = 31 * result + (relationBegin != null ? relationBegin.hashCode() : 0);
      result = 31 * result + (relationEnd != null ? relationEnd.hashCode() : 0);
      result = 31 * result + (factBegin != null ? factBegin.hashCode() : 0);
      result = 31 * result + (factEnd != null ? factEnd.hashCode() : 0);
      return result;
    }
  }



}


