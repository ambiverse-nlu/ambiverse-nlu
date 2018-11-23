package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Reads a directory of JSON files, assumes each file is a single document.
 */
public class JSONReader extends ResourceCollectionReaderBase {

  private Logger logger = LoggerFactory.getLogger(JSONReader.class);

  public static final String PARAM_TITLE_KEY = "titleKey";
  @ConfigurationParameter(name = PARAM_TITLE_KEY, mandatory = false)
  private String titleKey;

  public static final String PARAM_CONTENT_KEY = "contentKey";
  @ConfigurationParameter(name = PARAM_CONTENT_KEY, defaultValue = "content")
  private String contentKey;

  public static final String PARAM_ID_KEY = "idKey";
  @ConfigurationParameter(name = PARAM_ID_KEY, defaultValue = "id")
  private String idKey;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
  }

  @Override
  public void getNext(CAS cas) throws IOException, CollectionException {
    Resource res = nextFile();
    String name = res.getPath();
    JsonNode json = objectMapper.readTree(res.getInputStream());

    String title = null;
    if(titleKey != null) {
      title = json.get(titleKey).asText();
    }
    String content = json.get(contentKey).asText();

    StringBuilder sb = new StringBuilder();
    if (title != null) {
      sb.append(title).append(System.lineSeparator()).append(System.lineSeparator());
    } else {
      logger.debug(res.getPath() + " does not have a title field.");
    }

    if (content != null) {
      sb.append(content);
    } else {
      logger.debug(res.getPath() + " does not have a content field.");
    }

    JCas jcas;
    try {
      jcas = cas.getJCas();
    }
    catch (CASException e) {
      throw new CollectionException(e);
    }

    // Set doc id.
    String id = null;
    if(idKey != null) {
      id = json.get(idKey).asText();
    }
    if(id == null) {
      id = name;
    }
    DocumentMetaData dmd = new DocumentMetaData(jcas);
    dmd.addToIndexes();
    dmd.setDocumentId(id);
    jcas.setDocumentLanguage(getLanguage());
    jcas.setDocumentText(sb.toString());
  }

  @Override
  public boolean hasNext() throws IOException, CollectionException {
    return super.hasNext();
  }
}