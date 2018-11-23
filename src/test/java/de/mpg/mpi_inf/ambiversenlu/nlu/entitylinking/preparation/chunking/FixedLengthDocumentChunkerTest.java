package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.chunking;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.PreparedInput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.documentchunking.DocumentChunker;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.documentchunking.FixedLengthDocumentChunker;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FixedLengthDocumentChunkerTest {

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test");
  }

  @Test public void splitTextIntoFixedLengthChunks() throws Exception {
    String text = "Albert Einstein was born in Ulm, in the Kingdom of Württemberg in the German Empire on 14 March 1879."
        + " His father was Hermann Einstein, a salesman and engineer." + " His mother was Pauline Einstein (née Koch)."
        + " In 1880, the family moved to Munich, where his father and his uncle founded Elektrotechnische Fabrik J. Einstein & Cie, a company that manufactured electrical equipment based on direct current."
        + " After graduating, Einstein spent almost two frustrating years searching for a teaching post. "
        + " He acquired Swiss citizenship in February 1901. " + " Einstein was awarded a PhD by the University of Zurich."
        + " Einstein became an American citizen in 1940.";

    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text).withDisambiguationSettings(new DisambiguationSettings.Builder().build())
        .withChunkinStrategy(DocumentChunker.DOCUMENT_CHUNK_STRATEGY.MULTIPLE_FIXEDLENGTH);
    Document doc = dbuilder.build();

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DISAMBIGUATION_STANFORD);
    JCas jCas = dp.processDev(doc);
    Tokens tokens = Tokens.getTokensFromJCas(jCas);
    Mentions neMentions = Mentions.getNeMentionsFromJCas(jCas);
    Mentions cMentions = Mentions.getConceptMentionsFromJCas(jCas);

    DocumentChunker chunker = new FixedLengthDocumentChunker(2);
    PreparedInput pInp = chunker.process("test", tokens, cMentions, neMentions);
    assertEquals(4, pInp.getChunksCount());

    chunker = new FixedLengthDocumentChunker(3);
    pInp = chunker.process("test", pInp.getTokens(), pInp.getConceptMentions(), pInp.getNamedEntityMentions());
    assertEquals(3, pInp.getChunksCount());

    chunker = new FixedLengthDocumentChunker(4);
    pInp = chunker.process("test", pInp.getTokens(), pInp.getConceptMentions(), pInp.getNamedEntityMentions());
    assertEquals(2, pInp.getChunksCount());

    chunker = new FixedLengthDocumentChunker(1);
    pInp = chunker.process("test", pInp.getTokens(), pInp.getConceptMentions(), pInp.getNamedEntityMentions());
    assertEquals(8, pInp.getChunksCount());
  }
}
