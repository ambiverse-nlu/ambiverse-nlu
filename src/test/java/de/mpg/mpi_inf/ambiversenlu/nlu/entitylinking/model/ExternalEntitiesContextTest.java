package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import org.apache.uima.UIMAException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExternalEntitiesContextTest {

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test");
  }

  @Test public void test()
      throws EntityLinkingDataAccessException, UIMAException, NoSuchMethodException, MissingSettingException,
      IOException, ClassNotFoundException, UnprocessableDocumentException {
    Map<String, List<KBIdentifiedEntity>> dictionary = new HashMap<>();
    List<KBIdentifiedEntity> aidaCands = new ArrayList<>();
    KBIdentifiedEntity aida = KBIdentifiedEntity.getKBIdentifiedEntity("AIDA-MPI", "EXTERNAL");
    aidaCands.add(aida);
    dictionary.put("AIDA", aidaCands);

    Map<KBIdentifiedEntity, List<Keyphrase>> entityKeyphrases = new HashMap<>();
    List<Keyphrase> keyphrases = new ArrayList<>();
    String[] kps = new String[] {
        "Google", "entity disambiguation framework", "MPI", "software" };
    for (String kp : kps) {
      keyphrases.add(new Keyphrase(kp, 1));
    }
    entityKeyphrases.put(aida, keyphrases);

    ExternalEntitiesContext externalContext =
            new ExternalEntitiesContext(dictionary, entityKeyphrases, Language.getLanguageForString("en"), true);

    Mention aidaMention = new Mention();
    aidaMention.setMention("AIDA");
    Entities cands = externalContext.getDictionary().getEntities(aidaMention, true);
    assertEquals(1, cands.size());
    Entity e = cands.getEntities().iterator().next();
    assertEquals(1, externalContext.getEntityKeyphrases().size());
    int[] kpIds = externalContext.getEntityKeyphrases().get(e.getId());
    assertEquals(4, kpIds.length);
    for (int kpId : kpIds) {
      int[] kwIds = externalContext.getKeyphraseTokens().get(kpId);
      assertTrue(kwIds.length == 1 || kwIds.length == 3);
    }
  }
}
