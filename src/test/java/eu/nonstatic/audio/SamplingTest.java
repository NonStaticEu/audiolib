/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.nonstatic.timecode.TimeCode;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import org.junit.jupiter.api.Test;

class SamplingTest {

  static final AudioFormat FORMAT_44100_16_MONO =
      new AudioFormat(Encoding.PCM_SIGNED, 44100f, 16, 1, 2, 44100f, false);

  static final AudioFormat FORMAT_44100_16_STEREO =
      new AudioFormat(Encoding.PCM_SIGNED, 44100f, 16, 2, 4, 44100f, false);

  @Test
  void should_create_sampling_from_16bitLE_bytes() throws IOException {
    byte[] data = {
        (byte) 0x00, (byte) 0x40, // 16384
        (byte) 0x00, (byte) 0xC0, // -16384
        (byte) 0x00, (byte) 0x00, // 0
        (byte) 0xFF, (byte) 0x7F, // 32767
        (byte) 0x00, (byte) 0x80  // -32768
    };
    try (AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), FORMAT_44100_16_MONO, data.length / 2)) {
      Sampling sampling = Sampling.mono(ais);
      assertArrayEquals(new double[]{16384 / 32768.0, -16384 / 32768.0, 0.0, 32767 / 32768.0, -32768 / 32768.0}, sampling.samples());
      assertEquals(0, sampling.start());
      assertEquals(5, sampling.length());
      assertEquals(FORMAT_44100_16_MONO, sampling.format());
    }
  }

  @Test
  void should_reuse_input_stream_when_input_is_already_mono() throws IOException {
    byte[] data = {(byte) 0x00, (byte) 0x40, (byte) 0x00, (byte) 0xC0}; // 16384, -16384
    try (AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), FORMAT_44100_16_MONO, data.length / 2)) {
      Sampling sampling = Sampling.mono(ais);
      assertArrayEquals(new double[]{16384 / 32768.0, -16384 / 32768.0}, sampling.samples());
      assertEquals(FORMAT_44100_16_MONO, sampling.format());
    }
  }

  @Test
  void should_downmix_stereo_to_mono() throws IOException {
    short[] frames = {100, 200, 300, 400, 500, 600, 700, 800}; // L,R pairs
    byte[] data = new byte[frames.length * 2];
    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    for (short frame : frames) {
      buffer.putShort(frame);
    }
    try (AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(data), FORMAT_44100_16_STEREO, data.length / 4)) {
      Sampling sampling = Sampling.mono(ais);
      assertArrayEquals(new double[]{150 / 32768.0, 350 / 32768.0, 550 / 32768.0, 750 / 32768.0}, sampling.samples());
      assertEquals(1, sampling.channels());
    }
  }

  @Test
  void should_span_the_whole_array_by_default() {
    double[] samples = {0.1, 0.2, 0.3};
    Sampling sampling = new Sampling(samples, FORMAT_44100_16_MONO);
    assertEquals(0, sampling.start());
    assertEquals(samples.length, sampling.length());
    assertArrayEquals(samples, sampling.samples());
  }

  @Test
  void should_slice_using_start_timecode_and_duration() {
    double[] samples = new double[100_000];
    Sampling sampling = new Sampling(samples, FORMAT_44100_16_MONO);

    Sampling slice = sampling.slice(new TimeCode(0, 0, 10), Duration.ofMillis(500));

    assertEquals(11760, slice.start()); // 10 frames at 44100Hz/16bit
    assertEquals(22050, slice.length()); // 500ms at 44100Hz
    assertEquals(FORMAT_44100_16_MONO, slice.format());
  }

  @Test
  void should_slice_using_start_and_end_timecodes() {
    double[] samples = new double[100_000];
    Sampling sampling = new Sampling(samples, FORMAT_44100_16_MONO);

    Sampling slice = sampling.slice(new TimeCode(0, 0, 10), new TimeCode(0, 0, 20));

    assertEquals(11760, slice.start()); // 10 frames
    assertEquals(11760, slice.length()); // 10 frames (20 - 10)
  }

  @Test
  void should_compute_timecode_from_start() {
    double[] samples = new double[100_000];
    Sampling sampling = new Sampling(samples, 11760, samples.length, FORMAT_44100_16_MONO);

    assertEquals(new TimeCode(Duration.ofMillis(133)), sampling.timeCode());
  }

  @Test
  void should_compute_duration_from_length() {
    double[] samples = new double[100_000];
    Sampling sampling = new Sampling(samples, 0, 22050, FORMAT_44100_16_MONO);

    assertEquals(Duration.ofMillis(250), sampling.duration());
  }

  @Test
  void should_expose_format_properties() {
    AudioFormat format = new AudioFormat(Encoding.PCM_SIGNED, 48000f, 16, 2, 4, 48000f, true);
    Sampling sampling = new Sampling(new double[0], format);

    assertEquals(48000f, sampling.sampleRate());
    assertEquals(2, sampling.channels());
    assertTrue(sampling.bigEndian());
  }
}
