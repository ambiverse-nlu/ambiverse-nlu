package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.components;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers.*;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;

import java.io.IOException;

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

public class Reader {

  public static enum COLLECTION_FORMAT {
    TEXT, PDF, AIDA, HTML, XML, XML_WP, TREx, FACTDB, FACTTSV, DIFFBOTKG, NYT, JSON, CONCEPT_ENTITY
  }

  public static CollectionReaderDescription getCollectionReaderDescription(COLLECTION_FORMAT type, Object... params)
      throws ResourceInitializationException, ClassNotFoundException, NoSuchMethodException, MissingSettingException, InvalidXMLException,
      IOException {
    CollectionReaderDescription cr = createCollectionReaderDescription(type, params);
    return cr;
  }

  private static CollectionReaderDescription createCollectionReaderDescription(COLLECTION_FORMAT type, Object... params)
      throws ResourceInitializationException, IOException, InvalidXMLException, NoSuchMethodException, MissingSettingException,
      ClassNotFoundException {
    CollectionReaderDescription crd;
    switch (type) {
      case AIDA:
        crd = createReaderDescription(Conll2003AidaReader.class, params);
        break;
      case TEXT:
        crd = createReaderDescription(de.tudarmstadt.ukp.dkpro.core.io.text.TextReader.class, params);
        break;
      case PDF:
        crd = createReaderDescription(de.tudarmstadt.ukp.dkpro.core.io.pdf.PdfReader.class, params);
        break;
      case HTML:
        crd = createReaderDescription(de.tudarmstadt.ukp.dkpro.core.io.html.HtmlReader.class, params);
        break;
      case XML:
        crd = createReaderDescription(de.tudarmstadt.ukp.dkpro.core.io.xml.XmlReader.class, params);
        break;
      case XML_WP:
        crd = createReaderDescription(WordPressXMLReader.class, params);
        break;
      case TREx:
        crd = createReaderDescription(TRExReader.class, params);
        break;
      case FACTDB:
        crd = createReaderDescription(LoadFactAnnotations.class, params);
        break;
      case FACTTSV:
        crd = createReaderDescription(ReadFactTSV.class, params);
        break;
      case JSON:
        crd = createReaderDescription(JSONReader.class, params);
        break;
      case CONCEPT_ENTITY:
        crd = createReaderDescription(ConceptEntityDatasetReader.class, params);
        break;
      case NYT:
        crd = createReaderDescription(NYTCollectionReader.class, params);
        break;
      default:
        throw new IllegalArgumentException("Reader not known");
    }
    return crd;
  }

}
