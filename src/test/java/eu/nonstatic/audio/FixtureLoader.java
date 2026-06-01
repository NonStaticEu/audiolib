package eu.nonstatic.audio;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FixtureLoader {


  public static InputStream getResourceAsStream(String classpathLocation) throws IOException {
    InputStream is = FixtureLoader.class.getResourceAsStream(classpathLocation);
    InputStream fis;
    if(classpathLocation.endsWith(".gz")) {
      fis = new GZIPInputStream(is);
    } else if(classpathLocation.endsWith(".zip")) {
      ZipInputStream zis = new ZipInputStream(is);
      ZipEntry nextEntry = zis.getNextEntry(); // assuming there is only one entry
      assertNotNull(nextEntry, "Zip file is empty: " + classpathLocation);
      assertFalse(nextEntry.isDirectory(), "Entry is a directory: " + classpathLocation);
      fis = zis;
    } else {
      fis = is;
    }
    return fis;
  }
}
