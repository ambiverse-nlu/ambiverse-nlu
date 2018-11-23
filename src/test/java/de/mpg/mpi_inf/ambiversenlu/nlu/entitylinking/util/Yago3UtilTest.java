package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;

public class Yago3UtilTest {

  @Before
  public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test_multi");
  }

  @Test public void test() throws UnsupportedEncodingException {

    String kb_id = "<Sony_Alpha_Fish-Eye_16mm_f/2.8>";
    String url = Yago3Util.getEntityAsUrlPart(kb_id);
    assertEquals("Sony%20Alpha%20Fish-Eye%2016mm%20f%2F2.8", url);

    kb_id = "<de/test>";
    url = Yago3Util.getEntityAsUrlPart(kb_id);
    assertEquals("test", url);

  }

}
