package de.mpg.mpi_inf.ambiversenlu.nlu.ner;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import org.apache.uima.UIMAException;

import java.io.IOException;
import java.util.List;

public interface NER {

  public List<Name> findNames(String docId, String text, Language language)
      throws EntityLinkingDataAccessException, UIMAException, IOException, ClassNotFoundException,
      NoSuchMethodException, MissingSettingException, UnprocessableDocumentException;

  public String getId();
}
