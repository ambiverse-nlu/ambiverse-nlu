package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import gnu.trove.map.hash.TIntObjectHashMap;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import org.apache.uima.UIMAException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ContextTest {

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test");
  }

  @Test public void test() throws EntityLinkingDataAccessException {
    List<String> text = new LinkedList<String>();

    text.add("Jimmy");
    text.add("played");
    text.add("Les");
    text.add("Paul");
    text.add("played");

    Context context = new Context(new Tokens(text));
    assertEquals(text, context.getTokens());
    TIntObjectHashMap<String> id2word = DataAccess.getWordsForIds(context.getTokenIds());

    for (int i = 0; i < text.size(); ++i) {
      assertEquals(text.get(i), id2word.get(context.getTokenIds()[i]));
    }
  }

  @Test public void testExternalContext()
      throws EntityLinkingDataAccessException, UIMAException, NoSuchMethodException, MissingSettingException,
      IOException, ClassNotFoundException, UnprocessableDocumentException {
    List<String> text = new LinkedList<String>();

    text.add("Jimmy");
    text.add("played");
    text.add("Les");
    text.add("Paul");
    text.add("played");
    text.add("external1234");

    Map<String, List<KBIdentifiedEntity>> dictionary = new HashMap<>();
    List<KBIdentifiedEntity> aidaCands = new ArrayList<>();
    KBIdentifiedEntity aidaMPI = KBIdentifiedEntity.getKBIdentifiedEntity("TEST", "EXTERNAL");
    aidaCands.add(aidaMPI);
    dictionary.put("TEST", aidaCands);

    Map<KBIdentifiedEntity, List<Keyphrase>> entityKeyphrases = new HashMap<>();
    List<Keyphrase> aidaMPIKeyphrases = new ArrayList<>();
    aidaMPIKeyphrases.add(new Keyphrase("external1234", 1));
    entityKeyphrases.put(aidaMPI, aidaMPIKeyphrases);

    ExternalEntitiesContext externalContext = new ExternalEntitiesContext(dictionary, entityKeyphrases, Language.getLanguageForString("en"), true);

    Tokens tokens = new Tokens(text);
    tokens.setTransientTokenIds(externalContext.getTransientTokenIds());

    Context context = new Context(tokens);
    assertEquals(text, context.getTokens());

    int[] textAsId = context.getTokenIds();
    int[] existingIds = new int[textAsId.length - 1];
    for (int i = 0; i < textAsId.length - 1; i++) {
      existingIds[i] = textAsId[i];
    }

    List<String> existingTokens = new ArrayList<>(text);
    existingTokens.remove(existingTokens.size() - 1);

    TIntObjectHashMap<String> id2word = DataAccess.getWordsForIds(existingIds);

    for (int i = 0; i < text.size() - 1; ++i) {
      assertEquals(text.get(i), id2word.get(context.getTokenIds()[i]));
      assertFalse(externalContext.getTransientWordIds().contains(context.getTokenIds()[i]));
    }

    // Last word is not part of the database.
    int last = text.size() - 1;
    assertEquals(textAsId[last], externalContext.getIdForWord(text.get(last)));
  }
}
