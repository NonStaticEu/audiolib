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
import eu.nonstatic.audio.AudioInputStream;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.formats.AudioFormatException;
import eu.nonstatic.audio.formats.AudioInfoException;
import eu.nonstatic.audio.formats.AudioInfoSupplier;
import eu.nonstatic.audio.formats.wave.WaveInfo.WaveInfoBuilder;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WaveInfoSupplier implements AudioInfoSupplier<WaveInfo> {

  /**
   * <a href="https://www-mmsp.ece.mcgill.ca/Documents/AudioFormats/WAVE/WAVE.html">...</a>
   */
  public WaveInfo getInfos(InputStream is, String name) throws IOException, AudioInfoException {
    AudioInputStream ais = new AudioInputStream(is, name);
    return getInfos(ais);
  }

  public WaveInfo getInfos(AudioInputStream ais) throws IOException, AudioInfoException {
    try {
      int nbChunks = checkHeader(ais);
      return readDetails(ais, nbChunks);
    } catch(AudioFormatException e) {
      throw new AudioInfoException(e);
    } catch (EOFException e) {
      throw new AudioInfoException(ais.getName(), AudioIssue.eof(ais.location(), e));
    }
  }

  public static boolean isRiff(AudioInputStream ais) throws IOException {
    return "RIFF".equals(ais.readString(4));
  }

  private int checkHeader(AudioInputStream ais) throws AudioFormatException, IOException {
    long location = ais.location();
    if (!isRiff(ais)) {
      throw new AudioFormatException(ais.getName(), location, AudioFileType.WAVE, "No RIFF header");
    }

    location = ais.location();
    int nbChunks = ais.read32bitLE() - 4;
    if (!"WAVE".equals(ais.readString(4))) {
      throw new AudioFormatException(ais.getName(), location, AudioFileType.WAVE, "No WAVE id");
    }
    return nbChunks;
  }

  private WaveInfo readDetails(AudioInputStream ais, int nbChunks) throws AudioFormatException, IOException {
    WaveInfoBuilder builder = WaveInfo.builder().name(ais.getName());
    for (int c = 0; c < nbChunks; c++) {
      String ckName = ais.readString(4);
      int ckSize = ais.read32bitLE(); // 16: no cbSize/extension; 18: 16 + cbSize == 0; 40: 16 + cbSize == 22 + extension (22 bytes)
      ais.chunk(ckSize);

      if ("fmt ".equals(ckName)) {
        short format = ais.read16bitLE();
        builder.format(format);
        short numChannels = ais.read16bitLE();
        builder.numChannels(numChannels); // num channels
        builder.sampleRate(ais.read32bitLE());
        ais.skipNBytes(4); // data rate
        ais.skipNBytes(2); // data block size: numChannels * bitsPerSample/8
        builder.bitsPerSample(ais.read16bitLE());
        if(ais.chunkLeft() > 0 && ais.read16bitLE() > 0 && format != WaveFormat.PCM.value) {
          ais.skipNBytes(6); // wValidBitsPerSample & dwChannelMask
          builder.subFormat(ais.read16bitLE());
          ais.skipNBytes(14); // Remainder of the GUID: \x00\x00\x00\x00\x10\x00\x80\x00\x00\xAA\x00\x38\x9B\x71
        } else {
          ais.skipChunk();
        }
      } else if ("data".equals(ckName)) {
        builder.audioSize(ckSize);
        return builder.build();
      } else {
        ais.skipNBytes(ckSize);
      }
    }
    throw new AudioFormatException(ais.getName(), ais.location(), AudioFileType.WAVE, "No data chunk");
  }
}
