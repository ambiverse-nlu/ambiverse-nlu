package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.util.nyt.NYTAnnotationReader;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.util.nyt.NYTCorpusDocument;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.util.nyt.NYTCorpusDocumentParser;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.NYTArticleMetaData;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.SalientEntity;
import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.util.nyt.NYTAnnotationReader.getFreebase2YagoIds;
import static de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.util.nyt.NYTAnnotationReader.getNytAnnotations;

public class NYTCollectionReader extends ResourceCollectionReaderBase {

    private Logger logger = LoggerFactory.getLogger(NYTCollectionReader.class);
    /**
     * A number of documents which will be skipped at the beginning.
     */
    public static final String PARAM_OFFSET = "offset";
    @ConfigurationParameter(name = PARAM_OFFSET, mandatory = false)
    private int offset = 0;

    public static final String PARAM_ANNOTATED_FILE = "annotatedFile";
    @ConfigurationParameter(name = PARAM_ANNOTATED_FILE, mandatory = false)
    private String annotatedFile;

    public static final String PARAM_ID_MAPPING_FILE = "idsMappingFile";
    @ConfigurationParameter(name = PARAM_ID_MAPPING_FILE, mandatory = false)
    private String idsMappingFile;


    private Map<Integer, Map<Integer, NYTAnnotationReader.NYTMention>> nytAnnotations = null;
    private Map<String, String> fb2yagoIds = null;

    /**
     * Counting variable to keep track of the already skipped documents.
     */
    private int skipped = 0;
    private int missed = 0;
    private int total = 0;

