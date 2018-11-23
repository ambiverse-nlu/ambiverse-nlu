package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.EntityType;
import edu.stanford.nlp.util.StringUtils;
import gnu.trove.map.hash.TIntDoubleHashMap;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Stores all the KeyValueStores which should be crated and be able to access
 */
public enum DatabaseKeyValueStore {
  WORD_IDS_WORD("word_ids", String.class, Encoding.KRYO, 1, Integer.class, Encoding.KRYO, "word"),
  CONCEPT_CATEGORIES("concept_categories", Integer.class, Encoding.INTS, 1, int[].class, Encoding.INTS,
      "SELECT entity, types AS type FROM concept_categories", true, "entity"),
  CATEGORY_IDS_ID("category_ids", Integer.class, Encoding.KRYO, 1, KeyValueStoreRow[].class, Encoding.KRYO, "id"),
  ENTITY_IDS_ENTITY_KNOWLEDGEBASE("entity_ids", KeyValueStoreRow.class, Encoding.KRYO, 1, Integer.class, Encoding.INTS, "entity",
    "knowledgebase"),
  TYPE_TAXONOMY("type_taxonomy", Integer.class, Encoding.INTS, 1, int[].class, Encoding.INTS, "type"),
  DICTIONARY_LANGUAGE_NE(
      "dictionary_languages_ne", String.class, Encoding.KRYO, 20, String[].class, Encoding.KRYO,
        "SELECT DISTINCT language, mention FROM " + DataAccessSQL.ENTITY_LANGUAGES + "," +
            " (SELECT DISTINCT entity FROM " + DataAccessSQL.DICTIONARY + " WHERE " + DataAccessSQL.DICTIONARY + ".entitytype=" + EntityType.NAMED_ENTITY.ordinal() + ") as dictionary " +
            " WHERE dictionary.entity= " + DataAccessSQL.ENTITY_LANGUAGES + ".entity ORDER BY language", true, "language"),
  DICTIONARY_LANGUAGE_C(
      "dictionary_languages_c", String.class, Encoding.KRYO, 20, String[].class, Encoding.KRYO,
      "SELECT DISTINCT language, mention FROM " + DataAccessSQL.ENTITY_LANGUAGES + "," +
          " (SELECT DISTINCT entity FROM " + DataAccessSQL.DICTIONARY + " WHERE " + DataAccessSQL.DICTIONARY + ".entitytype=" + EntityType.CONCEPT.ordinal() + ") as dictionary " +
          " WHERE dictionary.entity= " + DataAccessSQL.ENTITY_LANGUAGES + ".entity ORDER BY language", true, "language"),
  ENTITY_MENTION("dictionary", Integer.class, Encoding.INTS, 1, String[].class, Encoding.KRYO,
      "SELECT DISTINCT entity, mention FROM " + DataAccessSQL.DICTIONARY + " ORDER BY entity", true, "entity"),
  DICTIONARY_MENTION("dictionary", String.class, Encoding.KRYO, 1, TIntDoubleHashMap.class, Encoding.KRYO,
      "SELECT DISTINCT mention, entity, prior FROM " + DataAccessSQL.DICTIONARY + " ORDER BY mention", true, "mention"),
  ENTITY_RANK_ENTITY("entity_rank", Integer.class,
      Encoding.INTS, 1, double[].class, Encoding.KRYO, "entity"),
  WIKIDATA_ID("wikidata_id", String.class, Encoding.KRYO, 1, Integer.class, Encoding.INTS,
      "SELECT wikidataid, entity FROM entity_metadata", true, "wikidataid"),
  ENTITY_METADATA_ENTITY("entity_metadata", Integer.class, Encoding.INTS, 1,
      KeyValueStoreRow[].class, Encoding.KRYO, "entity"),
  BIGRAM_COUNTS_BIGRAM(DataAccessSQL.BIGRAM_COUNTS, Integer.class, Encoding.INTS, 1,
      Integer.class, Encoding.INTS, "bigram"),
  ENTITY_BIGRAMS_ENTITY(DataAccessSQL.ENTITY_BIGRAMS, Integer.class, Encoding.INTS, 1, int[].class,
      Encoding.INTS, true, "entity"),
  ENTITY_KEYWORDS_ENTITY(DataAccessSQL.ENTITY_KEYWORDS, Integer.class, Encoding.INTS, 1, int[].class, Encoding.INTS,
      true, "entity"),
  ENTITY_IDS_ID("entity_ids", Integer.class, Encoding.KRYO, 1, KeyValueStoreRow.class, Encoding.KRYO,
      "id"),
  DICTIONARY_ENTITIES(DataAccessSQL.DICTIONARY, Integer.class, Encoding.INTS, 1, Integer.class, Encoding.INTS, "SELECT entity, entitytype FROM " + DataAccessSQL.DICTIONARY + " ORDER BY entity", true, "entity"),
  ENTITY_INLINKS_ENTITY("entity_inlinks", Integer.class, Encoding.INTS, 1, int[].class, Encoding.INTS,
      "SELECT entity, inlinks AS inlink FROM entity_inlinks", true, "entity"),
  ENTITY_TYPES_ENTITY("entity_types", Integer.class, Encoding.INTS, 1, int[].class, Encoding.INTS,
      "SELECT entity, types AS type FROM entity_types", true, "entity"),
  KEYWORD_COUNTS_KEYWORD(DataAccessSQL.KEYWORD_COUNTS, Integer.class, Encoding.INTS, 1, Integer.class, Encoding.INTS, "keyword"),
  METADATA_KEY("meta",
      String.class, Encoding.KRYO, 1, String.class, Encoding.KRYO, "key"),
  TYPE_IDS_ID("type_ids", Integer.class, Encoding.KRYO, 1,
      KeyValueStoreRow[].class, Encoding.KRYO, "id"),
  WORD_EXPANSION_WORD("word_expansion", Integer.class, Encoding.INTS, 1, Integer.class,
      Encoding.INTS, "word");

