package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.resource;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeInput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeOutput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.MessageResponse;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Identifies and links names of persons, organizations, locations, products, etc. to canonical entities in our knowledge base and Wikipedia.
 *
 */
@Path("factextraction/analyze") public interface AnalyzeResourceWithFacts {

  /**
   *
   * @param entity
   *      e.g. {
   *         "docId": "the-who-live-in-concert",
   *         "text": "When Who played Tommy in Columbus, Pete was at his best."
   *     }
   *
   */
  @POST @Consumes("application/json") @Produces({
      "application/json" }) AnalyzeResourceWithFacts.PostAnalyzeResponse postAnalyze(AnalyzeInput entity) throws Exception;



  public class PostAnalyzeResponse extends de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.resource.support.ResponseWrapper {

    private PostAnalyzeResponse(Response delegate) {
      super(delegate);
    }


    public static AnalyzeResourceWithFacts.PostAnalyzeResponse withJsonOK(AnalyzeOutput entity) {
      ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new AnalyzeResourceWithFacts.PostAnalyzeResponse(responseBuilder.build());
    }

    /**
     *  e.g. {"message": "analyze not found!" }
     *
     *
     * @param entity
     *     {"message": "analyze not found!" }
     *
     */
    public static AnalyzeResourceWithFacts.PostAnalyzeResponse withJsonNotFound(MessageResponse entity) {
      ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new AnalyzeResourceWithFacts.PostAnalyzeResponse(responseBuilder.build());
    }

    /**
     *  e.g. {"message": "You are not allowed to access this method!" }
     *
     *
     * @param entity
     *     {"message": "You are not allowed to access this method!" }
     *
     */
    public static AnalyzeResourceWithFacts.PostAnalyzeResponse withJsonMethodNotAllowed(MessageResponse entity) {
      ResponseBuilder responseBuilder = Response.status(405).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new AnalyzeResourceWithFacts.PostAnalyzeResponse(responseBuilder.build());
    }

    /**
     *  e.g. {"message": "Error 500 Request failed." }
     *
     *
     * @param entity
     *     {"message": "Error 500 Request failed." }
     *
     */
    public static AnalyzeResourceWithFacts.PostAnalyzeResponse withJsonInternalServerError(MessageResponse entity) {
      ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new AnalyzeResourceWithFacts.PostAnalyzeResponse(responseBuilder.build());
    }

    /**
     *  e.g. {"message": "Method not implemented yet!" }
     *
     *
     * @param entity
     *     {"message": "Method not implemented yet!" }
     *
     */
    public static AnalyzeResourceWithFacts.PostAnalyzeResponse withJsonServiceUnavailable(MessageResponse entity) {
      ResponseBuilder responseBuilder = Response.status(503).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new AnalyzeResourceWithFacts.PostAnalyzeResponse(responseBuilder.build());
    }

  }

}