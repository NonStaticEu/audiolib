/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.flac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.nonstatic.audio.AudioInfoException;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.AudioIssue.Type;
import eu.nonstatic.audio.AudioTestBase;
import eu.nonstatic.audio.FaultyStream;
import eu.nonstatic.audio.flac.FlacInfoSupplier.FlacInfo;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class FlacInfoSupplierTest implements AudioTestBase {

  FlacInfoSupplier infoSupplier = new FlacInfoSupplier();

  @Test
  void should_give_infos() throws IOException, AudioInfoException {
    FlacInfo flacInfo = infoSupplier.getInfos(FLAC_URL.openStream(), FLAC_NAME);
    assertEquals(Duration.ofMillis(3692L), flacInfo.getDuration());
    assertTrue(flacInfo.getIssues().isEmpty());
  }

  @Test
  void should_throw_read_issue() {
    IOException ioe = assertThrows(IOException.class, () -> infoSupplier.getInfos(new FaultyStream(), FLAC_NAME));
    assertEquals("reads: 0", ioe.getMessage());
  }

  @Test
  void should_throw_on_bad_flac_header() {
    ByteBuffer bb = ByteBuffer.allocate(12)
      .put("NOPE".getBytes())
      .putInt(1234);

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioInfoException aie = assertThrows(AudioInfoException.class, () -> infoSupplier.getInfos(bais, FLAC_NAME));
    assertEquals(1, aie.getIssues().size());
    assertEquals("No FLAC header at 0: /audio/Filtered_envelope_sawtooth_moog.flac", aie.getIssues().get(0).getCause().getMessage());
  }

  @Test
  void should_throw_when_no_stream_info() {
    ByteBuffer bb = ByteBuffer.allocate(5)
      .put("fLaC".getBytes())
      .put((byte)6);

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioInfoException aie = assertThrows(AudioInfoException.class, () -> infoSupplier.getInfos(bais, FLAC_NAME));
    assertEquals(1, aie.getIssues().size());
    assertEquals("STREAMINFO block not found at 4: /audio/Filtered_envelope_sawtooth_moog.flac", aie.getIssues().get(0).getCause().getMessage());
  }

  @Test
  void should_throw_on_eof() {
    ByteBuffer bb = ByteBuffer.allocate(4)
      .put("fLaC".getBytes());
      // and nothing else

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioInfoException iae = assertThrows(AudioInfoException.class, () -> infoSupplier.getInfos(bais, FLAC_NAME));
    assertEquals(1, iae.getIssues().size());
    AudioIssue issue = iae.getIssues().get(0);
    assertEquals(Type.EOF, issue.getType());
    assertEquals(4, issue.getLocation());
    assertEquals(EOFException.class, issue.getCause().getClass());
  }
}
