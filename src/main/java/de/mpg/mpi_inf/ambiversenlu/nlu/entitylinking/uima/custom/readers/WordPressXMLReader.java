package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers;

import com.ctc.wstx.stax.WstxInputFactory;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.structure.type.Field;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.CasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.*;

@TypeCapability(outputs = {
    "de.tudarmstadt.ukp.dkpro.core.api.structure.type.Field",
    "de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData" })

public class WordPressXMLReader extends CasCollectionReader_ImplBase {

  public static final String PARAM_SOURCE_LOCATION = "sourceLocation";

  @ConfigurationParameter(name = "sourceLocation", mandatory = true, description = "Location from which the input is read.") private String inputDirectory;

  public static final String PARAM_LANGUAGE = "language";

  @ConfigurationParameter(name = "language", mandatory = false, description = "Set this as the language of the produced documents.") private String language;

  public static final String PARAM_INCLUDE_TAG = "IncludeTag";

  @ConfigurationParameter(name = "IncludeTag", mandatory = true, defaultValue = {}, description = "optional, tags those should be worked on (if empty, then all tags\nexcept those ExcludeTags will be worked on)") private Set<String> includeTags;

  public static final String PARAM_EXCLUDE_TAG = "ExcludeTag";

  @ConfigurationParameter(name = "ExcludeTag", mandatory = true, defaultValue = {}, description = "optional, tags those should not be worked on. Out them should no\ntext be extracted and also no Annotations be produced.") private Set<String> excludeTags;

  public static final String PARAM_DOC_ID_TAG = "DocIdTag";

  @ConfigurationParameter(name = "DocIdTag", mandatory = false, description = "tag which contains the docId") private String docIdTag;

  public static final String PARAM_COLLECTION_ID = "collectionId";

  @ConfigurationParameter(name = "collectionId", mandatory = false, description = "The collection ID to set in the DocumentMetaData.") private String collectionId;

  private static final String MESSAGE_DIGEST = "de.tudarmstadt.ukp.dkpro.core.io.xml.XmlReader_Messages";

  private static final String INVALID_PATH_EXCEPTION = "invalid_path_error";

  private static final String EMPTY_DIRECTORY_EXCEPTION = "empty_directory_error";

  private static final String MISSING_DOC_ID_EXCEPTION = "missing_doc_id_error";

  private static final String EMPTY_DOC_ID_EXCEPTION = "empty_doc_id_error";

  private static final String MULTIPLE_DOC_ID_EXCEPTION = "multiple_doc_id_error";

  private static final String SUBSTITUTE_EXCEPTION = "substitute_error";

  private final ArrayList<File> xmlFiles = new ArrayList();

  private XMLStreamReader2 xmlReader;

  private int currentParsedFile;

  private int iDoc;

  private boolean useSubstitution;

  private Map<String, String> substitution;

  private String docIdElementLocalName;

  private String docIdAttributeName;

