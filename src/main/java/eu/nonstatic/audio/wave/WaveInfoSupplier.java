/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.wave;

import eu.nonstatic.audio.AudioFormat;
import eu.nonstatic.audio.AudioFormatException;
import eu.nonstatic.audio.AudioInfo;
import eu.nonstatic.audio.AudioInfoException;
import eu.nonstatic.audio.AudioInfoSupplier;
import eu.nonstatic.audio.AudioInputStream;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.wave.WaveInfoSupplier.WaveInfo;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WaveInfoSupplier implements AudioInfoSupplier<WaveInfo> {

  /**
   * https://www-mmsp.ece.mcgill.ca/Documents/AudioFormats/WAVE/WAVE.html
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
      throw new AudioFormatException(ais.getName(), location, AudioFormat.WAVE, "No RIFF header");
    }

    location = ais.location();
    int nbChunks = ais.read32bitLE() - 4;
    if (!"WAVE".equals(ais.readString(4))) {
      throw new AudioFormatException(ais.getName(), location, AudioFormat.WAVE, "No WAVE id");
    }
    return nbChunks;
  }

  private WaveInfo readDetails(AudioInputStream ais, int nbChunks) throws AudioFormatException, IOException {
    WaveInfo info = new WaveInfo(ais.getName());
    for (int c = 0; c < nbChunks; c++) {
      String ckName = ais.readString(4);
      int ckSize = ais.read32bitLE();

      if ("fmt ".equals(ckName)) {
        info.waveFormat = ais.read16bitLE(); // format
        info.numChannels = ais.read16bitLE(); // num channels
        info.sampleRate = ais.read32bitLE();
        ais.skipNBytesBackport(4); // data rate
        short frameSize = ais.read16bitLE(); //  numChannels * bitsPerSample/8
        info.bitsPerSample = (short)((frameSize << 3)/info.numChannels);
        ais.skipNBytesBackport(2); // bits per sample
        ais.skipNBytesBackport((long)ckSize - 16);
      } else if ("data".equals(ckName)) {
        info.audioSize = ckSize;
        return info;
      } else {
        ais.skipNBytesBackport(ckSize);
      }
    }
    throw new AudioFormatException(ais.getName(), ais.location(), AudioFormat.WAVE, "No data chunk");
  }

  @Getter
  public static final class WaveInfo implements AudioInfo {
    private final String name;
    private short waveFormat;
    private short numChannels;
    private int sampleRate;
    private short bitsPerSample;
    private int audioSize;

    public WaveInfo(String name) {
      this.name = name;
    }

    @Override
    public AudioFormat getFormat() {
      return AudioFormat.WAVE;
    }

    @Override
    public Duration getDuration() {
      return Duration.ofMillis(Math.round((audioSize * 8 * 1000.0) / (numChannels * sampleRate * bitsPerSample)));
    }
  }
}
