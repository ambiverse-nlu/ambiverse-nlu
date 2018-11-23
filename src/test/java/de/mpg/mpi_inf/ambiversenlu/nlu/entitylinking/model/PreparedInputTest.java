package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.model;

import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access.EntityLinkingDataAccessException;
import de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.config.ConfigUtils;
import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.util.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class PreparedInputTest {

  @Before public void setup() throws EntityLinkingDataAccessException {
    ConfigUtils.setConfOverride("unit_test");
  }

  @Test public void testLoadWrite() throws IOException, EntityLinkingDataAccessException, URISyntaxException {
    File orig = Paths.get(ClassLoader.getSystemResource("preparedinput/preparedinputtest.tsv").toURI()).toFile();
    String origContent = FileUtils.getFileContent(orig);
    
    PreparedInput prep = new PreparedInput(orig);
    
    File tmpFile = File.createTempFile("test", "tmp");
    tmpFile.deleteOnExit();
    
    prep.writeTo(tmpFile);
    
    String tmpContent = FileUtils.getFileContent(tmpFile);
    
    assertEquals(origContent, tmpContent);
  }
}
