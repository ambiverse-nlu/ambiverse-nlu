package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.entitysalience.EntitySalienceSpark;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.OptimaizeLanguageDetectorAnalysisEngine;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.KnowNER;
import de.tudarmstadt.ukp.dkpro.core.hunpos.HunPosTagger;
import de.tudarmstadt.ukp.dkpro.core.io.conll.Conll2009Writer;
import de.tudarmstadt.ukp.dkpro.core.languagetool.LanguageToolLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.matetools.MateLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.matetools.MateParser;
import de.tudarmstadt.ukp.dkpro.core.matetools.MateSemanticRoleLabeler;
import de.tudarmstadt.ukp.dkpro.core.opennlp.OpenNlpChunker;
import de.tudarmstadt.ukp.dkpro.core.rftagger.RfTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordNamedEntityRecognizer;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;

public enum Component {

  LANGUAGE_IDENTIFICATION(OptimaizeLanguageDetectorAnalysisEngine.class),
  EN_TOKENIZER(StanfordTokenizer.class),
  EN_POS(StanfordPosTagger.class),
  EN_LEMMATIZER(StanfordLemmatizer.class),
  EN_NER(StanfordNamedEntityRecognizer.class),
  EN_PARSERS(StanfordParser.class, "mode", "BASIC"),
  EN_DEPPARSESTANFORDRNN(StanfordRNNDParser.class),
  EN_SRL(MateSemanticRoleLabeler.class, MateSemanticRoleLabeler.PARAM_LANGUAGE, "en"),
  ZH_TOKENIZER(StanfordTokenizer.class),
  ZH_POS(StanfordPosTagger.class,StanfordPosTagger.PARAM_LANGUAGE, "zh"),
  RU_TOKENIZER(BreakIteratorSegmenter.class),
  RU_POS(HunPosTagger.class, HunPosTagger.PARAM_LANGUAGE, "ru"),
  RU_LEMMATIZER(LanguageToolLemmatizer.class),
  CS_TOKENIZER(BreakIteratorSegmenter.class),
  CS_POS(HunPosTagger.class, HunPosTagger.PARAM_LANGUAGE, "cs"),
  CS_LEMMATIZER(LanguageToolLemmatizer.class),
  CS_NER(StanfordNamedEntityRecognizer.class),//ino alaki gozashtam error nade
  ZH_NER(StanfordChineseNER.class),
  DE_TOKENIZER(StanfordTokenizer.class),
  DE_POS(StanfordPosTagger.class),
  DE_LEMMATIZER(MateLemmatizer.class, MateLemmatizer.PARAM_LANGUAGE, "de", MateLemmatizer.PARAM_UPPERCASE, true),
  DE_NER(StanfordNamedEntityRecognizer.class, "modelVariant", "hgc_175m_600.crf"),
  DE_NER2(StanfordProperNounNER.class),
  DE_DEPPARSE(MateParser.class, "language", "de"),
  DE_SRL(MateSemanticRoleLabeler.class, MateSemanticRoleLabeler.PARAM_LANGUAGE, "de"),
  ES_TOKENIZER(StanfordTokenizer.class),
  ES_POS(StanfordPosTagger.class),
  ES_LEMMATIZER(MateLemmatizer.class, "language", "es", MateLemmatizer.PARAM_UPPERCASE, true),
  ES_NER(StanfordNamedEntityRecognizer.class),
  FR_TOKENIZER(StanfordTokenizer.class),
  FR_POS(StanfordPosTagger.class),
  FR_NER(StanfordNamedEntityRecognizer.class),
//  DE_AIDA(AidaAnalysisEngine.class), replicated
  AIDA_USE_RESULTS(AidaAnalysisEngine.class, AidaAnalysisEngine.USE_RESULTS, true),
  AIDA_NO_RESULTS(AidaAnalysisEngine.class, AidaAnalysisEngine.USE_RESULTS, false),
  CLAUSIE(ClausIEAnalysisEngine.class, ClausIEAnalysisEngine.PARAM_REDUNDANT, false, ClausIEAnalysisEngine.PARAM_REVERBFORM, true, ClausIEAnalysisEngine.PARAM_KEEPONLYLONGEST, true, ClausIEAnalysisEngine.PARAM_PROCESSCCARGS, false),
  KNOW_NER_NED(KnowNER.class, "model", "NED"),
  DE_KNOW_NER_KB(KnowNER.class, "model", "KB"),
  KNOW_NER_KB(KnowNER.class, "model", "KB"),
  MATE2STANFORD_DEP(Mate2StanfordDepConverter.class),
  MORPHOLOGICAL_FEATURES(RfTagger.class),
  CONLL2009_WRITER(Conll2009Writer.class, "targetLocation", "./testConll"),
  FILTERENTITIESBYTYPE(FilterEntitiesByType.class, "nerTypes", "ORGANIZATION", "kgTypes", "wordnet_company_108058098"),
  DUMP2JSON(Dump2Json.class, "outDirectory", "output"),
  FILTERFACTS(FilterFacts.class, "words", "ORGANIZATION", "entities", "wordnet_company_108058098"),
  SALIENCE(EntitySalienceSpark.class, EntitySalienceSpark.PARAM_MODEL_PATH, "src/main/resources/entitysalience/models/model_RANDOM_FOREST/model"),
  CONLL_WRITER(ConllWriter.class, ConllWriter.PARAM_OUTPUT_DIRECTORY, "output", ConllWriter.PARAM_WRITE, "ner,sle"),
  