  public enum Encoding {KRYO, INTS};






  private String name;

  private String source;

  private boolean isSourceSorted;

  private boolean isSourceTable;

  private boolean valueIntPairs;

  private TreeSet<String> keys;

  private Class keyClass;

  private Class valueClass;

  private Encoding keyEncoding;

  private Encoding valueEncoding;

  private int partitions;

  /**
   * This is the main constructor and is usually only used by the other constructors.
   *
   * @param name The name of the KeyValueStore.
   * @param keyClass The type of the key.
   * @param valueClass The type of the value.
   * @param source The source the KeyValueStore should be created from (sql command or table name).
   * @param isSourceSorted Indicates if the source is sorted.
   * @param isSourceTable Indicates if the source is a table or a sql command.
   * @param keys The columns of the source which should be used as key.
   */
  DatabaseKeyValueStore(String name, Class keyClass, Encoding keyEncoding, int partitions, Class valueClass, Encoding valueEncoding, boolean valueIntPairs,
      String source, boolean isSourceSorted, boolean isSourceTable, String... keys) {
    this.name = name;
    this.keyClass = keyClass;
    this.valueClass = valueClass;
    this.source = source;
    this.isSourceSorted = isSourceSorted;
    this.isSourceTable = isSourceTable;
    this.keys = new TreeSet<>(Arrays.asList(keys));
    this.valueIntPairs = valueIntPairs;
    this.keyEncoding = keyEncoding;
    this.valueEncoding = valueEncoding;
    this.partitions = partitions;
  }

  /**
   * This constructor make it easier to create KeyValueStores from tables.
   * The name of the KeyValueStore will be generated from the table name and the keys.
   *
   * @param tableName The name of the table.
   * @param keyClass The type of the key.
   * @param valueClass The type of the value.
   * @param keys The columns of the table which should be used as key.
   */
  DatabaseKeyValueStore(String tableName, Class keyClass, Encoding keyEncoding, int partitions, Class valueClass, Encoding valueEncoding, String... keys) {
    this(null, keyClass, keyEncoding, partitions, valueClass, valueEncoding, false, tableName, false, true, keys);
    this.name = getMapName(this.source, this.keys);
  }

