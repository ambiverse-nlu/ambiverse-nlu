package de.mpg.mpi_inf.ambiversenlu.nlu.trie;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.lucene.util.fst.FST;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TextSpotterTest {

	private final List<int[]> tokens = new GsonBuilder().enableComplexMapKeySerialization().create()
			.fromJson("[[25,31],[32,40],[40,41],[42,45]," +
							"[46,47],[47,53],[54,65],[65,66],[67,74],[74,75],[75,76],[77,78],[78,79],[79,80],[81,83],[83,84],[84,89]," +
							"[90,94],[94,95],[95,103],[103,104],[104,105],[106,110],[110,111],[112,122],[122,123],[124,132],[132,133]," +
							"[133,134],[135,137],[138,144],[145,149],[149,150],[151,159],[159,160],[161,171],[171,172],[173,176],[176,177]," +
							"[177,178],[178,179],[180,194],[194,195],[196,200],[201,203],[204,215],[216,227],[228,241],[242,248],[248,249]," +
							"[250,257],[258,269],[270,276],[277,279],[280,286],[287,291],[292,296],[296,297],[298,310],[311,327],[327,328]]",
					new TypeToken<List<int[]>>() {}.getType());
	private final Set<Integer> begins = tokens.stream().map(t -> t[0] - 25).collect(Collectors.toSet());
	private final Set<Integer> ends = tokens.stream().map(t -> t[1] - 25).collect(Collectors.toSet());
	private final String sentenceText = "Albert Einstein, МФА [ˈalbɐt ˈaɪ̯nʃtaɪ̯n] слушать[C 1]; " +
			"14 Merke 1879(18790314), ECB1, Вюртемберг, Германия — 18 dfdfdf 1955, Принстон, Нью-Джерси, США) " +
			"— физик-теоретик, один из основателей современной теоретической Gatess, dfdfdfd Нобелевской премии " +
			"по физике 1921 года, общественный деятель-гуманист.";
	private final String sentenceText2 = "Albert Einstein, МФА [ˈalbɐt ˈaɪ̯nʃtaɪ̯n] слушать[C 1]; " +
			"14 Merke 1879(18790314), dfdf, Вюртемберг, Германия — 18 ECB123 1955, Gatessss, Нью-Джерси, США) " +
			"— физик-теоретик, один из основателей современной теоретической dfdfdf, dfdfdfd Нобелевской премии " +
			"по физике 1921 года, общественный деятель-гуманист.";

	@Test
	public void shouldSpotOneWord() throws IOException {

		Set<String> sortedMentions = new TreeSet<>(Collections.singletonList("Германия".toLowerCase()));
		FST<Long> trie = TrieBuilder.buildTrie(sortedMentions);
		Set<Spot> spots = TextSpotter.spotTrieEntriesInTextIgnoreCase(trie, sentenceText, begins, ends, 1.0);
		assertEquals(1, spots.size());
	}

	@Test
	public void shouldSpotOneFuzzyWord() throws IOException {

		Set<String> sortedMentions = new TreeSet<>(Collections.singletonList("Германие".toLowerCase())); //87.5% match
		FST<Long> trie = TrieBuilder.buildTrie(sortedMentions);

		Set<Spot> spots = TextSpotter.spotTrieEntriesInTextIgnoreCase(trie, sentenceText, begins, ends, 0.8);
		assertEquals(1, spots.size());
		Spot spot = spots.iterator().next();
		assertEquals("Германия", sentenceText.substring(spot.getBegin(), spot.getEnd()));
	}

	@Test
	public void shouldNotSpotOneFuzzyWord() throws IOException {

		Set<String> sortedMentions = new TreeSet<>(Collections.singletonList("Einstei1".toLowerCase())); //87.5% match
		FST<Long> trie = TrieBuilder.buildTrie(sortedMentions);

		Set<Spot> spots = TextSpotter.spotTrieEntriesInTextIgnoreCase(trie, sentenceText, begins, ends, 0.8);
		assertEquals(1, spots.size());
		Spot spot = spots.iterator().next();
		assertEquals("Einstein", sentenceText.substring(spot.getBegin(), spot.getEnd()));
	}

	@Test
	public void shouldSpotTwoWords() throws IOException {

		Set<String> sortedMentions = new TreeSet<>(Collections.singletonList("Нобелевской премии".toLowerCase())); //87.5% match
		FST<Long> trie = TrieBuilder.buildTrie(sortedMentions);

		Set<Spot> spots = TextSpotter.spotTrieEntriesInTextIgnoreCase(trie, sentenceText, begins, ends, 0.9);
		assertEquals(1, spots.size());
		Spot spot = spots.iterator().next();
		assertEquals("Нобелевской премии", sentenceText.substring(spot.getBegin(), spot.getEnd()));
	}

//	todo
//	@Test
	public void shouldSpotTwoFuzzyWords() throws IOException {

		Set<String> sortedMentions = new TreeSet<>(Collections.singletonList("Alber1 Einstei2".toLowerCase())); //76.47% match
		FST<Long> trie = TrieBuilder.buildTrie(sortedMentions);

		Set<Spot> spots = TextSpotter.spotTrieEntriesInTextIgnoreCase(trie, sentenceText, begins, ends, 0.90);
		assertEquals(1, spots.size());
		Spot spot = spots.iterator().next();
		assertEquals("Albert Einstein", sentenceText.substring(spot.getBegin(), spot.getEnd()));
	}

	@Test
	public void shouldSpotShortECB1Word() throws IOException {

		Set<String> sortedMentions = new TreeSet<>(Collections.singletonList("ECB".toLowerCase()));
		FST<Long> trie = TrieBuilder.buildTrie(sortedMentions);

		Set<Spot> spots = TextSpotter.spotTrieEntriesInTextIgnoreCase(trie, sentenceText, begins, ends, 0.9);
		assertEquals(1, spots.size());
		Spot spot = spots.iterator().next();
		assertEquals("ECB1", sentenceText.substring(spot.getBegin(), spot.getEnd()));
	}

	@Test
	public void shouldNotSpotECB123Word() throws IOException {

		Set<String> sortedMentions = new TreeSet<>(Collections.singletonList("ECB".toLowerCase()));
		FST<Long> trie = TrieBuilder.buildTrie(sortedMentions);

		Set<Spot> spots = TextSpotter.spotTrieEntriesInTextIgnoreCase(trie, sentenceText2, begins, ends, 0.9);
		assertEquals(0, spots.size());
	}

	@Test
	public void shouldSpotGatessWord() throws IOException {

		Set<String> sortedMentions = new TreeSet<>(Collections.singletonList("Gates".toLowerCase()));
		FST<Long> trie = TrieBuilder.buildTrie(sortedMentions);

		Set<Spot> spots = TextSpotter.spotTrieEntriesInTextIgnoreCase(trie, sentenceText, begins, ends, 0.8);
		assertEquals(1, spots.size());
		Spot spot = spots.iterator().next();
		assertEquals("Gatess", sentenceText.substring(spot.getBegin(), spot.getEnd()));
	}

	@Test
	public void shouldNotSpotGatessssWord() throws IOException {

		Set<String> sortedMentions = new TreeSet<>(Collections.singletonList("Gates".toLowerCase()));
		FST<Long> trie = TrieBuilder.buildTrie(sortedMentions);

		Set<Spot> spots = TextSpotter.spotTrieEntriesInTextIgnoreCase(trie, sentenceText2, begins, ends, 0.8);
		assertEquals(0, spots.size());
	}

	@Test
	public void shouldNotSpotShorterMerkeWord() throws IOException {

		Set<String> sortedMentions = new TreeSet<>(Collections.singletonList("Merkel".toLowerCase()));
		FST<Long> trie = TrieBuilder.buildTrie(sortedMentions);

		Set<Spot> spots = TextSpotter.spotTrieEntriesInTextIgnoreCase(trie, sentenceText, begins, ends, 0.9);
		assertEquals(0, spots.size());
	}

}