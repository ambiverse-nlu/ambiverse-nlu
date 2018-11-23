package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.trace.visualization;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.ClassPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;

public abstract class MainTemplateBuilder {

  public static final String MAIN_PATH= "debug/views/main.html.template";

  private Logger logger = LoggerFactory.getLogger(MainTemplateBuilder.class);

  private String title;
  private String body;
  private String footer;

  private String template;

  private String outputPath;

  public MainTemplateBuilder(String outputPath) throws IOException, URISyntaxException {
    this.outputPath = outputPath;
    this.template = readTemplate();
  }

  private String readTemplate() throws IOException, URISyntaxException {
    File file = ClassPathUtils.getFileFromClassPath(MAIN_PATH);
    String template = new String(Files.readAllBytes(file.toPath()), Charset.forName("UTF-8"));
    return template;
  }

  private void setTitle() {
    if(title != null) {
      logger.info("Building title");
      template = template.replaceAll("##TITLE##", title);
    }
  }

  private void setPath() {
    if(outputPath != null) {
      template = template.replaceAll("##PATH##", outputPath);
    }
  }

  private void setBody() {
    if(body != null) {
      logger.info("Building body.");
      template = template.replace("##BODY##", body);
    }
  }

  private void setFooter() {
    if(footer != null) {
      logger.info("Building footer.");
      template = template.replaceFirst("##FOOTER##", footer);
    }
  }

  public MainTemplateBuilder withTitle(String title) {
    this.title = title;
    return this;
  }

  public MainTemplateBuilder withBody(String body) {
    this.body = body;
    return this;
  }

  public MainTemplateBuilder withFooter(String footer) {
    this.footer = footer;
    return this;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public void setFooter(String footer) {
    this.footer = footer;
  }

  public String getTemplate() {
    return template;
  }

  protected abstract void buildBody() throws Exception;

  public void build() throws Exception {

    buildBody();

    setPath();
    setTitle();
    setBody();
    setFooter();
  }
}
