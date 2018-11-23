package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.dkprohelper.DKPro2CoreNlp;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.dkprohelper.DependencyFlavor;
import de.tudarmstadt.ukp.dkpro.core.api.resources.MappingProvider;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.internal.TokenKey;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.semgraph.SemanticGraphFactory;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.util.CoreMap;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.List;



public class StanfordRNNDParser extends JCasAnnotator_ImplBase {

  public static DependencyParser parser;

  public static MappingProvider mappingProvider;



  //mappingProvider.setDefault(MappingProvider.LOCATION, file.toURI().toURL().toString());;

  @Override public void initialize(UimaContext aContext) throws ResourceInitializationException {
    parser = DependencyParser.loadFromModelFile("edu/stanford/nlp/models/parser/nndep/english_SD.gz");
    mappingProvider = createConstituentMappingProvider(
        "de/tudarmstadt/ukp/dkpro/core/de.tudarmstadt.ukp.dkpro.core.api.syntax-asl/1.8.0/de.tudarmstadt.ukp.dkpro.core.api.syntax-asl-1.8.0.jar!/de/tudarmstadt/ukp/dkpro/core/api/syntax/tagset/en-stanford-dependency.map");
  }


  public static MappingProvider createConstituentMappingProvider(String aTagset) {
    MappingProvider p = new MappingProvider();
    p.setDefault("location", "classpath:/de/tudarmstadt/ukp/dkpro/core/api/syntax/tagset/${language}-${constituent.tagset}-constituency.map");
    p.setDefault("baseType", "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency");
    p.setDefault("constituent.tagset", "default");
    p.setOverride("language", "en");
    p.setOverride("constituent.tagset", aTagset);
    return p;
  }


/*
  public static MappingProvider createDependencyMappingProvider(String aMappingLocation, String aLanguage, HasResourceMetadata aSource) {
    MappingProvider p = createDependencyMappingProvider(aMappingLocation, (String)null, (String)aLanguage);
    p.addImport("dependency.tagset", aSource);
    return p;
  }
*/


  @Override public void process(JCas jCas) throws AnalysisEngineProcessException {
    mappingProvider.configure(jCas.getCas());
    DKPro2CoreNlp converter = new DKPro2CoreNlp();
    Annotation annotatios = converter.convert(jCas, new Annotation());
    List<CoreMap> sentences = annotatios.get(CoreAnnotations.SentencesAnnotation.class);
    for (CoreMap sentence : sentences) {
      GrammaticalStructure gs = parser.predict(sentence);
      SemanticGraph semanticGraph = SemanticGraphFactory.makeFromTree(gs, SemanticGraphFactory.Mode.CCPROCESSED, GrammaticalStructure.Extras.MAXIMAL, null);;
      semanticGraph.prettyPrint();
      semanticGraph = semanticGraphUniversalEnglishToEnglish(semanticGraph);
      sentence.set(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class, semanticGraph);
      for(SemanticGraphEdge edge: semanticGraph.edgeListSorted()) {
        System.out.println(edge);
      }
    }
    convertDependencies(jCas, annotatios, true);
  }

  public static void convertDependencies(JCas aJCas, Annotation document, boolean internStrings)
  {
    for (CoreMap s : document.get(CoreAnnotations.SentencesAnnotation.class)) {
      SemanticGraph graph = s.get(SemanticGraphCoreAnnotations.EnhancedDependenciesAnnotation.class);

      // If there are no dependencies for this sentence, skip it. Might well mean we
      // skip all sentences because normally either there are dependencies for all or for
      // none.
      if (graph == null) {
        continue;
      }

      for (IndexedWord root : graph.getRoots()) {
        Dependency dep = new ROOT(aJCas);
        dep.setDependencyType("root");
        dep.setDependent(root.get(TokenKey.class));
        dep.setGovernor(root.get(TokenKey.class));
        dep.setBegin(dep.getDependent().getBegin());
        dep.setEnd(dep.getDependent().getEnd());
        dep.setFlavor(DependencyFlavor.BASIC);
        dep.addToIndexes();
      }

      for (SemanticGraphEdge edge : graph.edgeListSorted()) {
        Token dependent = edge.getDependent().get(TokenKey.class);
        Token governor = edge.getGovernor().get(TokenKey.class);

        // For the type mapping, we use getShortName() instead, because the <specific>
        // actually doesn't change the relation type
        String labelUsedForMapping = edge.getRelation().getShortName();

        // The nndepparser may produce labels in which the shortName contains a colon.
        // These represent language-specific labels of the UD, cf:
        // http://universaldependencies.github.io/docs/ext-dep-index.html
        labelUsedForMapping = StringUtils.substringBefore(labelUsedForMapping, ":");

        // Need to use toString() here to get "<shortname>_<specific>"
        String actualLabel = edge.getRelation().toString();

        Type depRel = mappingProvider.getTagType(labelUsedForMapping);
        Dependency dep = (Dependency) aJCas.getCas().createFS(depRel);
        dep.setDependencyType(internStrings ? actualLabel.intern() : actualLabel);
        dep.setDependent(dependent);
        dep.setGovernor(governor);
        dep.setBegin(dep.getDependent().getBegin());
        dep.setEnd(dep.getDependent().getEnd());
        dep.setFlavor(edge.isExtra() ? DependencyFlavor.ENHANCED : DependencyFlavor.BASIC);
        dep.addToIndexes();
      }
    }
  }

  public static SemanticGraph semanticGraphUniversalEnglishToEnglish(SemanticGraph semanticGraph) {
    for (SemanticGraphEdge edge: semanticGraph.edgeListSorted()) {
      GrammaticalRelation oldRel = edge.getRelation();
      edge.setRelation(EnglishGrammaticalRelations.shortNameToGRel.get(oldRel.getShortName()));
    }
    return semanticGraph;
  }
}
