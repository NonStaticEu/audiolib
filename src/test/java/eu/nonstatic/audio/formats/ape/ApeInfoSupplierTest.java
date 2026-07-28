/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.ape;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.AudioIssue.Type;
import eu.nonstatic.audio.AudioTestBase;
import eu.nonstatic.audio.FaultyStream;
import eu.nonstatic.audio.formats.AudioInfoException;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class ApeInfoSupplierTest implements AudioTestBase {

  ApeInfoSupplier infoSupplier = new ApeInfoSupplier();

  @Test
  void should_give_infos() throws IOException, AudioInfoException {
    ApeInfo apeInfo = infoSupplier.getInfos(APE_URL.openStream(), APE_NAME);
    assertEquals(AudioFileType.APE, apeInfo.getType());
    assertEquals(Duration.ofNanos(1374353792L), apeInfo.getDuration());
    assertTrue(apeInfo.getIssues().isEmpty());
  }

  @Test
  void should_throw_read_issue() {
    IOException ioe = assertThrows(IOException.class, () -> infoSupplier.getInfos(new FaultyStream(), APE_NAME));
    assertEquals("reads: 0", ioe.getMessage());
  }

  @Test
  void should_throw_on_bad_APE_header() {
    ByteBuffer bb = ByteBuffer.allocate(12)
      .put("BAD ".getBytes())
      .putInt(1234);

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioInfoException aie = assertThrows(AudioInfoException.class, () -> infoSupplier.getInfos(bais, APE_NAME));
    assertEquals(1, aie.getIssues().size());
    assertEquals("No MAC header at 0: /audio/Casio-CTK-611-Reverse-Cymbal.ape", aie.getIssues().get(0).getCause().getMessage());
  }

  @Test
  void should_throw_on_eof() {
    ByteBuffer bb = ByteBuffer.allocate(4)
      .put("MAC ".getBytes());
      // and nothing else

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioInfoException iae = assertThrows(AudioInfoException.class, () -> infoSupplier.getInfos(bais, APE_NAME));
    assertEquals(1, iae.getIssues().size());
    AudioIssue issue = iae.getIssues().get(0);
    assertEquals(Type.EOF, issue.getType());
    assertEquals(4, issue.getLocation());
    assertEquals(EOFException.class, issue.getCause().getClass());
  }
}
