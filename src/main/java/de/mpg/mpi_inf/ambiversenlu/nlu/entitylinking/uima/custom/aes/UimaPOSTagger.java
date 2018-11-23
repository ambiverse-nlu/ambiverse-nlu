package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.custom.aes;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.EntityLinkingManager;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.dictionary.DictionaryEntriesDataProvider;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.datapreparation.yago.yago3.Yago3DictionaryEntriesSources;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.graph.similarity.exception.MissingSettingException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Token;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model.Tokens;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.DocumentProcessor;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.processor.UnprocessableDocumentException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.pipelines.PipelineType;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.CollectionUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.language.Language;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.model.ProcessedDocument;
import org.apache.uima.UIMAException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class UimaPOSTagger {

  public static Tokens tag(Language language, String text)
      throws UIMAException, IOException, EntityLinkingDataAccessException, NoSuchMethodException,
      ClassNotFoundException, MissingSettingException, UnprocessableDocumentException {
    return tag(language, text, PipelineType.POS_TAGGING);
  }

  public static Tokens tag(Language language, String text, PipelineType type)
      throws UIMAException, IOException, EntityLinkingDataAccessException, NoSuchMethodException,
      ClassNotFoundException, MissingSettingException, UnprocessableDocumentException {
    DocumentProcessor dp = DocumentProcessor.getInstance(type);
    Document doc = new Document.Builder().withText(text).withLanguage(language).build();
    ProcessedDocument output = dp.process(doc);
    return output.getTokens();
  }

  public static void main(String[] args)
      throws MissingSettingException, ClassNotFoundException, IOException, UnprocessableDocumentException,
      EntityLinkingDataAccessException, NoSuchMethodException, UIMAException {
    if (args.length != 3) {
      System.out.println("Usage: de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.uima.customComponents.aes.UimaPOSTagger <tsv source> <language - 3 code> <destination>");
      return;
    }
    EntityLinkingManager.init();

    new UimaPOSTagger().run(args[1], args[0], args[2]);
  }

  public void run(String language, String source, String destination) throws UIMAException, IOException, EntityLinkingDataAccessException, NoSuchMethodException, ClassNotFoundException, MissingSettingException, UnprocessableDocumentException {
    List<String> germanMentions = new ArrayList<>();
    File file = new File(source);
    Scanner in = new Scanner(file);

    while (in.hasNextLine()) {
      String line = in.nextLine();
      if (!line.contains("rdfs:label") || !line.contains("@" + Language.get3letterLanguage(language))) {
        continue;
      }
      String[] values = line.split("\t");
      if (values.length < 4) {
        continue;
      }
      germanMentions.add(values[3].substring(values[3].indexOf('"') + 1, values[3].lastIndexOf('"')));
    }
    Tokens test;
    Map<String, Integer> counts = new LinkedHashMap<>();
    int totalMentions = germanMentions.size();
    int processed = 0;
    for (String mention : germanMentions) {
      processed++;
      StringJoiner sg = new StringJoiner(" ");
      String text = mention + " es una entidad nombrada en nuestra base de datos.";
      test = tag(Language.getLanguageForString(language), text);
      int lenght = mention.length();
      for (Token token : test.getTokens()) {
        if (token.getBeginIndex() <= lenght) {
          sg.add(token.getPOS());
        }
      }
      counts.merge(sg.toString(), 1, (oldValue, one) -> oldValue + one);
      System.out.println("Processed " + processed + " mentions of " + totalMentions);
    }
    counts = CollectionUtils.sortMapByValue(counts, true);
    File fileOut = new File(destination);
    PrintWriter write = new PrintWriter(fileOut);
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      write.println(entry.getKey().replaceAll(" ", "*"));
    }
    write.close();
  }

  public void run(String language, DictionaryEntriesDataProvider dictionaryEntriesDataProvider, String destination) throws UIMAException, IOException, EntityLinkingDataAccessException, NoSuchMethodException, ClassNotFoundException, MissingSettingException, UnprocessableDocumentException {
    List<String> germanMentions = new ArrayList<>();

    for (Map.Entry<String, List<DictionaryEntity>> e : dictionaryEntriesDataProvider) {
      String mention = e.getKey();
      if (mention == null || "".equals(mention.trim())) {
        continue;
      }
      for (DictionaryEntity de : e.getValue()) {
        if (de.language.name().equals(language) && Yago3DictionaryEntriesSources.LABEL.equals(de.source)) {
//          String subject = de.entity;
          germanMentions.add(mention);
        }
      }
    }
    Tokens test;
    Map<String, Integer> counts = new LinkedHashMap<>();
    int totalMentions = germanMentions.size();
    int processed = 0;
    for (String mention : germanMentions) {
      processed++;
      StringJoiner sg = new StringJoiner(" ");
      String text = mention + " es una entidad nombrada en nuestra base de datos.";
//      test = tag(Language.getLanguageForString(language), text);
      test = tag(Language.getLanguageForString(language), text);
      int lenght = mention.length();
      for (Token token : test.getTokens()) {
        if (token.getBeginIndex() <= lenght) {
          sg.add(token.getPOS());
        }
      }
      counts.merge(sg.toString(), 1, (oldValue, one) -> oldValue + one);
      System.out.println("Processed " + processed + " mentions of " + totalMentions);
    }
    counts = CollectionUtils.sortMapByValue(counts, true);
    File fileOut = new File(destination);
    PrintWriter write = new PrintWriter(fileOut);
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      write.println(entry.getKey().replaceAll(" ", "*"));
    }
    write.close();
  }

}
