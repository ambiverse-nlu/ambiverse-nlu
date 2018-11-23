package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.Date;

/**
 * This custom deserializer takes an ISO 8601 formatted date-time expression and converts it to a regular Date object.
 * An example is a string such as "2016-05-31T15:20:50.000+02:00", which is then converted to a date.
 *
 */
public class DateDeserializer extends JsonDeserializer<Date> {

  @Override public Date deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
    String value = jp.readValueAs(String.class);

    return new Date(new DateTime(value).getValue());
  }

}