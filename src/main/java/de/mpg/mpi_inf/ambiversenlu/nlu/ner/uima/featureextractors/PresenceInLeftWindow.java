package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.featureextractors;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERLanguage;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.dkpro.tc.api.exception.TextClassificationException;
import org.dkpro.tc.api.features.Feature;
import org.dkpro.tc.api.type.TextClassificationTarget;

import java.util.Set;

/**
 * 
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 * 
 * Extracts a binary feature that indicates whether the current token or lemma (depends on language)
 * is present in the left window of size PARAM_PRESENCE_WINDOW_SIZE.
 * 
 * REQUIREMENTS: You need to set the parameter 
 * PresenceInLeftWindow.PARAM_PRESENCE_WINDOW_SIZE, default is 4.
 *
 */
public class PresenceInLeftWindow extends SynchronizedTcuLookUpTable{
	
	public static final String FEATURE_NAME = "presenceInLeftWindow";
	
	public static final String PARAM_PRESENCE_WINDOW_SIZE = "presenceWindowSize";
	@ConfigurationParameter(name = PARAM_PRESENCE_WINDOW_SIZE, mandatory = true, defaultValue = "4")
	protected int presenceWindowSize;
	
	@Override
	public Set<Feature> extract(JCas aView, TextClassificationTarget unit)
			throws TextClassificationException {
		super.extract(aView, unit);
		
		Integer idx = unitBegin2IdxTL.get().get(unit.getBegin());
		boolean featureVal = KnowNERLanguage.requiresLemma(aView.getDocumentLanguage())?
				lemmaPresentInLeftWindow(aView, idx) : presentInLeftWindow(idx);
		
		return new Feature(FEATURE_NAME, featureVal).asSet();
	}

	private boolean lemmaPresentInLeftWindow(JCas aView, Integer idx) {
		String current = JCasUtil.selectCovered(aView, Lemma.class, unitsTL.get().get(idx)).get(0).getValue();

		int i = 0;
		while(--idx >= 0 && i < presenceWindowSize){
			if(JCasUtil.selectCovered(aView, Lemma.class, unitsTL.get().get(idx)).get(0).getValue()
					.equals(current)){
				return true;
			}
			i++;
		}

		return false;
	}

	private boolean presentInLeftWindow(Integer idx) {
		String current = unitsTL.get().get(idx).getCoveredText();
//		System.out.print(current + ": ");
		
		int i = 0;
		while(--idx >= 0 && i < presenceWindowSize){
//			System.out.print(units.get(idx) + " ");
			if(unitsTL.get().get(idx).getCoveredText().equals(current)){
//				System.out.println("true");
				return true;
			}
			i++;
		}
//		System.out.println();
		
		return false;
	}

}
