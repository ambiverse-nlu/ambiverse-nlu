package de.mpg.mpi_inf.ambiversenlu.nlu.ner;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.NamedEntities;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KnowNERIntegrationTest {

    @Before
    public void setup() throws EntityLinkingDataAccessException {
        ConfigUtils.setConfOverride("integration_test");
    }

    @Test
    public void testEnglishNER() throws Exception {
        String text = "Angela Dorothea Merkel (English: /ˈæŋɡələ ˈmɜːrkəl/;[a] née Kasner; born 17 July 1954) is a German politician and the Chancellor of Germany since 2005.";
        Document.Builder dbuilder = new Document.Builder();
        dbuilder.withText(text);
        Document doc = dbuilder.build();

        DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.KNOWNER_KB);
        JCas jCas = dp.processDev(doc);

        assertEquals("en", jCas.getDocumentLanguage());

        NamedEntities namedEntities = NamedEntities.getNamedEntitiesFromJCas(jCas);
        for (String k : namedEntities.getWordLabelsMap().keySet()) {
            String label = namedEntities.getWordLabelsMap().get(k);
            switch (k) {
                case "Angela Dorothea Merkel":
                    assertTrue(label.equals("KnowNER-PER"));
                    break;
                case "German":
                    assertTrue(label.equals("KnowNER-MISC"));
                    break;
                case "Germany":
                    assertTrue(label.equals("KnowNER-LOC"));
                    break;
            }
        }
    }

    @Test
    public void testGermanNER() throws Exception {
        String text = "Angela Dorothea Merkel ist eine deutsche Politikerin (CDU) und seit dem 22. November 2005 amtierende Bundeskanzlerin der Bundesrepublik Deutschland.";
        Document.Builder dbuilder = new Document.Builder();
        dbuilder.withText(text);
        Document doc = dbuilder.build();

        DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.KNOWNER_KB);
        JCas jCas = dp.processDev(doc);

        assertEquals("de", jCas.getDocumentLanguage());

        NamedEntities namedEntities = NamedEntities.getNamedEntitiesFromJCas(jCas);

        for (String k : namedEntities.getWordLabelsMap().keySet()) {
            String label = namedEntities.getWordLabelsMap().get(k);
            switch (k) {
                case "Angela Dorothea Merkel":
                    assertTrue(label.equals("KnowNER-PER"));
                    break;
                case "CDU":
                    assertTrue(label.equals("KnowNER-ORG"));
                    break;
                case "deutsche":
                    assertTrue(label.equals("KnowNER-MISC"));
                    break;
                case "Bundesrepublik Deutschland":
                    assertTrue(label.equals("KnowNER-LOC"));
                    break;
            }
        }
    }

    @Test
    public void testSpanishNER() throws Exception {
        String text = "Angela Dorothea Merkel (Acerca de este sonido audio (?·i)) (Hamburgo, entonces en la Alemania " +
                "Occidental, 17 de julio de 1954) nacida con el nombre de Angela Dorothea Kasner, es una física " +
                "y política alemana que desempeña las funciones de canciller de su país desde 2005.";
        Document.Builder dbuilder = new Document.Builder();
        dbuilder.withText(text);
        Document doc = dbuilder.build();

        DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.KNOWNER_KB);
        JCas jCas = dp.processDev(doc);

        assertEquals("es", jCas.getDocumentLanguage());

        NamedEntities namedEntities = NamedEntities.getNamedEntitiesFromJCas(jCas);

        for (String k : namedEntities.getWordLabelsMap().keySet()) {
            String label = namedEntities.getWordLabelsMap().get(k);
            switch (k) {
                case "Angela Dorothea Merkel":
                    assertTrue(label.equals("KnowNER-PER"));
                    break;
                case "Angela Dorothea Kasner":
                    assertTrue(label.equals("KnowNER-PER"));
                    break;
                case "Alemania Occidental":
                    assertTrue(label.equals("KnowNER-LOC"));
                    break;
                case "Hamburgo":
                    assertTrue(label.equals("KnowNER-LOC"));
                    break;
            }
        }
    }
}
