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
public class AiffInfoSupplier implements AudioInfoSupplier<AiffInfo> {

  /**
   * <a href="https://www.mmsp.ece.mcgill.ca/Documents/AudioFormats/AIFF/Docs/AIFF-1.3.pdf">...</a>
   */
  public AiffInfo getInfos(InputStream is, String name) throws IOException, AudioInfoException {
    AudioInputStream ais = new AudioInputStream(is, name);
    try {
      boolean aifc = checkHeader(ais);
      return readInfos(ais, aifc);
    } catch(AudioFormatException e) {
      throw new AudioInfoException(e);
    } catch (EOFException e) {
      throw new AudioInfoException(name, AudioIssue.eof(ais.location(), e));
    }
  }

  private boolean checkHeader(AudioInputStream ais) throws AudioFormatException, IOException {
    long location = ais.location();
    if (!"FORM".equals(ais.readString(4))) {
      throw new AudioFormatException(ais.getName(), location, AudioFileType.AIFF, "No AIFF FORM header");
    }

    location = ais.location();
    ais.read32bitBE(); // total size
    String type = ais.readString(4);
    if ("AIFF".equals(type)) {
      return false;
    } else if("AIFC".equals(type)) {
      return true;
    } else {
      throw new AudioFormatException(ais.getName(), location, AudioFileType.AIFF, "No AIFF/AIFC id");
    }
  }

  private AiffInfo readInfos(AudioInputStream ais, boolean aifc) throws AudioFormatException, IOException {
    findChunk(ais, "COMM");
    short numChannels = ais.read16bitBE();
    int frameCount = ais.read32bitBE();
    short sampleSizeInBits = ais.read16bitBE();
    double sampleRate = ais.readExtendedFloatBE();

    String compression;
    boolean bigEndian = true;
    if(aifc) {
      compression = ais.readString(4);
      if("sowt".equals(compression)) {
        bigEndian = false;
      }
    } else {
      compression = "NONE";
    }

    return AiffInfo.builder()
        .name(ais.getName())
        .channels(numChannels)
        .frameCount(frameCount)
        .sampleSizeInBits(sampleSizeInBits)
        .sampleRate((float) sampleRate)
        .compression(compression)
        .bigEndian(bigEndian)
        .build();
  }

  private void findChunk(AudioInputStream ais, String name) throws AudioFormatException, IOException {
    try {
      while (true) {
        String ckName = ais.readString(4);
        int ckSize = ais.read32bitBE();
        if (name.equals(ckName)) {
          break;
        } else {
          ais.skipNBytes(ckSize);
        }
      }
    } catch(EOFException e) {
      throw new AudioFormatException(ais.getName(), ais.location(), AudioFileType.AIFF, "Chunk " + name + " not found", e);
    }
  }
}
