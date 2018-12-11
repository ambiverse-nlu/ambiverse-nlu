package de.mpg.mpi_inf.ambiversenlu.nlu.entitysalience;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.ResultMention;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.util.DocumentAnnotations;
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class EntitySalienceTest {

    @Before
    public void setup() throws EntityLinkingDataAccessException {
        ConfigUtils.setConfOverride("unit_test");
    }

    @Test
    public void testSalience() throws NoSuchMethodException, UIMAException, EntityLinkingDataAccessException, IOException, ClassNotFoundException, MissingSettingException, UnprocessableDocumentException {

        String text = "When Page played Kashmir at Knebworth, his Les Paul was uniquely tuned.";
        Document.Builder dbuilder = new Document.Builder();
        dbuilder.withText(text).withDisambiguationSettings(new DisambiguationSettings.Builder().build());
        DocumentAnnotations annotations = new DocumentAnnotations();
        annotations.addMention(5,4);
        dbuilder.withAnnotations(annotations);
        Document doc = dbuilder.build();

        DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.ENTITY_SALIENCE_STANFORD);

        JCas jCas = dp.processDev(doc);
        List<ResultMention> result = ResultMention.getResultMentionsFromJCas(jCas);

        Map<String, Double> repacked = repackageMappings(result);

        Double salience = repacked.get("TESTING:Jimmy_Page");
        assertTrue(salience >= 0.5);

        salience = repacked.get("TESTING:Kashmir_(song)");
        assertTrue(salience < 0.5);

        salience = repacked.get("TESTING:Knebworth_Festival");
        assertTrue(salience < 0.5);
    }

    private Map<String, Double> repackageMappings(List<ResultMention> result) {
        Map<String, Double> repack = new HashMap<>();
        for (ResultMention rm : result) {
            repack.put(rm.getBestEntity().getKgId(), rm.getBestEntity().getSalience());
        }
        return repack;
    }
}