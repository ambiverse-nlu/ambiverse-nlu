package de.mpg.mpi_inf.ambiversenlu.nlu.openie.clausie;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components.Component;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact;
import org.apache.uima.jcas.JCas;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.junit.Assert.assertEquals;

public class ClausIETest {

  @Before
  public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test");
    Component.CLAUSIE.params = new Object[] { "removeRedundant", false, "reverbForm", false, "keepOnlyLongest", false, "processCCargs", true };
  }

  @After
  public void tearDown() {
    Component.CLAUSIE.params = new Object[] { "removeRedundant", false, "reverbForm", true, "keepOnlyLongest", true, "processCCargs", false };
  }

  @Test public void testIndexedConstituentEnglish() throws Exception {
    String text = "Jack founded Alibaba in Hangzhou with investments from SoftBank and Goldman.";
    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text);
    dbuilder.withLanguage(Language.getLanguageForString("en"));
    Document doc = dbuilder.build();
    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.OPENIE_EN);
    JCas jCas = dp.processDev(doc);
    Collection<OpenFact> facts = select(jCas, OpenFact.class);

    assertEquals(4, facts.size());
    OpenFact of = facts.iterator().next();
    assertEquals("(\"Jack\", \"founded\", \"Alibaba with investments from Goldman\")", of.getText());
    assertEquals("Jack", ((Token) of.getSubject().getHead()).getCoveredText());
    assertEquals("founded", ((Token) of.getRelation().getHead()).getCoveredText());
    assertEquals("Alibaba", ((Token) of.getObject().getHead()).getCoveredText());
    assertEquals("Jack", of.getSubject().getCoveredText());
    assertEquals("founded", of.getRelation().getCoveredText());
    assertEquals(5, of.getObject().getTokens().size());
  }

  @Test public void testTextConstituentEnglish() throws Exception {
    String text = "Bell, a telecommunication company, which is based in Los Angeles, makes and distributes electronic, computer and building products.";

    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text);
    dbuilder.withLanguage(Language.getLanguageForString("en"));
    Document doc = dbuilder.build();

    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.OPENIE_EN);
    JCas jCas = dp.processDev(doc);

    List<OpenFact> facts = new ArrayList<>(select(jCas, OpenFact.class));

    assertEquals(8, facts.size());

    OpenFact of = facts.get(6);
    assertEquals("(\"Bell\", \"is\", \"a telecommunication company\")", of.getText());
    assertEquals("Bell", ((Token) of.getSubject().getHead()).getCoveredText());
    assertEquals("is", ((Token) of.getRelation().getHead()).getId());
    assertEquals("company", ((Token) of.getObject().getHead()).getCoveredText());

    assertEquals("a telecommunication company", of.getObject().getCoveredText()); //This is like this because constituents can be discountinous
    // but not in UIMA which requires continuous chunks. That's why we have an array of tokens there
    assertEquals("Bell", of.getSubject().getCoveredText());
    assertEquals("is", ((Token) of.getRelation().getHead()).getId());
    assertEquals(3, of.getObject().getTokens().size());
  }
}
