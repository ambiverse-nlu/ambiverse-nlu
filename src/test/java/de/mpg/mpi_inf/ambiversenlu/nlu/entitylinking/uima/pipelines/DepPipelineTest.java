package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DepPipelineTest {

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test_multi");
  }

  @Test public void testDepPipepilineGerman() throws Exception {

    String text = "Die Frau spielt Fussball.";
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text);
    Document doc = dbuilder.withLanguage(Language.getLanguageForString("de")).build();

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DEPENDENCY_PARSE);
    JCas jCas = dp.processDev(doc);

    Collection<Dependency> deps = JCasUtil.select(jCas, Dependency.class);
    for (Dependency dep : deps) {
      System.out.println(dep.getDependencyType() + "\t" + dep.getBegin());

    }

    Map<String, GrammaticalRelation> relmaps = new HashMap<>();
    relmaps.put("SB", EnglishGrammaticalRelations.SUBJECT);
    relmaps.put("OA", EnglishGrammaticalRelations.DIRECT_OBJECT);
    relmaps.put("DA", EnglishGrammaticalRelations.INDIRECT_OBJECT);
    relmaps.put("OG", EnglishGrammaticalRelations.PREPOSITIONAL_MODIFIER);
    relmaps.put("OP", EnglishGrammaticalRelations.PREPOSITIONAL_OBJECT);
    relmaps.put("APP", EnglishGrammaticalRelations.APPOSITIONAL_MODIFIER);
    relmaps.put("CJ", EnglishGrammaticalRelations.CONJUNCT);
    relmaps.put("MO", EnglishGrammaticalRelations.MODIFIER);
    relmaps.put("OC", EnglishGrammaticalRelations.CLAUSAL_COMPLEMENT);
    relmaps.put("PD", EnglishGrammaticalRelations.DIRECT_OBJECT); /////// check
    relmaps.put("SVP", EnglishGrammaticalRelations.MODIFIER); ////check
    relmaps.put("AC", EnglishGrammaticalRelations.PREPOSITIONAL_MODIFIER); ///check
    relmaps.put("ADC", EnglishGrammaticalRelations.ADJECTIVAL_COMPLEMENT);
    relmaps.put("AG", EnglishGrammaticalRelations.PREPOSITIONAL_MODIFIER);
    relmaps.put("AMS", EnglishGrammaticalRelations.MODIFIER); //check
    relmaps.put("AVC", EnglishGrammaticalRelations.NP_ADVERBIAL_MODIFIER);
    relmaps.put("CC", EnglishGrammaticalRelations.COMPLEMENT); //check
    relmaps.put("CD", EnglishGrammaticalRelations.COORDINATION);
    relmaps.put("CM", EnglishGrammaticalRelations.COORDINATION);
    relmaps.put("CP", EnglishGrammaticalRelations.MARKER);
    relmaps.put("CVC", EnglishGrammaticalRelations.VERBAL_MODIFIER); // check
    relmaps.put("DH", EnglishGrammaticalRelations.MODIFIER); //check
    relmaps.put("DM", EnglishGrammaticalRelations.MODIFIER); // check
    relmaps.put("EP", EnglishGrammaticalRelations.EXPLETIVE);
    relmaps.put("JU", EnglishGrammaticalRelations.COORDINATION); //check
    relmaps.put("MNR", EnglishGrammaticalRelations.NOUN_COMPOUND_MODIFIER);
    relmaps.put("NE", EnglishGrammaticalRelations.NEGATION_MODIFIER);
    relmaps.put("NK", EnglishGrammaticalRelations.DETERMINER);
    relmaps.put("NMC", EnglishGrammaticalRelations.NUMERIC_MODIFIER);
    relmaps.put("OA2", EnglishGrammaticalRelations.DIRECT_OBJECT);
    relmaps.put("OC", EnglishGrammaticalRelations.CLAUSAL_COMPLEMENT);
    relmaps.put("PAR", EnglishGrammaticalRelations.MODIFIER); //check
    relmaps.put("PG", EnglishGrammaticalRelations.PREPOSITIONAL_COMPLEMENT); //check
    relmaps.put("PH", EnglishGrammaticalRelations.MODIFIER); //check
    relmaps.put("PM", EnglishGrammaticalRelations.PHRASAL_VERB_PARTICLE); //check, surely not
    relmaps.put("PNC", EnglishGrammaticalRelations.NOUN_COMPOUND_MODIFIER); //check
    relmaps.put("RC", EnglishGrammaticalRelations.RELATIVE_CLAUSE_MODIFIER);
    relmaps.put("RE", EnglishGrammaticalRelations.MODIFIER); //check
    relmaps.put("RS", EnglishGrammaticalRelations.MODIFIER);//check
    relmaps.put("SBP", EnglishGrammaticalRelations.NOMINAL_PASSIVE_SUBJECT);
    relmaps.put("SP", EnglishGrammaticalRelations.CLAUSAL_SUBJECT);
    relmaps.put("UC", EnglishGrammaticalRelations.MODIFIER); //check
    relmaps.put("VO", EnglishGrammaticalRelations.MODIFIER); //check

  }

}
