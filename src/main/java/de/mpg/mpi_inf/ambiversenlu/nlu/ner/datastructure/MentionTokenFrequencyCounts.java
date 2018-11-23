package de.mpg.mpi_inf.ambiversenlu.nlu.ner.datastructure;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Token;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes.UimaTokenizer;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.StopWord;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.CassandraUtil;
import de.mpg.mpi_inf.ambiversenlu.nlu.ner.util.KnowNERSettings;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Dominic Seyler (dseyler@mpi-inf.mpg.de)
 *
 * This class can be used to retrieve the frequency counts for a given mention
 * token. If you set the flag 'loadFromDb' then the token counts are retrieved
 * from the database.
 *
 * REQUIREMENTS: Before calling the constructor with the 'loadFromDb' flag set,
 * you have to call it once to with 'loadFromDb' set to false to populate the
 * database. It is recommended to use the class
 * mpi.ner.task.InitializeDatabaseTables to populate the databases.
 */
public class MentionTokenFrequencyCounts implements MentionTokenCounts {

    private static final Logger logger = Logger.getLogger(MentionTokenFrequencyCounts.class);
	/**
	 * Name of table in data base
	 */
	private static final String TABLE_NAME = "mentiontokencounts";
	private String language;

	/**
	 * Map that stores the token counts
	 */
	private Map<String, Integer> mentionTokenCounts;

	public MentionTokenFrequencyCounts(String language) {
		mentionTokenCounts = new HashMap<>();
		this.language = language;
	}

	/**
	 * Loads from serialized file created with storeData(File f).
	 *
	 * @param file	File to read from
	 */
	public MentionTokenFrequencyCounts(String language, File file) throws IOException, ClassNotFoundException {
		logger.debug("Loading mention token counts from file: " + file);
		mentionTokenCounts = loadData(file);

		this.language = language;
	}

	public void init() throws EntityLinkingDataAccessException {
		Logger.getLogger(getClass()).info("Computing counts.");
		initializeCounts();
	}

	private void addMentionToken(String s) {
		Integer count = mentionTokenCounts.get(s);
		if(count == null){
			mentionTokenCounts.put(s, 1);
		} else {
			mentionTokenCounts.put(s, ++count);
		}
	}

	public int getCount(String s){
		Integer count = this.mentionTokenCounts.get(s);
		if(count == null){
			return 0;
		} else {
			return count;
		}
	}

	public Set<String> getKeys(){
		return mentionTokenCounts.keySet();
	}

	private void initializeCounts() throws EntityLinkingDataAccessException {
		Language lang = Language.getLanguageForString(language);

		Set<String> mentions = DataAccess.getMentionsforLanguage(lang, true, KnowNERSettings.getMentionTokenFrequencyCountsLimit());

		for (String m : mentions) {
			try {
				Tokens tokens = UimaTokenizer.tokenize(lang, m);

				for (Token token : tokens) {
					String t = token.getOriginal();
					if (t.length() > 1 && !StopWord.isStopwordOrSymbol(t, lang)) {
						this.addMentionToken(t);
					}
				}
			} catch (EntityLinkingDataAccessException | UIMAException | IOException | NoSuchMethodException | ClassNotFoundException | MissingSettingException | UnprocessableDocumentException e) {
				logger.warn("Issue when tokenizing mention '" + m + "': " + e.getLocalizedMessage());
			}
		}
	}

	public void storeData(){
		CassandraUtil.storeCountMap(KnowNERSettings.getKeyspace(language), TABLE_NAME, mentionTokenCounts);
	}

	private void loadData() {
		mentionTokenCounts = CassandraUtil.loadCountMap(KnowNERSettings.getKeyspace(language) + "." + TABLE_NAME);
	}

	public void storeData(File file) throws IOException {
		FileOutputStream fos = new FileOutputStream(file);

		Kryo kryo = new Kryo();
		kryo.register(HashMap.class, new MapSerializer());
		Output output = new Output(fos);
		kryo.writeClassAndObject(output, mentionTokenCounts);
		output.close();

		fos.close();
	}

	private Map<String, Integer> loadData(File file) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(file);

		Kryo kryo = new Kryo();
		kryo.register(HashMap.class, new MapSerializer());
		Input input = new Input(fis);
		Map<String, Integer> mentionTokenCounts = (Map<String, Integer>) kryo.readClassAndObject(input);
		input.close();

		fis.close();
		return mentionTokenCounts;
	}
}
