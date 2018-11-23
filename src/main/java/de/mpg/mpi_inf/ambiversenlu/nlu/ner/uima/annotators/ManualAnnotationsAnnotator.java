package de.mpg.mpi_inf.ambiversenlu.nlu.ner.uima.annotators;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.tc.api.type.TextClassificationOutcome;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class ManualAnnotationsAnnotator extends JCasAnnotator_ImplBase {

    public static final String MANUAL_ANNOTATIONS_LOCATION = "manualAnnotationsLocation";
    @ConfigurationParameter(name = MANUAL_ANNOTATIONS_LOCATION, mandatory = true)
    private String manualAnnotationsLocation;

    public static final String PARAM_NAMED_ENTITY_PER_TOKEN = "namedEntityPerToken";
    @ConfigurationParameter(name = PARAM_NAMED_ENTITY_PER_TOKEN, mandatory = false, defaultValue = "false")
    private boolean namedEntityPerToken;

    private Map<String, List<NamedEntityData>> manualDocMap;

    class NamedEntityData {
        int begin;
        int end;
        String value;
    }

    /**
     * Read a single sentence.
     */
    private List<String[]> readSentence(String[] documentTitle, BufferedReader reader)
            throws IOException, CollectionException {

        List<String[]> words = new ArrayList<>();
        String line;

        while ((line = reader.readLine()) != null) {
            assert words.isEmpty();
            if (line.contains("DOCSTART")) {
                documentTitle[0] = parseDocId(line);
                return words;
//            line = reader.nextLine();
            }
            if (StringUtils.isBlank(line)) {
                return words; // End of sentence
            }
            String[] fields = line.split("\t");
            words.add(fields);

        }
        return null;
    }

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        manualDocMap = extractManualDocuments((String) aContext.getConfigParameterValue("manualAnnotationsLocation"),
                (boolean) aContext.getConfigParameterValue("namedEntityPerToken"));
    }

    // should be called only if there is DOCSTART in the line
    private static String parseDocId(String line) {
        if (line == null || !line.contains("DOCSTART")) {
            throw new RuntimeException("");
        }

        return line.replaceAll("-DOCSTART- \\(", "").replaceAll("\\)", "");
    }

    private Map<String, List<NamedEntityData>> extractManualDocuments(String manualAnnotationsLocation, boolean namedEntityPerToken) {
        Map<String, List<NamedEntityData>> manualDocMap0 = new HashMap<>();
        int i = 0;
        try {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(manualAnnotationsLocation), "UTF-8"))) {

                String firstLine = reader.readLine();
                if (firstLine == null) {
                    throw new RuntimeException("File " + manualAnnotationsLocation + " is empty!");
                }
                String[] documentTitle = new String[]{parseDocId(firstLine)};
                ArrayList<NamedEntityData> documentNED = new ArrayList<>();
                manualDocMap0.put(documentTitle[0], documentNED);
                int cursor = 0;

                List<String[]> words;
                while ((words = readSentence(documentTitle, reader)) != null) {
                    if (words.isEmpty()) {
                        continue;
                    }
                    if (!manualDocMap0.containsKey(documentTitle[0])) {
                        documentNED = new ArrayList<>();
                        manualDocMap0.put(documentTitle[0], documentNED);
                        cursor = 0;
                    }

                    String prevType = "OTH";
                    NamedEntityData ne = null;
                    String type;

                    for (String[] word : words) {
                        word[0] = word[0]
                                .replace(":", "")
                                .replace("\\", "/");

                        if (word.length > 1) {
                            String position = word[1];
                            type = word[2];

                            if (position.equals("B") || "OTH".equals(prevType) || !type.equals(prevType)) {
                                ne = new NamedEntityData();
                                ne.begin = cursor;
                                ne.value = (namedEntityPerToken ? position + "-" : "") + type;
                                ne.end = cursor + word[0].length();
                                documentNED.add(ne);
                            } else {
                                if (namedEntityPerToken) {
                                    ne = new NamedEntityData();
                                    ne.begin = cursor;
                                    ne.value = position + "-" + type;
                                    documentNED.add(ne);
                                } else if (!ne.value.equals(type)) {
                                    throw new RuntimeException("Wrong chunks order detected! " + Arrays.toString(word));
                                }
                                ne.end = cursor + word[0].length();
                            }

//                            documentNED.add(ne);
                        } else {
                            type = "OTH";
                        }
                        prevType = type;
//                        System.out.println(word[0] + " - " + cursor + ":" + word[0].length() + 1);
                        cursor += word[0].length() + 1;
                        i++;
                    }
                    cursor++;
                }

            }
        } catch (IOException | CollectionException e) {
            e.printStackTrace();
        }
        System.out.println("total named entity data: " + manualDocMap0
                .keySet()
                .stream()
                .mapToInt(k -> manualDocMap0.get(k).size())
                .sum());
        return manualDocMap0;
    }

    @Override
    public void process(JCas jcas) throws AnalysisEngineProcessException {

        DocumentMetaData documentMetaData = JCasUtil.select(jcas, DocumentMetaData.class).iterator().next();

        Collection<NamedEntity> tcoNamedEntities = JCasUtil.select(jcas, NamedEntity.class);
        Map<Integer, NamedEntity> tcoNamedEntityMap = tcoNamedEntities.stream()
                .collect(Collectors.toMap(Annotation::getBegin, n -> n));
        jcas.removeAllExcludingSubtypes(NamedEntity.typeIndexID);

        Collection<Token> tokens = JCasUtil.select(jcas, Token.class);

        for (Token token : tokens) {
            String value;
            if (tcoNamedEntityMap.containsKey(token.getBegin())) {
                NamedEntity ne = tcoNamedEntityMap.get(token.getBegin());
                value = ne.getValue();
            } else {
                value = "OTH";
            }
            TextClassificationOutcome tco = new TextClassificationOutcome(jcas, token.getBegin(), token.getEnd());
            tco.setOutcome(value);
            tco.addToIndexes();
        }

        List<NamedEntityData> namedEntityData = manualDocMap.get(documentMetaData.getDocumentId());

        for (NamedEntityData ned : namedEntityData) {
            NamedEntity ne = new NamedEntity(jcas, ned.begin, ned.end);
            ne.setValue(ned.value);
            ne.addToIndexes();
        }
    }
}
