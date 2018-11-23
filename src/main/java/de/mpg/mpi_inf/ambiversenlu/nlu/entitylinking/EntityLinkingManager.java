package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking;

import com.datastax.driver.core.Session;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccessCache;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.CassandraConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.Tracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.data.EntityTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.data.MentionTracer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CassandraConnectionHolder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.LanguageDetector;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.*;

import static de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.EntityLinkingConfig.LANGUAGE_DETECTOR_PRELOAD;

public class EntityLinkingManager {

  private static Logger slogger_ = LoggerFactory.getLogger(EntityLinkingManager.class);

  // This is more coupled to SQL than it should be. Works for now.
  public static final String DB_AIDA = "DatabaseAida";

  public static final String DB_YAGO = "DatabaseYago";

  public static final String DB_YAGO2_FULL = "DatabaseYagoFull";

  public static final String DB_YAGO2_SPOTLX = "DatabaseYagoSpotlx";

  public static final String DB_HYENA = "DatabaseHYENA";

  public static final String DB_WEBSERVICE_LOGGER = "DatabaseWSLogger";

  public static String databaseAidaConfig = "database_aida.properties";

  private static String databaseYagoConfig = "database_yago.properties";

  private static String databaseWSLoggerConfig = "databaseWsLogger.properties";

  private static String databaseHYENAConfig = "database_hyena.properties";

  private static Map<String, String> dbIdToConfig = new HashMap<String, String>();

  private static Map<String, Properties> dbIdToProperties = new HashMap<String, Properties>();

  private static Map<String, DataSource> dbIdToDataSource = new HashMap<>();

  private static CassandraConnectionHolder cassandraConnectionHolder;

  static {
    dbIdToConfig.put(DB_AIDA, databaseAidaConfig);
    dbIdToConfig.put(DB_YAGO, databaseYagoConfig);
    dbIdToConfig.put(DB_HYENA, databaseHYENAConfig);
    dbIdToConfig.put(DB_WEBSERVICE_LOGGER, databaseWSLoggerConfig);
  }

  private static EntityLinkingManager tasks = null;

  public synchronized static void init() throws EntityLinkingDataAccessException {
    slogger_.debug("Initializing the EntityLinkingManager...");
    getTasksInstance();
    slogger_.debug("Should I preload the caches? "+EntityLinkingConfig.getBoolean(EntityLinkingConfig.DATAACESS_CACHE_PRELOAD));
    if (EntityLinkingConfig.getBoolean(EntityLinkingConfig.DATAACESS_CACHE_PRELOAD)) {
      slogger_.debug("Creating DataAccessCache singleton...");
      DataAccessCache.singleton();
    }
    if (EntityLinkingConfig.getBoolean(LANGUAGE_DETECTOR_PRELOAD)) {
      LanguageDetector.LanguageDetectorHolder.init();
    }
  }

  private static synchronized EntityLinkingManager getTasksInstance() {
    if (tasks == null) {
      slogger_.debug("Creating new Task Instance.");
      tasks = new EntityLinkingManager();
    }
    return tasks;
  }

  public static String getAidaDbIdentifierLight() throws SQLException {
	  Properties prop = getPropertiesForDbId(DB_AIDA);
	  return prop.getProperty("dataSource.databaseName");
  }

  /**
   * Creates database with dbId if needed.
   * @param dbId  ID of the db to create
   * @return true if it was created, false if it existed.
   * @throws SQLException
   */
  public static boolean createDatabaseIfNeeded(String dbId) throws SQLException {
    Properties prop = getPropertiesForDbId(dbId);
    Properties creationProp = new Properties();
    creationProp.putAll(prop);
    creationProp.setProperty("dataSource.databaseName", "postgres");  // Connect to the default DB

    HikariConfig config = new HikariConfig(creationProp);
    DataSource ds = new HikariDataSource(config);

    Connection conn = ds.getConnection();

    String dbToCreate = prop.getProperty("dataSource.databaseName");
    boolean exists = false;

    Statement stmt = conn.createStatement();
    try {
      stmt.execute("CREATE DATABASE " + dbToCreate);
      exists = true;
    } catch (SQLException e) {
      // This is a hack, of course something else could go wrong here.
      slogger_.info("It seems that '" + dbToCreate + "' already exists, no need to create. Please verify that nothing else went wrong.");
    } finally {
      stmt.close();
    }

    if (!exists) {
      stmt = conn.createStatement();

      slogger_.info("Creating database: " + dbToCreate);
      stmt.execute("CREATE DATABASE " + dbToCreate);

      stmt.close();
      conn.close();
      return false;
    } else {
      conn.close();
      return true;
    }
  }

