package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.readers;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.UIMAFramework;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.io.TCReaderSequence;
import org.dkpro.tc.api.type.TextClassificationOutcome;
import org.dkpro.tc.api.type.TextClassificationSequence;
import org.dkpro.tc.api.type.TextClassificationTarget;

import java.io.IOException;
import java.util.List;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

/**
 *
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * An extension to the Conll2003Reader to be able to use it with DkPro-tc.
 *
 */
public class Conll2003ReaderTcBmeow extends ResourceCollectionReaderBase implements TCReaderSequence {



    public static final String READER = "readerClassName";
    @ConfigurationParameter(name = READER, mandatory = true)
    private Class readerClassName;

    private CollectionReader reader;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        Object[] configurationParams = getConfigurationParams(aContext);
        CollectionReaderDescription readerDescription = createReaderDescription(readerClassName,
                configurationParams);
        reader = UIMAFramework.produceCollectionReader(readerDescription, getResourceManager(), null);
    }

    private Object[] getConfigurationParams(UimaContext aContext) {
        Object[] params = new Object[aContext.getConfigParameterNames().length * 2];
        int i = 0;
        for (String name : aContext.getConfigParameterNames()) {
            params[2*i] = name;
            params[2*i+1] = aContext.getConfigParameterValue(name);
            i++;
        }
        return params;
    }

    @Override
	public void getNext(CAS cas) throws IOException, CollectionException {
		reader.getNext(cas);

		JCas jcas;
        try {
            jcas = cas.getJCas();
        }
        catch (CASException e) {
            throw new CollectionException(e);
        }

		for(Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
			TextClassificationSequence sequence = new TextClassificationSequence(jcas, sentence.getBegin(),
					sentence.getEnd());
			sequence.addToIndexes();

			for(Token token : JCasUtil.selectCovered(jcas, Token.class, sentence)){
				TextClassificationTarget unit = new TextClassificationTarget(jcas, token.getBegin(), token.getEnd());
				unit.setSuffix(token.getCoveredText());
				unit.addToIndexes();

				TextClassificationOutcome outcome = new TextClassificationOutcome(jcas, token.getBegin(), token.getEnd());
				outcome.setOutcome(getTextClassificationOutcome(jcas, unit));
				outcome.addToIndexes();
			}

		}
	}

	@Override
	public String getTextClassificationOutcome(JCas jcas, TextClassificationTarget unit) throws CollectionException {
		List<NamedEntity> neList = JCasUtil.selectCovering(jcas, NamedEntity.class, unit);
		StringBuffer outcome = new StringBuffer();
		if(neList.size() == 1){
			NamedEntity ne = neList.get(0);
			List<Token> tokens = JCasUtil.selectCovered(jcas, Token.class, ne);

			if(tokens.size() == 1){
				outcome.append("W-");
			} else {
				for (int i = 0; i < tokens.size(); i++) {
					if(tokens.get(i).getCoveredText().equals(unit.getCoveredText())
							&& tokens.get(i).getBegin() == unit.getBegin()){
						if(i == 0){
							outcome.append("B-");
						} else if(i < tokens.size() - 1){
							outcome.append("M-");
						} else {
							outcome.append("E-");
						}
					}
				}
			}
			outcome.append(ne.getValue());

		} else if(neList.size() == 0){
			outcome.append("OTH");
		} else {
			throw new CollectionException(
					new Throwable("Could not get unique NER annotation to be used as TC outome. List size: " + neList.size() + " " + unit.getCoveredText()));
		}

		return outcome.toString();
	}

    @Override
    public boolean hasNext() throws IOException, CollectionException {
        return reader.hasNext();
    }
}
