package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.visualization;

import com.beust.jcommander.Parameter;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeOutput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.visualization.model.Configuration;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.visualization.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Runner extends ParameterizedExecutable{

  @Parameter(names = { "--input", "-i" }, description = "The input folder containing the data.")
  private static String input="disambiguationOutput/runs/";

  @Parameter(names = { "--output", "-o" }, description = "The output folder where the resulting htmls are written.")
  private static String output="disambiguationOutput/html/";

  @Parameter(names = { "--trace", "-t" }, description = "The output folder where the tracing results are written.")
  private static String trace= null; //"disambiguationOutput/tracing/";

  @Parameter(names = { "--title", "-ti" }, description = "The title of the generated output html page.")
  private static String title="Entity Linking Tracing";

  private Logger logger = LoggerFactory.getLogger(Runner.class);

  private static final String PUBLIC_ZIP = "debug/views/public.zip";

  public Runner(String[] args) throws Exception {
    super(args);
  }

  public static void main(String args[]) throws Exception {
    new Runner(args).run();
  }

  private Map<String, Document> readInputDirectory() throws IOException {

    logger.info("Reading documents from '{}'.", input);
    ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    Map<String, Document> documents = new HashMap<>();
    if(Files.isDirectory(Paths.get(input))) {
      Files.walk(Paths.get(input)).forEach(path -> {
        File file = path.toFile();
        if(file.isFile() && file.getName().endsWith(".json")) {
          String name = file.getName().substring(0, file.getName().indexOf(".json"));
          if(!documents.containsKey(name)) {
            Document d = new Document();
            d.setName(name);
            d.setPath(name+"/");
            documents.putIfAbsent(name, d);
          }
          String confName = file.getParentFile().getName();
          Configuration conf = new Configuration();
          conf.setName(confName);
          if(confName.contains("gold")) {
            conf.setGold(true);
          }

          String fileContent = null;
          try {
            fileContent = new String(Files.readAllBytes(file.toPath()));
          } catch (IOException e) {
            throw new RuntimeException();
          }
          AnalyzeOutput analyzeOutput = null;
          try {
            analyzeOutput = objectMapper.readValue(fileContent, AnalyzeOutput.class);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          Document d = documents.get(name);
          d.getConfOutput().put(conf, analyzeOutput);
        }
      });
    }

    //Sort the configurations by Gold
    for(Document d : documents.values()) {
      Map<Configuration, AnalyzeOutput> sorted = d.getConfOutput().entrySet().stream().sorted(Map.Entry.comparingByKey())
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

      d.setConfOutput(sorted);

    }
    return documents;
  }

  private void copyPublicData() throws IOException, URISyntaxException {

    if(!Files.exists(Paths.get(Paths.get(output)+"/public/"))) {
      logger.info("Public folder does not exist. Create it and copy over the public files.");
      ZipFile file = new ZipFile(ClassPathUtils.getFileFromClassPath(PUBLIC_ZIP));
      System.out.println(file);

      FileSystem fileSystem = FileSystems.getDefault();

      //Get file entries
      Enumeration<? extends ZipEntry> entries = file.entries();

      //Iterate over entries
      while (entries.hasMoreElements())
      {
        ZipEntry entry = entries.nextElement();
        //If directory then create a new directory in uncompressed folder
        if (entry.isDirectory())
        {
          logger.info("Creating Directory:" + output + entry.getName());
          Files.createDirectories(fileSystem.getPath(output+ entry.getName()));
        }
        //Else create the file
        else
        {
          InputStream is = file.getInputStream(entry);
          BufferedInputStream bis = new BufferedInputStream(is);
          String uncompressedFileName = output + entry.getName();
          Path uncompressedFilePath = fileSystem.getPath(uncompressedFileName);
          Files.createFile(uncompressedFilePath);
          FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName);
          while (bis.available() > 0)
          {
            fileOutput.write(bis.read());
          }
          fileOutput.close();
          logger.info("Unzipping :" + entry.getName());
        }
      }
    }
  }

  @Override protected void run() throws Exception {

    copyPublicData();


    Map<String, Document> documents = readInputDirectory();

    logger.info("Storing the results in '{}'.", output);
    logger.info("Tracing expected in location '{}'.", trace);
    IndexBuilder ig = new IndexBuilder(output,  documents);
    ig.withTitle(title);
    ig.build();
    ig.saveFile();

    for(Document document : documents.values()) {
      DocumentBuilder db = new DocumentBuilder(output, document, trace);
      db.withTitle(document.getName());
      db.build();
      db.saveFile();
    }
  }
}
