package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.service.web.model.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.Date;

/**
 * This custom serializer takes a regular Date object and converts it to an ISO 8601 formatted date-time expression. 
 * An example is a string such as "2016-05-31T15:20:50.000+02:00", which is the serialization result of a date.
 *
 */
public class DateSerializer extends JsonSerializer<Date> {

  @Override public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
    DateTime dateTime = new DateTime(value.getTime());
    gen.writeString(dateTime.toString());
  }

}
