/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Audiolib is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Audiolib. If not, see <https://www.gnu.org/licenses/>.
 */
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
