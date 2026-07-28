/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.aiff;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.formats.AudioFormatEx;
import java.time.Duration;
import java.util.Locale;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AiffInfo extends AudioFormatEx {
  private final String name;
  private final int frameCount;
  private final String compression;

  @Builder
  public AiffInfo(String name, int channels, float sampleRate, int sampleSizeInBits, int frameCount, String compression, boolean bigEndian) {
    super(new Encoding(compression.toUpperCase(Locale.ROOT)), sampleRate,
        sampleSizeInBits, channels, (sampleSizeInBits * channels)/8, sampleRate, bigEndian);
    this.name = name;
    this.frameCount = frameCount;
    this.compression = compression;
    this.bigEndian = bigEndian;
  }

  @Override
  public AudioFileType getType() {
    return AudioFileType.AIFF;
  }

  @Override
  public Duration getDuration() {
    return Duration.ofMillis(Math.round((frameCount * 1000.0) / sampleRate));
  }
}
