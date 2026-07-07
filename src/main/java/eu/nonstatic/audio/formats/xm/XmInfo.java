/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.xm;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.formats.AudioInfo;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class XmInfo implements AudioInfo {

  private static final int TICKS_PER_BPM_PER_MINUTE = 24;
  public static final int LINES_PER_PATTERN = 64;

  private final String name;
  private final String tracker;
  private final short length; // in patterns
  private final short tempo; // ticks per pattern line
  private final short bpm; // there are bpm * 2/5 ticks per second, that is 24*bpm ticks per minute
  private final short numChannels;
  private final short instruments;

  @Override
  public AudioFileType getType() {
    return AudioFileType.XM;
  }

  @Override
  public float getSampleRate() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Duration getDuration() {
    double nanos = (getSongTicks() * ((double)SECONDS_PER_MINUTE * NANOS_PER_SECOND)) / getTicksPerMinute();
    return Duration.ofNanos(Math.round(nanos));
  }

  public int getSongTicks() {
    return length * LINES_PER_PATTERN * tempo;
  }

  public int getTicksPerMinute() {
    return bpm * TICKS_PER_BPM_PER_MINUTE;
  }
}
