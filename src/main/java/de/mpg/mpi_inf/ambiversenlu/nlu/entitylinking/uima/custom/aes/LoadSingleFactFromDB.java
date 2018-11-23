package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.ObjectF;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.OpenFact;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Relation;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Subject;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.component.ViewCreatorAnnotator;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasBuilder;
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
import java.util.Properties;
import java.util.StringJoiner;

public class LoadSingleFactFromDB extends JCasAnnotator_ImplBase {

  private static Logger logger = LoggerFactory.getLogger(DumpOpenFacts2SQL.class);

  public static final String PARAM_DB_CONFIG_LOCATION = "dbConfigLocation";
  @ConfigurationParameter(name = PARAM_DB_CONFIG_LOCATION)
  private String dbConfigLocation;

  public static final String PARAM_NEGATIVE_ASSUMPTION = "nassumption";
  @ConfigurationParameter(name = "nassumption", mandatory = false)
  private boolean nassumption = true;

  private static DataSource ds;


  @Override public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    synchronized (LoadSingleFactFromDB.class) {
      if (ds == null) {
        try {
          Properties prop = ClassPathUtils.getPropertiesFromClasspath(dbConfigLocation);
          HikariConfig config = new HikariConfig(prop);
          ds = new HikariDataSource(config);
        } catch (IOException e) {
          throw new ResourceInitializationException(e);
        }
      }
    }
  }

  @Override public void process(JCas jcas) throws AnalysisEngineProcessException {

    JCas factView = ViewCreatorAnnotator.createViewSafely(jcas, "fact");


    DocumentMetaData md = JCasUtil.selectSingle(jcas, DocumentMetaData.class);
    DocumentMetaData md2 = new DocumentMetaData(factView);
    md2.setDocumentId(md.getDocumentId());
    md2.addToIndexes();
    if(md.getDocumentId().equals("dummy")) {
      return;
    }
    int fact = Integer.parseInt(md.getDocumentId());
    JCasBuilder doc = new JCasBuilder(factView);


    try {

      String factssql = "SELECT fact.id from fact, "
          + "(SELECT fact.sentence_id as sntid, fact.document_id as did, fact.subject_id as sid, fact.object_id as oid "
          + "FROM fact "
          + "WHERE fact.id="
          + fact + ")"
          + "as tmp WHERE fact.sentence_id = sntid AND fact.document_id = did AND fact.subject_id=sid AND fact.object_id=oid ";


      Connection connection = ds.getConnection();
      Statement stm = connection.createStatement();
      ResultSet rs = stm.executeQuery(factssql);

      StringJoiner facts = new StringJoiner(",");
      while(rs.next()) {
        facts.add(Integer.toString(rs.getInt("id")));
      }


      StringBuilder sql = new StringBuilder(

          "SELECT sentence, "
              +"fact.id as fid, "
              +"fact.begin as fbegin, fact.tend as ftend, "
              +"subject.begin as sbegin, subject.tend as stend,object.begin as obegin, "
              +"object.tend as otend, relation.begin as rbegin, relation.tend as rtend, "
              +"relation.uri as uri,relation.textual_form as textual_form, annotation "
              +"FROM fact, sentence, relation, subject, object, user_annotation "
              +"WHERE fact.document_id = sentence.document_id "
              +"AND fact.sentence_id = sentence.id "
              +"AND fact.subject_id = subject.id "
              +"AND fact.sentence_id = subject.sentence_id "
              +"AND fact.document_id = subject.document_id "
              +"AND fact.object_id = object.id "
              +"AND fact.relation_id = relation.id "
              +"AND fact.document_id = relation.document_id "
              +"AND fact.sentence_id = relation.sentence_id "
              +"AND relation.annotator = 'diffbot' "
              +"AND user_annotation.textual_form = relation.textual_form "
              +"AND user_annotation.uri = relation.uri "
              +"AND fact.id IN( " + facts.toString() + ") "
      );

      if(nassumption) {
        sql.append("AND annotation=1");
      }
      rs = stm.executeQuery(sql.toString());
      boolean addedSentence = false;
      while(rs.next()) {
        if(!addedSentence) {
          doc.add(rs.getString("sentence"), Sentence.class);
          addedSentence = true;
        }
        if(md.getDocumentId() == null) {
          md.setDocumentId(Integer.toString(rs.getInt("fid")));
        } else {
          md.setDocumentId(md.getDocumentId()+Integer.toString(rs.getInt("fid")));
        }
        OpenFact openFact = new OpenFact(factView, rs.getInt("fbegin"), rs.getInt("ftend"));
        Subject subject = new Subject(factView, rs.getInt("sbegin"), rs.getInt("stend"));
        openFact.setSubject(subject);
        subject.setExplicit(true);
        subject.addToIndexes();
        ObjectF object = new ObjectF(factView, rs.getInt("obegin"), rs.getInt("otend"));
        openFact.setObject(object);
        object.addToIndexes();
        object.setExplicit(true);
        Relation relation = new Relation(factView, rs.getInt("rbegin"), rs.getInt("rtend"));
        relation.setLabeled(true);
        relation.setExplicit(true);
        relation.setLabel(Boolean.toString(rs.getBoolean("annotation")));
        relation.setUri(rs.getString("uri"));
        relation.setText(rs.getString("textual_form"));
        openFact.setRelation(relation);
        relation.addToIndexes();
        openFact.addToIndexes();
      }
      rs.close();
      stm.close();
      connection.close();
      doc.close();
    } catch (SQLException e) {
      throw new AnalysisEngineProcessException(e);
    }

  }
}
