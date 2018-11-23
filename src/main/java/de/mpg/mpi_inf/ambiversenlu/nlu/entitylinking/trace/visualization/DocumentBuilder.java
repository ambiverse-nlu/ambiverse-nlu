package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.visualization;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeOutput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Entity;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Match;
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
import java.util.*;

public class DocumentBuilder extends MainTemplateBuilder{

  private static final String FILENAME = "index.html";
  private static final String DOCUMENT_TEMPLATE = "debug/views/document.html.template";
  private static final String TABLE_PARTIAL = "debug/views/partials/table.html.partial";

  private Logger logger = LoggerFactory.getLogger(DocumentBuilder.class);

  private String outputhPath;
  private Document document;
  private String traceOutput;

  public DocumentBuilder(String outputhPath, Document document, String traceOutput) throws IOException, URISyntaxException {
    super("../");
    this.outputhPath = outputhPath;
    this.document = document;
    this.traceOutput = traceOutput;
  }

  private String buildSummaryTHead() {
    StringBuilder thead = new StringBuilder();
    thead.append("\t<th>&nbsp;</th>\n");
    thead.append("\t<th>Conf ID</th>\n");
    thead.append("\t<th>Conf Name</th>\n");
    thead.append("\t<th>Avg Precision</th>\n");
    thead.append("\t<th>Avg Recall</th>\n");
    thead.append("\t<th>Mean Avg Precision</th>\n");
    thead.append("\t<th>Mean Avg Recall</th>\n");
    thead.append("\t<th>NER Precision</th>\n");
    thead.append("\t<th>NER Recall</th>\n");
    thead.append("\t<th>NER F1</th>\n");
    thead.append("\t<th>NED Precision</th>\n");
    thead.append("\t<th>NED Recall</th>\n");
    thead.append("\t<th>NED F1</th>\n");
    thead.append("\t<th>MAP</th>\n");

    return thead.toString();
  }

  private String buildSummaryTBody() {
    StringBuilder tbody = new StringBuilder();
    String graphTrace = null;

    int i=1;
    for(Configuration configuration : document.getConfOutput().keySet()) {
      if(traceOutput != null) {
        graphTrace = Paths.get(traceOutput).toAbsolutePath().toString() + "/"+ configuration.getName()+ "/html/graph/" + document.getName() + "_graphtrace.html";
      }
      tbody.append("\t<tr>\n");
      tbody.append("\t\t<td><input type='checkbox' checked='checked' value='"+i+"' class='show_conf'/></td>\n");
      tbody.append("\t\t<td>"+(i++)+"</td>\n");
      tbody.append("\t\t<td>");
      if(traceOutput != null) {
        tbody.append("<a href='"+graphTrace+"' target='_blank'>");
      }
      tbody.append(configuration.getName());
      if(traceOutput != null) {
        tbody.append("</a>");
      }
      tbody.append("</td>\n");
      if(configuration.getGold()) {
        tbody.append("\t\t<td>&nbsp</td>");
        tbody.append("\t\t<td>&nbsp</td>");
        tbody.append("\t\t<td>&nbsp</td>");
        tbody.append("\t\t<td>&nbsp</td>");
        tbody.append("\t\t<td>&nbsp</td>");
        tbody.append("\t\t<td>&nbsp</td>");
        tbody.append("\t\t<td>&nbsp</td>");
        tbody.append("\t\t<td>&nbsp</td>");
        tbody.append("\t\t<td>&nbsp</td>");
        tbody.append("\t\t<td>&nbsp</td>");
        tbody.append("\t\t<td>&nbsp</td>");
      } else {
        tbody.append("\t\t<td class='spot'>");
        tbody.append(Precision.round(configuration.getAvgPrecision(), 5));
        tbody.append("</td>\n");
        tbody.append("\t\t<td class='spot'>");
        tbody.append(Precision.round(configuration.getAvgRecall(), 5));
        tbody.append("</td>\n");
        tbody.append("\t\t<td class='spot'>");
        tbody.append(Precision.round(configuration.getMeanAvgPrecision(), 5));
        tbody.append("</td>\n");
        tbody.append("\t\t<td class='spot'>");
        tbody.append(Precision.round(configuration.getMeanAvgRecall(), 5));
        tbody.append("</td>\n");
        tbody.append("\t\t<td class='spot'>");
        tbody.append(Precision.round(configuration.getNerPrecision(), 5));
        tbody.append("</td>\n");
        tbody.append("\t\t<td class='spot'>");
        tbody.append(Precision.round(configuration.getNerRecall(), 5));
        tbody.append("</td>\n");
        tbody.append("\t\t<td class='spot'>");
        tbody.append(Precision.round(configuration.getNerF1(), 5));
        tbody.append("</td>\n");
        tbody.append("\t\t<td class='spot'>");
        tbody.append(Precision.round(configuration.getNedPrecision(), 5));
        tbody.append("</td>\n");
        tbody.append("\t\t<td class='spot'>");
        tbody.append(Precision.round(configuration.getNedRecall(), 5));
        tbody.append("</td>\n");
        tbody.append("\t\t<td class='spot'>");
        tbody.append(Precision.round(configuration.getNedF1(), 5));
        tbody.append("</td>\n");
        tbody.append("\t\t<td class='spot'>");
        tbody.append(Precision.round(configuration.getMap(), 5));
        tbody.append("</td>\n");
      }
      tbody.append("\t</tr>\n");

    }

    return tbody.toString();
  }


