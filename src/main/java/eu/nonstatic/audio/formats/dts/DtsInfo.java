/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.dts;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.formats.AudioInfo;
import eu.nonstatic.audio.formats.wave.WaveInfo;
import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter @RequiredArgsConstructor
public class DtsInfo implements AudioInfo {
  private final String name;
  private final short waveFormat;
  private final short numChannels;
  private final float sampleRate;
  private final short bitsPerSample;
  private final int audioSize;
  private final Duration duration;

  public DtsInfo(WaveInfo infos) {
    this(infos.getName(), infos.getFormat(), infos.getNumChannels(), infos.getSampleRate(), infos.getBitsPerSample(), infos.getAudioSize(), infos.getDuration());
  }

  @Override
  public AudioFileType getType() {
    return AudioFileType.DTS;
  }
}