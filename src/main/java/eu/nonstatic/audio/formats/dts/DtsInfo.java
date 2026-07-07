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
import eu.nonstatic.audio.formats.AudioFormatEx;
import eu.nonstatic.audio.formats.wave.WaveInfo;
import lombok.Getter;

import java.time.Duration;

@Getter
public class DtsInfo extends AudioFormatEx {
  private static final Encoding ENCODING = new Encoding(AudioFileType.DTS.name());

  private final String name;
  private final short waveFormat;
  private final int audioSize;
  private final Duration duration;

  public DtsInfo(String name, short waveFormat, int channels, float sampleRate, int sampleSizeInBits, int audioSize, Duration duration) {
    super(ENCODING, sampleRate,
        sampleSizeInBits, channels, sampleSizeInBits/8, sampleRate, false);
    this.name = name;
    this.waveFormat = waveFormat;
    this.audioSize = audioSize;
    this.duration = duration;
  }

  public DtsInfo(WaveInfo infos) {
    this(infos.getName(), infos.getFormat(), infos.getChannels(), infos.getSampleRate(), infos.getSampleSizeInBits(), infos.getAudioSize(), infos.getDuration());
  }

  @Override
  public AudioFileType getType() {
    return AudioFileType.DTS;
  }
}