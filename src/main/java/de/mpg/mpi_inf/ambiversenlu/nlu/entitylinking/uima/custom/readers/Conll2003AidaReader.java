package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.type.PositionInEntity;
import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.ViewCreatorAnnotator;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@TypeCapability(outputs = {"de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
        "de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity",
        "mpi.aida.uima.type.Entity"})
public class Conll2003AidaReader extends ResourceCollectionReaderBase {

    /**
     * Character encoding of the input data.
     */
    public static final String PARAM_ENCODING = ComponentParameters.PARAM_SOURCE_ENCODING;
    @ConfigurationParameter(name = PARAM_ENCODING, mandatory = false, defaultValue = "UTF-8")
    private String encoding;

    /**
     * The language.
     */
    public static final String PARAM_LANGUAGE = ComponentParameters.PARAM_LANGUAGE;
    @ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = false)
    private String language;

    public static final String PARAM_MANUAL_TOKENS_NER = "evaluateRecognition";
    @ConfigurationParameter(name = PARAM_MANUAL_TOKENS_NER, mandatory = false)
    private boolean evaluateNER;

    public static final String PARAM_SINGLE_FILE = "isOneFile";
    @ConfigurationParameter(name = PARAM_SINGLE_FILE, mandatory = false)
    private boolean isOneFile;

    public static final String PARAM_GREEDY = "greedy";
    @ConfigurationParameter(name = PARAM_GREEDY, mandatory = false, defaultValue = "false")
    private boolean greedy;

    /**
     * Inclusive
     */
    public static final String PARAM_FIRSTDOCUMENT = "firstDocument";
    @ConfigurationParameter(name = PARAM_FIRSTDOCUMENT, mandatory = false)
    private int begin = 0;

    /**
     * Inclusive
     */
    public static final String PARAM_LASTDOCUMENT = "lastDocument";
    @ConfigurationParameter(name = PARAM_LASTDOCUMENT, mandatory = false)
    private int end = Integer.MAX_VALUE;

    public static final String PARAM_ORDER = "order";
    @ConfigurationParameter(name = PARAM_ORDER, defaultValue = "DEFAULT")
    private OrderType orderType;


    public static final String PARAM_SENTENCE_END = "sentenceEnd";
    @ConfigurationParameter(name = PARAM_SENTENCE_END, mandatory = true, defaultValue = "NEWLINE")
    private SentenceEndType sentenceEnd;

    public static final String PARAM_NAMED_ENTITY_PER_TOKEN = "namedEntityPerToken";
    @ConfigurationParameter(name = PARAM_NAMED_ENTITY_PER_TOKEN, mandatory = false, defaultValue = "false")
    private boolean namedEntityPerToken;
    
    private List<String> goldStandardList;
    private boolean goldStandardFlush = false;

    public enum SentenceEndType {NEWLINE, DOT}

    private Scanner reader;
    private String nextDocId;
    private int current = 0;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        if (begin > end) {
            throw new ResourceInitializationException();
        }

        super.initialize(aContext);
    }

    private void convert(CAS aCAS) throws IOException, CollectionException, AnalysisEngineProcessException, CASException, NoSuchMethodException, MissingSettingException, ClassNotFoundException {
        JCas jcas;
        try {
            jcas = aCAS.getJCas();
        } catch (CASException e) {
            throw new CollectionException(e);
        }

        JCasBuilder doc = new JCasBuilder(jcas);

        JCas goldView = ViewCreatorAnnotator.createViewSafely(jcas, "gold");

        List<String[]> words;
        if (reader.hasNext()) {

            String docId;

//        the constrained range over documents is available only when isOneFile = true
            if (!isOneFile) {
                docId = parseDocId(reader.nextLine());
            } else if (nextDocId == null) {

                String line = null;


                while (current <= begin && reader.hasNext() && (line = reader.findWithinHorizon("-DOCSTART- \\(.*\\)", 0)) != null) {
                    current++;
                }
                if (current <= begin) {
                    throw new RuntimeException("Begin " + begin + " is out of range of the jcas (" + current + ")");
                }

                docId = parseDocId(line);

            } else {
                current++;
                docId = nextDocId;
            }

            DocumentMetaData documentMetaData = JCasUtil.select(jcas, DocumentMetaData.class).iterator().next();
            documentMetaData.setDocumentId(docId);
            documentMetaData.setDocumentTitle(docId);
        }

        while ((words = readSentence()) != null) {
            if (words.isEmpty()) {
                continue;
            }

            int sentenceBegin = doc.getPosition();
            int sentenceEnd = sentenceBegin;

            NamedEntity ne = null;
            Entity e = null;
            String prevType = "OTH";

            for (String[] word : words) {
//                this hint is necessary to let crfsuite version 0.12 run properly
                String word1 = orderType.getWord(word)
                        .replace(":", "/")
                        .replace("\\", "/");
                Token token = doc.add(word1, Token.class);
                sentenceEnd = token.getEnd();
                if (language == null ||
                        !language.equals("zh")) {
                    doc.add(" ");
                }

                token.addToIndexes();

                if (orderType == OrderType.DEFAULT) {
                    if(orderType.isEntity(word)) {
                        if (orderType.getPosition(word).equals("B")) {
                            ne = new NamedEntity(jcas);
                            ne.setBegin(token.getBegin());
                            if(word.length > 4) {
                                ne.setValue(orderType.getType(word));
                            }
                            ne.setEnd(token.getEnd());
                            if(!evaluateNER) {
                                ne.addToIndexes();
                            }

                            e = new Entity(goldView);
                            e.setID(orderType.getEntity(word));
                            e.setBegin(token.getBegin());
                            e.setEnd(token.getEnd());
                            e.addToIndexes();
                        } else {
                            ne.setEnd(token.getEnd());
                            e.setEnd(token.getEnd());
                        }
                    }
                } else {

                    if (!greedy) {
                        if (orderType.hasLemma()) {
                            Lemma lemma = new Lemma(jcas, token.getBegin(), token.getEnd());
                            lemma.setValue(orderType.getLemma(word));
                            lemma.addToIndexes();
                        }

                        if (orderType.hasPOS()) {
                            POS pos = new POS(jcas, token.getBegin(), token.getEnd());
                            pos.setPosValue(orderType.getPOS(word));
                            pos.addToIndexes();
                        }
                    }

                    String type;

                    if (orderType.isEntity(word)) {
                        String position = orderType.getPosition(word);
                        type = orderType.getType(word);
                        PositionInEntity positionInEntity = new PositionInEntity(jcas, token.getBegin(), token.getEnd());
                        positionInEntity.setPositionInEntity(position);
                        positionInEntity.addToIndexes();

                        if (position.equals("B") || "OTH".equals(prevType) || !type.equals(prevType)) {
                            ne = new NamedEntity(jcas);
                            ne.setBegin(token.getBegin());

                            ne.setValue((namedEntityPerToken? position + "-" : "") + type);
                            ne.setEnd(token.getEnd());
                            if (!evaluateNER) {
                                ne.addToIndexes();
                            }

                            if (!greedy && orderType.hasEntity()) {
                                e = new Entity(goldView);
                                e.setID(orderType.getEntity(word));
                                e.setBegin(token.getBegin());
                                e.setEnd(token.getEnd());
                                e.addToIndexes();
                            }

                        } else {

                            if (!greedy && orderType.hasEntity()) {
                                e.setEnd(token.getEnd());
                            }

                            if (namedEntityPerToken) {
                                ne = new NamedEntity(jcas);
                                ne.setBegin(token.getBegin());

                                ne.setValue(position + "-" + type);
                                if (!evaluateNER) {
                                    ne.addToIndexes();
                                }

                            } else if (!ne.getValue().equals(type)) {
                                throw new RuntimeException("Wrong chunks order detected! " + Arrays.toString(word));
                            }
                            ne.setEnd(token.getEnd());
                        }

                        if (goldStandardList != null) {
                            goldStandardList.add(position + "-" + type);
                        }
                    } else {
                        type = "OTH";

                        if (goldStandardList != null) {
                            goldStandardList.add(type);
                        }
                    }
                    prevType = type;
                }

            }

            Sentence sentence = new Sentence(jcas, sentenceBegin, sentenceEnd);
            sentence.addToIndexes();
            doc.add("\n");
        }
        doc.close();
        if (!greedy && orderType != OrderType.DEFAULT) {
            Document.Builder dbuilder = new Document.Builder();
            dbuilder.withText(jcas.getDocumentText()).withDisambiguationSettings(new DisambiguationSettings.Builder().build());
            Document ds = dbuilder.build();
            ds.addSettingstoJcas(jcas);
        }
    }

    /**
     * Read a single sentence.
     */
    private List<String[]> readSentence()
            throws IOException, CollectionException {
        if (!reader.hasNextLine()) {
            return null;
        }
        List<String[]> words = new ArrayList<>();
        String line;
        while (reader.hasNextLine()) {
            line = reader.nextLine();
            if (line.contains("DOCSTART")) {
                if (isOneFile) {
                    nextDocId = parseDocId(line);
                    return null;
                } else {
                    throw new RuntimeException("There are more than DOCSTART in one document!");
                }
            }
            if (StringUtils.isBlank(line)) {
                break; // End of sentence
            }
            String[] fields = line.split("\t");
            words.add(fields);

            if (sentenceEnd == SentenceEndType.DOT
                    && ".".equals(fields[0]) && !"dummy".equals(fields[1])) {
                break;
            }
        }
        return words;
    }

    // should be called only if there is DOCSTART in the line
    private static String parseDocId(String line) {
        if (line == null || !line.contains("DOCSTART")) {
            throw new RuntimeException("");
        }

        return line.replaceAll("-DOCSTART- \\(", "").replaceAll("\\)", "");
    }

    @Override
    public void getNext(CAS aCAS) throws IOException, CollectionException {
        if (!isOneFile || reader == null) {
            Resource res = nextFile();
            reader = new Scanner(new InputStreamReader(res.getInputStream(), encoding));
        }
        initCas(aCAS, null);

        try {
            convert(aCAS);
        } catch (AnalysisEngineProcessException | NoSuchMethodException | MissingSettingException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (CASException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initCas(CAS aCas, Resource aResource) {
        try {
            // Set the document metadata
            DocumentMetaData docMetaData = DocumentMetaData.create(aCas);
            docMetaData.setLanguage(language);
//      docMetaData.setDocumentTitle(new File(aResource.getPath()).getName());
//      docMetaData.setDocumentUri(aResource.getResolvedUri().toString() + qualifier);
//      docMetaData.setDocumentId("doc id");
//      if (aResource.getBase() != null) {
//        docMetaData.setDocumentBaseUri(aResource.getResolvedBase());
//        docMetaData.setCollectionId(aResource.getResolvedBase());
//      }

            // Set the document language
            aCas.setDocumentLanguage(language);
        } catch (CASException e) {
            // This should not happen.
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNext() throws IOException, CollectionException {
        if (isOneFile && reader != null && reader.hasNextLine()) {
            if (end != Integer.MAX_VALUE && current > end) {
                return false;
            }
            return true;
        } else {
            reader = null;
            return super.hasNext();
        }
    }

    @Override
    public void destroy() {
        IOUtils.closeQuietly(reader);
    }


    @Override
    public Progress[] getProgress() {
        // Auto-generated method stub
        return null;
    }


}
