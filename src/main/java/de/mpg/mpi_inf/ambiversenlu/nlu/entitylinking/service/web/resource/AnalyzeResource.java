package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.resource;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeInput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.AnalyzeOutput;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.MessageResponse;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.Meta;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Identifies and links names of persons, organizations, locations, products, etc. to canonical entities in our knowledge base and Wikipedia.
 *
 */
@Path("entitylinking/analyze") public interface AnalyzeResource {

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
      "application/json" }) AnalyzeResource.PostAnalyzeResponse postAnalyze(AnalyzeInput entity) throws Exception;

  /**
   *
   */
  @GET @Path("_meta") @Produces({
      "application/json" }) AnalyzeResource.GetAnalyzeMetaResponse getAnalyzeMeta() throws Exception;

  public class GetAnalyzeMetaResponse extends de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.resource.support.ResponseWrapper {

    private GetAnalyzeMetaResponse(Response delegate) {
      super(delegate);
    }

    /**
     *  e.g. {
     *   "dumpVersion": "20160701",
     *   "languages": [
     *     "de", "de", "es", "zh"
     *   ],
     *   "creationDate":"2016-05-31T15:20:50.52Z"
     * }
     *
     *
     * @param entity
     *     {
     *       "dumpVersion": "20160701",
     *       "languages": [
     *         "de", "de", "es", "zh"
     *       ],
     *       "creationDate":"2016-05-31T15:20:50.52Z"
     *     }
     *
     */
    public static AnalyzeResource.GetAnalyzeMetaResponse withJsonOK(Meta entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new AnalyzeResource.GetAnalyzeMetaResponse(responseBuilder.build());
    }

    /**
     *  e.g. {"message": "_metum not found!" }
     *
     *
     * @param entity
     *     {"message": "_metum not found!" }
     *
     */
    public static AnalyzeResource.GetAnalyzeMetaResponse withJsonNotFound(MessageResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new AnalyzeResource.GetAnalyzeMetaResponse(responseBuilder.build());
    }

    /**
     *  e.g. {"message": "You are not allowed to access this method!" }
     *
     *
     * @param entity
     *     {"message": "You are not allowed to access this method!" }
     *
     */
    public static AnalyzeResource.GetAnalyzeMetaResponse withJsonMethodNotAllowed(MessageResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(405).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new AnalyzeResource.GetAnalyzeMetaResponse(responseBuilder.build());
    }

    /**
     *  e.g. {"message": "Method not implemented yet!" }
     *
     *
     * @param entity
     *     {"message": "Method not implemented yet!" }
     *
     */
    public static AnalyzeResource.GetAnalyzeMetaResponse withJsonServiceUnavailable(MessageResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(503).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new AnalyzeResource.GetAnalyzeMetaResponse(responseBuilder.build());
    }

    /**
     *  e.g. {"message": "Error 500 Request failed." }
     *
     * @param entity
     *     {"message": "Error 500 Request failed." }
     */
    public static AnalyzeResource.GetAnalyzeMetaResponse withJsonInternalServerError(MessageResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new AnalyzeResource.GetAnalyzeMetaResponse(responseBuilder.build());
    }

  }

  public class PostAnalyzeResponse extends de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.resource.support.ResponseWrapper {

    private PostAnalyzeResponse(Response delegate) {
      super(delegate);
    }

    /**
     *  e.g. {
     *   "docId": "the-who-live-in-concert",
     *   "matches": [
     *     {
     *       "charLength": 3,
     *       "charOffset": 5,
     *       "entity": {
     *         "kgId": "YAGO3:<The_Who>",
     *         "score": 0.5370212826340575
     *       },
     *       "text": "Who"
     *     },
     *     {
     *       "charLength": 5,
     *       "charOffset": 16,
     *       "entity": {
     *         "kgId": "YAGO3:<Tommy_(album)>",
     *         "score": 0.08736831103697579
     *       },
     *       "text": "Tommy"
     *     },
     *     {
     *       "charLength": 8,
     *       "charOffset": 25,
     *       "entity": {
     *         "kgId": "YAGO3:<Columbus,_Ohio>",
     *         "score": 0.11467049750069586
     *       },
     *       "text": "Columbus"
     *     },
     *     {
     *       "charLength": 4,
     *       "charOffset": 35,
     *       "entity": {
     *         "kgId": "YAGO3:<Pete_Townshend>",
     *         "score": 0.11581026679874379
     *       },
     *       "text": "Pete"
     *     }
     *   ]
     * }
     *
     *
     * @param entity
     *     {
     *       "docId": "the-who-live-in-concert",
     *       "matches": [
     *         {
     *           "charLength": 3,
     *           "charOffset": 5,
     *           "entity": {
     *             "kgId": "YAGO3:<The_Who>",
     *             "score": 0.5370212826340575
     *           },
     *           "text": "Who"
     *         },
     *         {
     *           "charLength": 5,
     *           "charOffset": 16,
     *           "entity": {
     *             "kgId": "YAGO3:<Tommy_(album)>",
     *             "score": 0.08736831103697579
     *           },
     *           "text": "Tommy"
     *         },
     *         {
     *           "charLength": 8,
     *           "charOffset": 25,
     *           "entity": {
     *             "kgId": "YAGO3:<Columbus,_Ohio>",
     *             "score": 0.11467049750069586
     *           },
     *           "text": "Columbus"
     *         },
     *         {
     *           "charLength": 4,
     *           "charOffset": 35,
     *           "entity": {
     *             "kgId": "YAGO3:<Pete_Townshend>",
     *             "score": 0.11581026679874379
     *           },
     *           "text": "Pete"
     *         }
     *       ]
     *     }
     *
     */
    public static AnalyzeResource.PostAnalyzeResponse withJsonOK(AnalyzeOutput entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new AnalyzeResource.PostAnalyzeResponse(responseBuilder.build());
    }

    /**
     *  e.g. {"message": "analyze not found!" }
     *
     *
     * @param entity
     *     {"message": "analyze not found!" }
     *
     */
    public static AnalyzeResource.PostAnalyzeResponse withJsonNotFound(MessageResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new AnalyzeResource.PostAnalyzeResponse(responseBuilder.build());
    }

    /**
     *  e.g. {"message": "You are not allowed to access this method!" }
     *
     *
     * @param entity
     *     {"message": "You are not allowed to access this method!" }
     *
     */
    public static AnalyzeResource.PostAnalyzeResponse withJsonMethodNotAllowed(MessageResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(405).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new AnalyzeResource.PostAnalyzeResponse(responseBuilder.build());
    }

    /**
     *  e.g. {"message": "Error 500 Request failed." }
     *
     *
     * @param entity
     *     {"message": "Error 500 Request failed." }
     *
     */
    public static AnalyzeResource.PostAnalyzeResponse withJsonInternalServerError(MessageResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new AnalyzeResource.PostAnalyzeResponse(responseBuilder.build());
    }

    /**
     *  e.g. {"message": "Method not implemented yet!" }
     *
     *
     * @param entity
     *     {"message": "Method not implemented yet!" }
     *
     */
    public static AnalyzeResource.PostAnalyzeResponse withJsonServiceUnavailable(MessageResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(503).header("Content-Type", "application/json");
      responseBuilder.entity(entity);
      return new AnalyzeResource.PostAnalyzeResponse(responseBuilder.build());
    }

  }

}
