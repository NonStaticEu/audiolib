/**
 * Audiolib
 * Copyright (C) 2025 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.ape;

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
      short sampleSizeInBits;
      short numChannels;
      int sampleRate;

      if (version >= 3980) {
        ais.skipNBytes(2); // padding1
        int descriptorLength = ais.read32bitLE();
        ais.skipNBytes(4); // headerLength
        ais.skipNBytes(4); // seekTableLength
        ais.skipNBytes(4); // wavHeaderLength
        ais.skipNBytes(4); // audioDataLength
        ais.skipNBytes(4); // audioDataLengthHigh
        ais.skipNBytes(4); // wavTailLength
        ais.readNBytesStrict(16); // md5

        /* Skip any unknown bytes at the end of the descriptor.
           This is for future compatibility */
        if (descriptorLength > 52) {
          ais.skipNBytes(descriptorLength - 52L);
        }

        compressionType = ais.read16bitLE();
        ais.skipNBytes(2); // formatFlags
        blocksPerFrame = ais.read32bitLE();
        finalFrameBlocks = ais.read32bitLE();
        totalFrames = ais.read32bitLE();
        sampleSizeInBits = ais.read16bitLE();
        numChannels = ais.read16bitLE();
        sampleRate = ais.read32bitLE();
      } else {
        compressionType = ais.read16bitLE();
        formatFlags = ais.read16bitLE();
        numChannels = ais.read16bitLE();
        sampleRate = ais.read32bitLE();
        ais.skipNBytes(4); // wavHeaderLength
        ais.skipNBytes(4); // wavTailLength
        totalFrames = ais.read32bitLE();
        finalFrameBlocks = ais.read32bitLE();

        if ((formatFlags & MAC_FORMAT_FLAG_8_BIT) != 0) {
          sampleSizeInBits = 8;
        } else if ((formatFlags & MAC_FORMAT_FLAG_24_BIT) != 0) {
          sampleSizeInBits = 24;
        } else {
          sampleSizeInBits = 16;
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
          .channels(numChannels)
          .sampleRate(sampleRate)
          .sampleSizeInBits(sampleSizeInBits)
          .blocksPerFrame(blocksPerFrame)
          .finalFrameBlocks(finalFrameBlocks)
          .frameCount(totalFrames)
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
      throw new AudioFormatException(ais.getName(), location, AudioFileType.WAVE, "No MAC header");
    }
  }
}
