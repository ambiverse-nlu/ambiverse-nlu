package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import org.apache.uima.UIMAException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum PipelineType {
  DUMMY(new DummyPipeline()),

  LANGUAGE_DETECTION(new LanguageDetectionPipeline()),
  TOKENIZATION(new TokenizationPipeline()),
  POS_TAGGING(new POSTaggingPipeline()),
  DISAMBIGUATION(new DisambiguationPipeline()),
  DISAMBIGUATION_STANFORD(new DisambiguationStanfordPipeline()),
  EVALUATION(new EvaluationPipeline()),
  EVALUATION_ONLYNED(new EvaluationOnlyNEDPipeline()),
  OPENIE_EN(new OpenIE()),
  KNOWNER_KB(new KnowNerKB()),
  DEPENDENCY_PARSE(new DependencyParsingPipeline()),

  ENTITY_SALIENCE(new EntitySalience()),
  ENTITY_SALIENCE_STANFORD(new EntitySalienceStanford()),
  ENTITY_CONCEPT_SALIENCE(new EntityConceptSalience()),
  ENTITY_CONCEPT_SALIENCE_STANFORD(new EntityConceptSalienceStanford()),
  FACTS_WITH_SALIENCE_EN(new FactsWithSalience()),
  FACTS_WITH_SALIENCE_EN_STANFORD(new FactsWithSalienceStanford()),
  FACT_ENTITY_CONCEPT_SALIENCE(new FactsEntityConceptSalience()),
  FACT_ENTITY_CONCEPT_SALIENCE_STANFORD(new FactsEntityConceptSalienceStanford()),

  OPENFACT_EXTRACTION_EN(new OpenFactExtractionEN()),

  READ_COLLECTION(new ReadCollectionPipeline());

  private boolean initalized = false;

  private final Pipeline pipeline;

  PipelineType(Pipeline pipeline) {
    this.pipeline = pipeline;
  }

  public Map<String, String> getSteps() {
    return pipeline.getSteps();
  }

  public boolean isEvaluation() {
    if (this.name().startsWith("EVALUATION")) {
      return true;
    }
    return false;
  }

  //Necessary as UIMA-DKPRO performs lazy loading of resources
  public synchronized  void dummyCall() throws NoSuchMethodException, IOException, ClassNotFoundException, UnprocessableDocumentException, UIMAException, EntityLinkingDataAccessException, MissingSettingException {
    Document doc = new Document.Builder().withText("dummy").build();
    dummyCall(doc);
  }

  public synchronized void dummyCall(Document document) throws UIMAException, IOException, ClassNotFoundException, NoSuchMethodException,
      MissingSettingException, EntityLinkingDataAccessException, UnprocessableDocumentException {
    if (!initalized) {
      Set<Language> prepareForLanguages = new HashSet<>();

      if (document.getLanguage() == null) {
        prepareForLanguages.addAll(pipeline.supportedLanguages());
      } else {
        prepareForLanguages.add(document.getLanguage());
      }

      for (Language language : prepareForLanguages) {
        Document dummy_doc = document.clone();
        dummy_doc.setText(language.getDummySentence());
        dummy_doc.setLanguage(language);
        dummy_doc.setDocumentId("dummy_doc_"+language);
        DocumentProcessor processor = DocumentProcessor.getInstance(this);
        processor.process(dummy_doc);
      }
    }
    initalized = true;
  }
}
