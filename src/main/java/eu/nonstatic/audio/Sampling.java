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

import eu.nonstatic.timecode.TimeCode;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Duration;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

/**
 * @param samples
 * @param start number of samples
 * @param length number of samples
 * @param format
 */
public record Sampling(double[] samples, int start, int length, AudioFormat format, boolean lenient) {

  public Sampling {
    if(lenient) {
      start = Math.min(samples.length, Math.max(0, start));
      length = Math.min(Math.max(0, length), samples.length - start);
    }

    if (start < 0 || length < 0 || start+length > samples.length || format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
      throw new IllegalArgumentException("samples: %d, start: %d, length: %d, encoding: %s".formatted(samples.length, start, length, format.getEncoding()));
    }
  }

  public Sampling(double[] samples, int start, int length, AudioFormat format) {
    this(samples, start, length, format, false);
  }

  public Sampling(double[] samples, AudioFormat format, boolean lenient) {
    this(samples, 0, samples.length, format, lenient);
  }

  public Sampling(double[] samples, AudioFormat format) {
    this(samples, format, false);
  }

  public static Sampling mono(AudioInputStream ais) throws IOException {
    try (AudioInputStream mis = AudioUtils.getMonoInputStream(ais)) {
      byte[] bytes = mis.readAllBytes();
      ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

      int numSamples = bytes.length / 2; // sampleSizeInBits / 8
      double[] samples = new double[numSamples];

      for (int i = 0; i < numSamples; i++) {
        samples[i] = buffer.getShort() / 32768.0; // 2 << (sampleSizeInBits-1) to scale to [-1, +1]
      }

      return new Sampling(samples, mis.getFormat());
    }
  }

  public Sampling safe() {
    return new Sampling(samples, start, length, format, true);
  }

  public Sampling unsafe() {
    return new Sampling(samples, start, length, format, false);
  }

  public Sampling slice(TimeCode start, Duration duration) {
    return new Sampling(samples, timeCodeToSamples(start), durationToSamples(duration), format, lenient);
  }

  public Sampling slice(TimeCode start, TimeCode end) {
    return new Sampling(samples, timeCodeToSamples(start), framesToSamples(end.toFrameCount() - start.toFrameCount()), format, lenient);
  }

  public TimeCode timeCode() {
    return new TimeCode(samplesToDuration(start));
  }

  public Duration duration() {
    return samplesToDuration(length);
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

  private int timeCodeToSamples(TimeCode timeCode) {
    return framesToSamples(timeCode.toFrameCount());
  }

  private int framesToSamples(int frames) {
    return (int) (format.getSampleRate() * format.getSampleSizeInBits() * frames) / (75 * 8);
  }

  private int durationToSamples(Duration duration) {
    return (int)((format.getSampleRate() * duration.toMillis()) / 1000);
  }

  private Duration samplesToDuration(int samples) {
    float millis = (samples * 1000 * 8) / (format.getSampleRate() * format.getSampleSizeInBits());
    return Duration.ofMillis((long)millis);
  }
}
