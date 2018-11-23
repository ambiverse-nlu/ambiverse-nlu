package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Domain;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.DomainWord;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.type.Synset;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.timing.RunningTimer;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.filehandlers.FileLines;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DomainWordsGenerator extends JCasAnnotator_ImplBase {

  private static final String WORDNETDOMAINPATH = "src/main/resources/wordnet_domains/wn-domains-3.2/wn-domains-3.2-20070223_wordnetVersion30";
  private static Pattern domainFileLinePattern = Pattern.compile("^(\\d{8})-(.)\t(.*)$");
  private Map<String, List<String>> domainSynsets;
  private Dictionary wordnet;


  @Override
  public void initialize(org.apache.uima.UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    try {
      loadDomainSynsetsMap();
    } catch (IOException e) {
      System.err.println("Could not load domain synsets map.");
    }
    try {
      wordnet = Dictionary.getDefaultResourceInstance();
    } catch (JWNLException e) {
      System.err.println("Could not load wordnet.");
    }
  };


  private void loadDomainSynsetsMap() throws IOException {
    FileLines wordnetDomain = new FileLines(WORDNETDOMAINPATH);
    domainSynsets = new HashMap<>();

    for(String line:wordnetDomain) {
      Matcher matcher = domainFileLinePattern.matcher(line);
      if(matcher.find()) {
        String pos = "";
        switch (matcher.group(2)) {
          case "n":
            pos = "1";
            break;
          case "v":
            pos = "2";
            break;
          case "a":
          case "s":
            pos = "3";
            break;
          case "r":
            pos = "4";
            break;
        }
        String synset = pos + matcher.group(1);
        String domains = matcher.group(3);
        String[] split = domains.split("\\s");
        for(String sp:split) {
          domainSynsets.computeIfAbsent(sp, v -> new ArrayList<>()).add(synset);
        }
      }
    }
    wordnetDomain.close();
  }


  private List<String> getWordForSynset(String synsetId) throws NumberFormatException {
    List<String> words = new ArrayList<>();
    
    POS pos = POS.NOUN;
    switch (synsetId.charAt(0)) {
      case '1':
        pos = POS.NOUN;
        break;
      case '2':
        pos = POS.VERB;
        break;
      case '3':
        pos = POS.ADJECTIVE;
        break;
      case '4':
        pos = POS.ADVERB;
    }
    Long synsetIdlong = new Long(synsetId.substring(1, synsetId.length()));
    try {
      net.sf.extjwnl.data.Synset synset = wordnet.getSynsetAt(pos, synsetIdlong);
      for(Word w:synset.getWords()) {
        words.add(w.getLemma().toLowerCase());
      }
    }
    catch(Exception e) {
      System.err.println("Error while getting words for synset: " + synsetId);
    }
    return words;
  }


  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    Integer runningId = RunningTimer.recordStartTime("DomainWordGenerator");
    Collection<Domain> domainsArray = JCasUtil.select(jCas, Domain.class);
    Set<String> allSynsets = new HashSet<>();
    Set<String> allWords = new HashSet<>();
    
    for (Domain domain:domainsArray) {
      List<String> synsets = domainSynsets.get(domain.getDomain());
      if(synsets != null) {
        allSynsets.addAll(synsets);
      }
      
      for (String synset:synsets) {
        try {
          List<String> words = getWordForSynset(synset);
          allWords.addAll(words);
        } catch (NumberFormatException e) {
          System.err.println("Making new Long error for synset: " + synset);
        }
      }
    }
    
    for(String synset:allSynsets) {
      Synset jCasSynset = new Synset(jCas);
      jCasSynset.setSynset(synset);
      jCasSynset.addToIndexes();
    }
    
    for(String word:allWords) {
      DomainWord jCasConceptCandidate = new DomainWord(jCas);
      jCasConceptCandidate.setDomainWord(word);
      jCasConceptCandidate.addToIndexes();
    }
    RunningTimer.recordEndTime("DomainWordGenerator", runningId);
  }
}
