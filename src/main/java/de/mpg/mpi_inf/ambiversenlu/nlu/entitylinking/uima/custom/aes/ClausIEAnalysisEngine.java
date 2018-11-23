package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.dkprohelper.DKPro2CoreNlp;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie.ClausIE;
import de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie.Options;
import de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie.Proposition;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.internal.TokenKey;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;


@TypeCapability(inputs = {
    "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Dependency",
    "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree",
    "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma" },
    outputs = {
    "de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact" })

public class ClausIEAnalysisEngine extends JCasAnnotator_ImplBase {

  private Logger logger_ = LoggerFactory.getLogger(ClausIEAnalysisEngine.class);

  public static final String PARAM_REDUNDANT = "removeRedundant";
  @ConfigurationParameter(name = "removeRedundant", mandatory = false)
  private Boolean removeRedundant;

  public static final String PARAM_REVERBFORM = "reverbForm";
  @ConfigurationParameter(name = "reverbForm", mandatory = false, defaultValue = "false")
  private Boolean reverbForm;

  public static final String PARAM_KEEPONLYLONGEST = "keepOnlyLongest";
  @ConfigurationParameter(name = "keepOnlyLongest", mandatory = false, defaultValue = "false")
  private Boolean keepOnlyLongest;

  public static final String PARAM_PROCESSCCARGS = "processCCargs";
  @ConfigurationParameter(name = "processCCargs", mandatory = false, defaultValue = "true")
  private Boolean processCCargs;

  Options clausieOptions;

  @Override public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    try {
      clausieOptions = new Options();
    } catch (IOException | URISyntaxException e) {
      throw new ResourceInitializationException(e);
    }
    clausieOptions.keepOnlyLongest = keepOnlyLongest;
    clausieOptions.reverbRelationStyle = reverbForm;
    clausieOptions.processCcNonVerbs = processCCargs;

  }

  @Override public void process(JCas jCas) throws AnalysisEngineProcessException {
    DKPro2CoreNlp converter = new DKPro2CoreNlp();
    Annotation annotatios = converter.convert(jCas, new Annotation());
    List<CoreMap> sentences = annotatios.get(CoreAnnotations.SentencesAnnotation.class);

    long startTime = System.currentTimeMillis();
    int factCount = 0;
    int exception = 0;
    for (CoreMap sentence : sentences) {
      try {
        SemanticGraph semanticGraph = sentence.get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class);
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
        ClausIE clausIE = new ClausIE(semanticGraph, tree, clausieOptions);
        clausIE.detectClauses();
        clausIE.generatePropositions();
        List<Proposition> propositions = new ArrayList<>(clausIE.getPropositions());

        if(removeRedundant) {
          removeRedundant(propositions);
        }

        for (Proposition p : propositions) {
  //        System.out.println(p);
  //        System.out.println(p.getDictRelation());

            OpenFact of = new OpenFact(jCas);
            Subject subject = addConstituentToJCas(jCas, Subject.class, p, 0);
            of.setBegin(subject.getBegin());
            of.setSubject(subject);
            Relation relation = addConstituentToJCas(jCas, Relation.class, p, 1);
            relation.setNormalizedForm(p.getDictRelation());
            of.setRelation(relation);
            ObjectF object = addConstituentToJCas(jCas, ObjectF.class, p, 2);
            of.setEnd(object.getEnd());
            of.setObject(object);
            of.setText(p.toString());
            of.addToIndexes();
            factCount++;
        }
      } catch (Exception e) {
        exception++;
        logger_.info("Exception at ClausIEAnalysisEngine: "+e.getMessage() + "\n" + e.getStackTrace().toString());
        if(exception > 15) {
          throw new AnalysisEngineProcessException(e);
        }
      }
    }
    double runTime = System.currentTimeMillis() - startTime;
    String docId = JCasUtil.selectSingle(jCas, DocumentMetaData.class).getDocumentId();
    logger_.info( "Document '" + docId + "' done in " + runTime + "ms (" + factCount + " facts).");
  }

  private void removeRedundant(List<Proposition> propositions) {
    List<Set<IndexedWord>> content = new ArrayList<>();
    for(Proposition p: propositions) {
      Set<IndexedWord> words = new HashSet<>();
      for(int c: p.getConstituents().keySet()) {
        words.addAll(p.getTokensForConstituent(c));
      }
      content.add(words);
    }
    Set<Integer> toRemove = new HashSet<>();
    for(int i = 0; i < content.size(); i++) {
      for(int j = 0; j < content.size(); j++) {
        if(j==i || toRemove.contains(i)) {
          continue;
        }
        if(content.get(j).containsAll(content.get(i))) {
          toRemove.add(i);
        }
      }
    }
    Iterator<Proposition> it = propositions.iterator();
    int index = 0;
    while(it.hasNext()) {
      it.next();
      if(toRemove.contains(index)) {
        it.remove();
      }
      index++;
    }
  }

  private <K extends Constituent> K addConstituentToJCas(JCas jCas, Class<K> clazz, Proposition p, int index) throws IllegalAccessException {
    List<IndexedWord> words = p.getTokensForConstituent(index);
    FSArray tokens = new FSArray(jCas, words.size());
    int begin = Integer.MAX_VALUE;
    int end = -1;
    for (int i = 0; i < words.size(); i++) {
      IndexedWord w = words.get(i);
      Token token;
      if(w.get(TokenKey.class) == null) {
        token = new Token(jCas);
        token.setId(w.value());
      } else {
        token = w.get(TokenKey.class);
      }
      tokens.set(i, token);
      if (token.getBegin() < begin) {
        begin = token.getBegin();
      }
      if (token.getEnd() + 1 > end) {
        end = token.getEnd();
      }
    }
    tokens.addToIndexes();
    Constituent annotation = getInstancedConstitient(jCas, begin, end, clazz);
    annotation.setTokens(tokens);
    annotation.setText(p.getConstituents().get(index));
    IndexedWord head = p.getHead(index);
    Token token;
    if(head != null && head.get(TokenKey.class) == null) {
      token = new Token(jCas);
      token.setId(head.value());
    } else {
      token = head != null ? head.get(TokenKey.class) : null;
    }
    annotation.setHead(token);
    jCas.addFsToIndexes(clazz.cast(annotation));
    return clazz.cast(annotation);
  }

  private<K extends Constituent> K getInstancedConstitient(JCas jCas, int begin, int end, Class<K> clazz) {
    if(clazz.equals(Subject.class)) {
      return clazz.cast(new Subject(jCas, begin, end));
    } else if(clazz.equals(Relation.class)) {
      return clazz.cast(new Relation(jCas, begin, end));
    } else if(clazz.equals(ObjectF.class)) {
      return clazz.cast(new ObjectF(jCas, begin, end));
    } else {
      throw new IllegalArgumentException("Open facts should be composed by a subject, a relation and an object");
    }
  }
}
