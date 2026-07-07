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
import eu.nonstatic.audio.formats.AudioInfo;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter @Builder
public final class WaveInfo implements AudioInfo {

  private final @NonNull String name;
  private final short format;
  private final Short subFormat;
  private final short numChannels;
  private final float sampleRate;
  private final short bitsPerSample;
  private final int audioSize;

  @Override
  public AudioFileType getType() {
    return AudioFileType.WAVE;
  }

  @Override
  public Duration getDuration() {
    return Duration.ofMillis(Math.round((audioSize * 8 * 1000.0) / (numChannels * sampleRate * bitsPerSample)));
  }
}
