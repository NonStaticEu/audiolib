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
import eu.nonstatic.audio.AudioInputStream;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.formats.AudioFormatException;
import eu.nonstatic.audio.formats.AudioInfoException;
import eu.nonstatic.audio.formats.AudioInfoSupplier;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlacInfoSupplier implements AudioInfoSupplier<FlacInfo> {

  private static final int STREAMINFO_BLOCK_TYPE = 0;

  /**
   * <a href="https://xiph.org/flac/format.html#metadata_block_streaminfo">...</a>
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
      throw new AudioFormatException(ais.getName(), location, AudioFileType.FLAC, "No FLAC header");
    }
  }

  private FlacInfo readInfos(AudioInputStream ais) throws AudioFormatException, IOException {
    long location = ais.location();
    int blockType = ais.readStrict() & 0x7;
    if (blockType == STREAMINFO_BLOCK_TYPE) {
      ais.skipNBytes(3); // length
      ais.skipNBytes(10);
      long samplingInfo = ais.read64bitBE();

      int samplingRate = (int) (samplingInfo >> 44);
      int numChannels = (((int) (samplingInfo >> 41)) & 0x7) + 1;
      int sampleSizeInBits = (((int) (samplingInfo >> 36)) & 0x1F) + 1;
      long totalSamples = (samplingInfo & 0xFFFFFFFFFL);

      return FlacInfo.builder()
          .name(ais.getName())
          .sampleRate(samplingRate)
          .channels((short)numChannels)
          .sampleSizeInBits((short)sampleSizeInBits)
          .frameCount((int)totalSamples)
          .build();
    } else {
      throw new AudioFormatException(ais.getName(), location, AudioFileType.FLAC, "STREAMINFO block not found");
    }
  }
}
