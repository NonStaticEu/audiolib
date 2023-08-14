/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio;

import eu.nonstatic.audio.WaveInfoSupplier.WaveInfo;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WaveInfoSupplier implements AudioInfoSupplier<WaveInfo> {

  /**
   * https://www-mmsp.ece.mcgill.ca/Documents/AudioFormats/WAVE/WAVE.html
   */
  public WaveInfo getInfos(InputStream is, String name) throws AudioFormatException, IOException, AudioInfoException {
    AudioInputStream ais = new AudioInputStream(is, name);
    try {
      int nbChunks = checkHeader(ais);
      return readDetails(ais, nbChunks);
    } catch (EOFException e) {
      throw new AudioInfoException(name, AudioIssue.eof(ais.location(), e));
    }
  }

  private int checkHeader(AudioInputStream ais) throws AudioFormatException, IOException {
    if (!"RIFF".equals(ais.readString(4))) {
      throw new AudioFormatException(ais.name, AudioFormat.WAVE, "Not a WAVE file");
    }
    int nbChunks = ais.read32bitLE() - 4;
    if (!"WAVE".equals(ais.readString(4))) {
      throw new AudioFormatException(ais.name, AudioFormat.WAVE, "No WAVE id");
    }
    return nbChunks;
  }

  private WaveInfo readDetails(AudioInputStream ais, int nbChunks) throws AudioFormatException, IOException {
    WaveInfo info = new WaveInfo();
    for (int c = 0; c < nbChunks; c++) {
      String ckName = ais.readString(4);
      int ckSize = ais.read32bitLE();

      if ("fmt ".equals(ckName)) {
        info.format = ais.read16bitLE(); // format
        info.numChannels = ais.read16bitLE(); // num channels
        info.frameRate = ais.read32bitLE();
        ais.skipNBytesBeforeJava12(4); // data rate
        info.frameSize = ais.read16bitLE(); //  numChannels * bitsPerSample/8
        ais.skipNBytesBeforeJava12(2); // bits per sample
        ais.skipNBytesBeforeJava12((long)ckSize - 16);
      } else if ("data".equals(ckName)) {
        info.audioSize = ckSize;
        return info;
      } else {
        ais.skipNBytesBeforeJava12(ckSize);
      }
    }
    throw new AudioFormatException(ais.name, AudioFormat.WAVE, "No data chunk");
  }

  @Getter
  public static final class WaveInfo implements AudioInfo {
    private short format;
    private short numChannels;
    private int frameRate;
    private short frameSize; // bytes
    private int audioSize;

    @Override
    public Duration getDuration() {
      return Duration.ofMillis(Math.round((audioSize * 1000.0) / (frameRate * frameSize)));
    }

    @Override
    public List<AudioIssue> getIssues() {
      return List.of();
    }
  }
}