  private String buildContent() {
    logger.info("{} - Building document content.", document.getName());
    StringBuilder contentBuilder = new StringBuilder();
    List<List<String>> lines = new LinkedList<>();

    boolean hasGold = false;
    Map<Integer, Match> goldMatches = new HashMap<>();
    Map<String, Entity> entities = new HashMap<>();
    for(Map.Entry<Configuration, AnalyzeOutput> entry : document.getConfOutput().entrySet()) {
      if(entry.getKey().getGold()) {
        hasGold = true;
        for(Match m : entry.getValue().getMatches()) {
          goldMatches.put(m.getCharOffset(), m);
        }
      }
      for(Entity entity : entry.getValue().getEntities()) {
        entities.put(entity.getId(), entity);
      }
    }


    for(Map.Entry<Configuration, AnalyzeOutput> entry : document.getConfOutput().entrySet()) {
      if(entry.getValue().getText() != null) {
        String[] splits = entry.getValue().getText().split("\n");
        List<String> annotatedLines = new ArrayList<>();

        TreeMap<Integer, Match> matches = new TreeMap<>();
        for(Match m : entry.getValue().getMatches()) {
          matches.put(m.getCharOffset(), m);
        }

        int offset = 0;
        for (String line : splits) {
          if(line.equals("")) {
            annotatedLines.add("<br />");
          }else {
            annotatedLines.add(annotateLine(line,
                matches,
                offset,
                goldMatches,
                entry.getKey().getGold(),
                hasGold,
                entry.getKey().getName(),
                document.getName(),
                entities));
          }
          offset += line.length() + 1;
        }
        lines.add(annotatedLines);
      }
    }

    if(!lines.isEmpty()) {
      for (int i = 0; i < lines.get(0).size(); i++) {
        contentBuilder.append("<ol class='linenums'>");
        for (int j = 0; j < lines.size(); j++) {
          contentBuilder.append("<li class='L" + (j + 1) + "'><span style='color: #000'>");
          contentBuilder.append(lines.get(j).get(i));
          contentBuilder.append("</span></li>");
        }
        contentBuilder.append("</ol>");
      }
    }


    return contentBuilder.toString();
  }

