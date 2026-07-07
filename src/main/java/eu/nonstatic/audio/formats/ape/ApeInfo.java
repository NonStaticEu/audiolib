/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.ape;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.formats.AudioInfo;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class ApeInfo implements AudioInfo {
  private final String name;
  private final short version;
  private final short compressionType;
  private final short numChannels;
  private final float sampleRate;
  private final short bitsPerSample;
  private final int finalFrameBlocks;
  private final int blocksPerFrame;
  private final int numFrames;

  @Override
  public AudioFileType getType() {
    return AudioFileType.APE;
  }

  @Override
  public Duration getDuration() {
    int numSamples = finalFrameBlocks;
    if(numFrames > 1) {
      numSamples += blocksPerFrame * (numFrames-1);
    }
    return Duration.ofNanos((long) (1_000_000_000L * numSamples / sampleRate));
  }
}