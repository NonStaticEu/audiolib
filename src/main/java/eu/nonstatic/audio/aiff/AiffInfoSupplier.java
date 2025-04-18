/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.aiff;

import eu.nonstatic.audio.AudioFormat;
import eu.nonstatic.audio.AudioFormatException;
import eu.nonstatic.audio.AudioInfo;
import eu.nonstatic.audio.AudioInfoException;
import eu.nonstatic.audio.AudioInfoSupplier;
import eu.nonstatic.audio.AudioInputStream;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.aiff.AiffInfoSupplier.AiffInfo;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AiffInfoSupplier implements AudioInfoSupplier<AiffInfo> {

  /**
   * https://www.mmsp.ece.mcgill.ca/Documents/AudioFormats/AIFF/Docs/AIFF-1.3.pdf
   */
  public AiffInfo getInfos(InputStream is, String name) throws IOException, AudioInfoException {
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
    if (!"FORM".equals(ais.readString(4))) {
      throw new AudioFormatException(ais.getName(), location, AudioFormat.AIFF, "No AIFF FORM header");
    }

    location = ais.location();
    ais.read32bitBE(); // total size
    if (!"AIFF".equals(ais.readString(4))) {
      throw new AudioFormatException(ais.getName(), location, AudioFormat.AIFF, "No AIFF id");
    }
  }

  private AiffInfo readInfos(AudioInputStream ais) throws AudioFormatException, IOException {
    findChunk(ais, "COMM");
    return AiffInfo.builder()
        .name(ais.getName())
        .numChannels(ais.read16bitBE())
        .numFrames(ais.read32bitBE())
        .bitsPerSample(ais.read16bitBE())
        .sampleRate(ais.readExtendedFloatBE())
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
          ais.skipNBytesBackport(ckSize);
        }
      }
    } catch(EOFException e) {
      throw new AudioFormatException(ais.getName(), ais.location(), AudioFormat.AIFF, "Chunk " + name + " not found", e);
    }
  }

  @Getter @Builder
  public static class AiffInfo implements AudioInfo {
    private final String name;
    private short numChannels;
    private double sampleRate;
    private short bitsPerSample;
    private int numFrames;

    @Override
    public AudioFormat getFormat() {
      return AudioFormat.AIFF;
    }

    @Override
    public Duration getDuration() {
      return Duration.ofMillis(Math.round((numFrames * 1000.0) / sampleRate));
    }
  }
}
