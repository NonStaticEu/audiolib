/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.ogg.codec;

import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.formats.ogg.OggCodec;
import eu.nonstatic.audio.formats.ogg.OggInfo;
import java.time.Duration;
import javax.sound.sampled.AudioFormat;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

public class OggVorbisInfo extends OggInfo {

  private static final AudioFormat.Encoding ENCODING = new AudioFormat.Encoding("VORBISENC");

  private long bitCount; // channels/bitRate/sampleRate => bits

  @Getter
  private final SamplingDetails samplingDetails;

  public OggVorbisInfo(String name, int serialNumber, SamplingDetails samplingDetails) {
    super(name, ENCODING, samplingDetails.sampleRate, -1,
            samplingDetails.channels, 1, (float) samplingDetails.bitRate /8,
            false, serialNumber);
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
   * Approximate, as the bitRate is mostly informational
   */
  public Duration getBitCountDuration() {
    double seconds = bitCount / (double) samplingDetails.bitRate;
    return Duration.ofNanos(Math.round(seconds * 1_000_000_000.0));
  }

  @Override
  protected void addIssue(@NonNull AudioIssue issue) {
    super.addIssue(issue);
  }

  @Builder
  @EqualsAndHashCode
  public static final class SamplingDetails {
    private int version;
    private short channels;
    private int bitRate; // just a hint as the doc says
    private int sampleRate;
  }
}
