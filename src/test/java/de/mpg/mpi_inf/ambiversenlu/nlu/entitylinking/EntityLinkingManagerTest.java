package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.settings.DisambiguationSettings;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.util.DocumentAnnotations;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Mentions;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.preparation.Preparator;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import org.apache.uima.jcas.JCas;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EntityLinkingManagerTest {

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test");
  }

  @Test public void dropMentionsBelowOccurrenceCount() throws Exception {
    String text = "one and two and two and three and three and three";

    Document.Builder dbuilder = new Document.Builder();
    dbuilder.withText(text).withLanguage(Language.getLanguageForString("en")).withDisambiguationSettings(new DisambiguationSettings.Builder().build());
    DocumentAnnotations annotations = new DocumentAnnotations();
    annotations.addMention(0, 3);
    annotations.addMention(8, 3);
    annotations.addMention(16, 3);
    annotations.addMention(24, 5);
    annotations.addMention(34, 5);
    annotations.addMention(44, 5);

    dbuilder.withAnnotations(annotations);
    Document doc = dbuilder.build();
    DocumentProcessor dp = DocumentProcessor.getInstance(PipelineType.DUMMY);

    JCas jCas = dp.processDev(doc);
    Mentions mentions = Mentions.getNeMentionsFromJCas(jCas);
    assertEquals(6, mentions.size());

    jCas = dp.processDev(doc);
    mentions = Mentions.getNeMentionsFromJCas(jCas);
    Preparator.dropMentionsBelowOccurrenceCount(mentions, 2);
    assertEquals(5, mentions.size());

    jCas = dp.processDev(doc);
    mentions = Mentions.getNeMentionsFromJCas(jCas);
    Preparator.dropMentionsBelowOccurrenceCount(mentions, 3);
    assertEquals(3, mentions.size());

    jCas = dp.processDev(doc);
    mentions = Mentions.getNeMentionsFromJCas(jCas);
    Preparator.dropMentionsBelowOccurrenceCount(mentions, 4);
    assertEquals(0, mentions.size());
  }
}
