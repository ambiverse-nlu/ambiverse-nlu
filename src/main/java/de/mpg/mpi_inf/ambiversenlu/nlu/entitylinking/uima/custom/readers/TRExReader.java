package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers;

import com.jsoniter.JsonIterator;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.util.trex.TAnnotation;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.util.trex.TTriples;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.util.trex.TrexDoc;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.*;
import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TRExReader extends ResourceCollectionReaderBase {

  /**
   * The language.
   */
  public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;

  @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
  private String language;

  private String json;

  @Override public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
  }

  private void convert(CAS aCAS) throws IOException, CollectionException, AnalysisEngineProcessException, CASException {
    JsonIterator iter = JsonIterator.parse(json);

    TrexDoc tdoc = iter.read(TrexDoc.class);
    JCas jcas;
    try {
      jcas = aCAS.getJCas();
    } catch (CASException e) {
      throw new CollectionException(e);
    }
    jcas.setDocumentText(tdoc.text);
    jcas.setDocumentLanguage("en");

    DocumentMetaData metaData = new DocumentMetaData(jcas);
    metaData.setDocumentTitle(tdoc.title);
    metaData.setDocumentId(tdoc.docid);
    metaData.setDocumentUri(tdoc.uri);

    for (int[] word : tdoc.words_boundaries) {
      Token token = new Token(jcas, word[0], word[1]);
      token.addToIndexes();
    }

    for (int[] sb : tdoc.sentences_boundaries) {
      Sentence sentence = new Sentence(jcas, sb[0], sb[1]);
      sentence.addToIndexes();
    }


    for (TAnnotation entity : tdoc.entities) {
      NamedEntity ne = new NamedEntity(jcas, entity.boundaries[0], entity.boundaries[1]);
      ne.addToIndexes();
      AidaEntity ae = new AidaEntity(jcas, entity.boundaries[0], entity.boundaries[1]);
      ae.setID(entity.uri);
      ae.addToIndexes();
    }

    for (TTriples triple : tdoc.triples) {
      if(triple.subject.boundaries != null &&
          triple.object.boundaries != null &&
          triple.subject.boundaries[0] > triple.object.boundaries[1]) {
        TAnnotation tmp = triple.subject; //It seems that in the data is wrongly assigned
        triple.subject = triple.object;
        triple.object = tmp;
      }
      Subject subject = getConstituent(triple.subject, Subject.class, jcas);
      subject.setAnnotator("TREx");
      subject.setConfidence(-1);
      Relation relation = getConstituent(triple.predicate, Relation.class, jcas);
      relation.setConfidence(-1);
      relation.setAnnotator("TREx");
      ObjectF object = getConstituent(triple.object, ObjectF.class, jcas);
      object.setAnnotator("TREx");
      object.setConfidence(-1);
      OpenFact of = new OpenFact(jcas);
      of.setAnnotator("TREx");
      if(triple.confidence != null) {
        of.setConfidence(triple.confidence);
      } else {
        of.setConfidence(-1.0);
      }
      Integer begin = getFactPosition(true, subject, relation, object);
      Integer end = getFactPosition(false, subject, relation, object);
      if(begin != null) {
        of.setBegin(begin);
      }
      if(end != null) {
        of.setEnd(end);
      }
      of.setSubject(subject);
      of.setRelation(relation);
      of.setObject(object);
      of.addToIndexes();
    }
  }

  private Integer getFactPosition(boolean min, Constituent... constituents) {
    Integer result = null;
    for(Constituent c: constituents) {
      if(!c.getExplicit()) {
        continue;
      }
      if(min && (result == null || result > c.getBegin() )) {
        result = c.getBegin();
      } else if(!min && (result == null || result < c.getEnd())) {
        result = c.getEnd();
      }
    }
    return result;
  }

  private <K extends Constituent> K getConstituent(TAnnotation constituent, Class<K> clazz, JCas jCas) {
    Constituent result = getInstancedConstitient(jCas, constituent, clazz);
    if(constituent.boundaries != null) {
      result.setExplicit(true);
      result.setBegin(constituent.boundaries[0]);
      result.setEnd(constituent.boundaries[1]);
    }
    result.setUri(constituent.uri);
    List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, result.getBegin(), result.getEnd());
    FSArray array = new FSArray(jCas, tokens.size());
    for (int i = 0; i < tokens.size(); i++) {
      array.set(i, tokens.get(i));
    }
    array.addToIndexes();
    result.setTokens(array);
    jCas.addFsToIndexes(clazz.cast(result));
    return clazz.cast(result);
  }

  private<K extends Constituent> K getInstancedConstitient(JCas jCas, TAnnotation constituent, Class<K> clazz) {
    if(clazz.equals(Subject.class)) {
      return clazz.cast(new Subject(jCas));
    } else if(clazz.equals(Relation.class)) {
      return clazz.cast(new Relation(jCas));
    } else if(clazz.equals(ObjectF.class)) {
      return clazz.cast(new ObjectF(jCas));
    } else {
      throw new IllegalArgumentException("Open facts should be composed by a subject, a relation and an object");
    }
  }

  @Override public void getNext(CAS aCAS) throws IOException, CollectionException {
    Resource res = nextFile();
    initCas(aCAS, res);
    Path path = Paths.get(res.getResolvedUri());
    json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    try {
      convert(aCAS);
    } catch (AnalysisEngineProcessException | CASException e) {
      throw new RuntimeException(e);
    }
  }
}
