package de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.wikilinklikelihood.WikiLinkLikelihoodProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.CassandraUtil;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.basics3.FactComponent;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * This class can be used to retrieve the Wikipedia linklikelihood for a
 * token. If pass null to the constructor, the probabilities are retrieved
 * from the database.
 *
 * REQUIREMENTS: Before calling the constructor with null, you have to call
 * it once to with the path to the file with the Wikipedia probabilities
 * to populate the database. It is recommended to use the class
 * mpi.ner.task.InitializeDatabaseTables to populate the databases.
 */
public class WikipediaLinkProbabilities {

	/**
	 * Name of table in data base
	 */
	private static final String TABLE_NAME = "x";
	private String keyspace;

	/**
	 * Map that stores the token link frequency
	 */
	private Map<String, Double> wikiLinkProbs;

	Logger logger = Logger.getLogger(getClass());

	/**
	 * loads from database
	 * @param keyspace
	 */
	public WikipediaLinkProbabilities(String keyspace) {
		this.keyspace = keyspace;
		logger.info("Wikipedia link probabilities from database.");
		loadData();
	}

	/**
	 * Loads from serialized file created with storeData(File f).
	 * @param file	File to read from
	 */
	public WikipediaLinkProbabilities(File file) throws IOException, ClassNotFoundException {
		logger.debug("Loading link probabilities from file: " + file);
		wikiLinkProbs = loadData(file);
	}

	public WikipediaLinkProbabilities(String keyspace, List<WikiLinkLikelihoodProvider> wikiLinkProbabilitiesProviders, String language) {
		this(wikiLinkProbabilitiesProviders, language);
		this.keyspace = keyspace;
	}

	public WikipediaLinkProbabilities(List<WikiLinkLikelihoodProvider> wikiLinkProbabilitiesProviders, String language) {
		logger.info("Loading probabilities from yago file.");

		wikiLinkProbs = new HashMap<>();
		for (WikiLinkLikelihoodProvider provider : wikiLinkProbabilitiesProviders) {
			for (Map.Entry<String, Double> e : provider) {
				if (e.getKey() == null || e.getKey().equals("")) {
					continue;
				}

				String token = e.getKey();
				String tokenLanguage = FactComponent.getLanguageOfString(token);
				if (tokenLanguage.equals(language)) {
					wikiLinkProbs.put(FactComponent.asJavaString(token), e.getValue());
				}
			}
		}
	}

	public double getProbability(String s) {
		Double count = this.wikiLinkProbs.get(s);
		if(count == null){
			return 0;
		} else {
			return count;
		}
	}

	public Set<String> getKeys() {
		return wikiLinkProbs.keySet();
	}

	public void storeData() {
		CassandraUtil.storeProbabilityMap(keyspace, TABLE_NAME, wikiLinkProbs);
	}

	private void loadData() {
		wikiLinkProbs = CassandraUtil.loadProbabilityMap(keyspace + "." + TABLE_NAME);
	}

	public void storeData(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);

		Kryo kryo = new Kryo();
		kryo.register(HashMap.class, new MapSerializer());
		Output output = new Output(fos);
		kryo.writeClassAndObject(output, wikiLinkProbs);
		output.close();

		fos.close();
	}

	private Map<String, Double> loadData(File file) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(file);

		Kryo kryo = new Kryo();
		kryo.register(HashMap.class, new MapSerializer());
		Input input = new Input(fis);
		Map<String, Double> probs = (Map<String, Double>) kryo.readClassAndObject(input);
		input.close();

		fis.close();
		return probs;
	}
}
