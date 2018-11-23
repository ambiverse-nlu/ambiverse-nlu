package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import com.google.common.collect.Range;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.DataAccess;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.filereading.FileEntries;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes.Pair;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.util.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreparedInput implements Iterable<PreparedInputChunk>, Serializable {

  protected Logger logger_ = LoggerFactory.getLogger(PreparedInput.class);

  /** Must not be null. */
  protected String docId_;

  /**
   * The title of the document. Not part of the text that will be disambiguated.
   */
  protected String title_;

  /** Must not be null. */
  protected String text_;

  /**
   * The range of the title in the text_.
   */
  protected Range<Integer> titleRange_;

  /**
   * The range of the abstract in the text_.
   */
  protected Range<Integer> abstractRange_;

  protected List<PreparedInputChunk> chunks_;

  protected static Pattern p = Pattern.compile("-DOCSTART- \\((.*?)\\)");

  /**
   * Timestamp (at midnight) of when this document was published. May be empty.
   */
  protected long timestamp_;

  protected transient Set<String> punctuations_ = getPuncuations();

  public PreparedInput(String docId, List<PreparedInputChunk> chunks) {
    logger_.debug("Creating prepared input for document {} and chunks {}.", docId, chunks);
    setDocId(docId);
    chunks_ = chunks;
  }

  /**
   * Use this very carefully, e.g. during dataset construction.
   * When reading a dataset this should never be called.
   *
   * @param docId The id of the document.
   */
  public void setDocId(String docId) {
    docId_ = docId;
  }

  public String getDocId() {
    return docId_;
  }

  public String getTitle() {
    return title_;
  }

  public void setTitle(String title) {
    this.title_ = title;
  }

  public Range getTitleRange() {
    return titleRange_;
  }

  public void setTitleRange(Range titleRange) {
    this.titleRange_ = titleRange;
  }

  public Range getAbstractRange() {
    return abstractRange_;
  }

  public void setAbstractRange(Range abstractRange) {
    this.abstractRange_ = abstractRange;
  }


  public int getMentionSize() {
    int mentionSize = 0;
    for (PreparedInputChunk c : this) {
      mentionSize += (c.getConceptMentions().getMentions().size() + c.getNamedEntityMentions().getMentions().size());
    }
    return mentionSize;
  }

  public int getChunksCount() {
    return chunks_.size();
  }

  public Tokens getTokens() {
    Tokens allTokens = new Tokens();
    for (PreparedInputChunk c : this) {
      for (Token t : c.getTokens()) {
        allTokens.addToken(t);
      }
    }
    return allTokens;
  }

  public Mentions getConceptMentions() {
    Mentions conceptMentions = new Mentions();
    for (PreparedInputChunk c : this) {
      for (Map<Integer, Mention> innerMap : c.getConceptMentions().getMentions().values()) {
        for (Mention m : innerMap.values()) {
          conceptMentions.addMention(m);
        }
      }
    }
    return conceptMentions;
  }
  
  public Mentions getNamedEntityMentions() {
    Mentions namedEntityMentions = new Mentions();
    for (PreparedInputChunk c : this) {
      for (Map<Integer, Mention> innerMap : c.getNamedEntityMentions().getMentions().values()) {
        for (Mention m : innerMap.values()) {
          namedEntityMentions.addMention(m);
        }
      }
    }
    return namedEntityMentions;
  }
  
  public Mentions getMentions() {
    Mentions allMentions = new Mentions();
    for (PreparedInputChunk c : this) {
      for (Map<Integer, Mention> innerMap : c.getConceptMentions().getMentions().values()) {
        for (Mention m : innerMap.values()) {
          allMentions.addMention(m);
        }
      }
      for (Map<Integer, Mention> innerMap : c.getNamedEntityMentions().getMentions().values()) {
        for (Mention m : innerMap.values()) {
          allMentions.addMention(m);
        }
      }
    }
    return allMentions;
  }

  public String getOriginalText() {
    return text_;
  }

  @Override public Iterator<PreparedInputChunk> iterator() {
    return chunks_.iterator();
  }

  public void setMentionEntitiesTypes(Set<Type> filteringTypes) {
    for (PreparedInputChunk c : this) {
      c.getConceptMentions().setEntitiesTypes(filteringTypes);
      c.getNamedEntityMentions().setEntitiesTypes(filteringTypes);
    }
  }

  /**
   * Loads the necessary information from a file in AIDA-collection-format.
   *
   * @param file  File in AIDA collection format.
   */
  public PreparedInput(File file) throws IOException, EntityLinkingDataAccessException {
    this(file, 0, true);
  }

  /**
   * Loads the necessary information from a file in AIDA-collection-format, 
   * discarding mentions with less than the given minimum occrurence count.
   *
   * @param file  File in AIDA collection format.
   * @param mentionMinOccurrences Minimum number of occurrences a mention must have to be included
   *                              (must be present in data)
   * @param inludeOODMentions Set to false to drop all mentions that are not in the dictionary.                             
   */
  public PreparedInput(File file, int mentionMinOccurrences, boolean inludeOODMentions) throws IOException, EntityLinkingDataAccessException {
    Pair<PreparedInputChunk, Long> loaded = loadFrom(file, mentionMinOccurrences, inludeOODMentions);
    setDocId(loaded.first.getChunkId());
    chunks_ = new ArrayList<PreparedInputChunk>(1);
    chunks_.add(loaded.first);
    timestamp_ = loaded.second;
  }

  protected Pair<PreparedInputChunk, Long> loadFrom(File f, int mentionMinOccurrences, boolean includeOutOfDictionaryMentions)
      throws IOException, EntityLinkingDataAccessException {
    String docId = null;
    Tokens tokens = null;
    Mentions mentions = null;
    long timestamp = 0;
    // Helpers.
    boolean first = true;
    int sentence = 0;
    int position = -1;
    int index = 0;
    for (String line : new FileEntries(f)) {
      if (first) {
        // Read metadata.
        if (!line.startsWith("-DOCSTART-")) {
          logger_.error("Invalid input format, first line has to start with " + "-DOCSTART-");
        } else {
          // Parse metadata.
          String[] data = line.split("\t");
          Matcher m = p.matcher(data[0]);
          if (m.find()) {
            // Initialize datastructures.
            docId = m.group(1);
            tokens = new Tokens();
            mentions = new Mentions();
            // Read time if it exists.
            if (data.length > 1) {
              String[] dateParts = data[1].split("-");
              timestamp = new DateTime(Integer.parseInt(dateParts[0]), Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[2]), 0, 0,
                  DateTimeZone.UTC).getMillis();
            }
          } else {
            logger_.error("Could not find docid in " + line);
          }
        }
        first = false;
      } else {
        // Read document line by line
        if (line.length() == 0) {
          sentence++;
          continue;
        }
        String[] data = line.split("\t");
        position++;
        boolean mentionStart = false;
        String word = null;
        String textMention = null;
        String entity = null;
        String ner = null;
        int mentionOccurrenceCount = 0;
        if (data.length == 0) {
          logger_.warn("Line length 0 for doc id " + docId);
        }
        // Simple token.
        if (data.length >= 1) {
          word = data[0];
        }
        // Mention.
        if (data.length >= 3) {
          mentionStart = "B".equals(data[1]);
          textMention = data[2];
        }
        // Groundtruth label
        if (data.length >= 4) {
          entity = data[3];
        }
        // Mention with Stanford ner label.
        if (data.length >= 5) {
          ner = data[4];
        }
        if (data.length >= 6) {
          mentionOccurrenceCount = Integer.parseInt(data[5]);
        }
        if ((data.length == 2) || data.length >= 7) {
          logger_.warn("Line has wrong format: '" + line + "' for docId " + docId);
        }

        if (punctuations_.contains(word) && tokens.size() > 0) {
          Token at = tokens.getToken(tokens.size() - 1);
          at.setOriginalEnd("");
          index = index - 1;
        }
        int endIndex = index + word.length();
        Token at = new Token(position, word, " ", index, endIndex, sentence, 0, null, ner);
        tokens.addToken(at);
        if (textMention != null && mentionStart && (mentionOccurrenceCount >= mentionMinOccurrences)) {
          Mention mention = new Mention();
          mention.setCharOffset(index);
          mention.setCharLength(textMention.length());
          mention.setMention(textMention);
          mention.addGroundTruthResult(entity);
          mention.setOccurrenceCount(mentionOccurrenceCount);
          mentions.addMention(mention);
        }
        index = endIndex + 1;
      }
    }
    if (!includeOutOfDictionaryMentions) {
      Map<String, Entities> candidates =
          DataAccess.getEntitiesForMentions(mentions.getMentionNames(), 1.0, 0, true);
      Mentions mentionsToInclude = new Mentions();
      for (Map<Integer, Mention> innerMap : mentions.getMentions().values()) {
        for (Mention m : innerMap.values()) {
          Entities cands = candidates.get(m.getMention());
          if (!cands.isEmpty()) {
            mentionsToInclude.addMention(m);
          }
        }
      }
      mentions = mentionsToInclude;
    }
    if (tokens != null) {
      setTokensPositions(mentions, tokens);
    }
    PreparedInputChunk prepInput = new PreparedInputChunk(docId, tokens, new Mentions(), mentions);
    return new Pair<PreparedInputChunk, Long>(prepInput, timestamp);
  }

  /**
   * Assumes a PreparedInput with only a single chunk. Multi-Chunk documents
   * should never be stored.
   *
   * Mentions will be aligned to the tokens present in the document according
   * to their character offset and length.
   *
   * @param writer
   * @throws IOException
   */
  public void writeTo(BufferedWriter writer) throws IOException {
    writer.write("-DOCSTART- (");
    writer.write(docId_.replace('/', '_'));
    writer.write(")");
    if (timestamp_ != 0) {
      DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC();
      String timeString = fmt.print(timestamp_);
      writer.write("\t" + timeString);
    }
    writer.newLine();
    int currentToken = 0;
    if (chunks_.size() > 1 || chunks_.size() == 0) {
      throw new IllegalStateException("AIDA disk formats do not support " + "chunked documents. This document contains " + chunks_.size() + "cunks.");
    }
    PreparedInputChunk chunk = chunks_.get(0);
    // Align mentions to underlying tokens.
    setTokensPositions(chunk.getConceptMentions(), chunk.getTokens());
    setTokensPositions(chunk.getNamedEntityMentions(), chunk.getTokens());
    for (Map<Integer, Mention> innerMap : getMentions().getMentions().values()) {
      for (Mention mention : innerMap.values()) {
        // Write up to mention.
        writeTokens(chunk.getTokens(), currentToken, mention.getStartToken(), writer);
        currentToken = mention.getEndToken() + 1;
        // Add mention.
        writeTokensMention(chunk.getTokens(), mention, writer);
      }
    }
    writeTokens(chunk.getTokens(), currentToken, chunk.getTokens().size(), writer);
  }

  public void writeTo(File file) throws IOException {
    BufferedWriter writer = FileUtils.getBufferedUTF8Writer(file);
    writeTo(writer);
    writer.flush();
    writer.close();
  }

  private void writeTokens(Tokens tokens, int from, int to, BufferedWriter writer) throws IOException {
    for (int i = from; i < to; i++) {
      if (i > 0 && tokens.getToken(i - 1).getSentence() != tokens.getToken(i).getSentence()) {
        writer.newLine();
      }
      writer.write(tokens.getToken(i).getOriginal());
      writer.newLine();
    }
  }

  private void writeTokensMention(Tokens tokens, Mention mention, BufferedWriter writer) throws IOException {
    String start = "B";
    for (int i = mention.getStartToken(); i <= mention.getEndToken(); i++) {
      if (i > 0 && tokens.getToken(i - 1).getSentence() != tokens.getToken(i).getSentence()) {
        writer.newLine();
      }
      if (mention.getGroundTruthResult() == null) {
        mention.addGroundTruthResult("--UNKNOWN--");
      }
      String NE = (tokens.getToken(i).getNE() != null) ? tokens.getToken(i).getNE() : "NULL";
      String line = tokens.getToken(i).getOriginal() + "\t" + start + "\t" + mention.getMention() + "\t" + mention.getGroundTruthResultString() + "\t" + NE;
      if (mention.getOccurrenceCount() > 0) {
        line += "\t" + mention.getOccurrenceCount();
      }
      writer.write(line);
      writer.newLine();
      start = "I";
    }
  }

  //TODO fix this later.... Now it only works if for every offset there is only one mention at most.
  // Should be re-written in an easier way now that we have a map of offset, length to mentions
  public static void setTokensPositions(Mentions mentions, Tokens tokens) {
    int startToken = -1;
    int endToken = -1;
    int t = 0;
    int i = 0;
    Mention mention = null;
    Token token = null;
    List<Integer> offsets = Arrays.asList(mentions.getMentions().keySet().toArray(new Integer[0]));


    while (t < tokens.size() && i < offsets.size()) {
      for(int length : mentions.getMentions().get(offsets.get(i)).keySet()) {
        mention = mentions.getMentions().get(offsets.get(i)).get(length);
        token = tokens.getToken(t);
        if (startToken >= 0) {
          if (token.getEndIndex() > mention.getCharOffset() + mention.getCharLength()) {
            mention.setStartToken(startToken);
            mention.setId(startToken);
            mention.setEndToken(endToken);
            if (mention.getMention() == null) {
              mention.setMention(tokens.toText(startToken, endToken));
            }
            startToken = -1;
            endToken = -1;
            i++;
          } else {
            endToken = token.getId();
            t++;
          }
        } else {
          if (token.getBeginIndex() >= mention.getCharOffset() && mention.getCharOffset() <= token.getEndIndex()) {
            startToken = token.getId();
            endToken = token.getId();
          } else {
            t++;
          }
        }
      }
    }
    
    if (startToken >= 0) {
      if (token.getEndIndex() >= mention.getCharOffset() + mention.getCharLength()) {
        mention.setStartToken(startToken);
        mention.setId(startToken);
        mention.setEndToken(endToken);
      }
    }
  }

  private Set<String> getPuncuations() {
    HashSet<String> punctuations = null;
    punctuations = new HashSet<String>();
    punctuations.add(".");
    punctuations.add(":");
    punctuations.add(",");
    punctuations.add(";");
    punctuations.add("!");
    punctuations.add("?");
    punctuations.add("'s");
    return punctuations;
  }

  public long getTimestamp() {
    return timestamp_;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp_ = timestamp;
  }

}
