package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import com.google.common.base.Objects;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class DumpOpenFacts2SQL extends JCasAnnotator_ImplBase  {

  private static Logger logger = LoggerFactory.getLogger(DumpOpenFacts2SQL.class);

  public static final String PARAM_DB_CONFIG_LOCATION = "dbConfigLocation";
  @ConfigurationParameter(name = PARAM_DB_CONFIG_LOCATION)
  private String dbConfigLocation;

  private static DataSource ds;
  private static boolean indicesCreated = false;


  private enum Tables {SENTENCE, SUBJECT, RELATION, OBJECT, FACT, DOCUMENT}

  public static int i = 0;




  @Override public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    synchronized(DumpOpenFacts2SQL.class) {
      if(ds == null) {
        Properties prop = null;
        try {
          prop = ClassPathUtils.getPropertiesFromClasspath(dbConfigLocation);
        } catch (IOException e) {
          throw new ResourceInitializationException(e);
        }
        HikariConfig config = new HikariConfig(prop);
        ds = new HikariDataSource(config);
      }



      //createDB();
    }
  }


  @Override public void process(JCas jCas) throws AnalysisEngineProcessException {
    DocumentMetaData documentMetadata = JCasUtil.selectSingle(jCas, DocumentMetaData.class);
    if(documentMetadata.getDocumentId().equals(Document.DUMMY_ID)) {
      return;
    }

    //Put this in the addDocument, to add it as filename, and check if this file was already processed or not!
    //documentMetadata.getDocumentId()
      logger.info("Processing document {}.", documentMetadata.getDocumentTitle());
      long docId = addDocument(jCas.getDocumentText(), documentMetadata.getDocumentTitle());
      for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {

        long sentenceId = addSentence(docId, sentence.getCoveredText());

        List<OpenFact> facts = JCasUtil.selectCovered(OpenFact.class, sentence);
        Map<Cache, Long> subjectCache = new HashMap<>();
        Map<Cache, Long> objectCache = new HashMap<>();
        Map<Cache, Long> relationCache = new HashMap<>();
        Set<FactCache> factCache = new HashSet<>();
        for (OpenFact openFact : facts) {
          if (openFact.getEnd() <= sentence.getEnd() && openFact.getBegin() >= sentence.getBegin()) {
            try {

              Cache sc = new Cache(openFact.getSubject().getBegin() - sentence.getBegin(), openFact.getSubject().getEnd() - sentence.getBegin(), openFact.getSubject().getText(), openFact.getSubject().getUri(), openFact.getSubject().getAnnotator());
              Cache oc = new Cache(openFact.getObject().getBegin() - sentence.getBegin(), openFact.getObject().getEnd() - sentence.getBegin(), openFact.getObject().getText(), openFact.getObject().getUri(), openFact.getObject().getAnnotator());
              Cache rc = new Cache(openFact.getRelation().getBegin(), openFact.getRelation().getEnd(), openFact.getRelation().getText(), openFact.getRelation().getUri(), openFact.getRelation().getAnnotator());
              long subjectId;
              long objectId;
              long relationId;
              if (!subjectCache.containsKey(sc)) {
                subjectId = addConstituent(docId, sentenceId, openFact.getSubject(), sentence.getBegin());
                subjectCache.put(sc, subjectId);
              } else {
                subjectId = subjectCache.get(sc);
              }

              if (!objectCache.containsKey(oc)) {
                objectId = addConstituent(docId, sentenceId, openFact.getObject(), sentence.getBegin());
                objectCache.put(oc, objectId);
              } else {
                objectId = objectCache.get(oc);
              }

              if (!relationCache.containsKey(rc)) {
                relationId = addConstituent(docId, sentenceId, openFact.getRelation(), 0);
                relationCache.put(rc, relationId);
              } else {
                relationId = relationCache.get(rc);
              }


              Double confidence = openFact.getConfidence();
              String b;
              String e;
              if (openFact.getBegin() == openFact.getEnd()) {
                b = null;
                e = null;
              } else {
                b = Integer.toString((openFact.getBegin() - sentence.getBegin()));
                e = Integer.toString((openFact.getEnd() - sentence.getBegin()));
              }
              String text = null;
              if (openFact.getText() != null) {
                text = "'" + openFact.getText().replaceAll("'", "''") + "'";
              }
              String uri = null;
              if (openFact.getUri() != null) {
                uri = "'" + openFact.getUri().replaceAll("'", "''") + "'";
              }
              String annotator = null;
              if (openFact.getAnnotator() != null) {
                annotator = "'" + openFact.getAnnotator().replaceAll("'", "''") + "'";
              }
              String c = null;
              if (confidence >= 0) {
                c = confidence.toString();
              }

              FactCache fc = new FactCache(subjectId, relationId, objectId, (openFact.getBegin() - sentence.getBegin()), (openFact.getEnd() - sentence.getBegin()), text, uri, annotator);
              if (!factCache.contains(fc)) {
                factCache.add(fc);
                String values = getValues(Long.toString(docId), Long.toString(sentenceId),
                        Long.toString(subjectId), Long.toString(relationId), Long.toString(objectId),
                        b, e, text, uri, annotator, c);
                String statement = createFactStatement(values);
                addRow(statement);
              }
            } catch (SQLException e) {
              throw new AnalysisEngineProcessException(e);
            }
          }
        }
      }
  }



  private String getValues(String... values) {
    StringJoiner sj = new StringJoiner(",");
    for(String v: values) {
      sj.add(v);
    }
    return sj.toString();
  }

  /**
   * Inserts sentence for document
   *
   * @param docId
   * @param sentence
   * @return last inserted id
   * @throws AnalysisEngineProcessException
   */
  private long addSentence(Long docId, String sentence) throws AnalysisEngineProcessException {
    try {
      String values = getValues(Long.toString(docId), "'"+sentence.replaceAll("'", "''")+"'");
      String statement = createSentenceStatement(values);
      //Cache cache = new Cache(docid, sentenceid, null, null, null);
      return addRow(statement);
    } catch (SQLException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  /**
   * Inserts text into the document table.
   *
   * @param text
   * @return last inserted document id
   * @throws AnalysisEngineProcessException
   */
  private long addDocument(String text, String filename) throws AnalysisEngineProcessException {
    try {
      String values = getValues("'"+text.replaceAll("'", "''")+"'", "'"+filename.replaceAll("'", "''")+"'");
      String statement = createDocumenttatement(values);
      return addRow(statement);
    } catch (SQLException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  /**
   * Inserts a row according to a string statement and returns the last inserted row id;
   * @param statement
   * @return last inserted id
   * @throws SQLException
   */
  private long addRow(String statement) throws SQLException {
    Connection con = ds.getConnection();
    Statement st = con.createStatement();
    long lastId = -1;
    synchronized (DumpOpenFacts2SQL.class) {
        String generatedColumns[] = {"id"};

        st.executeUpdate(statement, generatedColumns);

        ResultSet lastInsertedRow = st.getGeneratedKeys();
        if(lastInsertedRow.next()) {
          lastId = lastInsertedRow.getLong(1);
        }
      //}
    }
    st.close();
    con.close();
    return lastId;
  }

  /**
   * Depends on the type of the constituent it inserts the data into the corresponding table and returns the last inserted id.
   * @param documentId
   * @param sentenceId
   * @param constituent
   * @return last inserted id
   * @throws SQLException
   */
  private long addConstituent(long documentId, long sentenceId, Constituent constituent, int sentenceOffset) throws SQLException {
    //int id = getConstituentId(documentid, sentenceid, constituent.getBegin(), constituent.getEnd(), constituent.getUri(), constituent.getAnnotator(), constituent.getText());
    //Cache cache;
    Tables table;
    if(constituent instanceof Subject) {
      table = Tables.SUBJECT;
      //cache = new Cache(documentid, sentenceid, id, null, null);
    } else if(constituent instanceof ObjectF) {
      table = Tables.OBJECT;
      //cache = new Cache(documentid, sentenceid, null, null, id);
    } else if(constituent instanceof Relation) {
      table = Tables.RELATION;
      //cache = new Cache(documentid, sentenceid, null, id, null);
    } else {
      throw new IllegalArgumentException("A constitunet should be either a subject, an object or a relation");
    }
    return addConstituent(table, documentId, sentenceId, constituent.getBegin() - sentenceOffset, constituent.getEnd() - sentenceOffset, constituent.getText(),
        constituent.getUri(), constituent.getAnnotator(), constituent.getConfidence());
  }

  /**
   * Inserts data into "subject", "object" or "relation" table.
   * The data is inserted according to the enum table.
   *
   * @param table
   * @param documentId
   * @param sentenceId
   * @param begin
   * @param end
   * @param text
   * @param uri
   * @param annotator
   * @param confidence
   * @return the last inserted id
   * @throws SQLException
   */
  private long addConstituent(Tables table, long documentId, long sentenceId, int begin, int end, String text,
      String uri, String annotator, double confidence) throws SQLException {
    String b;
    String e;
    if(begin == 0 && end == 0) {
      b = null;
      e = null;
    } else {
      b= Integer.toString(begin);
      e= Integer.toString(end);
    }

    String c;
    if(confidence < 0) {
      c = null;
    } else {
      c = Double.toString(confidence);
    }
    if(text != null) {
      text = "'"+text.replaceAll("'", "''")+"'";
    }
    if(uri != null) {
      uri =  "'"+uri.replaceAll("'", "''")+"'";
    }
    if(annotator != null) {
      annotator = "'"+annotator.replaceAll("'", "''") +"'";
    }


    String values = getValues(Long.toString(documentId), Long.toString(sentenceId),  b,
        e, text, uri, annotator, c);
    String statement = createConstituentStatement(table, values);
    return addRow(statement);
  }


  @Override public void destroy() {
    synchronized(DumpOpenFacts2SQL.class) {
      if (!indicesCreated) {
        //createConstrainsAndIndices();
        indicesCreated = true;
      }
    }
  }

  /**
   * Creates tables without constrains and indices in the database
   */
  private void createDB() {
    logger.info("Creating the database");
    if (ds == null) {
      try {
        Properties prop = ClassPathUtils.getPropertiesFromClasspath(dbConfigLocation);
        HikariConfig config = new HikariConfig(prop);
        ds = new HikariDataSource(config);
        Connection con = ds.getConnection();
        Statement stmt = con.createStatement();

        stmt.execute("CREATE TABLE IF NOT EXISTS " + Tables.DOCUMENT + "(\n" +
                "  id bigserial,\n" +
                "  text text,\n" +
                "  filename text\n" +
                ")");


        stmt.execute("CREATE TABLE IF NOT EXISTS " + Tables.SENTENCE + "(\n" +
                "  id bigserial,\n" +
                "  document_id bigint,\n" +
                "  sentence text" +
            ")");
        stmt.execute("CREATE TABLE IF NOT EXISTS " + Tables.SUBJECT + "(\n" +
                "  id bigserial,\n" +
                "  document_id bigint,\n" +
                "  sentence_id bigint,\n" +
                "  begin integer,\n" +
                "  tend integer,\n" +
                "  textual_form text,\n" +
                "  uri character varying(256),\n" +
                "  annotator character varying(256),\n" +
                "  confidence double precision" +
            ")");
        stmt.execute("CREATE TABLE IF NOT EXISTS " + Tables.RELATION + "(\n" +
                "  id bigserial,\n" +
                "  document_id bigint,\n" +
                "  sentence_id bigint,\n" +
                "  begin integer,\n" +
                "  tend integer,\n" +
                "  textual_form text,\n" +
                "  uri character varying(256),\n" +
                "  annotator character varying(256),\n" +
                "  confidence double precision" +
                ")");
        stmt.execute("CREATE TABLE IF NOT EXISTS " + Tables.OBJECT + "(\n" +
                "  id bigserial,\n" +
                "  document_id bigint,\n" +
                "  sentence_id bigint,\n" +
                "  begin integer,\n" +
                "  tend integer,\n" +
                "  textual_form text,\n" +
                "  uri character varying(256),\n" +
                "  annotator character varying(256),\n" +
                "  confidence double precision" +
                ")");
        stmt.execute("CREATE TABLE IF NOT EXISTS " + Tables.FACT + "(\n" +
                "  id bigserial,\n" +
                "  document_id bigint,\n" +
                "  sentence_id bigint,\n" +
                "  subject_id bigint,\n" +
                "  relation_id bigint,\n" +
                "  object_id bigint,\n" +
                "  begin integer,\n" +
                "  tend integer,\n" +
                "  textual_form text,\n" +
                "  uri character varying(256),\n" +
                "  annotator character varying(256),\n" +
                "  confidence double precision" +
                ")");

        //Create helper tables for the annotator tool
        stmt.execute("CREATE TABLE IF NOT EXISTS annotator_user\n" +
                "(\n" +
                "  id bigserial NOT NULL,\n" +
                "  uuid character varying(256) NOT NULL,\n" +
                "  first_name character varying(45) NOT NULL,\n" +
                "  last_name character varying(45) NOT NULL,\n" +
                "  email character varying(45) NOT NULL,\n" +
                "  provider character varying(45) NOT NULL,\n" +
                "  avatar_url character varying(45),\n" +
                "  creation_date timestamp without time zone NOT NULL,\n" +
                "  login_date timestamp without time zone,\n" +
                "  auth_id integer NOT NULL,\n" +
                "  CONSTRAINT annotator_user_pkey PRIMARY KEY (id)\n" +
                ")");
        stmt.execute("CREATE TABLE  IF NOT EXISTS user_annotation\n" +
                "(\n" +
                "  id bigserial NOT NULL,\n" +
                "  user_id bigint NOT NULL,\n" +
                "  textual_form text,\n" +
                "  uri character varying(256) NOT NULL,\n" +
                "  annotation integer,\n" +
                "  annotation_date timestamp without time zone NOT NULL,\n" +
                "  CONSTRAINT user_annotation_pkey PRIMARY KEY (id),\n" +
                "  CONSTRAINT fk_user_annotation_annotator_user FOREIGN KEY (user_id)\n" +
                "      REFERENCES annotator_user (id) MATCH SIMPLE\n" +
                "      ON UPDATE NO ACTION ON DELETE NO ACTION\n" +
                ")");

        con.close();
        stmt.close();
      } catch(SQLException | IOException e){
        e.printStackTrace();
      }
    }

  }

  /**
   * Create all constrains and indices for all tables.
   */
  public void createConstrainsAndIndices() {
      try {
        Properties prop = ClassPathUtils.getPropertiesFromClasspath(dbConfigLocation);
        HikariConfig config = new HikariConfig(prop);
        ds = new HikariDataSource(config);
        Connection con = ds.getConnection();
        Statement stmt = con.createStatement();


        //Document Table
        stmt.execute("ALTER TABLE " + Tables.DOCUMENT +
                "  ADD CONSTRAINT document_pkey PRIMARY KEY(id);");


        //Sentence Table
        stmt.execute("ALTER TABLE " + Tables.SENTENCE +
                "  ADD CONSTRAINT sentence_pkey PRIMARY KEY(id, document_id);");
        stmt.execute("ALTER TABLE " + Tables.SENTENCE +
                "  ADD CONSTRAINT fk_sentence_document FOREIGN KEY (document_id)\n" +
                "      REFERENCES "+ Tables.DOCUMENT +" (id) MATCH SIMPLE\n" +
                "      ON UPDATE NO ACTION ON DELETE NO ACTION;");
        stmt.execute("CREATE INDEX fki_sentence_document\n" +
                "  ON " + Tables.SENTENCE +
                "  USING btree\n" +
                "  (document_id)");


        //Subject Table
        stmt.execute("ALTER TABLE " + Tables.SUBJECT +
                "  ADD CONSTRAINT subject_pkey PRIMARY KEY(id, document_id);");
        stmt.execute("ALTER TABLE " + Tables.SUBJECT +
                " ADD CONSTRAINT fk_subject_sentence FOREIGN KEY (sentence_id, document_id)\n" +
                "      REFERENCES " + Tables.SENTENCE +" (id, document_id) MATCH SIMPLE\n" +
                "      ON UPDATE NO ACTION ON DELETE NO ACTION;");
        stmt.execute("CREATE INDEX fk_subject_sentence_idx ON " + Tables.SUBJECT + " USING btree (document_id, sentence_id)");

        //Object Table
        stmt.execute("ALTER TABLE " + Tables.OBJECT +
                "  ADD CONSTRAINT object_pkey PRIMARY KEY(id, document_id);");
        stmt.execute("ALTER TABLE " + Tables.OBJECT +
                " ADD CONSTRAINT fk_object_sentence FOREIGN KEY (sentence_id, document_id)\n" +
                "      REFERENCES " + Tables.SENTENCE +" (id, document_id) MATCH SIMPLE\n" +
                "      ON UPDATE NO ACTION ON DELETE NO ACTION;");
        stmt.execute("CREATE INDEX fk_object_sentence_idx ON " + Tables.OBJECT + " USING btree (document_id, sentence_id)");

        //Relation Table
        stmt.execute("ALTER TABLE " + Tables.RELATION +
                "  ADD CONSTRAINT relation_pkey PRIMARY KEY(id, document_id);");
        stmt.execute("ALTER TABLE " + Tables.RELATION +
                "  ADD CONSTRAINT fk_relation_sentence FOREIGN KEY (sentence_id, document_id)\n" +
                "      REFERENCES " + Tables.SENTENCE +" (id, document_id) MATCH SIMPLE\n" +
                "      ON UPDATE NO ACTION ON DELETE NO ACTION;");
        stmt.execute("CREATE INDEX fk_relation_sentence_idx ON " + Tables.RELATION + " USING btree (document_id, sentence_id)");
        stmt.execute("CREATE INDEX relation_annotator_idx  ON " + Tables.RELATION + "  USING btree (annotator COLLATE pg_catalog.\"default\");");
        stmt.execute("CREATE INDEX relation_confidence_desc_idx ON " + Tables.RELATION + " USING btree (confidence DESC NULLS LAST);");
        stmt.execute("CREATE INDEX relation_uri_idx ON " + Tables.RELATION + " USING btree (uri COLLATE pg_catalog.\"default\");");


        //FACT TABLE
        //Relation Table
        stmt.execute("ALTER TABLE " + Tables.FACT +
                "  ADD CONSTRAINT fact_pkey PRIMARY KEY(id, document_id, sentence_id, subject_id, relation_id, object_id);");
        stmt.execute("ALTER TABLE " + Tables.FACT +
                "  ADD CONSTRAINT fk_feed_sentence FOREIGN KEY (sentence_id, document_id)\n" +
                "       REFERENCES sentence (id, document_id) MATCH SIMPLE\n" +
                "       ON UPDATE NO ACTION ON DELETE NO ACTION;");
        stmt.execute("ALTER TABLE " + Tables.FACT +
                "  ADD CONSTRAINT fk_feed_subject FOREIGN KEY (subject_id, document_id)\n" +
                "        REFERENCES subject (id, document_id) MATCH SIMPLE\n" +
                "        ON UPDATE NO ACTION ON DELETE NO ACTION;");
        stmt.execute("ALTER TABLE " + Tables.FACT +
                "  ADD CONSTRAINT fk_feed_object FOREIGN KEY (object_id, document_id)\n" +
                "        REFERENCES object (id, document_id) MATCH SIMPLE\n" +
                "        ON UPDATE NO ACTION ON DELETE NO ACTION;");
        stmt.execute("ALTER TABLE " + Tables.FACT +
                " ADD CONSTRAINT fk_feed_relation FOREIGN KEY (relation_id, document_id)\n" +
                "        REFERENCES relation (id, document_id) MATCH SIMPLE\n" +
                "        ON UPDATE NO ACTION ON DELETE NO ACTION;");
        stmt.execute("CREATE INDEX fk_fact_object_idx ON " + Tables.FACT + " USING btree (document_id, object_id)");
        stmt.execute("CREATE INDEX fk_fact_subject_idx ON " + Tables.FACT + " USING btree (document_id, subject_id)");
        stmt.execute("CREATE INDEX fk_fact_relation_idx ON " + Tables.FACT + " USING btree (document_id, relation_id)");
        stmt.execute("CREATE INDEX fk_fact_sentence_idx ON " + Tables.FACT + " USING btree (document_id, sentence_id)");
        stmt.execute("CREATE INDEX fact_uri_idx ON " + Tables.FACT + " USING btree (uri COLLATE pg_catalog.\"default\")");
        stmt.execute("CREATE INDEX fact_annotator_idx ON " + Tables.FACT + " USING btree (annotator COLLATE pg_catalog.\"default\")");
        stmt.execute("CREATE INDEX fact_textual_form_idx ON " + Tables.FACT + " USING btree (textual_form COLLATE pg_catalog.\"default\")");


        stmt.execute("CREATE INDEX fk_user_annotation_annotator_user_idx ON user_annotation USING btree (user_id);");
        stmt.execute("CREATE INDEX user_annotation_uri_idx ON user_annotation USING btree (uri COLLATE pg_catalog.\"default\");");




        con.close();
        stmt.close();
      } catch (SQLException | IOException e) {
        e.printStackTrace();
      }
  }



  private String createFactStatement(String values) {
    String result = "INSERT INTO " + Tables.FACT + " (document_id, sentence_id, subject_id, relation_id, "
        + "object_id, begin, tend, textual_form, uri, annotator, confidence)  VALUES (" + values + ") RETURNING id;--";
    return result;
  }

  private String createSentenceStatement(String values) {
    String result = "INSERT INTO " + Tables.SENTENCE +  " (document_id, sentence) VALUES (" + values + ") RETURNING id;--";
    return result;
  }

  private String createDocumenttatement(String values) {
    String result = "INSERT INTO " + Tables.DOCUMENT +  " (text, filename) VALUES (" + values + ") RETURNING id;--";
    return result;
  }

  private String createConstituentStatement(Tables table, String values) {
    String result = "INSERT INTO " + table +  " (document_id, sentence_id, begin, tend, textual_form, uri, annotator, confidence)  VALUES (" + values + ") RETURNING id;--";
    return result;
  }

  public static class Cache{
    private Integer begin;
    private Integer end;
    private String textualForm;
    private String uri;
    private String annotator;

    public Cache(Integer begin, Integer end, String textualForm, String uri, String annotator) {
      this.begin = begin;
      this.end = end;
      this.textualForm = textualForm;
      this.uri = uri;
      this.annotator = annotator;
    }

    public Integer getBegin() {
      return begin;
    }

    public void setBegin(Integer begin) {
      this.begin = begin;
    }

    public Integer getEnd() {
      return end;
    }

    public void setEnd(Integer end) {
      this.end = end;
    }

    public String getTextualForm() {
      return textualForm;
    }

    public void setTextualForm(String textualForm) {
      this.textualForm = textualForm;
    }

    public String getUri() {
      return uri;
    }

    public void setUri(String uri) {
      this.uri = uri;
    }

    public String getAnnotator() {
      return annotator;
    }

    public void setAnnotator(String annotator) {
      this.annotator = annotator;
    }

    @Override
    public int hashCode() {

      return ((begin == null) ? 0 : begin.hashCode())
              + ((end == null) ? 0 : end.hashCode())
              + ((textualForm == null) ? 0 : textualForm.hashCode())
              + ((uri == null) ? 0 : uri.hashCode())
              + ((annotator == null) ? 0 : annotator.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof Cache)) {
        return false;
      }
      if (obj == null) {
        return false;
      }


      Cache c = (Cache) obj;
      return  Objects.equal(c.getBegin(), this.getBegin())
              && Objects.equal(c.getEnd(), this.getEnd())
              && Objects.equal(c.getUri(), this.getUri())
              && Objects.equal(c.getTextualForm(), this.getTextualForm())
              && Objects.equal(c.getAnnotator(), this.getAnnotator());
    }


    @Override
    public String toString() {
      return "Cache{" +
              "begin=" + begin +
              ", end=" + end +
              ", textualForm='" + textualForm + '\'' +
              ", uri='" + uri + '\'' +
              ", annotator='" + annotator + '\'' +
              '}';
    }
  }

  public static class FactCache{
    private Long subjectId;
    private Long relationId;
    private Long objectId;
    private Integer begin;
    private Integer end;
    private String textualForm;
    private String uri;
    private String annotator;

    public FactCache(Long subjectId, Long relationId, Long objectId, Integer begin, Integer end, String textualForm, String uri, String annotator) {
      this.subjectId = subjectId;
      this.relationId = relationId;
      this.objectId = objectId;
      this.begin = begin;
      this.end = end;
      this.textualForm = textualForm;
      this.uri = uri;
      this.annotator = annotator;
    }

    public Long getSubjectId() {
      return subjectId;
    }

    public void setSubjectId(Long subjectId) {
      this.subjectId = subjectId;
    }

    public Long getRelationId() {
      return relationId;
    }

    public void setRelationId(Long relationId) {
      this.relationId = relationId;
    }

    public Long getObjectId() {
      return objectId;
    }

    public void setObjectId(Long objectId) {
      this.objectId = objectId;
    }

    public Integer getBegin() {
      return begin;
    }

    public void setBegin(Integer begin) {
      this.begin = begin;
    }

    public Integer getEnd() {
      return end;
    }

    public void setEnd(Integer end) {
      this.end = end;
    }

    public String getTextualForm() {
      return textualForm;
    }

    public void setTextualForm(String textualForm) {
      this.textualForm = textualForm;
    }

    public String getUri() {
      return uri;
    }

    public void setUri(String uri) {
      this.uri = uri;
    }

    public String getAnnotator() {
      return annotator;
    }

    public void setAnnotator(String annotator) {
      this.annotator = annotator;
    }

    @Override
    public int hashCode() {
      return  ((subjectId == null) ? 0 : subjectId.hashCode())
              + ((objectId == null) ? 0 : objectId.hashCode())
              + ((relationId == null) ? 0 : relationId.hashCode())
              + ((begin == null) ? 0 : begin.hashCode())
              + ((end == null) ? 0 : end.hashCode())
              + ((textualForm == null) ? 0 : textualForm.hashCode())
              + ((uri == null) ? 0 : uri.hashCode())
              + ((annotator == null) ? 0 : annotator.hashCode());
    }


    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (!(obj instanceof FactCache)) {
        return false;
      }
      if (obj == null) {
        return false;
      }

      FactCache c = (FactCache) obj;
      return  Objects.equal(c.getSubjectId(), this.getSubjectId())
              && Objects.equal(c.getObjectId(), this.getObjectId())
              && Objects.equal(c.getRelationId(), this.getRelationId())
              && Objects.equal(c.getBegin(), this.getBegin())
              && Objects.equal(c.getEnd(), this.getEnd())
              && Objects.equal(c.getUri(), this.getUri())
              && Objects.equal(c.getTextualForm(), this.getTextualForm())
              && Objects.equal(c.getAnnotator(), this.getAnnotator());
    }
  }

}