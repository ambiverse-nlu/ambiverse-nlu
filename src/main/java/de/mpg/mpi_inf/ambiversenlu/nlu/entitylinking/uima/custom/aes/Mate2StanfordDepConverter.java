package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import edu.stanford.nlp.trees.EnglishGrammaticalRelations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.HashMap;
import java.util.Map;

public class Mate2StanfordDepConverter extends JCasAnnotator_ImplBase {

  private static Map<String, GrammaticalRelation> relmaps;

  @Override public void initialize(UimaContext aContext) {
    relmaps = new HashMap<>();
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
    //relmaps.put("--", GrammaticalRelation.ROOT);
  }

  @Override public void process(JCas jCas) throws AnalysisEngineProcessException {
    for (Dependency d : JCasUtil.select(jCas, Dependency.class)) {
      
      if (!d.getDependencyType().equals("--")) {
        GrammaticalRelation rel = relmaps.get(d.getDependencyType());
        
        String s = rel.toString();
        d.setDependencyType(s);
      } else {
        if (d.getDependencyType().equals("--") && d.getDependent().getBegin() == d.getGovernor().getBegin() && 
            d.getDependent().getEnd() == d.getGovernor().getEnd()) {
          d.setDependencyType(GrammaticalRelation.ROOT.toString());
          
        }
      }
    }
  }
}
