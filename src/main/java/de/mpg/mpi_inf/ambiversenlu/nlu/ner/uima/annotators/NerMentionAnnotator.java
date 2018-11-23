package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type.NerMention;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.type.TextClassificationOutcome;

import java.util.Collection;
import java.util.List;

/**
 * 
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * This annotates the spans for mentions in an already classified document.
 * It is required for MentionSpansEvaluationWriter
 */
@TypeCapability(
		inputs = {
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
				"de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
				"de.tudarmstadt.ukp.dkpro.tc.api.type.TextClassificationOutcome"},
		outputs = {
				"NerMention"
		})
public class NerMentionAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
		for(Sentence sentence : sentences){
			List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
			TextClassificationOutcome previous = null;
			int begin = 0;
			int end = 0;
			for (int i = 0; i < tokens.size(); i++) {
				Token token = tokens.get(i);
				
				List<TextClassificationOutcome> classOutcomes = JCasUtil.selectCovered(jCas, TextClassificationOutcome.class, token);
				TextClassificationOutcome classOutcome = classOutcomes.get(0);
				String outcomeClassType = classOutcome.getOutcome().replaceAll(".-", "");
				
				if(i == tokens.size()-1){
					// we reached the end of the sentence.
					if(previous != null){
						if(outcomeClassType.equals(previous.getOutcome().replaceAll(".-", ""))){
							end = token.getEnd();
							addMentionToCas(jCas, previous, begin, end);
						} else {
							addMentionToCas(jCas, previous, begin, end);
							addMentionToCas(jCas, classOutcome, token.getBegin(), token.getEnd());
						}
					} else {
						addMentionToCas(jCas, classOutcome, token.getBegin(), token.getEnd());
					}
					break;
				}

				
				if(previous == null){
					previous = classOutcome;
					begin = token.getBegin();
					end = token.getEnd();
					continue;
				}
				
				
				if(outcomeClassType.equals(previous.getOutcome().replaceAll(".-", ""))){
					previous = classOutcome;
					end = token.getEnd();
				} else {
					addMentionToCas(jCas, previous, begin, end);
					previous = classOutcome;
					begin = token.getBegin();
					end = token.getEnd();
				}
								
			}
		}
	}
	
	private void addMentionToCas(JCas jCas, TextClassificationOutcome outcome, int begin, int end){
		//ignore mentions labeled as other (OTH)
		if(outcome.getOutcome().equals("OTH")){
			return;
		}
		
		NerMention mention = new NerMention(jCas);
		mention.setClassType(outcome.getOutcome().replaceAll(".-", ""));
		mention.setBmeowTag(outcome.getOutcome().replaceAll("-.*", ""));
		mention.setBegin(begin);
		mention.setEnd(end);
		mention.addToIndexes();
	}
			
		
}
