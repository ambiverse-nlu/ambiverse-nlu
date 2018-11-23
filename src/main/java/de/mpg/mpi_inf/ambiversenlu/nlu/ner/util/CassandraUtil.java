/**
 *
 */
package de.mpg.mpi_inf.ambiversenlu.nlu.ner.util;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CassandraConnectionHolder;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.NERManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * This is a helper class that can be used to create and retrieve
 * data from a simple Cassandra key value store.
 *
 * REQUIREMENTS: You need to have Cassandra configured in your AIDA
 * project (see: adia/src/main/config/default/cassandra.properties)
 */
public class CassandraUtil {

	/**
	 * Size for statement batch processing
	 */
	private static final int BATCH_SIZE = 5_000;

	private static Logger logger = Logger.getLogger(CassandraUtil.class);

//	public static final String KEY_SPACE = CassandraConfig.get(CassandraConfig.KEYSPACE);

	private static final String TABLE_KEY_NAME = "tok"; // 'token' is a reserved keyword in cql
	private static final String TABLE_VALUE_NAME = "value";

	private static void storeMap(String keyspace, String tableName, Map<String, ? extends Number> map, Class<? extends Number> datatype) {
//		if(map.isEmpty()){
//			return;
//		}

		/**
		 * Holds the results from batch processing
		 */
		List<ResultSetFuture> futures = new ArrayList<>(BATCH_SIZE);

		try {
			Session session = NERManager.getCassandraConnectionHolder(false).getSession();
//			session.execute(String.format("USE %s;", KEY_SPACE));
			session.execute(String.format("DROP TABLE IF EXISTS %s;", keyspace + "." + tableName));
			StringBuffer sb = new StringBuffer();
			sb.append(String.format("CREATE TABLE %s ( ", keyspace + "." + tableName));
			sb.append(String.format("%s varchar, ", TABLE_KEY_NAME));

			if(datatype.equals(Integer.class)){
				sb.append(String.format("%s int, ", TABLE_VALUE_NAME));
			} else if(datatype.equals(Double.class)){
				sb.append(String.format("%s double, ", TABLE_VALUE_NAME));
			} else{
				throw new InvalidParameterException("Wrong datatype was passed to storeMap().");
			}

			sb.append(String.format("PRIMARY KEY (%s) ", TABLE_KEY_NAME));
			sb.append(");");

			session.execute(sb.toString());

			//Prepare the statement for insertion
			RegularStatement rStatement = QueryBuilder.insertInto(keyspace, tableName)
					.value(TABLE_KEY_NAME, QueryBuilder.bindMarker())
					.value(TABLE_VALUE_NAME, QueryBuilder.bindMarker());

			PreparedStatement pStatement = session.prepare(rStatement);

			int i = 0;
			for(String token : map.keySet()){
				if (futures.size() >= BATCH_SIZE) {
					flush(futures);
				}

				BoundStatement bStatement = new BoundStatement(pStatement);

				Object value = map.get(token);
				futures.add(session.executeAsync(bStatement.bind(token, value)));

				if(++i % 1000000 == 0){
					logger.info(i + " tokens added to map.");
				}
			}
			flush(futures);
		} catch (Exception e) {
			logger.error("Map could not be STORED in database.");
			e.printStackTrace();
		}

	}

	private static Map<String, Object> loadMap(String tableName, Class<? extends Number> datatype){
		Map<String, Object> map = new HashMap<>();

		try {
			Session session = NERManager.getCassandraConnectionHolder(true).getSession();
			String query = String.format(
					"SELECT %s, %s FROM %s;", TABLE_KEY_NAME, TABLE_VALUE_NAME, tableName);
			Statement statement = new SimpleStatement(query);
			statement.setConsistencyLevel(ConsistencyLevel.ONE);
			ResultSet resultSet = session.execute(statement);
			if(datatype.equals(Integer.class)){
				for(Row row : resultSet){
					map.put(row.getString(TABLE_KEY_NAME), row.getInt(TABLE_VALUE_NAME));
				}
			} else if(datatype.equals(Double.class)){
				for(Row row : resultSet){
					map.put(row.getString(TABLE_KEY_NAME), row.getDouble(TABLE_VALUE_NAME));
				}
			} else{
				throw new InvalidParameterException("Wrong datatype was passed to loadMap().");
			}
		} catch (IOException e) {
			logger.error("Count map could not be LOADED from database");
			e.printStackTrace();
		}

		return map;
	}

	private static void flush(List<ResultSetFuture> futures) {
		logger.debug("Flushing " + futures.size() + " objects");
		for (ResultSetFuture rsf : futures) {
			rsf.getUninterruptibly();
		}
		futures.clear();
	}

	public static void storeCountMap(String keyspace, String tableName, Map<String, Integer> counts){
		storeMap(keyspace, tableName, counts, Integer.class);
	}

	public static Map<String, Integer> loadCountMap(String tableName){
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Map<String, Integer> loadMap = (Map)loadMap(tableName, Integer.class);
		return loadMap;
	}

	public static void storeProbabilityMap(String keyspace, String tableName, Map<String, Double> probs){
		storeMap(keyspace, tableName, probs, Double.class);

	}

	public static Map<String, Double> loadProbabilityMap(String tableName){
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Map<String, Double> loadMap = (Map)loadMap(tableName, Double.class);
		return loadMap;
	}

	/**
	 * acquires a keyspace only for write
	 * @throws IOException
	 */
	public static CassandraConnectionHolder acquireCassandraConnection() throws IOException {
		return NERManager.getCassandraConnectionHolder(false);
	}

	/**
	 * creates keyspace if not exists
	 * @throws IOException
	 */
	public static void createKeySpaceIfNotExists(String keyspace) throws IOException {
		CassandraConnectionHolder connectionHolder = NERManager.getCassandraConnectionHolder(false);
		connectionHolder.createKeyspaceIfNotExists(keyspace);
	}

	public static boolean checkTable(String keyspace, String tableName) throws IOException {
		CassandraConnectionHolder cassandraConnectionHolder = NERManager.getCassandraConnectionHolder(true);
		Session session = cassandraConnectionHolder.getSession();
		ResultSet rs = session.execute("SELECT table_name " +
				"FROM system_schema.tables WHERE keyspace_name='" + keyspace + "' and table_name = '" + tableName + "';");
		boolean tableExists = rs.all().size() > 0;
		if (!tableExists) {
			return false;
		}
		ResultSet execute = session.execute("select * from " + keyspace + "." + tableName + " limit 1;");
		return !execute.isExhausted();
	}
}
