package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.readers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;
import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.factory.JCasBuilder;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LoadFactAnnotations extends ResourceCollectionReaderBase {

   private Logger logger = LoggerFactory.getLogger(LoadFactAnnotations.class);


  public static final String PARAM_DB_CONFIG_LOCATION = "dbConfigLocation";
  @ConfigurationParameter(name = PARAM_DB_CONFIG_LOCATION)
  private String dbConfigLocation;

  public static final String PARAM_NEGATIVE_ASSUMPTION = "nassumption";
  @ConfigurationParameter(name = "nassumption", mandatory = false)
  private boolean nassumption = true;

  public static final String PARAM_LANGUAGE = "language";
  @ConfigurationParameter(name = "language", defaultValue = "en"
  )
  private String language;

  private static ConcurrentLinkedQueue<Integer> facts;

  private static DataSource ds;

  @Override public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    synchronized (LoadFactAnnotations.class) {
      if(facts == null) {
        facts = new ConcurrentLinkedQueue<>();
        Properties prop = null;
        try {
          prop = ClassPathUtils.getPropertiesFromClasspath(dbConfigLocation);
          HikariConfig config = new HikariConfig(prop);
          ds = new HikariDataSource(config);
        } catch (IOException e) {
          throw new ResourceInitializationException(e);
        }
      }
    }
  }

  private void convert(CAS aCAS, int fact) throws CollectionException, SQLException {
    JCas jcas;
    try {
      jcas = aCAS.getJCas();
    } catch (CASException e) {
      throw new CollectionException(e);
    }
    JCasBuilder doc = new JCasBuilder(jcas);
    DocumentMetaData md = JCasUtil.selectSingle(jcas, DocumentMetaData.class);
    md.setDocumentId(Integer.toString(fact));
    doc.add("fact");
    doc.close();
  }

  /**
   * Read a single sentence.
   */
  @Override public void getNext(CAS aCas) throws IOException, CollectionException {
    synchronized (LoadFactAnnotations.class) {
      try{
        if(super.hasNext()) {
          Resource res = nextFile();
          Scanner in = new Scanner(new InputStreamReader(res.getInputStream()));
          StringJoiner sj = new StringJoiner(" OR user_annotation.uri= ");
          if(in.hasNextLine()) {
            sj.add("user_annotation.uri="+"'" + in.nextLine() + "'");
          }
          while(in.hasNextLine()) {
            String line = "'" + in.nextLine() + "'";
            sj.add(line);
          }
          Connection con = ds.getConnection();
          Statement stm = con.createStatement();
          StringBuilder sql = new StringBuilder(
              "SELECT distinct on(fact.sentence_id, fact.document_id) fact.id as fid " +
                  "FROM fact, relation, user_annotation " +
                  "WHERE (" + sj.toString() + ") " +
                  "AND user_annotation.textual_form = relation.textual_form " +
                  "AND user_annotation.uri = relation.uri " +
                  "AND relation.annotator = 'diffbot' " +
                  "AND relation.id = fact.relation_id " +
                  "AND relation.sentence_id = fact.sentence_id " +
                  "AND relation.document_id = fact.document_id "
          );
          if(nassumption) {
            sql.append("AND annotation=1");
          }
          ResultSet rs = stm.executeQuery(sql.toString());
          while(rs.next()) {
            int id = rs.getInt("fid");
            facts.add(id);
          }
          rs.close();
          stm.close();
          con.close();

        }
      } catch(SQLException e) {
        throw new CollectionException(e);
      }
    }


    if(facts.isEmpty()) {
      return;
    }

    if(facts.size() % 1000 == 0) {
      logger.info("FACTS REMAINING: " + facts.size());
    }

    Integer fact = facts.poll();
    try {
      DocumentMetaData m = DocumentMetaData.create(aCas);
      m.setLanguage(language);
      m.addToIndexes();
    } catch (CASException e) {
      throw new CollectionException (e);
    }

    try {
      convert(aCas, fact);
    } catch (SQLException e) {
      throw  new CollectionException(e);
    }
  }

  @Override public boolean hasNext() throws IOException, CollectionException {
    return super.hasNext() || !facts.isEmpty();
  }
}
