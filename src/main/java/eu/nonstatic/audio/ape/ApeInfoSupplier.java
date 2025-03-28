/**
 * Audiolib
 * Copyright (C) 2025 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.ape;

import eu.nonstatic.audio.AudioFormat;
import eu.nonstatic.audio.AudioFormatException;
import eu.nonstatic.audio.AudioInfo;
import eu.nonstatic.audio.AudioInfoException;
import eu.nonstatic.audio.AudioInfoSupplier;
import eu.nonstatic.audio.AudioInputStream;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.ape.ApeInfoSupplier.ApeInfo;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApeInfoSupplier implements AudioInfoSupplier<ApeInfo> {
  /* The earliest and latest file formats supported by this library */
  private static final short  APE_MIN_VERSION = 3800;
  private static final short  APE_MAX_VERSION = 3990;

  private static final short  MAC_FORMAT_FLAG_8_BIT                = 1; // is 8-bit [OBSOLETE]
  private static final short  MAC_FORMAT_FLAG_CRC                  = 2; // uses the new CRC32 error detection [OBSOLETE]
  private static final short  MAC_FORMAT_FLAG_HAS_PEAK_LEVEL       = 4; // uint32 nPeakLevel after the header [OBSOLETE]
  private static final short  MAC_FORMAT_FLAG_24_BIT               = 8; // is 24-bit [OBSOLETE]
  private static final short  MAC_FORMAT_FLAG_HAS_SEEK_ELEMENTS   = 16; // has the number of seek elements after the peak level
  private static final short  MAC_FORMAT_FLAG_CREATE_WAV_HEADER   = 32; // create the wave header on decompression (not stored)

  @Override
  public ApeInfo getInfos(InputStream is, String name) throws AudioInfoException, IOException {
    AudioInputStream ais = new AudioInputStream(is, name);
    try {
      checkHeader(ais);
      short version = ais.read16bitLE(); // 3800, 3990

      // Inspired from https://github.com/FFmpeg/FFmpeg/blob/master/libavformat/ape.c

      short compressionType;
      short formatFlags;
      int blocksPerFrame;
      int finalFrameBlocks;
      int totalFrames;
      short bitsPerSample;
      short numChannels;
      int sampleRate;

      if (version >= 3980) {
        ais.skipNBytesBackport(2); // padding1
        int descriptorLength = ais.read32bitLE();
        ais.skipNBytesBackport(4); // headerLength
        ais.skipNBytesBackport(4); // seekTableLength
        ais.skipNBytesBackport(4); // wavHeaderLength
        ais.skipNBytesBackport(4); // audioDataLength
        ais.skipNBytesBackport(4); // audioDataLengthHigh
        ais.skipNBytesBackport(4); // wavTailLength
        ais.readNBytesStrict(16); // md5

        /* Skip any unknown bytes at the end of the descriptor.
           This is for future compatibility */
        if (descriptorLength > 52) {
          ais.skipNBytesBackport(descriptorLength - 52);
        }

        compressionType = ais.read16bitLE();
        ais.skipNBytesBackport(2); // formatFlags
        blocksPerFrame = ais.read32bitLE();
        finalFrameBlocks = ais.read32bitLE();
        totalFrames = ais.read32bitLE();
        bitsPerSample = ais.read16bitLE();
        numChannels = ais.read16bitLE();
        sampleRate = ais.read32bitLE();
      } else {
        compressionType = ais.read16bitLE();
        formatFlags = ais.read16bitLE();
        numChannels = ais.read16bitLE();
        sampleRate = ais.read32bitLE();
        ais.skipNBytesBackport(4); // wavHeaderLength
        ais.skipNBytesBackport(4); // wavTailLength
        totalFrames = ais.read32bitLE();
        finalFrameBlocks = ais.read32bitLE();

        if ((formatFlags & MAC_FORMAT_FLAG_8_BIT) != 0) {
          bitsPerSample = 8;
        } else if ((formatFlags & MAC_FORMAT_FLAG_24_BIT) != 0) {
          bitsPerSample = 24;
        } else {
          bitsPerSample = 16;
        }

        if (version >= 3950) {
          blocksPerFrame = 73728 * 4;
        } else if (version >= 3900 || (version >= 3800 && compressionType >= 4000)) {
          blocksPerFrame = 73728;
        } else {
          blocksPerFrame = 9216;
        }
      }

      return ApeInfo.builder()
          .name(ais.getName())
          .version(version)
          .compressionType(compressionType)
          .numChannels(numChannels)
          .sampleRate(sampleRate)
          .bitsPerSample(bitsPerSample)
          .blocksPerFrame(blocksPerFrame)
          .finalFrameBlocks(finalFrameBlocks)
          .numFrames(totalFrames)
          .build();
    } catch(AudioFormatException e) {
      throw new AudioInfoException(e);
    } catch (EOFException e) {
      throw new AudioInfoException(ais.getName(), AudioIssue.eof(ais.location(), e));
    }
  }

  private void checkHeader(AudioInputStream ais) throws AudioFormatException, IOException {
    long location = ais.location();
    if (!"MAC ".equals(ais.readString(4))) {
      throw new AudioFormatException(ais.getName(), location, AudioFormat.WAVE, "No MAC header");
    }
  }


  @Getter @Builder
  public static class ApeInfo implements AudioInfo {
    private final String name;
    private final short version;
    private final short compressionType;
    private final short numChannels;
    private final int sampleRate;
    private final short bitsPerSample;
    private final int finalFrameBlocks;
    private final int blocksPerFrame;
    private final int numFrames;

    @Override
    public Duration getDuration() {
      int numSamples = finalFrameBlocks;
      if(numFrames > 1) {
        numSamples += blocksPerFrame * (numFrames-1);
      }
      return Duration.ofMillis(1000L * numSamples / sampleRate);
    }
  }
}
