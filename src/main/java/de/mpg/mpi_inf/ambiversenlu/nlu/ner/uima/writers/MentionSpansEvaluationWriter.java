/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.writers;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type.NerMention;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import org.apache.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.CasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.*;
import java.util.*;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 * 
 * Outputs a file with stats about mention spans evaluations for each file and the 
 * whole document collection.
 *
 * REQUIREMENTS: You need to run NerMentionAnnotator first.
 * You need to specify the output directory by setting the parameter
 * MentionSpansEvaluationWriter.PARAM_OUTPUT_FILE
 */
public class MentionSpansEvaluationWriter extends CasConsumer_ImplBase {
	
	private static enum Type { PER, LOC, ORG, MISC, TOTAL, SOFT};
	
	/**
	 * metrics that are being recorded
	 */
	private static enum Spans { TRUE_POSITIVES, FALSE_POSITIVES, RELEVANT };
	
	/**
	 * results of the cas evaluations
	 */
	private List<Map<String, Integer>> results;
	
	public static final String PARAM_OUTPUT_FILE = "outputFile";
	@ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true, defaultValue = "")
	private File outputFile;

	/**
	 * output writer
	 */
	private PrintWriter out;
	
	/**
	 * number of current cas
	 */
	private int iCas;
	
	private Logger logger;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
		results = new ArrayList<>();
		
		logger = Logger.getLogger(getClass());
		
		try {
			out = new PrintWriter(new  OutputStreamWriter(new FileOutputStream(outputFile)));
		} catch (FileNotFoundException e) {
			throw new ResourceInitializationException(e);
		}
	}
	
	@Override
	public void process(CAS aCAS) throws AnalysisEngineProcessException {
		
		out.println("======== CAS " + iCas + " begin ==================================");
	    out.println();

		CAS initialView = aCAS.getView("_InitialView");
		Map<String, Integer> result = processView(initialView);
		results.add(result);
//	    Iterator<CAS> viewIt = aCAS.getViewIterator();
//	    while (viewIt.hasNext()) {
//	      CAS view = viewIt.next();
//	      Map<String, Integer> result = processView(view);
//	      results.add(result);
//	    }
	    
	    out.println("======== CAS " + iCas + " end ==================================");
	    out.println();
	    out.println();
	    out.flush();
	    
	    
	    iCas++;
	    
	}

	private Map<String, Integer>  processView(CAS view) {
		out.println("-------- View " + view.getViewName() + " begin ----------------------------------");
	    out.println();
	    
	    int totalCorrect = 0;
	    int totalIncorrect = 0;
	    
	    double precision = 0.0;
	    double recall = 0.0;
	    double f1 = 0.0;
	    
	    int totalNes = 0;
	    
	    int perIncorrect = 0;
	    int orgIncorrect = 0;
	    int locIncorrect = 0;
	    int miscIncorrect = 0;
	    
	    int softCorrect = 0;
	    int softIncorrect = 0;
	    
	    try {
			JCas jCas = view.getJCas();
			Collection<DocumentMetaData> metaData = JCasUtil.select(jCas, DocumentMetaData.class);
			
			String title = metaData.iterator().next().getDocumentTitle();
			
			out.print("Document Title: " + title);
			out.println();
			out.println();
			
			Collection<NamedEntity> nerGold = JCasUtil.select(jCas, NamedEntity.class);
			Map<Integer, NamedEntity> beginIdxMap = new HashMap<>();
			nerGold.stream().forEach(m -> beginIdxMap.put(m.getBegin(), m));
			
			Collection<NamedEntity> goldStandard = JCasUtil.select(jCas, NamedEntity.class);
			Collection<NerMention> classifications = JCasUtil.select(jCas, NerMention.class);
//			nes.stream().forEach(e -> out.println(e.getValue() + ": " + e.getBegin() + ", " + e.getEnd()));
			
			logger.trace("Number annoations in gold standard: " + goldStandard.size());
			logger.trace("Number classifactions: " + classifications.size());
			
			for (NerMention classification : classifications) {
				// soft overlap
				List<NamedEntity> coveringMention = JCasUtil.selectCovering(jCas, NamedEntity.class, classification);
				if(coveringMention.isEmpty()){
					softIncorrect++;
				} else {
					softCorrect++;
				}
				
				// exact overlap
				NamedEntity gold = beginIdxMap.get(classification.getBegin());
				if(gold != null){
					if(gold.getEnd() == classification.getEnd()){
						logger.trace(gold.getValue() + ", " + classification.getClassType() + ": correct");
						totalCorrect++;
					} else {
						logger.trace(gold.getValue() + ", " + classification.getClassType() + ": incorrect");
						totalIncorrect++;
						if(gold.getValue().equals("PER")){
							perIncorrect++;
						} else if (gold.getValue().equals("ORG")){
							orgIncorrect++;
						} else if (gold.getValue().equals("LOC")){
							locIncorrect++;
						} else if (gold.getValue().equals("MISC")){
							miscIncorrect++;
						}
					}
				} else {
					logger.trace(classification.getClassType() + ": no mention found in gold standard with begin index: " + classification.getBegin());
					totalIncorrect++;
					String tag = classification.getBmeowTag().replaceAll(".-", "");
					if(tag.equals("PER")){
						perIncorrect++;
					} else if (tag.equals("ORG")){
						orgIncorrect++;
					} else if (tag.equals("LOC")){
						locIncorrect++;
					} else if (tag.equals("MISC")){
						miscIncorrect++;
					}
				}
			}
			totalNes = goldStandard.size();
			precision = totalCorrect/(double)(totalCorrect+totalIncorrect);
			recall = totalCorrect / (double) totalNes;
			f1 = 2 * ((precision * recall) / (precision + recall));
			
		} catch (CASException e) {
			e.printStackTrace();
		}
			   
	    
	    out.println("==== EVALUATION RESULTS begin =====");
	    out.println();
	    out.println("Correctly identified mentions: " + totalCorrect);
	    out.println("Incorrectly identified mentions: " + totalIncorrect);
	    out.println("Precision: " + precision);
	    out.println("Recall: " + recall);
	    out.println("F1: " + f1);
	    out.println();
	    out.println("Incorrectly identified PERSONS:" + perIncorrect);
	    out.println("Incorrectly identified ORGANIZATIONS:" + orgIncorrect);
	    out.println("Incorrectly identified LOCATIONS:" + locIncorrect);
	    out.println("Incorrectly identified MISC:" + miscIncorrect);
	    out.println();
	    out.println("Correctly identified mentions (soft): " + softCorrect);
	    out.println("Incorrectly identified mentions (soft): " + softIncorrect);
	    out.println();
	    out.println("==== EVALUATION RESULTS end =====");
	    
	    out.println("-------- View " + view.getViewName() + " end ----------------------------------");
	    out.println();
	    
	    Map<String, Integer> result = new HashMap<>();
	    result.put(Type.TOTAL + Spans.TRUE_POSITIVES.toString(), totalCorrect);
	    result.put(Type.TOTAL + Spans.FALSE_POSITIVES.toString(), totalIncorrect);
	    result.put(Type.TOTAL + Spans.RELEVANT.toString(), totalNes);
	    
	    result.put(Type.PER + Spans.FALSE_POSITIVES.toString(), perIncorrect);
	    result.put(Type.ORG + Spans.FALSE_POSITIVES.toString(), orgIncorrect);
	    result.put(Type.LOC + Spans.FALSE_POSITIVES.toString(), locIncorrect);
	    result.put(Type.MISC + Spans.FALSE_POSITIVES.toString(), miscIncorrect);
	    
	    result.put(Type.SOFT + Spans.TRUE_POSITIVES.toString(), softCorrect);
	    result.put(Type.SOFT + Spans.FALSE_POSITIVES.toString(), softIncorrect);
	    
	    return result;
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		super.collectionProcessComplete();
		
		int correct = 0;
		int incorrect = 0;
		double precision = 0.0;
		double recall = 0.0;
		double f1 = 0.0;
		
		int totalRelevant = 0;

		int perIncorrect = 0;
	    int orgIncorrect = 0;
	    int locIncorrect = 0;
	    int miscIncorrect = 0;
		
		int softCorrect = 0;
	    int softIncorrect = 0;
		
		for (Map<String, Integer> map : results) {
			correct += map.get(Type.TOTAL + Spans.TRUE_POSITIVES.toString());
			incorrect += map.get(Type.TOTAL + Spans.FALSE_POSITIVES.toString());
			totalRelevant += map.get(Type.TOTAL + Spans.RELEVANT.toString());
			perIncorrect += map.get(Type.PER + Spans.FALSE_POSITIVES.toString());
			orgIncorrect += map.get(Type.ORG + Spans.FALSE_POSITIVES.toString());
			locIncorrect += map.get(Type.LOC + Spans.FALSE_POSITIVES.toString());
			miscIncorrect += map.get(Type.MISC + Spans.FALSE_POSITIVES.toString());
			softCorrect += map.get(Type.SOFT + Spans.TRUE_POSITIVES.toString());
			softIncorrect += map.get(Type.SOFT + Spans.FALSE_POSITIVES.toString());
		}
		
		precision = correct/(double)(correct+incorrect);
		recall = correct / (double) totalRelevant;
		f1 = 2 * ((precision * recall) / (precision + recall));
		
		out.println("==== COLLECTION EVALUATION RESULTS begin =====");
	    out.println();
	    out.println("Correctly identified mentions: " + correct);
	    out.println("Incorrectly identified mentions: " + incorrect);
	    out.println("Precision: " + precision);
	    out.println("Recall: " + recall);
	    out.println("F1: " + f1);
	    out.println();
	    out.println("Incorrectly identified PERSONS:" + perIncorrect);
	    out.println("Incorrectly identified ORGANIZATIONS:" + orgIncorrect);
	    out.println("Incorrectly identified LOCATIONS:" + locIncorrect);
	    out.println("Incorrectly identified MISC:" + miscIncorrect);
	    out.println();
	    out.println("Correctly identified mentions (soft): " + softCorrect);
	    out.println("Incorrectly identified mentions (soft): " + softIncorrect);
	    out.println();
	    out.println("==== COLLECTION EVALUATION RESULTS end =====");
	    out.flush();

        out.close();
	}
}
