package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.writers;

import de.mpg.mpi_inf.ambiversenlu.nlu.ner.evaluation.ConllEvaluation;
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
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.TextClassificationOutcome;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PredictionsWriter extends CasConsumer_ImplBase {

    public static final String PARAM_MENTION_OUTPUT_FILE = "mentionOutputFile";
    @ConfigurationParameter(name = PARAM_MENTION_OUTPUT_FILE, mandatory = true, defaultValue = "")
    private File mentionOutputFile;

    public static final String PARAM_TOKEN_OUTPUT_FILE = "tokenOutputFile";
    @ConfigurationParameter(name = PARAM_TOKEN_OUTPUT_FILE, mandatory = true, defaultValue = "")
    private File tokenOutputFile;

    public static final String PARAM_LANGUAGE = "language";
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;


    public static final String PARAM_KNOW_NER = "knowNER";
    @ConfigurationParameter(name = PARAM_KNOW_NER, mandatory = true, defaultValue = "false")
    private boolean knowNER;

    public static final String PARAM_NAMED_ENTITY_PER_TOKEN = "namedEntityPerToken";
    @ConfigurationParameter(name = PARAM_NAMED_ENTITY_PER_TOKEN, mandatory = false, defaultValue = "false")
    private boolean namedEntityPerToken;

    public static final String PARAM_POSITION_TYPE = "positionType";
    @ConfigurationParameter(name = PARAM_POSITION_TYPE, mandatory = true)
    private ConllEvaluation.TrainedPositionType positionType;

    private List<String> out = new ArrayList<>();
//    private List<String> verboseOut = new ArrayList<>();

    private int iCas;


    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

//        try {
//            out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(mentionOutputFile)));
//        } catch (FileNotFoundException e) {
//            throw new ResourceInitializationException(e);
//        }
        out.add("#Gold  Prediction");
    }

    @Override
    public void process(CAS aCAS) throws AnalysisEngineProcessException {

        try {
            JCas jCas = aCAS.getJCas();

            Collection<Sentence> sentences = JCasUtil.select(jCas, Sentence.class);
            for (Sentence sentence : sentences) {
                List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class, sentence);
                Class<? extends Annotation> goldAnnotation;
                Class<? extends Annotation> predAnnotation;
                if (knowNER) {
                    goldAnnotation = NamedEntity.class;
                    predAnnotation = TextClassificationOutcome.class;
                } else {
                    goldAnnotation = TextClassificationOutcome.class;
                    predAnnotation = NamedEntity.class;
                }

                for (Token token : tokens) {

                    String line = getTag(goldAnnotation, jCas, token) + "\t" + getTag(predAnnotation, jCas, token);
                    out.add(line);
//                    verboseOut.add(token.getCoveredText() + "\t" + line);

                }
//                verboseOut.add("\n");
                out.add("\n");
            }
//            System.out.println(out);
        }catch (CASException e) {
            e.printStackTrace();
        }
//        out.flush();
    }

    private <T extends Annotation> String getTag(Class<T> clazz, JCas jCas, Token token) {
        List<T> annotationList = JCasUtil.selectCovering(jCas, clazz, token);

        String value = annotationList.isEmpty()? "OTH" : clazz == NamedEntity.class?
                ((NamedEntity)annotationList.get(0)).getValue():
                ((TextClassificationOutcome)annotationList.get(0)).getOutcome();

        if (!"OTH".equals(value)) {

//                        spanish uses different signs for class labels...
            if ("es".equals(language)) {
                value = value.replace("LUG", "LOC")
                        .replace("OTROS", "MISC")
                        .replace("PERS", "PER");
            }
            value = value.replace("ORGANIZATION", "ORG")
                    .replace("LOCATION", "LOC")
                    .replace("PERSON", "PER");
//                        todo add chunk tags if they are not predicted rethink about spanish!
//                        if (!value.contains("-")) {
//
//                            value = ("OTH".equals(prevNE) || !prevNE.contains(value) ? "B-" : "I-") + value;
//                        }
        }
        return value;
    }

    @Override
    public void collectionProcessComplete() throws AnalysisEngineProcessException {
        super.collectionProcessComplete();
//        out.close();
//        try {
//            Files.write(Paths.get("predictions_" + mentionOutputFile.getName().replace("ConllMentionEvaluation_", "") + ".txt"), verboseOut);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            new ConllEvaluation(tokenOutputFile, mentionOutputFile, positionType, true)
                    .run(out);
        } catch (IOException e) {
            throw new AnalysisEngineProcessException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