  public static Connection getConnectionForDatabase(String dbId) throws SQLException {
    Properties prop = getPropertiesForDbId(dbId);
    return getConnectionForNameAndProperties(dbId, prop);
  }

  public static Properties getPropertiesForDbId(String dbId) throws SQLException {
    Properties prop = dbIdToProperties.get(dbId);
    if (prop == null) {
      try {
        prop = ConfigUtils.loadProperties(dbIdToConfig.get(dbId));
        dbIdToProperties.put(dbId, prop);

        // Set default Postgres port.
        if (!prop.containsKey("dataSource.portNumber") || prop.getProperty("dataSource.portNumber") == null || prop
            .getProperty("dataSource.portNumber").isEmpty()) {
          prop.setProperty("dataSource.portNumber", "5432");
        }
      } catch (IOException e) {
        throw new SQLException(e);
      }
    }
    return prop;
  }

  public static Connection getConnectionForNameAndProperties(String dbId, Properties prop) throws SQLException {

    Connection conn = null;

    // Create config if needed.
    synchronized (dbIdToDataSource) {
      DataSource ds = dbIdToDataSource.get(dbId);
      if (ds == null) {
        String serverName = prop.getProperty("dataSource.serverName");
        String database = prop.getProperty("dataSource.databaseName");
        String username = prop.getProperty("dataSource.user");
        String port = prop.getProperty("dataSource.portNumber");
        slogger_.debug("Connecting to database " + username + "@" + serverName + ":" + port + "/" + database);

        HikariConfig config = new HikariConfig(prop);
        ds = new HikariDataSource(config);
        dbIdToDataSource.put(dbId, ds);
      }

      // Establish the connection - wait in case the DB server is not ready and wait is wanted
      boolean shouldWait = EntityLinkingConfig.getBoolean("dataaccess.startup.wait");
      int maxWaitTimeInSeconds = EntityLinkingConfig.getAsInt("dataaccess.startup.timeoutins");
      int totalWaitTimeInSeconds = 0;

      Integer id = RunningTimer.recordStartTime("getConnection");

      do {
        try {
          conn = ds.getConnection();
        } catch (SQLException se) {
          try {
            slogger_.info("Postgres DB seems unavailable. This is expected during first startup using Docker. " +
                    "Waiting for 60s in addition (already waited {}s, will wait up to {}s in total).", totalWaitTimeInSeconds, maxWaitTimeInSeconds);
            Thread.sleep(60000);
            totalWaitTimeInSeconds += 60;

            // Also add the Hikari timeout
            totalWaitTimeInSeconds += ds.getLoginTimeout();

            if (totalWaitTimeInSeconds >= maxWaitTimeInSeconds) {
              throw new SQLTimeoutException("Postgres DB still seems unavailable after waiting " + totalWaitTimeInSeconds + "s. Giving up...");
            }
          } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
          }
        }
      } while (conn == null && shouldWait && totalWaitTimeInSeconds < maxWaitTimeInSeconds);

      RunningTimer.recordEndTime("getConnection", id);

      return conn;
    }
  }

  public static Properties getDatabaseProperties(String hostname, Integer port, String username, String password, Integer maxCon, String database) {
    Properties prop = new Properties();
    prop.put("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
    prop.put("maximumPoolSize", maxCon);
    prop.put("dataSource.user", username);
    prop.put("dataSource.password", password);
    prop.put("dataSource.databaseName", database);
    prop.put("dataSource.serverName", hostname);
    prop.put("dataSource.portNumber", port);
    return prop;
  }

  public static void releaseConnection(Connection con) throws SQLException {
    // Just in case, we always want autoCommit set to true when the connection is
    // released, as this is the default.
    if(con != null) {
      if (!con.getAutoCommit()) {
        con.setAutoCommit(true);
      }
      con.close();
    }
  }

  /**
   * Gets a Cassandra connection holder
   *
   * @param read indicates that the application is reading from the Database, this should be true when running AIDA and false when building the repository
   */
  public static synchronized CassandraConnectionHolder getCassandraConnectionHolder(boolean read) throws IOException {
    if (cassandraConnectionHolder == null) {
      cassandraConnectionHolder = new CassandraConnectionHolder(read, CassandraConfig.FILE_NAME);

      if (!read) {
        cassandraConnectionHolder.createKeyspaceIfNotExists(CassandraConfig.get(CassandraConfig.FILE_NAME, CassandraConfig.KEYSPACE));
      }

      Session session = cassandraConnectionHolder.getSession();
      session.execute("USE " + CassandraConfig.get(CassandraConfig.FILE_NAME, CassandraConfig.KEYSPACE));
      slogger_.info("Using keyspace " + CassandraConfig.get(CassandraConfig.FILE_NAME, CassandraConfig.KEYSPACE));
    }
    return cassandraConnectionHolder;
  }

  /**
   * Gets an AIDA entity for the given entity id.
   * This is slow, as it accesses the DB for each call.
   * Do in batch using DataAccess directly for a larger number
   * of entities.
   *
   * @return AIDA Entity
   */
  public static Entity getEntity(KBIdentifiedEntity entity) throws EntityLinkingDataAccessException {
    int id = DataAccess.getInternalIdForKBEntity(entity);
    return new Entity(entity, id);
  }

  public static Entities getEntities(Set<KBIdentifiedEntity> kbEntities) throws EntityLinkingDataAccessException {
    TObjectIntHashMap<KBIdentifiedEntity> ids = DataAccess.getInternalIdsForKBEntities(kbEntities);
    Entities entities = new Entities();
    for (TObjectIntIterator<KBIdentifiedEntity> itr = ids.iterator(); itr.hasNext(); ) {
      itr.advance();
      entities.add(new Entity(itr.key(), itr.value()));
    }
    return entities;
  }

  /**
   * Gets an AIDA entity for the given AIDA entity id.
   * This is slow, as it accesses the DB for each call.
   * Do in batch using DataAccess directly for a larger number
   * of entities.
   *
   * @param entityId  Internal AIDA int ID
   * @return AIDA Entity
   */
  public static Entity getEntity(int entityId) throws EntityLinkingDataAccessException {
    KBIdentifiedEntity kbEntity = DataAccess.getKnowlegebaseEntityForInternalId(entityId);
    return new Entity(kbEntity, entityId);
  }

  /**
   * Extracts all candidate entities from a Mentions collection, not including the ones that have been
   * passed as ExternalEntitiesContext. It also adds tracers if a Tracer has been passed.
   *
   * @param mentions mentions to extract candidates from.
   * @param externalContext Context that has been passed externally.
   * @param tracer  Tracer to trace mention-entity objects.
   * @return All (non-external) candidate entities of the input.
   */
  public static Entities getAllEntities(Mentions mentions, ExternalEntitiesContext externalContext, Tracer tracer) {
    Entities entities = new Entities();
    for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
      for (Mention mention : innerMap.values()) {
        MentionTracer mt = new MentionTracer(mention);
        tracer.addMention(mention, mt);
        for (Entity entity : mention.getCandidateEntities()) {
          EntityTracer et = new EntityTracer(entity.getId());
          tracer.addEntityForMention(mention, entity.getId(), et);
        }
        for (Entity entity : mention.getCandidateEntities()) {
          //        if (!externalContext.contains(entity)) {
          // JH: Not sure why there was this check - it keeps aida from adding existing entities
          // for new mentions. Comment out for now.
          entities.add(entity);
          //        }
        }
      }
    }
    return entities;
  }

  /**
   * Uppercases a token of more than 4 characters. This is
   * used as pre-processing method during name recognition
   * and dictionary matching mainly.
   *
   * @param token Token to check for conflation.
   * @return ALL-UPPERCASE token if token is longer than 4 characters.
   */
  public static String conflateToken(String token, boolean isNamedEntity) {
    if(isNamedEntity) {
      if (token.length() >= 4) {
        token = token.toUpperCase(Locale.ENGLISH);
      }
      return token;
    } else {
      return token.toLowerCase();
    }
  }

  public static String conflateToken(String token, Language lang, boolean isNamedEntity) {
    if(isNamedEntity) {
      if (token.length() >= 4) {
        token = token.toUpperCase(new Locale(lang.name()));
      }
      return token;
    } else {
      return token.toLowerCase(new Locale(lang.name()));
    }

  }

  public static void shutDown() throws Throwable {
    tasks = null;
    EntityLinkingConfig.shutdown();
    if (cassandraConnectionHolder != null) {
      cassandraConnectionHolder.finalize();
    }
  }
}
