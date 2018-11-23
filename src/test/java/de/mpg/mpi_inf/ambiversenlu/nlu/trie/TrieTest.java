package de.mpg.mpi_inf.ambiversenlu.nlu.trie;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Token;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes.UimaTokenizer;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import org.apache.lucene.util.fst.FST;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TrieTest {

  @Before
  public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test_multi");
  }

  @Test
  public void testTrieChinese() throws Exception {
    Set<String> dictionary = new TreeSet<>();
    dictionary.add("马拉多纳");
    dictionary.add("1986年世界杯");
    FST<Long> trie = TrieBuilder.buildTrie(dictionary);

    String text = "马拉多纳扮演1986年世界杯。";
    Set<Integer> begginingPositions = new HashSet<>();
    Set<Integer> endingPrositions = new HashSet<>();
    Tokens tokens = UimaTokenizer.tokenize(Language.getLanguageForString("zh"), text);
    for(Token token: tokens.getTokens()) {
      begginingPositions.add(token.getBeginIndex());
      endingPrositions.add(token.getEndIndex());
    }
    Set<Spot> spots =  TextSpotter.spotTrieEntriesInTextIgnoreCase(trie,text,begginingPositions,endingPrositions,0.9);
    assertEquals(spots.size(), 2);
    ListMultimap<String, Spot> results = ArrayListMultimap.create();
    for(Spot spot : spots) {
      String concept = Utils.getStringbyKey(spot.getMatch(), trie);
      results.put(concept, spot);
      String textMatch = text.substring(spot.getBegin(), spot.getEnd());
//      System.out.println("CONCEPT: " + concept + " TEXT MATCH: " + textMatch + " begin: " + spot.getBegin() + " end: " + spot.getEnd());
    }

    assertTrue(results.containsKey("马拉多纳"));
    Spot maradona = results.get("马拉多纳").get(0);
    assertEquals(maradona.getBegin(), 0);
    assertEquals(maradona.getEnd(), 4);

    assertTrue(results.containsKey("1986年世界杯"));
    Spot worldCup = results.get("1986年世界杯").get(0);
    assertEquals(worldCup.getBegin(), 6);
    assertEquals(worldCup.getEnd(), 14);

  }

  @Test
  public void testTrieSpanish() throws Exception {
    Set<String> dictionary = new TreeSet<>();
    dictionary.add("maradona");
    dictionary.add("villa");
    dictionary.add("villa fiorito");
    dictionary.add("niña");
    dictionary.add("cosecha");
    dictionary.add("acecha");
    dictionary.add("buena");
    FST<Long> trie = TrieBuilder.buildTrie(dictionary);

    String text = "De niño Maradona vivia en Villa Fiorito una de las villas en los suburbios de Buenos Aires";
    Set<Integer> begginingPositions = new HashSet<>();
    Set<Integer> endingPrositions = new HashSet<>();
    Tokens tokens = UimaTokenizer.tokenize(Language.getLanguageForString("es"), text);
    for(Token token: tokens.getTokens()) {
      begginingPositions.add(token.getBeginIndex());
      endingPrositions.add(token.getEndIndex());
    }
    Set<Spot> spots =  TextSpotter.spotTrieEntriesInTextIgnoreCase(trie,text,begginingPositions,endingPrositions,0.9);
    assertEquals(spots.size(), 6);
    ListMultimap<String, Spot> results = ArrayListMultimap.create();
    for(Spot spot : spots) {
      String concept = Utils.getStringbyKey(spot.getMatch(), trie);
      results.put(concept, spot);
      String textMatch = text.substring(spot.getBegin(), spot.getEnd());
//      System.out.println("CONCEPT: " + concept + " TEXT MATCH: " + textMatch + " begin: " + spot.getBegin() + " end: " + spot.getEnd());
    }

    assertTrue(results.containsKey("niña"));
    Spot nina = results.get("niña").get(0);
    assertEquals(nina.getBegin(), 3);
    assertEquals(nina.getEnd(), 7);

    assertTrue(results.containsKey("villa"));
    Spot villa1 = results.get("villa").get(0);
    assertEquals(villa1.getBegin(), 26);
    assertEquals(villa1.getEnd(), 31);
    Spot villa2 = results.get("villa").get(1);
    assertEquals(villa2.getBegin(), 51);
    assertEquals(villa2.getEnd(), 57);

    assertTrue(results.containsKey("villa fiorito"));
    Spot villa3 = results.get("villa fiorito").get(0);
    assertEquals(villa3.getBegin(), 26);
    assertEquals(villa3.getEnd(), 39);

    assertTrue(results.containsKey("buena"));
    Spot buena = results.get("buena").get(0);
    assertEquals(buena.getBegin(), 78);
    assertEquals(buena.getEnd(), 84);

    assertTrue(results.containsKey("maradona"));
    Spot maradona = results.get("maradona").get(0);
    assertEquals(maradona.getBegin(), 8);
    assertEquals(maradona.getEnd(), 16);
    }
}