  private String annotateLine(String line, TreeMap<Integer, Match> matches,
      int offset, Map<Integer, Match> goldMatches, boolean isGold,
      boolean hasGold, String confPath, String documentName, Map<String, Entity> allEntities) {
    StringBuilder lineBuilder = new StringBuilder();
    int prevOffset = 0;

    Map<Integer, Match> matchesForLine = matches.subMap(offset, offset + line.length());
    for(Match match : matchesForLine.values()) {
      int charOffset = match.getCharOffset() - offset;
      if(match.getCharOffset() >= offset && match.getCharOffset() <= offset + line.length()) {
        if(prevOffset<charOffset) {
          lineBuilder.append(line.substring(prevOffset, charOffset));
        }
        if(match.getEntity() != null && match.getEntity().getId() != null) {
          lineBuilder.append("<a target='_blank' class='mention");
          if(hasGold) {
            if (isGold) {
              lineBuilder.append(" gold");
            } else {
              if (goldMatches.get(match.getCharOffset()) != null && goldMatches.get(match.getCharOffset()).getEntity().getId().equals(match.getEntity().getId())) {
                lineBuilder.append(" green");
              } else {
                lineBuilder.append(" red");
              }
            }
          } else {
            lineBuilder.append(" blue");
          }
          lineBuilder.append("'");
          lineBuilder.append(" href='" + match.getEntity().getId()+"'");

          lineBuilder.append(">");
          lineBuilder.append("<i style='width:15px;' class='fa");
          String type = "";
          if(match.getType() != null) {
            type = match.getType();
          } else if(match.getEntity()!= null && match.getEntity().getId() != null){
            type = allEntities.get(match.getEntity().getId()).getType().toString();
          }
          switch(type) {
            case "PER" :
            case "PERSON":
              lineBuilder.append(" fa-user");
              break;
            case "ORGANIZAITON" :
            case "ORG" : lineBuilder.append(" fa-building");
              break;
            case "LOCATION":
            case "LOC" : lineBuilder.append(" fa-map-marker");
              break;
            default: lineBuilder.append(" fa-question-circle");
              break;
          }
          lineBuilder.append("'");
          if(match.getEntity() != null && allEntities.get(match.getEntity().getId()) != null) {
            Entity entity = allEntities.get(match.getEntity().getId());
            lineBuilder.append(" data-toggle='popover' data-trigger='hover' data-html='true' data-placement='top'");
            lineBuilder.append(" title='" + entity.getName() + "'");
            lineBuilder.append(" data-content='");
            lineBuilder.append("<div>ID: "+entity.getId().substring("http://www.wikidata.org/entity/".length())+"</div>");
            lineBuilder.append("<div>Type: "+entity.getType()+"</div>");
            lineBuilder.append("<div>Salience: "+Precision.round(entity.getSalience(), 5)+"</div>");
            lineBuilder.append("<div><a href=\""+entity.getUrl()+"\">Link</a></div>");
            lineBuilder.append("'");
          }
          lineBuilder.append("></i></a>");
        } else {
          lineBuilder.append("<span style='padding-left:15px;' class='space'></span>");
        }
        lineBuilder.append("<a target='_blank' class='mention word");
        if(hasGold) {
          if (isGold) {
            lineBuilder.append(" gold selected");
          } else {
            if (goldMatches.get(match.getCharOffset()) != null && goldMatches.get(match.getCharOffset()).getEntity().getId().equals(match.getEntity().getId())) {
              lineBuilder.append(" green selected");
            } else {
              lineBuilder.append(" red selected");
            }
          }
        } else {
          lineBuilder.append(" blue");
        }
        if(match.getEntity() != null && match.getEntity().getId() != null) {
          lineBuilder.append(" underline");
        } else {
          lineBuilder.append(" gray");
        }
        lineBuilder.append("'");
        if (traceOutput != null) {
          String tOut = Paths.get(traceOutput).toAbsolutePath().toString() + "/" + confPath + "/html/all/similarity/";
          lineBuilder.append(" href='" + tOut + documentName + "/" + match.getCharOffset() + ".html'");
        } else {
          if (match.getEntity() != null && match.getEntity().getId() != null) {
            lineBuilder.append(" href='" + match.getEntity().getId() + "'");
          }
        }
        if (match.getEntity() != null && match.getEntity().getId() != null) {
          lineBuilder.append(" data-url='" + match.getEntity().getId() + "'");
        }
        if(match.getEntity() != null && match.getEntity().getConfidence() != null) {
          lineBuilder.append(" data-toggle='tooltip' title='Confidence: " + Precision.round(match.getEntity().getConfidence(), 5) + "'");
        }
        lineBuilder.append(">");
        lineBuilder.append(match.getText());
        lineBuilder.append("</a>");
      }
      prevOffset = (match.getCharOffset() + match.getCharLength()) - offset;
    }
    if (prevOffset <= line.length()) {
      int endIndex = line.length();
      lineBuilder.append(line.substring(prevOffset, endIndex));
    }
    return lineBuilder.toString();
  }



  @Override protected void buildBody() throws Exception {
    String summaryTHead = buildSummaryTHead();
    String summaryTBody = buildSummaryTBody();

    String tableTemplate = new String(Files.readAllBytes(ClassPathUtils.getFileFromClassPath(TABLE_PARTIAL).toPath()), Charset.forName("UTF-8"));
    tableTemplate = tableTemplate.replace("##ID##", "document-"+document.getName());
    tableTemplate = tableTemplate.replace("##THEAD##", summaryTHead);
    tableTemplate = tableTemplate.replace("##TBODY##", summaryTBody);


    String documentTemplate = new String(Files.readAllBytes(ClassPathUtils.getFileFromClassPath(DOCUMENT_TEMPLATE).toPath()), Charset.forName("UTF-8"));
    documentTemplate = documentTemplate.replaceFirst("##DOCUMENT_ID##", document.getName());
    documentTemplate = documentTemplate.replaceFirst("##DOCUMENT_CONF_TABLE##", tableTemplate);

    String body = buildContent();
    documentTemplate = documentTemplate.replace("##ANNOTATED_CONTENT##", body);
    this.setBody(documentTemplate);

  }

  public void saveFile() throws IOException {
    if(!Files.exists(Paths.get(outputhPath + document.getName()))) {
      Files.createDirectory(Paths.get(outputhPath + document.getName()));
    }
    Files.write(Paths.get(outputhPath + document.getName() + "/"+ FILENAME), this.getTemplate().getBytes());
  }
}