  /**
   * This constructor make it easier to create KeyValueStores from tables.
   * The name of the KeyValueStore will be generated from the table name and the keys.
   *
   * @param tableName The name of the table.
   * @param keyClass The type of the key.
   * @param valueClass The type of the value.
   * @param keys The columns of the table which should be used as key.
   */
  DatabaseKeyValueStore(String tableName, Class keyClass, Encoding keyEncoding, int partitions, Class valueClass, Encoding valueEncoding, boolean valueIntPairs,
      String... keys) {
    this(null, keyClass, keyEncoding, partitions, valueClass, valueEncoding, valueIntPairs, tableName, false, true, keys);
    this.name = getMapName(this.source, this.keys);
  }

  /**
   * This is constructor makes is easier to create KeyValueStores from SQL commands.
   * The name of the KeyValueStore will be generated from name prefix and the keys.
   *
   * @param namePrefix Prefix of the KeyValueStore name.
   * @param keyClass The type of the key.
   * @param valueClass The type of the value.
   * @param sqlCommand The SQL command the KeyValueStore is created from.
   * @param isSourceSorted Indicates if the table returned by the sql command is already correctly sorted.
   * @param keys The columns of the table returned by the sql command which should be used as key.
   */
  DatabaseKeyValueStore(String namePrefix, Class keyClass, Encoding keyEncoding, int partitions, Class valueClass, Encoding valueEncoding, String sqlCommand,
      boolean isSourceSorted, String... keys) {
    this(null, keyClass, keyEncoding, partitions, valueClass, valueEncoding, false, sqlCommand, isSourceSorted, false, keys);
    this.name = getMapName(namePrefix, this.keys);
  }

  public String getName() {
    return name;
  }

  public String getSource() {
    return source;
  }

  public boolean isSourceSorted() {
    return isSourceSorted;
  }

  public boolean isSourceTable() {
    return isSourceTable;
  }

  public TreeSet<String> getKeys() {
    return keys;
  }

  public Class getKeyClass() {
    return keyClass;
  }

  public Class getValueClass() {
    return valueClass;
  }

  public Encoding getKeyEncoding() {
    return keyEncoding;
  }

  public Encoding getValueEncoding() {
    return valueEncoding;
  }

  private static String getMapName(String table, Set<String> keys) {
    return table + "-" + StringUtils.join(keys, "_");
  }

  public boolean isValueIntPairs() {
    return valueIntPairs;
  }

  public int getPartitions() {
    return partitions;
  }
}

/*

ENCODING STATS CASSANDRA

command -> nodetool cfstats

aida_en_yago2_20100817_v11_kryo

bigram_counts_bigram               257693459
dictionary_mention                 476115974
entity_bigrams_entity              954338758
entity_ids_entity_knowledgebase   1622349641
entity_ids_id                      498856484
entity_inlinks_entity              189297119
entity_keywords_entity             583499936
entity_metadata_entity             600114698
entity_rank_entity                  78961714
entity_types_entity                 91033629
keyword_counts_keyword              65882968
meta_key                                   0
type_ids_id                         20554545
word_expansion_word                423238949
word_ids_word                     1732551452

aida_en_yago2_20100817_v11_protos

bigram_counts_bigram			         258597158
dictionary_mention                 420171835
entity_bigrams_entity             1625889131
entity_ids_entity_knowledgebase    644859679
entity_ids_id                      423370535
entity_inlinks_entity              258467043
entity_keywords_entity            1046053472
entity_metadata_entity             569176426
entity_rank_entity                  89184819
entity_types_entity                105074831
keyword_counts_keyword              66101164
meta_key                                5298
type_ids_id                         17223570
word_expansion_word                432773404
word_ids_word                     1527030840

*/