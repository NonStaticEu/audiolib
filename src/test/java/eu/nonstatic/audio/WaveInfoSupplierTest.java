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

import eu.nonstatic.audio.AudioIssue.Type;
import eu.nonstatic.audio.WaveInfoSupplier.WaveInfo;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class WaveInfoSupplierTest implements AudioTestBase {

  WaveInfoSupplier infoSupplier = new WaveInfoSupplier();

  @Test
  void should_give_infos() throws AudioFormatException, IOException, AudioInfoException {
    WaveInfo waveInfo = infoSupplier.getInfos(WAVE_URL.openStream(), WAVE_NAME);
    assertEquals(Duration.ofMillis(8011L), waveInfo.getDuration());
    assertTrue(waveInfo.getIssues().isEmpty());
  }


  @Test
  void should_throw_read_issue() {
    IOException ioe = assertThrows(IOException.class, () -> infoSupplier.getInfos(new FaultyStream(), WAVE_NAME));
    assertEquals("reads: 0", ioe.getMessage());
  }

  @Test
  void should_throw_on_bad_riff_header() {
    ByteBuffer bb = ByteBuffer.allocate(12)
      .put("XXXX".getBytes())
      .putInt(1234)
      .put("WAVE".getBytes());

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioFormatException afe = assertThrows(AudioFormatException.class, () -> infoSupplier.getInfos(bais, WAVE_NAME));
    assertEquals("No RIFF header at 0: /audio/Amplitudenmodulation.wav", afe.getMessage());
  }

  @Test
  void should_throw_on_bad_wave_id_header() {
    ByteBuffer bb = ByteBuffer.allocate(12)
      .put("RIFF".getBytes())
      .putInt(1234)
      .put("XXXX".getBytes());

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioFormatException afe = assertThrows(AudioFormatException.class, () -> infoSupplier.getInfos(bais, WAVE_NAME));
    assertEquals("No WAVE id at 4: /audio/Amplitudenmodulation.wav", afe.getMessage());
  }

  @Test
  void should_throw_when_no_format_info() {
    ByteBuffer bb = ByteBuffer.allocate(24)
      .put("RIFF".getBytes())
      .order(ByteOrder.LITTLE_ENDIAN)
      .putInt(5) // 4+chunks
      .order(ByteOrder.BIG_ENDIAN)
      .put("WAVE".getBytes())
      .put("duh!".getBytes())
      .order(ByteOrder.LITTLE_ENDIAN)
      .putInt(4) //chunk size
      .putInt(42); // dummy chunk

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioFormatException afe = assertThrows(AudioFormatException.class, () -> infoSupplier.getInfos(bais, WAVE_NAME));
    assertEquals("No data chunk at 24: /audio/Amplitudenmodulation.wav", afe.getMessage());
  }

  @Test
  void should_throw_on_eof() {
    ByteBuffer bb = ByteBuffer.allocate(16)
      .put("RIFF".getBytes())
      .putInt(1) // chunks
      .put("WAVE".getBytes())
      .put("fmt ".getBytes()); // incomplete, just chunk header
      // and nothing else

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioInfoException iae = assertThrows(AudioInfoException.class, () -> infoSupplier.getInfos(bais, WAVE_NAME));
    assertEquals(1, iae.getIssues().size());
    AudioIssue issue = iae.getIssues().get(0);
    assertEquals(Type.EOF, issue.getType());
    assertEquals(15, issue.getLocation());
    assertEquals(EOFException.class, issue.getCause().getClass());
  }
}
