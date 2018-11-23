package de.mpg.mpi_inf.ambiversenlu.nlu.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.evaluation.EvaluationCounts;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ResultMention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Fact;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.openie.model.OpenFact;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.Pair;

import java.util.List;


public class ProcessedDocument {

  public ProcessedDocument(String docId, Language language, String text) {
    this.docId = docId;
    this.language = language;
    this.text = text;
  }

  private String docId;

  private Language language;

  private final String text;

  private String title;

  private List<OpenFact> facts;

  private List<ResultMention> entityAnnotations;

  private Tokens tokens;

  private EvaluationCounts ecDisambiaguation;
  
  private EvaluationCounts ecNamedEntitiesDisambiaguation;
  
  private EvaluationCounts ecConceptsDisambiaguation;
  
  private EvaluationCounts ecUnknownsDisambiaguation;
  
  private EvaluationCounts ecConceptsDisambiaguationFP;
  
  
  private EvaluationCounts ecNER;
  
  private EvaluationCounts ecNERNamedEntities;
  
  private EvaluationCounts ecNERConcepts;
  
  private EvaluationCounts ecNERUnknowns;
  
  private Mentions mentions;
  
  private List<String> domains;

  private List<String> synsets;

  private List<String> domainWords;
  
  private List<Pair<Integer, String> > topTypes;

  //TODO: This was added to speed up development, should be properly handled in the pipeline
  private List<Fact> serviceFacts;

  public List<Fact> getServiceFacts() {
    return serviceFacts;
  }

  public void setServiceFacts(List<Fact> serviceFacts) {
    this.serviceFacts = serviceFacts;
  }

  public void setEntityAnnotations(List<ResultMention> entityAnnotations) {
    this.entityAnnotations = entityAnnotations;
  }

  public void setEvaluationCountsDisambiguation(EvaluationCounts ec) {
    this.ecDisambiaguation = ec;
  }
  
  public void setEvaluationCountsConceptsDisambiguation(EvaluationCounts ec) {
    this.ecConceptsDisambiaguation = ec;
  }
  
  public void setEvaluationCountsNamedEntitiesDisambiguation(EvaluationCounts ec) {
    this.ecNamedEntitiesDisambiaguation = ec;
  }
  
  public void setEvaluationCountsConceptsDisambiguationFP(EvaluationCounts ecConceptsDisambiaguationFP) {
    this.ecConceptsDisambiaguationFP = ecConceptsDisambiaguationFP;
  }

  public void setEvaluationCountsNER(EvaluationCounts ec) {
    this.ecNER = ec;
  }
  
  public void setEvaluationCountsNERNamedEntities(EvaluationCounts ec) {
    this.ecNERNamedEntities = ec;
  }
  
  public void setEvaluationCountsNERConcepts(EvaluationCounts ec) {
    this.ecNERConcepts = ec;
  }

  public void setTokens(Tokens tokens) {
    this.tokens = tokens;
  }

  public List<ResultMention> getEntityAnnotations() {
    return entityAnnotations;
  }

  public EvaluationCounts getEvaluationCountsConceptsDisambiguation() {
    return ecConceptsDisambiaguation;
  }
  
  public EvaluationCounts getEvaluationCountsNamedEntitiesDisambiguation() {
    return ecNamedEntitiesDisambiaguation;
  }
  
  public EvaluationCounts getEvaluationCountsDisambiguation() {
    return ecDisambiaguation;
  }
  
  public EvaluationCounts getEvaluationCountsConceptsDisambiguationFP() {
    return ecConceptsDisambiaguationFP;
  }
  
  public EvaluationCounts getEvaluationCountsNER() {
    return ecNER;
  }
  
  public EvaluationCounts getEvaluationCountsNERNamedEntities() {
    return ecNERNamedEntities;
  }
  
  public EvaluationCounts getEvaluationCountsNERConcepts() {
    return ecNERConcepts;
  }
  
  public Tokens getTokens() {
    return tokens;
  }

  public String getDocId() {
    return docId;
  }
  
  public void setDocId(String docId) {
    this.docId = docId;
  }

  public Language getLanguage() {
    return language;
  }

  public void setMentions(Mentions mentions) {
    this.mentions = mentions;
  }

  public Mentions getMentions() {
    return mentions;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  public String getText() {
    return text;
  }
  
  public List<String> getDomains() {
    return domains;
  }

  public void setDomains(List<String> domains) {
    this.domains = domains;
  }

  public List<String> getSynsets() {
    return synsets;
  }

  public void setSynsets(List<String> synsets) {
    this.synsets = synsets;
  }

  public List<String> getDomainWords() {
    return domainWords;
  }

  public void setDomainWords(List<String> domainWords) {
    this.domainWords = domainWords;
  }

  public List<Pair<Integer, String>> getTopTypes() {
    return topTypes;
  }

  public void setTopTypes(List<Pair<Integer, String>> topTypes) {
    this.topTypes = topTypes;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public List<OpenFact> getFacts() {
    return facts;
  }

  public void setFacts(List<OpenFact> facts) {
    this.facts = facts;
  }

  public EvaluationCounts getEvaluationCountsUnknownsDisambiaguation() {
    return ecUnknownsDisambiaguation;
  }

  
  public void setEvaluationCountsUnknownsDisambiaguation(EvaluationCounts ecUnknownsDisambiaguation) {
    this.ecUnknownsDisambiaguation = ecUnknownsDisambiaguation;
  }


  public EvaluationCounts getEvaluationCountsNERUnknowns() {
    return ecNERUnknowns;
  }


  public void setEvaluationCountsNERUnknowns(EvaluationCounts ecNERUnknowns) {
    this.ecNERUnknowns = ecNERUnknowns;
  }

}
