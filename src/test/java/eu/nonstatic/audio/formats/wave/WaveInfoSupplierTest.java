/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.wave;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.AudioIssue.Type;
import eu.nonstatic.audio.AudioTestBase;
import eu.nonstatic.audio.FaultyStream;
import eu.nonstatic.audio.formats.AudioInfoException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class WaveInfoSupplierTest implements AudioTestBase {

  WaveInfoSupplier infoSupplier = new WaveInfoSupplier();

  @Test
  void should_give_infos() throws IOException, AudioInfoException {
    WaveInfo waveInfo = infoSupplier.getInfos(WAVE_URL.openStream(), WAVE_NAME);
    assertEquals(AudioFileType.WAVE, waveInfo.getType());
    assertEquals(Duration.ofMillis(8011L), waveInfo.getDuration());
    assertNull(waveInfo.getSubFormat());
    assertTrue(waveInfo.getIssues().isEmpty());
  }


  @Test
  void should_read_fmt_chunk_with_cbSize_0() throws IOException, AudioInfoException {
    ByteBuffer bb = ByteBuffer.allocate(46).order(ByteOrder.LITTLE_ENDIAN)
      .put("RIFF".getBytes())
      .putInt(6) // 4 + 2 chunks (fmt + data)
      .put("WAVE".getBytes())

      .put("fmt ".getBytes())
      .putInt(18) // 16 + cbSize (2 bytes) == 0
      .putShort(WaveFormat.PCM.getValue())
      .putShort((short) 2)   // numChannels
      .putInt(44100)         // sampleRate
      .putInt(176400)        // data rate
      .putShort((short) 4)   // data block size
      .putShort((short) 16)  // bitsPerSample
      .putShort((short) 0)   // cbSize == 0: no extension

      .put("data".getBytes())
      .putInt(1000); // audioSize

    WaveInfo waveInfo = infoSupplier.getInfos(new ByteArrayInputStream(bb.array()), WAVE_NAME);
    assertEquals(WaveFormat.PCM.getValue(), waveInfo.getFormat());
    assertEquals(2, waveInfo.getChannels());
    assertEquals(44100f, waveInfo.getSampleRate());
    assertEquals(16, waveInfo.getSampleSizeInBits());
    assertEquals(1000, waveInfo.getAudioSize());
    assertNull(waveInfo.getSubFormat());
    assertTrue(waveInfo.getIssues().isEmpty());
  }

  @Test
  void should_read_fmt_chunk_with_cbSize_22() throws IOException, AudioInfoException {
    ByteBuffer bb = ByteBuffer.allocate(68).order(ByteOrder.LITTLE_ENDIAN)
      .put("RIFF".getBytes())
      .putInt(6) // 4 + 2 chunks (fmt + data)
      .put("WAVE".getBytes())

      .put("fmt ".getBytes())
      .putInt(40) // 16 + cbSize (2 bytes) + 22 bytes extension
      .putShort(WaveFormat.EXTENSIBLE.getValue()) // must not be PCM to read the extension
      .putShort((short) 2)   // numChannels
      .putInt(48000)         // sampleRate
      .putInt(288000)        // data rate
      .putShort((short) 6)   // data block size
      .putShort((short) 24)  // bitsPerSample
      .putShort((short) 22)  // cbSize == 22: extension follows
      .putShort((short) 24)  // wValidBitsPerSample
      .putInt(3)             // dwChannelMask
      .putShort(WaveFormat.PCM.getValue()) // subFormat
      .put(new byte[14]);    // remainder of the GUID

    bb.put("data".getBytes()).putInt(2000); // audioSize

    WaveInfo waveInfo = infoSupplier.getInfos(new ByteArrayInputStream(bb.array()), WAVE_NAME);
    assertEquals(WaveFormat.EXTENSIBLE.getValue(), waveInfo.getFormat());
    assertEquals(2, waveInfo.getChannels());
    assertEquals(48000f, waveInfo.getSampleRate());
    assertEquals(24, waveInfo.getSampleSizeInBits());
    assertEquals(2000, waveInfo.getAudioSize());
    assertEquals(WaveFormat.PCM.getValue(), waveInfo.getSubFormat());
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
    AudioInfoException aie = assertThrows(AudioInfoException.class, () -> infoSupplier.getInfos(bais, WAVE_NAME));
    assertEquals(1, aie.getIssues().size());
    assertEquals("No RIFF header at 0: /audio/Amplitudenmodulation.wav", aie.getIssues().get(0).getCause().getMessage());
  }

  @Test
  void should_throw_on_bad_wave_id_header() {
    ByteBuffer bb = ByteBuffer.allocate(12)
      .put("RIFF".getBytes())
      .putInt(1234)
      .put("XXXX".getBytes());

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioInfoException aie = assertThrows(AudioInfoException.class, () -> infoSupplier.getInfos(bais, WAVE_NAME));
    assertEquals(1, aie.getIssues().size());
    assertEquals("No WAVE id at 4: /audio/Amplitudenmodulation.wav", aie.getIssues().get(0).getCause().getMessage());
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
    AudioInfoException aie = assertThrows(AudioInfoException.class, () -> infoSupplier.getInfos(bais, WAVE_NAME));
    assertEquals(1, aie.getIssues().size());
    assertEquals("No data chunk at 24: /audio/Amplitudenmodulation.wav", aie.getIssues().get(0).getCause().getMessage());
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
