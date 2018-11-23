package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.Pair;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.util.CoreNlpUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.uima.jcas.JCas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.apache.uima.fit.util.JCasUtil.select;
import static org.apache.uima.fit.util.JCasUtil.selectCovered;

public class Tokens implements Iterable<Token>, Serializable {

  private static final long serialVersionUID = 8015832523759790735L;

  private List<Token> tokens = null;

  private String originalStart = "";

  private String originalEnd = "";

  private int pageNumber = -1;

  private List<Pair<Integer, Integer>> sentencesBorders = null;

  private TObjectIntHashMap<String> transientTokenIds = new TObjectIntHashMap<>(16, 0.75f, Context.UNKNOWN_TOKEN_ID);

  public Tokens() {
    tokens = new ArrayList<>();
  }

  public Tokens(List<String> words) {
    tokens = new ArrayList<>();
    int tokenIndex = 0;
    int charOffset = 0;
    for (String word : words) {
      int endOffset = charOffset + word.length();
      Token t = new Token(tokenIndex, word, charOffset, endOffset, 1);
      ++tokenIndex;
      // Add a virtual space.
      charOffset = endOffset + 2;
      tokens.add(t);
    }
  }

  public Token getToken(int position) {
    return tokens.get(position);
  }

  public List<Token> getTokens() {
    return tokens;
  }

  public int size() {
    return tokens.size();
  }

  public void addToken(Token token) {
    tokens.add(token);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder(200);
    for (int i = 0; i < tokens.size(); i++) {
      sb.append(tokens.get(i).toString()).append('\n');
    }
    return sb.toString();
  }

  //This does not work with Chinese because the tokenization is not white space separated
  public String toText(int startToken, int endToken) {
    StringBuilder sb = new StringBuilder(200);
    for (int i = startToken; i <= endToken; i++) {
      sb.append(tokens.get(i).getOriginal());
      if (i + 1 <= endToken) {
        sb.append(tokens.get(i).getOriginalEnd());
      }
    }
    return sb.toString();
  }

  public String toTextLemmatized(int startToken, int endToken) {
    StringBuilder sb = new StringBuilder(200);
    for (int i = startToken; i <= endToken; i++) {
      sb.append(tokens.get(i).getLemma());
      if (i + 1 <= endToken) {
        sb.append(tokens.get(i).getOriginalEnd());
      }
    }
    return sb.toString();
  }

  /**
   * Use the method for evaluation with combination tokenizer.type.*conll.
   * (by converting tokens read from CoNLL dataset into text which).
   * @return
   */
  public String toTextCoNLL() {
    StringBuilder sb = new StringBuilder(200);
    sb.append(originalStart);
    for (int i = 0; i < tokens.size(); i++) {
      sb.append(tokens.get(i).getOriginal());
      sb.append(" ");
    }
    sb.append(originalEnd);
    return sb.toString();
  }

  public void setOriginalStart(String text) {
    originalStart = text;
  }

  public void setOriginalEnd(String text) {
    originalEnd = text;
  }

  public String getOriginalStart() {
    return originalStart;
  }

  public String getOriginalEnd() {
    return originalEnd;
  }

  @Override public Iterator<Token> iterator() {
    return tokens.iterator();
  }

  public List<List<Token>> getSentenceTokens() {
    List<List<Token>> sentences = new ArrayList<List<Token>>();
    int currentSentenceId = -1;
    List<Token> currentSentence = null;
    for (Token t : this) {
      if (t.getSentence() != currentSentenceId) {
        List<Token> sentence = new ArrayList<Token>();
        sentences.add(sentence);
        currentSentence = sentence;
        ++currentSentenceId;
      }
      currentSentence.add(t);
    }
    return sentences;
  }

  /**
   * Copies tokens from interval.
   *
   * @param from inclusive
   * @param to exclusive
   * @return range of original tokens
   */
  public Tokens copyOfRange(int from, int to) {
    int newLength = to - from;
    if (newLength < 0) throw new IllegalArgumentException(from + " > " + to);

    Tokens copy = new Tokens();

    for (Token token : tokens.subList(from, to))
      copy.addToken(token);

    return copy;
  }

  /**
   * Returns list of sentences.
   *
   * @return
   * @deprecated Buggy, use getSentenceTokens instead!
   */
  @Deprecated public List<Tokens> getSentences() {
    List<Tokens> sentences = new LinkedList<Tokens>();

    int from = 0;
    int to = 0;
    for (int i = 0; i < tokens.size(); ++i) {
      to = i;
      if (tokens.get(from).getSentence() != tokens.get(to).getSentence()) {
        sentences.add(copyOfRange(from, to));
        from = to;
        continue;
      }
      if (to == tokens.size() - 1) {
        sentences.add(copyOfRange(from, to + 1));
        break;
      }
    }

    return sentences;
  }

  /**
   * Returns list of borders (left token number of a sentence, right (exclusive) token number of a sentence).
   *
   * @return
   */
  public List<Pair<Integer, Integer>> getSentencesBorders() {
    if (sentencesBorders == null) {
      sentencesBorders = new LinkedList<Pair<Integer, Integer>>();
      int from = 0;
      int to = 0;
      for (int i = 0; i < tokens.size(); ++i) {
        to = i;
        if (tokens.get(from).getSentence() != tokens.get(to).getSentence()) {
          sentencesBorders.add(new Pair<Integer, Integer>(from, to));
          from = to;
          continue;
        }
        if (to == tokens.size() - 1) {
          sentencesBorders.add(new Pair<Integer, Integer>(from, to + 1));
          break;
        }
      }
    }
    return sentencesBorders;
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(int pNumber) {
    pageNumber = pNumber;
    for (Token tok : tokens) {
      tok.setPageNumber(pNumber);
    }
  }

  public void setTransientTokenIds(TObjectIntHashMap<String> tokenIds) {
    transientTokenIds = tokenIds;
  }

  public int getIdForTransientToken(String token) {
    return transientTokenIds.get(token);
  }

  public static Tokens getTokensFromJCas(JCas jCas) {
    Tokens tokens = new Tokens();
    int s_number = 0; //DKPro does not give sentence index????????
    int t_number = 0;
    for (Sentence sentence : select(jCas, Sentence.class)) {
      List<de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token> dktokens = selectCovered(jCas,
          de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token.class, sentence);
      for (de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token t : dktokens) {
        CoreLabel taggedWord = CoreNlpUtils.tokenToWord(
            t); //This step should be avoided. Transform directly from DKPRO to AIDA TOKEN. Problem POS mappings. AIDA works with Stanford tags
        Token aidaToken = new Token(t_number, t.getCoveredText(), t.getBegin(), t.getEnd(), 0);
        aidaToken.setPOS(taggedWord.get(CoreAnnotations.PartOfSpeechAnnotation.class));
        aidaToken.setSentence(s_number);
        tokens.addToken(aidaToken);
        t_number++;
      }
      s_number++;
    }
    return tokens;
  }
}