  public WordPressXMLReader() {
  }

  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    File inDir = new File(this.inputDirectory);
    if (!inDir.isDirectory()) {
      throw new ResourceInitializationException("de.tudarmstadt.ukp.dkpro.core.io.xml.XmlReader_Messages", "invalid_path_error",
          new Object[] { inDir });
    } else {
      File[] split = inDir.listFiles();
      File[] var4 = split;
      int var5 = split.length;

      for (int var6 = 0; var6 < var5; ++var6) {
        File file = var4[var6];
        if (file.isFile() && (file.toString().endsWith(".xml") || file.toString().endsWith(".sgml"))) {
          this.xmlFiles.add(file);
        }
      }

      Collections.sort(this.xmlFiles);
      if (this.xmlFiles.isEmpty()) {
        throw new ResourceInitializationException("de.tudarmstadt.ukp.dkpro.core.io.xml.XmlReader_Messages", "empty_directory_error",
            new Object[] { inDir });
      } else {
        this.currentParsedFile = 0;
        if (this.docIdTag != null && this.docIdTag.contains("/@")) {
          int var8 = this.docIdTag.indexOf("/@");
          this.docIdElementLocalName = this.docIdTag.substring(0, var8);
          this.docIdAttributeName = this.docIdTag.substring(var8 + 2);
        } else {
          this.docIdElementLocalName = this.docIdTag;
        }

      }
    }
  }

  public void getNext(CAS aCAS) throws IOException, CollectionException {
    JCas jcas;
    try {
      jcas = aCAS.getJCas();
    } catch (CASException var6) {
      throw new CollectionException(var6);
    }

    try {
      if (this.xmlReader == null) {
        WstxInputFactory e = new WstxInputFactory();
        this.xmlReader = e.createXMLStreamReader((File) this.xmlFiles.get(this.currentParsedFile));
        this.iDoc = 0;
      }

      this.parseSubDocument(jcas);
      System.out.println(jcas.getDocumentText());
      ++this.iDoc;
      if (this.xmlReader.getDepth() < 2) {
        this.xmlReader.closeCompletely();
        this.xmlReader = null;
        ++this.currentParsedFile;
      }

    } catch (XMLStreamException var4) {
      var4.printStackTrace();
      throw new CollectionException(var4);
    } catch (Exception var5) {
      var5.printStackTrace();
      throw new CollectionException(var5);
    }
  }

  public Progress[] getProgress() {
    return new Progress[] { new ProgressImpl(this.currentParsedFile, this.xmlFiles.size(), "entities") };
  }

  public boolean hasNext() throws IOException, CollectionException {
    return this.xmlReader != null ? true : this.currentParsedFile >= 0 && this.currentParsedFile < this.xmlFiles.size();
  }

  public void close() throws IOException {
  }

  private void parseSubDocument(JCas jcas) throws XMLStreamException, IOException, CollectionException {
    if (this.language != null) {
      jcas.setDocumentLanguage(this.language);
    }

    LinkedList openTagStack = new LinkedList();
    String docTag = this.seekSubDocumentRoot();
    StringBuilder documentText = new StringBuilder();
    String docId = null;

    String fileName;
    String docUri;
    while (this.xmlReader.hasNext() && this.xmlReader.getDepth() > 1) {
      String dotPlace;
      if (this.xmlReader.isStartElement()) {
        if (!this.xmlReader.getPrefix().isEmpty()) {
          fileName = this.xmlReader.getPrefix() + ":" + this.xmlReader.getName().getLocalPart();
        } else {
          fileName = this.xmlReader.getName().getLocalPart();
        }
        openTagStack.push(fileName);
        dotPlace = null;
        if (this.isDocIdElement(fileName) && this.docIdAttributeName != null) {
          dotPlace = this.xmlReader.getAttributeValue((String) null, this.docIdAttributeName);
        }

        this.xmlReader.next();
        docUri = this.collectText();
        if (docUri.length() > 0) {
          if (this.isDocIdElement(fileName) && this.docIdAttributeName == null) {
            dotPlace = docUri;
          }
          this.processText(jcas, fileName, docUri, documentText);
        }

        if (dotPlace != null) {
          if (docId != null) {
            throw new CollectionException("multiple_doc_id_error", new Object[] { this.docIdTag });
          }

          if (dotPlace.length() == 0) {
            throw new CollectionException("empty_doc_id_error", new Object[] { this.docIdTag });
          }

          docId = dotPlace;
        }
      } else if (this.xmlReader.isCharacters()) {
        fileName = (String) openTagStack.peek();
        dotPlace = this.collectText();
        if (dotPlace.length() != 0) {
          this.processText(jcas, fileName, dotPlace, documentText);
        }
      } else if (this.xmlReader.isEndElement()) {
        fileName = this.xmlReader.getName().getLocalPart();
        if (docTag.equals(fileName)) {
          this.xmlReader.nextTag();
          break;
        }
        openTagStack.poll();
        this.xmlReader.next();
      } else if (this.xmlReader.getEventType() == XMLStreamConstants.CDATA) {
        fileName = (String) openTagStack.peek();
        dotPlace = this.xmlReader.getText();
        if (dotPlace.length() != 0) {
          this.processText(jcas, fileName, dotPlace, documentText);
        }
        this.xmlReader.next();
      }
    }

    jcas.setDocumentText(documentText.toString());
    fileName = ((File) this.xmlFiles.get(this.currentParsedFile)).getName();
    int dotPlace1 = fileName.lastIndexOf(46);
    if (this.docIdTag != null) {
      if (docId == null) {
        throw new CollectionException("de.tudarmstadt.ukp.dkpro.core.io.xml.XmlReader_Messages", "missing_doc_id_error",
            new Object[] { this.docIdTag });
      }
    } else if (dotPlace1 >= 0) {
      docId = fileName.substring(0, dotPlace1) + "-" + this.iDoc;
    }

    docUri = ((File) this.xmlFiles.get(this.currentParsedFile)).toURI().toString();
    DocumentMetaData docMetaData = DocumentMetaData.create(jcas);
    docMetaData.setDocumentId(docId);
    docMetaData.setDocumentUri(docUri + "#" + docId);
    docMetaData.setCollectionId(this.collectionId);
  }

  private void createFieldAnnotation(JCas jcas, String localName, int begin, int end) {
    String fieldName = null;
    if (this.useSubstitution) {
      fieldName = (String) this.substitution.get(localName);
      if (fieldName == null) {
        fieldName = localName;
      }
    } else {
      fieldName = localName;
    }

    Field field = new Field(jcas, begin, end);
    field.setName(fieldName);
    field.addToIndexes();
  }

  private boolean isIncluded(String tagName) {
    boolean needToBeParsed = this.includeTags.size() == 0 || this.includeTags.contains(tagName);
    if (this.excludeTags.size() > 0 && this.excludeTags.contains(tagName)) {
      needToBeParsed = false;
    }

    return needToBeParsed;
  }

  private void processText(JCas jcas, String localName, String elementText, StringBuilder documentText) {
    if (this.isIncluded(localName)) {
      int begin = documentText.length();
      elementText = elementText.replaceAll("\n", "").replaceAll("\\s*(\\r|\\n)", "")
          .replaceAll("((\\[(.*|\\s*)])<(.*|\\s*)\\/>)|(<\\/(.*|\\s*)>)|(\\[(.*|\\s*)\\])|(<(.*|\\s*)>)", "");
      documentText = documentText.append(elementText.replaceAll("\r", "").trim());
      documentText = documentText.append("\n\n");
      int end = documentText.length() - 1;
      this.createFieldAnnotation(jcas, localName, begin, end);
    }

  }

  private String collectText() throws XMLStreamException {
    StringBuilder elementText = new StringBuilder();

    while (this.xmlReader.isCharacters()) {
      elementText.append(this.xmlReader.getText().replaceAll("\r", "").trim());
      this.xmlReader.next();
    }

    return elementText.toString();
  }

  private String seekSubDocumentRoot() throws XMLStreamException, IOException {
    String docTag = null;
    if (this.xmlReader.isStartElement() && this.xmlReader.getDepth() > 1) {
      docTag = this.xmlReader.getName().getLocalPart();
    } else {
      while (this.xmlReader.hasNext() && this.xmlReader.getDepth() < 2) {
        this.xmlReader.next();
      }

      while (true) {
        if (!this.xmlReader.hasNext() || this.xmlReader.isStartElement()) {
          if (this.xmlReader.getDepth() != 2 || !this.xmlReader.isStartElement()) {
            throw new IOException("file is empty: " + this.xmlFiles.get(this.currentParsedFile));
          }

          docTag = this.xmlReader.getName().getLocalPart();
          break;
        }

        this.xmlReader.next();
      }
    }

    return docTag;
  }

  private boolean isDocIdElement(String localName) {
    return this.docIdElementLocalName != null && this.docIdElementLocalName.equals(localName);
  }
}

