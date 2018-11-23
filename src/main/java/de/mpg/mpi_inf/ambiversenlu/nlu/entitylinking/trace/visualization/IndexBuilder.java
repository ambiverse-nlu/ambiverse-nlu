package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.visualization;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.visualization.model.Configuration;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.visualization.model.Document;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class IndexBuilder extends MainTemplateBuilder {

  private Logger logger = LoggerFactory.getLogger(IndexBuilder.class);

  private static final String FILENAME = "index.html";
  private static final String INDEX_TEMPLATE = "debug/views/index.html.template";
  private static final String TABLE_PARTIAL = "debug/views/partials/table.html.partial";

  private String outputPath;
  private Map<String, Document> documents;


  public IndexBuilder(String outputPath, Map<String, Document> documents) throws IOException, URISyntaxException {
    super("");
    this.outputPath = outputPath;
    this.documents = documents;
  }

  private String buildTableHead() {
    StringBuilder thead = new StringBuilder();
    thead.append("<th>Document ID</th>");
    int confCount = 0;
    for(Document d: documents.values()) {
      confCount = d.getConfOutput().size() > confCount ? d.getConfOutput().size() : confCount;
    }

    for(int i=1; i<confCount+1; i++) {
      thead.append("<th>Conf ");
      thead.append(i);
      thead.append("</th>");
    }
    return thead.toString();
  }

  private String buildTBody() {
    StringBuilder tbody = new StringBuilder();

    int confCount = 0;
    for(Document d: documents.values()) {
      confCount = d.getConfOutput().size() > confCount ? d.getConfOutput().size() : confCount;
    }

    for(Document d : documents.values()) {
      tbody.append("\t<tr>\n");
      tbody.append("\t\t<td>");
      tbody.append("<a href='");
      tbody.append(d.getPath());
      tbody.append("'>");
      tbody.append(d.getName());
      tbody.append("</a>");
      tbody.append("</td>\n");


      for(Configuration configuration : d.getConfOutput().keySet() ) {
        Double value = Precision.round(configuration.getAvgPrecision(), 5);
        tbody.append("\t\t<td style='width: 100px;' class='spot'>");
        tbody.append(value);
        tbody.append("</td>\n");
      }

      tbody.append("\t</tr>\n");
    }

    return tbody.toString();
  }

  @Override protected void buildBody() throws Exception {
    String thead = buildTableHead();
    String tbody = buildTBody();


    String tableTemplate = new String(Files.readAllBytes(ClassPathUtils.getFileFromClassPath(TABLE_PARTIAL).toPath()), Charset.forName("UTF-8"));
    tableTemplate = tableTemplate.replace("##ID##", "documents");
    tableTemplate = tableTemplate.replace("##THEAD##", thead);
    tableTemplate = tableTemplate.replace("##TBODY##", tbody);


    String indexTemplate = new String(Files.readAllBytes(ClassPathUtils.getFileFromClassPath(INDEX_TEMPLATE).toPath()), Charset.forName("UTF-8"));
    indexTemplate = indexTemplate.replaceFirst("##TABLE##", tableTemplate);

    this.setBody(indexTemplate);
  }


  public void saveFile() throws IOException {
    if(!Files.exists(Paths.get(outputPath))) {
      Files.createDirectory(Paths.get(outputPath));
    }
    Files.write(Paths.get(outputPath + FILENAME), this.getTemplate().getBytes());
  }

}
