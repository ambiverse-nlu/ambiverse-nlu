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
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class EntitySalienceIntegrationTest {

    @Before
    public void setup() throws EntityLinkingDataAccessException {
        ConfigUtils.setConfOverride("integration_test");
    }

    @Test
    public void testSalience() throws NoSuchMethodException, UIMAException, EntityLinkingDataAccessException, IOException, ClassNotFoundException, MissingSettingException, UnprocessableDocumentException {

        String text = "Messi and Maradona played soccer in Barcelona. Maradona also played in Napoli.";
        Document.Builder dbuilder = new Document.Builder();
        dbuilder.withText(text).withDisambiguationSettings(new DisambiguationSettings.Builder().build());

        Document doc = dbuilder.build();

        DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.ENTITY_SALIENCE_STANFORD);

        JCas jCas = dp.processDev(doc);
        List<ResultMention> result = ResultMention.getResultMentionsFromJCas(jCas);

        Map<String, Double> repacked = repackageMappings(result);

        Double salience = repacked.get("YAGO3:<Lionel_Messi>");
        assertTrue(salience >= 0.5);

        salience = repacked.get("YAGO3:<Diego_Maradona>");
        assertTrue(salience >= 0.5);

        salience = repacked.get("YAGO3:<Barcelona>");
        assertTrue(salience < 0.5);

        salience = repacked.get("YAGO3:<S.S.C._Napoli>");
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