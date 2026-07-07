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
import eu.nonstatic.audio.formats.AudioFormatEx;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.Duration;

@Getter
public final class WaveInfo extends AudioFormatEx {

  private final @NonNull String name;
  private final short format;
  private final Short subFormat;
  private final int audioSize;

  @Builder
  public WaveInfo(@NonNull String name, short format, Short subFormat, int channels, float sampleRate, int sampleSizeInBits, int audioSize) {
    super(getEncoding(format, subFormat, sampleSizeInBits), sampleRate, sampleSizeInBits, channels,
            (sampleSizeInBits * channels)/8, sampleRate, false);
    this.name = name;
    this.format = format;
    this.subFormat = subFormat;
    this.audioSize = audioSize;
  }

  @Override
  public AudioFileType getType() {
    return AudioFileType.WAVE;
  }

  @Override
  public Duration getDuration() {
    return Duration.ofMillis(Math.round((audioSize * 8 * 1000.0) / (channels * sampleRate * sampleSizeInBits)));
  }

  private static Encoding getEncoding(short format, Short subFormat, int sampleSizeInBits) {
    WaveFormat waveFormat = WaveFormat.ofValue(format);
    return switch (waveFormat) {
      case PCM -> sampleSizeInBits <= 8 ? Encoding.PCM_UNSIGNED : Encoding.PCM_SIGNED;
      case IEEE_FLOAT -> Encoding.PCM_FLOAT;
      case ALAW -> Encoding.ALAW;
      case MULAW -> Encoding.ULAW;
      case EXTENSIBLE -> new Encoding(WaveFormat.ofValue(subFormat).name());
      default -> new Encoding(waveFormat.name());
    };
  }
}
