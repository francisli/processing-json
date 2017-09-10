package com.francisli.processing.json;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Unit test for simple App.
 */
public class JSONParserTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public JSONParserTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( JSONParserTest.class );
    }

    public static class VolumeInfo {
        public String title;
    }

    public static class Volume {
        public String id;
        public VolumeInfo volumeInfo;
    }

    public static class Response {
        public String kind;
        public long totalItems;
        public ArrayList<Volume> items;
    }

    public void testParseString() {
      JSONParser<Response> parser = new JSONParser<Response>() {};
      try {
          Response result = parser.parse("{\"kind\":\"string\",\"totalItems\":2}");
          assertEquals("string", result.kind);
          assertEquals(2, result.totalItems);
      } catch (Exception e) {
          throw new RuntimeException(e);
      }
    }

    public void testParse()
    {
        JSONParser<Response> parser = new JSONParser<Response>() {};
        try {
            Response result = parser.parse(new FileInputStream("test.json"));
            assertEquals("books#volumes", result.kind);
            assertEquals(1, result.totalItems);
            assertNotNull(result.items);
            assertEquals(1, result.items.size());
            assertEquals("yZ1APgAACAAJ", result.items.get(0).id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
