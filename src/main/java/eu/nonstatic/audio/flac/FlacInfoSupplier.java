/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.flac;

import eu.nonstatic.audio.AudioFormat;
import eu.nonstatic.audio.AudioFormatException;
import eu.nonstatic.audio.AudioInfo;
import eu.nonstatic.audio.AudioInfoException;
import eu.nonstatic.audio.AudioInfoSupplier;
import eu.nonstatic.audio.AudioInputStream;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.flac.FlacInfoSupplier.FlacInfo;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlacInfoSupplier implements AudioInfoSupplier<FlacInfo> {

  private static final int STREAMINFO_BLOCK_TYPE = 0;

  /**
   * https://xiph.org/flac/format.html#metadata_block_streaminfo
   */
  public FlacInfo getInfos(InputStream is, String name) throws IOException, AudioInfoException {
    AudioInputStream ais = new AudioInputStream(is, name);
    try {
      checkHeader(ais);
      return readInfos(ais);
    } catch(AudioFormatException e) {
      throw new AudioInfoException(e);
    } catch (EOFException e) {
      throw new AudioInfoException(name, AudioIssue.eof(ais.location(), e));
    }
  }

  private void checkHeader(AudioInputStream ais) throws AudioFormatException, IOException {
    long location = ais.location();
    if (!"fLaC".equals(ais.readString(4))) {
      throw new AudioFormatException(ais.getName(), location, AudioFormat.FLAC, "No FLAC header");
    }
  }

  private FlacInfo readInfos(AudioInputStream ais) throws AudioFormatException, IOException {
    long location = ais.location();
    int blockType = ais.readStrict() & 0x7;
    if (blockType == STREAMINFO_BLOCK_TYPE) {
      ais.skipNBytesBackport(3); // length
      ais.skipNBytesBackport(10);
      long samplingInfo = ais.read64bitBE();

      int samplingRate = (int) (samplingInfo >> 44);
      int numChannels = (((int) (samplingInfo >> 41)) & 0x7) + 1;
      int bitsPerSample = (((int) (samplingInfo >> 36)) & 0x1F) + 1;
      long totalSamples = (samplingInfo & 0xFFFFFFFFFL);

      return FlacInfo.builder()
          .name(ais.getName())
          .sampleRate(samplingRate)
          .numChannels((short)numChannels)
          .bitsPerSample((short)bitsPerSample)
          .numFrames(totalSamples)
          .build();
    } else {
      throw new AudioFormatException(ais.getName(), location, AudioFormat.FLAC, "STREAMINFO block not found");
    }
  }




  @Getter @Builder
  public static class FlacInfo implements AudioInfo {
    private final String name;
    private final short numChannels;
    private final int sampleRate;
    private final short bitsPerSample;
    private final long numFrames;

    @Override
    public Duration getDuration() {
      return Duration.ofMillis(Math.round((numFrames * 1000.0) / sampleRate));
    }
  }
}
