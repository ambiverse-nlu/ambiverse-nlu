package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.*;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/*
 * Taken from
 * http://stackoverflow.com/questions/19019879/jersey-content-gzip-deflate *
 */
@Provider // This Annotation is IMPORTANT!
public class GZipInterceptor implements ReaderInterceptor, WriterInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(GZipInterceptor.class);
  
  private static final String CONTENT_ENCODING = "Content-Encoding";

  private static final String ACCEPT_ENCODING = "Accept-Encoding";

  private static final String GZIP = "gzip";

  private static final String APPLICATION_GZIP = "application/gzip";

  @Override public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
    List<String> header = context.getHeaders().get(CONTENT_ENCODING);

    // decompress gzip input stream if necessary
    if (header != null && (header.contains(GZIP) || header.contains(APPLICATION_GZIP))) {
      context.setInputStream(new GZIPInputStream(context.getInputStream()));
    }

    // We need to store the request headers that we need in aroundWriteTo() in the
    // current read/write context as a property. Reading the headers in aroundWriteTo()
    // will only return the response headers, not those of the request.
    List<String> acceptEncodings = context.getHeaders().get(ACCEPT_ENCODING);
    context.setProperty(ACCEPT_ENCODING, acceptEncodings);

    logger.debug("read: " + acceptEncodings);

    return context.proceed();
  }

  @Override public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {

    @SuppressWarnings("unchecked") List<String> acceptEncodings = (List<String>) context.getProperty(ACCEPT_ENCODING);

    logger.debug("write: " + acceptEncodings);

    // Only gzip the response if asked to do so in the request headers.
    if (acceptEncodings != null && (acceptEncodings.contains("gzip") || acceptEncodings.contains(APPLICATION_GZIP))) {
      context.setOutputStream(new GZIPOutputStream(context.getOutputStream()));
      context.getHeaders().add("Content-Encoding", "gzip");
    }

    context.proceed();
  }
}