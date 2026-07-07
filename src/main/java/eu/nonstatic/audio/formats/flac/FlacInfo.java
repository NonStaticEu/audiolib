/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.flac;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.formats.AudioFormatEx;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Getter
public class FlacInfo extends AudioFormatEx {
  private static final Encoding ENCODING = new Encoding(AudioFileType.FLAC.name());

  private final String name;
  private final int frameCount;

  @Builder
  public FlacInfo(String name, int channels, float sampleRate, int sampleSizeInBits, int frameCount) {
    super(ENCODING, sampleRate,
        sampleSizeInBits, channels, -1, -1.f, false);
    this.name = name;
    this.frameCount = frameCount;
  }

  @Override
  public AudioFileType getType() {
    return AudioFileType.FLAC;
  }

  @Override
  public Duration getDuration() {
    return Duration.ofMillis(Math.round((frameCount * 1000.0) / sampleRate));
  }
}