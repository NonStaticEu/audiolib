/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.nonstatic.audio.AiffInfoSupplier.AiffInfo;
import eu.nonstatic.audio.AudioIssue.Type;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class AiffInfoSupplierTest implements AudioTestBase {

  AiffInfoSupplier infoSupplier = new AiffInfoSupplier();

  @Test
  void should_give_infos() throws AudioFormatException, IOException, AudioInfoException {
    AiffInfo aiffInfo = infoSupplier.getInfos(AIFF_URL.openStream(), AIFF_NAME);
    assertEquals(Duration.ofMillis(30407L), aiffInfo.getDuration());
    assertTrue(aiffInfo.getIssues().isEmpty());
  }

  /**
   * Tests AudioInfoSupplier.getInfo default methods actually
   */
  @Test
  void should_give_infos_from_file() throws AudioFormatException, IOException, AudioInfoException {
    File tempFile = File.createTempFile("music", ".aiff");
    AudioTestBase.copyFileContents(AIFF_URL, tempFile.toPath());
    AiffInfo aiffInfo = infoSupplier.getInfos(tempFile);
    assertEquals(Duration.ofMillis(30407L), aiffInfo.getDuration());
  }

  @Test
  void should_throw_read_issue() {
    IOException ioe = assertThrows(IOException.class, () -> infoSupplier.getInfos(new FaultyStream(), AIFF_NAME));
    assertEquals("reads: 0", ioe.getMessage());
  }

  @Test
  void should_throw_on_bad_form_header() {
    ByteBuffer bb = ByteBuffer.allocate(12)
      .put("XXXX".getBytes())
      .putInt(1234)
      .put("AIFF".getBytes());

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioFormatException afe = assertThrows(AudioFormatException.class, () -> infoSupplier.getInfos(bais, AIFF_NAME));
    assertEquals("No AIFF FORM header at 0: /audio/Arpeggio.aiff", afe.getMessage());
  }

  @Test
  void should_throw_on_bad_aiff_id_header() {
    ByteBuffer bb = ByteBuffer.allocate(12)
      .put("FORM".getBytes())
      .putInt(1234)
      .put("XXXX".getBytes());

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioFormatException afe = assertThrows(AudioFormatException.class, () -> infoSupplier.getInfos(bais, AIFF_NAME));
    assertEquals("No AIFF id at 4: /audio/Arpeggio.aiff", afe.getMessage());
  }

  @Test
  void should_throw_on_eof() {
    ByteBuffer bb = ByteBuffer.allocate(22)
      .put("FORM".getBytes())
      .putInt(1234)
      .put("AIFF".getBytes())

      .put("COMM".getBytes())
      .putInt(18)
      .putShort((short) 2);
      // incomplete !

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioInfoException iae = assertThrows(AudioInfoException.class, () -> infoSupplier.getInfos(bais, AIFF_NAME));
    assertEquals(1, iae.getIssues().size());
    AudioIssue issue = iae.getIssues().get(0);
    assertEquals(Type.EOF, issue.getType());
    assertEquals(21, issue.getLocation());
    assertEquals(EOFException.class, issue.getCause().getClass());
  }

  @Test
  void should_throw_when_no_comm_chunk_1() {
    ByteBuffer bb = ByteBuffer.allocate(12);
    bb.put("FORM".getBytes());
    bb.putInt(1234);
    bb.put("AIFF".getBytes());
    // and nothing else

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioFormatException afe = assertThrows(AudioFormatException.class, () -> infoSupplier.getInfos(bais, AIFF_NAME));
    assertEquals("Chunk COMM not found at 11: /audio/Arpeggio.aiff", afe.getMessage());
  }

  @Test
  void should_throw_when_no_comm_chunk_2() {
    ByteBuffer bb = ByteBuffer.allocate(28) // BIG_ENDIAN by default
      .put("FORM".getBytes())
      .putInt(1234)
      .put("AIFF".getBytes())

      .put("FOOO".getBytes())
      .putInt(6)
      .put(new byte[]{1, 2, 3, 4, 5, 6});

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioFormatException afe = assertThrows(AudioFormatException.class, () -> infoSupplier.getInfos(bais, AIFF_NAME));
    assertEquals("Chunk COMM not found at 27: /audio/Arpeggio.aiff", afe.getMessage());
  }

  @Test
  void should_find_comm_chunk() throws AudioFormatException, IOException, AudioInfoException {
    ByteBuffer bb = ByteBuffer.allocate(52) // BIG_ENDIAN by default
      .put("FORM".getBytes())
      .putInt(1234)
      .put("AIFF".getBytes())

      .put("FOOO".getBytes())
      .putInt(6)
      .put(new byte[]{1, 2, 3, 4, 5, 6})

      .put("COMM".getBytes())
      .putInt(18)
      .putShort((short) 2)
      .putInt(0)
      .putShort((short) 0)
      .putInt(0);

    AiffInfo infos = infoSupplier.getInfos(new ByteArrayInputStream(bb.array()), AIFF_NAME);
    assertEquals(2, infos.getNumChannels());
  }
}