    private NYTCorpusDocumentParser nytParser = new NYTCorpusDocumentParser();

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);
        synchronized (NYTCollectionReader.class) {
            if (annotatedFile != null && nytAnnotations == null) {
                try {
                    logger.info("Loading NYT training annotations ...");
                    nytAnnotations = getNytAnnotations(annotatedFile);
                } catch (IOException e) {
                    throw new ResourceInitializationException(e);
                }
            }
            if(idsMappingFile!=null && fb2yagoIds == null) {
                try {
                    logger.info("Loading Freebase to YAGO id mappings ...");
                    fb2yagoIds = getFreebase2YagoIds(idsMappingFile);
                } catch (IOException e) {
                    throw new ResourceInitializationException(e);
                }
            }
        }
    }

    private void setDocumenText(JCas aJCas, String documentBody) {
        if (documentBody != null) {
            aJCas.setDocumentText(documentBody);
        } else {
            aJCas.setDocumentText("");
        }
    }



    @Override
    public void getNext(CAS cas) throws IOException, CollectionException {
        while (isBelowOffset()) {
            nextFile();
            skipped++;
            logger.info("Skipped documents in the this reader {} out of {}.", skipped,  this.getResources().size());
        }
        Resource xmlFile = nextFile();
        NYTCorpusDocument nytDocument = null;
        skipped++;
        try {
            nytDocument = nytParser.parseNYTCorpusDocumentFromFile(xmlFile.getInputStream(), false);
        } catch (ParserConfigurationException | SAXException | ParseException e) {
            logger.error("{}: Error: {}.", nytDocument.getGuid(), e.getMessage());
        }

        while (!canBeAdded(nytDocument.getGuid()) && getResourceIterator().hasNext()) {
            xmlFile = nextFile();
            skipped++;
            try {
                nytDocument = nytParser.parseNYTCorpusDocumentFromFile(xmlFile.getInputStream(), false);
            } catch (ParserConfigurationException | SAXException | ParseException e) {
                //do nothing
                logger.error("{}: Error: {}.", nytDocument.getGuid(), e.getMessage());
            }
        }

        JCas aJCas = null;
        try {
            aJCas = cas.getJCas();
        } catch (CASException e) {
            e.printStackTrace();
        }
        if(nytDocument!= null && canBeAdded(nytDocument.getGuid())) {
            initCas(cas, xmlFile);
                setDocumenText(aJCas, nytDocument.getBody());
            NYTArticleMetaData articleMetaData = createNYTArticleMetaData(aJCas, nytDocument);
            articleMetaData.addToIndexes();

            DocumentMetaData md = JCasUtil.selectSingle(aJCas, DocumentMetaData.class);
            md.setDocumentId(Integer.toString(articleMetaData.getGuid()));


            if (nytAnnotations != null
                    && !nytAnnotations.isEmpty()
                    && nytAnnotations.get(nytDocument.getGuid()) != null
                    && !nytAnnotations.get(nytDocument.getGuid()).isEmpty()) {
                int oldEnd = 0;
                for (Entry<Integer, NYTAnnotationReader.NYTMention> nytMention : nytAnnotations.get(nytDocument.getGuid()).entrySet()) {
                    oldEnd = createEntityAnnotations(aJCas, nytMention.getValue(), oldEnd, articleMetaData);
                }
            }
        }
    }

    private boolean isBelowOffset() {
        return skipped < offset && getResourceIterator().hasNext();
    }

    private boolean canBeAdded(int guid) {
        if (nytAnnotations != null && !nytAnnotations.isEmpty()) {
            return nytAnnotations.containsKey(guid);
        } else {
            return true; //This means the annotation file is not provided and all documents should be read.
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    private static StringArray toStringArray(List<String> stringList, JCas aJCas) {
        if (!stringList.isEmpty()) {
            String[] strings = stringList.toArray(new String[0]);
            int length = strings.length;
            StringArray stringArray = new StringArray(aJCas, length);
            stringArray.copyFromArray(strings, 0, 0, length);
            return stringArray;
        } else {
            return new StringArray(aJCas, 0);
        }
    }

    private NYTArticleMetaData createNYTArticleMetaData(JCas aJCas, NYTCorpusDocument doc) {
        NYTArticleMetaData articleMetaData = new NYTArticleMetaData(aJCas);

        //articleMetaData.setLanguage(java.util.Locale.US.toString());
        articleMetaData.setGuid(doc.getGuid());

        URL alternateUrl = doc.getAlternateURL();
        if (alternateUrl != null) {
            articleMetaData.setAlternateUrl(alternateUrl.toString());
        }

        URL url = doc.getUrl();
        if (url != null) {
            articleMetaData.setAlternateUrl(url.toString());
        }

        if(doc.getNormalizedByline() != null) {
            articleMetaData.setAuthor(doc.getNormalizedByline());
        }

        if(doc.getArticleAbstract() != null) {
            articleMetaData.setArticleAbstract(doc.getArticleAbstract());
        }

        if(doc.getColumnName() !=null) {
            articleMetaData.setColumnName(doc.getColumnName());
        }

        if(doc.getDescriptors() != null) {
            articleMetaData.setDescriptors(toStringArray(doc.getDescriptors(), aJCas));
        }

        if(doc.getHeadline() != null) {
            articleMetaData.setHeadline(doc.getHeadline());
        }

        if(doc.getOnlineDescriptors() != null) {
            articleMetaData.setOnlineDescriptors(toStringArray(doc.getOnlineDescriptors(), aJCas));
        }

        if(doc.getOnlineHeadline() != null) {
            articleMetaData.setOnlineHeadline(doc.getOnlineHeadline());
        }

        if(doc.getOnlineSection() != null) {
            articleMetaData.setOnlineSection(doc.getOnlineSection());
        }

        if(doc.getPublicationDate()!=null) {
            articleMetaData.setPublicationDate(doc.getPublicationDate().toString());
        }

        if(doc.getSection() != null) {
            articleMetaData.setSection(doc.getSection());
        }

        if(doc.getTaxonomicClassifiers() != null) {
            articleMetaData.setTaxonomicClassifiers(toStringArray(doc.getTaxonomicClassifiers(), aJCas));
        }

        if(doc.getTypesOfMaterial() != null) {
            articleMetaData.setTypesOfMaterial(toStringArray(doc.getTypesOfMaterial(), aJCas));
        }
        return articleMetaData;
    }

    private int createEntityAnnotations(JCas aJCas, NYTAnnotationReader.NYTMention nytMention,
        int oldEnd, NYTArticleMetaData articleMetaData) {
        //logger.info("OldEnd: "+ Integer.toString(oldEnd));
        String text = aJCas.getDocumentText().substring(oldEnd);
        int begin = text.indexOf(nytMention.getEntityMention());
        //logger.info("Begin: " + begin);
/*
        if(begin < 0) {
            missed++;
            logger.info("Missed entities {} out of {}.", missed,  total);
            logger.info("Title: " + articleMetaData.getHeadline());
            logger.info(aJCas.getDocumentText());
            logger.info(aJCas.getDocumentText().substring(oldEnd));
            logger.info(nytMention.getEntityMention());
            logger.info(Integer.toString(begin));
            logger.info(Integer.toString(oldEnd));
            return oldEnd;
        }
*/
        begin +=  oldEnd;
        //logger.info("Nbegin: " + begin);
        int end = begin + nytMention.getEntityMention().length();
        //logger.info("end: " + begin);
        SalientEntity salientEntity = new SalientEntity(aJCas, begin, end);
        salientEntity.setLabel(nytMention.getLabel());
        if(fb2yagoIds!= null && fb2yagoIds.containsKey(nytMention.getId())) {
            salientEntity.setID(fb2yagoIds.get(nytMention.getId()));
        } else {
            salientEntity.setID(nytMention.getId());
        }
        salientEntity.addToIndexes();
        total++;
/*
        if(!salientEntity.getCoveredText().equals(nytMention.getEntityMention())) {
            logger.info("Title: " + articleMetaData.getHeadline());
            logger.info(aJCas.getDocumentText());
            logger.info("Entity does not match!");
            logger.info(nytMention.getEntityMention());
            logger.info(salientEntity.getCoveredText());
            logger.info("begin:"+ Integer.toString(begin));
            logger.info("end:" + Integer.toString(end));
            logger.info("old end: " + oldEnd);
            logger.info(aJCas.getDocumentText().substring(oldEnd));
        }
*/
        return end;
    }
}