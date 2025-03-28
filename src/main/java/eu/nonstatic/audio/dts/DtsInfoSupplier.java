/**
 * Audiolib
 * Copyright (C) 2025 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.dts;

import eu.nonstatic.audio.AudioFormat;
import eu.nonstatic.audio.AudioFormatException;
import eu.nonstatic.audio.AudioInfo;
import eu.nonstatic.audio.AudioInfoException;
import eu.nonstatic.audio.AudioInfoSupplier;
import eu.nonstatic.audio.AudioInputStream;
import eu.nonstatic.audio.dts.DtsInfoSupplier.DtsInfo;
import eu.nonstatic.audio.wave.WaveInfoSupplier;
import eu.nonstatic.audio.wave.WaveInfoSupplier.WaveInfo;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DtsInfoSupplier implements AudioInfoSupplier<DtsInfo> {

  @Override
  public DtsInfo getInfos(InputStream is, String name) throws AudioInfoException, IOException {
    AudioInputStream ais = new AudioInputStream(is, name);

    ais.mark(4);
    if(WaveInfoSupplier.isRiff(ais)) {
      ais.reset();
      WaveInfo infos = new WaveInfoSupplier().getInfos(ais);
      return DtsInfo.of(infos);
    } else {
      throw new AudioInfoException(new AudioFormatException(ais.getName(), 0, AudioFormat.DTS, "DTS format not supported"));
    }
  }


  @Getter @Builder
  public static class DtsInfo implements AudioInfo {
    private final String name;
    private final short format;
    private final short numChannels;
    private final int sampleRate;
    private final short bitsPerSample;
    private final int audioSize;
    private final Duration duration;

    public static DtsInfo of(WaveInfo infos) {
      return DtsInfo.builder()
          .name(infos.getName())
          .format(infos.getFormat())
          .numChannels(infos.getNumChannels())
          .sampleRate(infos.getSampleRate())
          .bitsPerSample(infos.getBitsPerSample())
          .audioSize(infos.getAudioSize())
          .duration(infos.getDuration())
          .build();
    }
  }
}
