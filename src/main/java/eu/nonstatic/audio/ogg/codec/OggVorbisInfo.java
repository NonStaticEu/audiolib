/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.ogg.codec;

import eu.nonstatic.audio.ogg.OggCodec;
import eu.nonstatic.audio.ogg.OggInfo;
import java.time.Duration;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

public class OggVorbisInfo extends OggInfo {

  private long bitCount; // channels/bitRate/sampleRate => bits

  @Getter
  private final SamplingDetails samplingDetails;

  public OggVorbisInfo(String name, int serialNumber, SamplingDetails samplingDetails) {
    super(name, serialNumber);
    this.samplingDetails = samplingDetails;
  }

  @Override
  public OggCodec getCodec() {
    return OggCodec.VORBIS;
  }

  protected void appendBytes(int bytes) {
    bitCount += 8L * bytes;
  }

  public boolean isEmpty() {
    return bitCount == 0;
  }

  @Override
  public Duration getDuration() {
    double seconds = (lastGranule - firstGranule) / (double) samplingDetails.sampleRate;
    return Duration.ofNanos(Math.round(seconds * 1_000_000_000.0));
  }

  /**
   * Approximate, as the bitRate is
   */
  public Duration getBitCountDuration() {
    double seconds = bitCount / (double) samplingDetails.bitRate;
    return Duration.ofNanos(Math.round(seconds * 1_000_000_000.0));
  }

  @Override
  public OggVorbisInfo copy() {
    OggVorbisInfo copy = new OggVorbisInfo(getName(), getSerialNumber(), getSamplingDetails());
    copy.bitCount = bitCount;
    into(copy);
    return copy;
  }

  @Builder
  @EqualsAndHashCode
  public static final class SamplingDetails {
    private int version;
    private int numChannels;
    private int bitRate; // just a hint as the doc says
    private int sampleRate;
  }
}
