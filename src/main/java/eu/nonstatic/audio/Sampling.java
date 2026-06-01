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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public record Sampling(double[] samples, AudioFormat format) {

  public static Sampling of(AudioInputStream ais) throws IOException {
    try (AudioInputStream mis = AudioUtils.getMonoInputStream(ais)) {

      byte[] bytes = mis.readAllBytes();
      ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

      int numSamples = bytes.length / 2;
      double[] samples = new double[numSamples];

      for (int i = 0; i < numSamples; i++) {
        samples[i] = buffer.getShort() / 32768.0;
      }

      AudioFormat format = mis.getFormat();
      return new Sampling(samples, format);
    }
  }

  public int length() {
    return samples.length;
  }

  public float sampleRate() {
    return format.getSampleRate();
  }

  public int channels() {
    return format.getChannels();
  }

  public boolean bigEndian() {
    return format.isBigEndian();
  }
}
