package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Reader;
import org.apache.uima.UIMAException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.component.CasDumpWriter;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.PARAM_LANGUAGE;
import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.PARAM_PATTERNS;
import static de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase.PARAM_SOURCE_LOCATION;
import static de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.Conll2003AidaReader.*;
import static org.apache.uima.fit.component.CasDumpWriter.PARAM_OUTPUT_FILE;

public class Conll2003AidaReaderTest {

    @Test
    public void testAidaReader() throws UIMAException, ClassNotFoundException, NoSuchMethodException, IOException, MissingSettingException, EntityLinkingDataAccessException {
        ConfigUtils.setConfOverride("unit_test");

        callReader(2, 3);
        Assert.assertEquals(2, Files.readAllLines(Paths.get("casdump.txt"))
                .stream()
                .filter(s -> s.contains("DocumentMetaData"))
                .count());
        Assert.assertEquals(1, Files.readAllLines(Paths.get("casdump.txt"))
                .stream()
                .filter(s -> s.contains("documentTitle: \"3 China\""))
                .count());
        Assert.assertEquals(1, Files.readAllLines(Paths.get("casdump.txt"))
                .stream()
                .filter(s -> s.contains("documentTitle: \"4 China\""))
                .count());

        callReader(0, 0);
        Assert.assertEquals(1, Files.readAllLines(Paths.get("casdump.txt"))
                .stream()
                .filter(s -> s.contains("DocumentMetaData"))
                .count());

        callReader(4, 4);
        Assert.assertEquals(1, Files.readAllLines(Paths.get("casdump.txt"))
                .stream()
                .filter(s -> s.contains("DocumentMetaData"))
                .count());

        callReader(4, 5);
        Assert.assertEquals(1, Files.readAllLines(Paths.get("casdump.txt"))
                .stream()
                .filter(s -> s.contains("DocumentMetaData"))
                .count());
        String errorMessage = null;
        try {
            callReader(5, 7);
        } catch (RuntimeException e) {
            errorMessage = e.getMessage();
        }
        Assert.assertEquals(errorMessage, "Begin 5 is out of range of the jcas (5)");

    }

    private void callReader(int begin, int end) throws NoSuchMethodException, MissingSettingException, IOException, ClassNotFoundException, UIMAException {
        CollectionReaderDescription readerDescription = Reader.getCollectionReaderDescription(Reader.COLLECTION_FORMAT.AIDA,
                PARAM_SOURCE_LOCATION, "src/test/resources/ner/test_collections/",
                PARAM_PATTERNS, "CoNLL-YAGO_ext_small_en.tsv", // 5 docs total
                PARAM_LANGUAGE, "en",
                PARAM_SINGLE_FILE, true,
                PARAM_FIRSTDOCUMENT, begin,
                PARAM_LASTDOCUMENT, end,
                PARAM_ORDER, OrderType.WORD_POS_POSITION_MENTION_ENTITY_TYPE
        );


        SimplePipeline.runPipeline(readerDescription, AnalysisEngineFactory.createEngineDescription(CasDumpWriter.class,
                PARAM_OUTPUT_FILE, "casdump.txt"));


    }

}