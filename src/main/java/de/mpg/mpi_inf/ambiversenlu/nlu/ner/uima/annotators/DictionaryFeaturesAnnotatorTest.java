/**
 * 
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators;

import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiReader;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.pipeline.SimplePipeline;

import java.io.IOException;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 */
public class DictionaryFeaturesAnnotatorTest {
	
	public static void main(String[] args) throws UIMAException, IOException {
        if (args.length < 3) {
            System.out.println("Necessary parameters: <location of the xmi> <name of the xmi> <location of the dictionaries>");
        }
		CollectionReaderDescription reader = createReaderDescription(XmiReader.class, XmiReader.PARAM_LANGUAGE, "en",
				XmiReader.PARAM_SOURCE_LOCATION, args[0],
				XmiReader.PARAM_PATTERNS, args[1]);
		
		AnalysisEngineDescription dictAnnotator = createEngineDescription(DictionaryFeaturesAnnotator.class);
		
		AnalysisEngineDescription writer = createEngineDescription(CasDumpWriter.class,
				CasDumpWriter.PARAM_OUTPUT_FILE, "files/DictionaryFeaturesAnnotatorTest" + System.currentTimeMillis() + ".txt");

		SimplePipeline.runPipeline(reader, dictAnnotator, writer);
		
	}
}
