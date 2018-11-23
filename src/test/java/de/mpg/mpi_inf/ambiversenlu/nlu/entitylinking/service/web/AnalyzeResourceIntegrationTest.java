package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.*;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.resource.impl.AnalyzeResourceImpl;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AnalyzeResourceIntegrationTest extends JerseyTest {

  private static String PATH = "entitylinking/analyze";

  private static final String ID_PREFIX = "http://www.wikidata.org/entity/";

  protected Application configure() {
    ConfigUtils.setConfOverride("integration_test");
    ResourceConfig config = new ResourceConfig(AnalyzeResourceImpl.class);

    // Find first available port.
    forceSet(TestProperties.CONTAINER_PORT, "0");

    return config;
  }

  @Test public void testAnalyze() throws Exception {
    AnalyzeInput request = new AnalyzeInput();
    request.setText("Ma founded Alibaba in Hangzhou with investments from SoftBank and Goldman.");
    //    List<AnnotatedMention> annotatedMentions = new ArrayList();
    //    AnnotatedMention annotatedMention = new AnnotatedMention();
    //    annotatedMention.withCharLength(3).withCharOffset(5);
    //    annotatedMentions.add(annotatedMention);
    //    request.setAnnotatedMentions(annotatedMentions);

    // Fire GET request and get the response
    Response response = target(PATH).request(MediaType.APPLICATION_JSON_TYPE)
        .buildPost(javax.ws.rs.client.Entity.entity(request, MediaType.APPLICATION_JSON_TYPE)).invoke();

    //Check the status of the response
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    AnalyzeOutput elo = response.readEntity(AnalyzeOutput.class);
    assertEquals(5, elo.getMatches().size());
    List<Match> resultMentions = elo.getMatches();

    assertEquals(new Entity().withId(ID_PREFIX + "Q1137062").getId(), resultMentions.get(0).getEntity().getId());
    assertEquals(new Entity().withId(ID_PREFIX + "Q1359568").getId(), resultMentions.get(1).getEntity().getId());
    assertEquals(new Entity().withId(ID_PREFIX + "Q4970").getId(), resultMentions.get(2).getEntity().getId());
    assertEquals(new Entity().withId(ID_PREFIX + "Q201653").getId(), resultMentions.get(3).getEntity().getId());
    assertEquals(new Entity().withId(ID_PREFIX + "Q193326").getId(), resultMentions.get(4).getEntity().getId());

    response.close();
  }

  @Test public void testAnalyzeConfidence() throws Exception {
    AnalyzeInput request = new AnalyzeInput();
    request.setText("When Who played Tommy in Columbus, Pete was at his best.");
    request.setConfidenceThreshold(0.1);
    List<AnnotatedMention> annotatedMentions = new ArrayList();
    AnnotatedMention annotatedMention = new AnnotatedMention().withCharLength(3).withCharOffset(5);

    annotatedMentions.add(annotatedMention);
    request.setAnnotatedMentions(annotatedMentions);

    // Fire GET request and get the response
    Response response = target(PATH).request(MediaType.APPLICATION_JSON_TYPE)
        .buildPost(javax.ws.rs.client.Entity.entity(request, MediaType.APPLICATION_JSON_TYPE)).invoke();

    //Check the status of the response
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    AnalyzeOutput elo = response.readEntity(AnalyzeOutput.class);
    assertEquals(4, elo.getMatches().size());
    List<Match> resultMentions = elo.getMatches();

    assertEquals(new Entity().withId(ID_PREFIX + "Q93346").getId(), resultMentions.get(0).getEntity().getId());
//    assertEquals(new Entity().getId(), resultMentions.get(1).getEntity().getId());
    assertEquals(new Entity().withId(ID_PREFIX + "Q16567").getId(), resultMentions.get(2).getEntity().getId());
    assertEquals(new Entity().withId(ID_PREFIX + "Q26933").getId(), resultMentions.get(3).getEntity().getId());

    response.close();
  }

  /**
   * Does not actually test NoCoherence, because the example does not show different results when using coherence and when not.
   * TODO: Find better example
   * @throws Exception
   */
  @Test public void testAnalyzeNoCoherence() throws Exception {
    AnalyzeInput request = new AnalyzeInput();
    request.setText("When Who played Tommy in Columbus, Pete was at his best.");
    request.setCoherentDocument(false);
    request.setConfidenceThreshold(0.0);
    List<AnnotatedMention> annotatedMentions = new ArrayList();
    AnnotatedMention annotatedMention = new AnnotatedMention();
    annotatedMention.withCharLength(3).withCharOffset(5);
    annotatedMentions.add(annotatedMention);
    request.setAnnotatedMentions(annotatedMentions);

    // Fire GET request and get the response
    Response response = target(PATH).request(MediaType.APPLICATION_JSON_TYPE)
        .buildPost(javax.ws.rs.client.Entity.entity(request, MediaType.APPLICATION_JSON_TYPE)).invoke();

    //Check the status of the response
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    AnalyzeOutput elo = response.readEntity(AnalyzeOutput.class);
    assertEquals(4, elo.getMatches().size());
    List<Match> resultMentions = elo.getMatches();

    assertEquals(new Entity().withId(ID_PREFIX + "Q93346").getId(), resultMentions.get(0).getEntity().getId());
    assertEquals(new Entity().withId(ID_PREFIX + "Q313529").getId(), resultMentions.get(1).getEntity().getId());
    assertEquals(new Entity().withId(ID_PREFIX + "Q16567").getId(), resultMentions.get(2).getEntity().getId());
    assertEquals(new Entity().withId(ID_PREFIX + "Q26933").getId(), resultMentions.get(3).getEntity().getId());

    response.close();
  }

  @Test public void testAnalyzeLanguge() throws Exception {
    AnalyzeInput request = new AnalyzeInput();
    request.setText("When Who played Tommy in Columbus, Pete was at his best.");
    request.setLanguage("en");
    List<AnnotatedMention> annotatedMentions = new ArrayList();
    AnnotatedMention annotatedMention = new AnnotatedMention();
    annotatedMention.withCharLength(3).withCharOffset(5);
    annotatedMentions.add(annotatedMention);
    request.setAnnotatedMentions(annotatedMentions);

    // Fire GET request and get the response
    Response response = target(PATH).request(MediaType.APPLICATION_JSON_TYPE)
        .buildPost(javax.ws.rs.client.Entity.entity(request, MediaType.APPLICATION_JSON_TYPE)).invoke();

    //Check the status of the response
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    AnalyzeOutput elo = response.readEntity(AnalyzeOutput.class);
    //Assert the input language with the output language
    assertEquals(elo.getLanguage(), request.getLanguage());
    assertEquals(4, elo.getMatches().size());
    List<Match> resultMentions = elo.getMatches();

    assertEquals(new Entity().withId(ID_PREFIX + "Q93346").getId(), resultMentions.get(0).getEntity().getId());
    assertEquals(new Entity().withId(ID_PREFIX + "Q372055").getId(), resultMentions.get(1).getEntity().getId());
    assertEquals(new Entity().withId(ID_PREFIX + "Q16567").getId(), resultMentions.get(2).getEntity().getId());
    assertEquals(new Entity().withId(ID_PREFIX + "Q26933").getId(), resultMentions.get(3).getEntity().getId());
    response.close();
  }
}