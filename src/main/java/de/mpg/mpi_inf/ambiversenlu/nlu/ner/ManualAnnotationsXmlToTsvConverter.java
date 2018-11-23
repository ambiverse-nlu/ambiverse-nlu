package de.mpg.mpi_inf.ambiversenlu.nlu.ner;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Token;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes.UimaTokenizer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.Util;
import org.apache.uima.UIMAException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ManualAnnotationsXmlToTsvConverter {

    private static final Pattern NAMED_ENTITY_PATTERN = Pattern.compile(".*;(.*)");

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, MissingSettingException, EntityLinkingDataAccessException, ClassNotFoundException, UnprocessableDocumentException, NoSuchMethodException, UIMAException {

        if (args.length != 2) {
            throw new RuntimeException("Usage: <neDatasetPath> <xmlLocation>");
        }
        Path neDatasetPath = Paths.get(args[0]);
        if (Files.exists(neDatasetPath)) {
            Files.delete(neDatasetPath);
        }
        Files.createFile(neDatasetPath);
        String xmlLocation = args[1];

        System.setProperty("aida.conf", "default");
        
        Files.walk(Paths.get(xmlLocation), 1)
                .filter(p -> {
                    try {
                        return Files.isRegularFile(p) && !Files.isHidden(p);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(ManualAnnotationsXmlToTsvConverter::processDocument)
                .filter(Objects::nonNull)
                .forEach(l -> {
                    try {
                        Files.write(neDatasetPath, l, StandardOpenOption.APPEND);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });



    }

    private static List<String> processDocument(Path file) {
       try {
           Object[] objects = processXML(file.toString());
           String docText = (String) objects[0];
           Map<Integer, Object[]> docTypedSpots = (Map<Integer, Object[]>) objects[1];
           Tokens tokens = UimaTokenizer.tokenize(Language.getLanguageForString("en"), docText);
           List<List<Token>> sentenceTokens = tokens.getSentenceTokens();
           List<String> doc = new ArrayList<>();
           doc.add("-DOCSTART- (" + file.getFileName().toString().replace("-Named_Entity", "") + ")\n");

           for (List<Token> sentence : sentenceTokens) {
               int sentenceBegin = sentence.get(0).getBeginIndex();

               Map<Integer, Object[]> sentenceTypedSpots = extractSentenceTypedSpots(sentenceBegin, docTypedSpots);

               String sentenceText = docText.substring(sentenceBegin, sentence.get(sentence.size() - 1).getEndIndex());
               List<Integer> begins = sentence.stream().map(t -> t.getBeginIndex() - sentenceBegin).collect(Collectors.toList());
               List<Integer> ends = sentence.stream().map(t -> t.getEndIndex() - sentenceBegin).collect(Collectors.toList());

               String result = Util.produceAidaSentence(sentenceTypedSpots, begins, ends, sentenceText);
               if ("".equals(result)) {
                   continue;
               }
               doc.add(result);
           }
           if (doc.size() < 2) {
               return null;
           }
           return doc;
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }

    private static Map<Integer, Object[]> extractSentenceTypedSpots(int sentenceBegin, Map<Integer, Object[]> docTypedSpots) {
        return docTypedSpots.keySet()
                .stream()
                .filter(b -> b >= sentenceBegin)
                .collect(Collectors.toMap(b -> b - sentenceBegin, b -> new Object[]{((Integer)docTypedSpots.get(b)[0]) - sentenceBegin,
                        docTypedSpots.get(b)[1]}));
    }

    private static Object[] processXML(String xmlLocation) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new File(xmlLocation));

        Node body = doc.getElementsByTagName("body").item(0);
        String docText = body.getTextContent().trim();

//        identifying typed spots
        Map<Integer, Object[]> docTypedSpots = new HashMap<>();
        int cursor = 0;
        int childNumber = body.getChildNodes().getLength();
        for (int i = 0; i < childNumber; i++) {
            int nodeLength = body.getChildNodes().item(i).getTextContent().length();

            if (body.getChildNodes().item(i).getAttributes() != null) {
                String rawNamedEntityType = body.getChildNodes().item(i).getAttributes().getNamedItem("features").getNodeValue();
//            named_entity;organization
                Matcher matcher = NAMED_ENTITY_PATTERN.matcher(rawNamedEntityType);

                if (matcher.matches()) {
                    docTypedSpots.put(cursor - 1, new Object[]{cursor + nodeLength - 1, matcher.group(1).toUpperCase()});
                }
            }
            cursor += nodeLength;
        }

        assert docText.length() == cursor - 2;

        return new Object[]{docText, docTypedSpots};
    }
}
