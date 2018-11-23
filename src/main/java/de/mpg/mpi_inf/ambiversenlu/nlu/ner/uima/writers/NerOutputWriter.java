/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.writers;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.CasConsumer_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.TextClassificationOutcome;

import java.io.*;
import java.util.Collection;
import java.util.List;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 * 
 * Writes a simple text representation of the ground truth and the annotated data to a file.
 *
 */
public class NerOutputWriter extends CasConsumer_ImplBase {
	
	public static final String PARAM_OUTPUT_FILE = "outputFile";
	@ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true, defaultValue = "")
	private File outputFile;

	private PrintWriter out;
	
	private int iCas;
	
	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		
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
		processView(initialView);
//	    Iterator<CAS> viewIt = aCAS.getViewIterator();
//		while (viewIt.hasNext()) {
//	      CAS view = viewIt.next();
//	      processView(view);
//
//	    }
	    
	    out.println("======== CAS " + iCas + " end ==================================");
	    out.println();
	    out.println();
	    out.flush();

	    iCas++;
	    
	}

	/**
	 * @param view
	 */
	private void processView(CAS view) {
		out.println("-------- View " + view.getViewName() + " begin ----------------------------------");
	    out.println();
		
	    try {
			JCas jCas = view.getJCas();
			Collection<DocumentMetaData> metaData = JCasUtil.select(jCas, DocumentMetaData.class);
			
			String title = metaData.iterator().next().getDocumentTitle();
			
			out.print("Document Title: " + title);
			out.println();
			out.println();
			
			Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
			for(Sentence sentence : sentences){
				List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
				// print ground truth
				boolean inside = false;
				for(Token token : tokens){
					List<NamedEntity> ners = JCasUtil.selectCovering(jCas, NamedEntity.class, token);
					if(ners.isEmpty()){
						if(inside == true){
							out.print("] ");
							inside = false;
						} else {
							out.print(" ");
						}
					} else {
						if(inside == false){
							inside = true;
							NamedEntity ne = ners.get(0);
							out.print(" ");
							out.print(ne.getValue());
							out.print("[");
						} else {
							out.print(" ");
						}
					}
					out.print(token.getCoveredText());
				}
				if(inside){
					out.print("]");
				}
				out.println();
				
				// print classification outcome
				inside = false;
				for(Token token : tokens){
					List<TextClassificationOutcome> classOutcomes = JCasUtil.selectCovered(jCas, TextClassificationOutcome.class, token);
					TextClassificationOutcome classOutcome = classOutcomes.get(0);
					if(classOutcome.getOutcome().equals("OTH")){
						if(inside == true){
							out.print("] ");
							inside = false;
						} else {
							out.print(" ");
						}
					} else {
						if(inside == false){
							inside = true;
							out.print(" ");
							out.print(classOutcome.getOutcome().replaceAll(".-", ""));
							out.print("[");
						} else {
							out.print(" ");
						}
					}
					out.print(token.getCoveredText());
				}
				if(inside){
					out.print("]");
				}
				out.println();
				out.println();
			}
			
		} catch (CASException e) {
			e.printStackTrace();
		}
	    
	    
	    out.println("-------- View " + view.getViewName() + " end ----------------------------------");
	    out.println();
	}

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        super.collectionProcessComplete();

        out.close();
    }
}
