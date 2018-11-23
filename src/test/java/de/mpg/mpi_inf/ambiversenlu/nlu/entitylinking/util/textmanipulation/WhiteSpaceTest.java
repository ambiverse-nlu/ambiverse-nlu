package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.util.textmanipulation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WhiteSpaceTest {

  @Test public void test() {
    String text = " This is a test ";
    assertEquals(Whitespace.isWhiteSpace(text), false);

    text = "\n\n\n\n\n\n   \n\n\n";
    assertEquals(Whitespace.isWhiteSpace(text), true);
  }

}