  DOMAIN_EXTRACTOR(DomainExtractor.class),// This component should be used after AIDA component. It uses result named entities for domain extraction.
  DOMAIN_WORD_GENERATOR(DomainWordsGenerator.class),
  TOP_CATEGORY_EXTRACTOR(TopCategoriesExtractor.class),
  CONCEPT_SPOTTER_FUZZY_09(ConceptSpotter.class, ConceptSpotter.PARAM_MATCHING_RATIO, (float) 0.9), //ConceptSpotter is set for all languages, takes long initialization. We can have separate components for each language.
  CONCEPT_SPOTTER_EXACT(ConceptSpotter.class, ConceptSpotter.PARAM_MATCHING_RATIO, (float) 1.0),
  CD_USE_RESULTS(CdAnalysisEngine.class, CdAnalysisEngine.USE_RESULTS, true),
  CD_NO_RESULTS(CdAnalysisEngine.class, CdAnalysisEngine.USE_RESULTS, false),
  JD(JointAidaConceptAnalysisEngine.class),
  CHUNCKER(OpenNlpChunker.class),//Not used anymore-can be removed
  
  //Concept Filters:
  FILTER_CONCEPTS_BY_NOTHING_NEs(FilterConceptsByNothing.class, FilterConceptsByNothing.FILTER_NEs, true),//Filter existing NE mentions from ConceptCandidates
  FILTER_CONCEPTS_BY_NOTHING(FilterConceptsByNothing.class, FilterConceptsByNothing.FILTER_NEs, false), //No filter. (used in Joint)
  FILTER_CONCEPTS_BY_POS_NOUNPHRASES_NEs(FilterConceptsByPOStagsNounPhrases.class, FilterConceptsByPOStagsNounPhrases.FILTER_NEs, true),//Filter NEs and phrases without any noun using POS tags.
  FILTER_CONCEPTS_BY_POS_NOUNPHRASES(FilterConceptsByPOStagsNounPhrases.class, FilterConceptsByPOStagsNounPhrases.FILTER_NEs, false),//Filter phrases without any noun using POS tags (used in Joint).
  
  FILTER_CONCEPTS_BY_CATS_NEs(FilterConceptsByCategories.class, FilterConceptsByCategories.FILTER_NEs, true),//Filter NEs and concepts not containing top Categories.
  FILTER_CONCEPTS_BY_CATS(FilterConceptsByCategories.class, FilterConceptsByCategories.FILTER_NEs, false),//Filter concepts not containing top Categories (Joint).
  FILTER_CONCEPTS_BY_DOMAIN_WORDS_NEs(FilterConceptsByDomainWords.class, FilterConceptsByDomainWords.FILTER_NEs, true),//Filter NEs and concepts not in Domain Words of NEs.
  FILTER_CONCEPTS_BY_DOMAIN_WORDS(FilterConceptsByDomainWords.class, FilterConceptsByDomainWords.FILTER_NEs, false);//Filter concepts not in Domain Words of NEs.


  public Class<? extends JCasAnnotator_ImplBase> component;

  public Object[] params;

  Component(Class<? extends JCasAnnotator_ImplBase> component, Object... params) {
    this.component = component;
    this.params = params;
  }

}