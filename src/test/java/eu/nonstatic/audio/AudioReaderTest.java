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

import static eu.nonstatic.audio.AudioUtils.getMonoInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AudioReaderTest {

  @Test
  void readsMonoWavFile(@TempDir Path tempDir) throws Exception {
    Path wavFile = tempDir.resolve("test.wav");
    writeWav(wavFile, 44100f, 1, 44100); // 1 second mono

    AudioInputStream ais = getMonoInputStream(Files.newInputStream(wavFile));
    AudioFormat format = ais.getFormat();
    byte[] data = ais.readAllBytes();

    assertEquals(44100f, format.getSampleRate());
    assertEquals(44100 * 16/8, data.length);
  }

  @Test
  void readsStereoWavAsMonoFile(@TempDir Path tempDir) throws Exception {
    Path wavFile = tempDir.resolve("stereo.wav");
    writeWav(wavFile, 44100f, 2, 44100); // 1 second stereo

    AudioInputStream ais = getMonoInputStream(Files.newInputStream(wavFile));
    AudioFormat format = ais.getFormat();
    byte[] data = ais.readAllBytes();

    assertEquals(44100f, format.getSampleRate());
    // Stereo converted to mono: same number of frames
    assertEquals(44100 * 16/8, data.length);
  }

  /**
   * Writes a synthetic WAV file with a sine wave.
   */
  private void writeWav(Path path, float sampleRate, int channels, int numFrames) throws IOException {
    int bytesPerSample = 2;
    int frameSize = channels * bytesPerSample;
    byte[] data = new byte[numFrames * frameSize];
    ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

    for (int i = 0; i < numFrames; i++) {
      short sample = (short) (Math.sin(2 * Math.PI * 440 * i / sampleRate) * 16000);
      for (int ch = 0; ch < channels; ch++) {
        buf.putShort(sample);
      }
    }

    AudioFormat format = new AudioFormat(sampleRate, 16, channels, true, false);
    try (AudioInputStream ais = new AudioInputStream(
        new ByteArrayInputStream(data), format, numFrames)) {
      AudioSystem.write(ais, AudioFileFormat.Type.WAVE, path.toFile());
    }
  }
}
