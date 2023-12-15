/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.mpeg;

import static java.util.Map.entry;

import eu.nonstatic.audio.AudioFormat;
import eu.nonstatic.audio.AudioFormatException;
import eu.nonstatic.audio.AudioInfo;
import eu.nonstatic.audio.AudioInfoException;
import eu.nonstatic.audio.AudioInfoSupplier;
import eu.nonstatic.audio.AudioInputStream;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.mpeg.MpegAudioInfoSupplier.MpegInfo;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class MpegAudioInfoSupplier implements AudioInfoSupplier<MpegInfo> {

  private static final int MPEG_VERSION_2_5 = 0;
  private static final int MPEG_VERSION_2 = 2;
  private static final int MPEG_VERSION_1 = 3;

  private static final int MPEG_LAYER_I = 3;
  private static final int MPEG_LAYER_II = 2;
  private static final int MPEG_LAYER_III = 1;

  private static final int MODE_STEREO = 0;
  private static final int MODE_JOINT_STEREO = 1;
  private static final int MODE_DUAL_CHANNEL = 2;
  private static final int MODE_MONO = 3;

  // pretty much the same as https://bitbucket.org/ijabz/jaudiotagger/src/master/src/org/jaudiotagger/audio/mp3/MPEGFrameHeader.java
  private static final Map<Integer, Integer> MPEG_BIT_RATE_MAP = Map.ofEntries(
      // MPEG-1, Layer I (E)
      entry(0x1E, 32),
      entry(0x2E, 64),
      entry(0x3E, 96),
      entry(0x4E, 128),
      entry(0x5E, 160),
      entry(0x6E, 192),
      entry(0x7E, 224),
      entry(0x8E, 256),
      entry(0x9E, 288),
      entry(0xAE, 320),
      entry(0xBE, 352),
      entry(0xCE, 384),
      entry(0xDE, 416),
      entry(0xEE, 448),
      // MPEG-1, Layer II (C)
      entry(0x1C, 32),
      entry(0x2C, 48),
      entry(0x3C, 56),
      entry(0x4C, 64),
      entry(0x5C, 80),
      entry(0x6C, 96),
      entry(0x7C, 112),
      entry(0x8C, 128),
      entry(0x9C, 160),
      entry(0xAC, 192),
      entry(0xBC, 224),
      entry(0xCC, 256),
      entry(0xDC, 320),
      entry(0xEC, 384),
      // MPEG-1, Layer III (A)
      entry(0x1A, 32),
      entry(0x2A, 40),
      entry(0x3A, 48),
      entry(0x4A, 56),
      entry(0x5A, 64),
      entry(0x6A, 80),
      entry(0x7A, 96),
      entry(0x8A, 112),
      entry(0x9A, 128),
      entry(0xAA, 160),
      entry(0xBA, 192),
      entry(0xCA, 224),
      entry(0xDA, 256),
      entry(0xEA, 320),
      // MPEG-2, Layer I (6)
      entry(0x16, 32),
      entry(0x26, 48),
      entry(0x36, 56),
      entry(0x46, 64),
      entry(0x56, 80),
      entry(0x66, 96),
      entry(0x76, 112),
      entry(0x86, 128),
      entry(0x96, 144),
      entry(0xA6, 160),
      entry(0xB6, 176),
      entry(0xC6, 192),
      entry(0xD6, 224),
      entry(0xE6, 256),
      // MPEG-2, Layer II (4)
      entry(0x14, 8),
      entry(0x24, 16),
      entry(0x34, 24),
      entry(0x44, 32),
      entry(0x54, 40),
      entry(0x64, 48),
      entry(0x74, 56),
      entry(0x84, 64),
      entry(0x94, 80),
      entry(0xA4, 96),
      entry(0xB4, 112),
      entry(0xC4, 128),
      entry(0xD4, 144),
      entry(0xE4, 160),
      // MPEG-2, Layer III (2)
      entry(0x12, 8),
      entry(0x22, 16),
      entry(0x32, 24),
      entry(0x42, 32),
      entry(0x52, 40),
      entry(0x62, 48),
      entry(0x72, 56),
      entry(0x82, 64),
      entry(0x92, 80),
      entry(0xA2, 96),
      entry(0xB2, 112),
      entry(0xC2, 128),
      entry(0xD2, 144),
      entry(0xE2, 160)
  );

  private static final Map<Integer, Integer> MPEG_SAMPLING_V1_MAP = Map.of(
      0, 44100,
      1, 48000,
      2, 32000
  );
  private static final Map<Integer, Integer> MPEG_SAMPLING_V2_MAP = Map.of(
      0, 22050,
      1, 24000,
      2, 16000
  );
  private static final Map<Integer, Integer> MPEG_SAMPLING_V25_MAP = Map.of(
      0, 11025,
      1, 12000,
      2, 8000
  );

  private static final Map<Integer, Map<Integer, Integer>> MPEG_SAMPLING_RATE_MAP = Map.of(
      MPEG_VERSION_1, MPEG_SAMPLING_V1_MAP,
      MPEG_VERSION_2, MPEG_SAMPLING_V2_MAP,
      MPEG_VERSION_2_5, MPEG_SAMPLING_V25_MAP
  );

  private static final Map<Integer, Integer> MPEG_MODE_CHANNEL_MAP = Map.of(
      MODE_STEREO, 2,
      MODE_JOINT_STEREO, 2,
      MODE_DUAL_CHANNEL, 2,
      MODE_MONO, 1
  );

  private static final int LAYER_I_SAMPLES_PER_FRAME = 384;
  private static final int LAYER_II_OR_III_SAMPLES_PER_FRAME = 1152;


  private final AudioFormat format;

  protected MpegAudioInfoSupplier(AudioFormat format) {
    this.format = format;
  }

  /**
   * https://mutagen-specs.readthedocs.io/en/latest/id3/id3v2.4.0-structure.html
   * http://www.datavoyage.com/mpgscript/mpeghdr.htm
   * https://phoxis.org/2010/05/08/synch-safe/
   * Lyrics custom tag: found some intel in the code: https://sourceforge.net/projects/mp3diags/
   * Example: LYRICSBEGININD0000200ETT00040Jam & Spoon - Tripomatic Fairytales 2002CRC0000835FAE1F2000085LYRICS200
   * https://en.wikipedia.org/wiki/APE_tag
   * We're assuming there is no weird sync/alignment issue
   */
  public MpegInfo getInfos(InputStream is, String name) throws AudioFormatException, AudioInfoException, IOException {

    AudioInputStream ais = new AudioInputStream(is, name);
    try {
      findData(ais);
      skipID3v2(ais);
    } catch (EOFException e) {
      throw new AudioInfoException(name, AudioIssue.eof(ais.location(), e));
    }
    // Let it fail as an IOException as long as we haven't reached frames

    long framesLocation = ais.location();
    MpegInfo info = new MpegInfo(name);
    while(!readFramesWithResync(ais, info));

    if (info.isEmpty()) {
      throw new AudioFormatException(name, framesLocation, format, "Could not find a single frame");
    }
    return info;
  }

  private void findData(AudioInputStream ais) throws IOException {
    do {
      ais.mark(1);
    } while(ais.readStrict() == 0x00);
    ais.reset();
  }


  void skipID3v2(AudioInputStream ais) throws IOException {
    ais.mark(3);
    String tag2 = ais.readString(3);
    if ("ID3".equals(tag2)) {
      ais.readStrict(); // minorVersion
      ais.readStrict(); // revVersion
      int flags = ais.readStrict();
      // byte length of the extended header, the padding and the frames after desynchronisation.
      // If a footer is present this equals to (‘total size’ - 20) bytes, otherwise (‘total size’ - 10) bytes.
      int size = read32bitSynchSafe(ais);
      // (flags & 0x2) != 0; // extended
      boolean footer = (flags & 0x8) != 0;
      ais.skipNBytesBackport((long)size + (footer ? 10 : 0));
    } else { // no ID3v2
      ais.reset();
    }
  }

  private boolean readFramesWithResync(AudioInputStream ais, MpegInfo info) throws IOException {
    boolean stop = false;

    try {
      readFrames(ais, info); // reads till a frame is malformed, or EOF

      if (isEndOfFileAhead(ais)) {
        stop = true;
      } else {
        long locationBeforeResync = ais.location();
        int skipped = resync(ais);
        if(skipped >= 0) {
          info.addIssue(AudioIssue.sync(locationBeforeResync, skipped));
          log.info("Resync after skipping {} bytes", skipped);
        } else {
          stop = true;
        }
      }
    } catch(EOFException e) {
      stop = true;

      log.warn("End of file reached, incomplete frame: {}", ais.getName());
      info.incomplete = true;
      info.addIssue(AudioIssue.eof(ais.location(), e));
    }

    return stop;
  }

  private void readFrames(AudioInputStream ais, MpegInfo info) throws IOException {
    try {
      FrameDetails frameDetails;
      while ((frameDetails = readFrame(ais)) != null) {
        info.appendFrame(frameDetails);
      }
    } catch(MalformedFrameException e) {
      long location = e.getLocation();
      log.warn("Frame is malformed at {}, will seek till next one", location);
      info.addIssue(AudioIssue.format(location, e));
    }
  }

  /**
   * @param ais
   * @return the frame details or null if what's read looks like no frame
   * @throws MalformedFrameException when the frame header looks unhealthy
   * @throws IOException read error or EOF while skipping over a frame
   */
  FrameDetails readFrame(AudioInputStream ais) throws MalformedFrameException, IOException {
    ais.mark(4);

    long headerLocation = ais.location();
    int header;
    try {
      header = readHeader(ais);
      if (!isMpegFrame(header)) {
        ais.reset();
        return null;
      }
    } catch(EOFException e) { // Means we reached the EOF just after the end of the previous frame. Our job is done.
      return null;
    }

    FrameDetails details = new FrameDetails();

    details.version = (header >> 19) & 0x3; // 00: MPEG Version 2.5, 01: reserved, 10: MPEG Version 2 (ISO/IEC 13818-3), 11: MPEG Version 1 (ISO/IEC 11172-3)
    details.layer = (header >> 17) & 0x3; // 00: reserved, 01: Layer III, 10: Layer II, 11: Layer I
    // ((header >> 16) & 0x1) == 0; // protection
    int samplingIndex = ((header >> 10) & 0x3);
    int padding = ((header >> 9) & 0x1);
    int channelIndex = ((header >> 6) & 0x3); // 00: Stereo, 01: Joint stereo (Stereo), 10: Dual channel (Stereo), 11: Single channel (Mono)
    details.numChannels = Optional.ofNullable(MPEG_MODE_CHANNEL_MAP.get(channelIndex))
        .orElseThrow(() -> new MalformedFrameException(ais.getName(), headerLocation, "Cannot compute channel number"));

    int bitRateKey = ((header >> 16) & 0x0E) | ((header >> 8) & 0xF0);
    Integer bitRate = MPEG_BIT_RATE_MAP.get(bitRateKey);
    if (bitRate == null) { // free
      int bitRateIndex = ((header >> 12) & 0xF);
      throw new MalformedFrameException(ais.getName(), headerLocation, "Cannot handle bitrate for index " + Integer.toHexString(bitRateIndex));
    }
    details.bitRate = bitRate;

    Integer sampleRate = Optional.ofNullable(MPEG_SAMPLING_RATE_MAP.get(details.version))
        .map(map -> map.get(samplingIndex))
        .orElseThrow(() -> new MalformedFrameException(ais.getName(), headerLocation, "Cannot compute sampling rate"));
    if(sampleRate == null) {
      throw new MalformedFrameException(ais.getName(), headerLocation, "Cannot handle sampling for index " + Integer.toHexString(samplingIndex));
    }
    details.sampleRate = sampleRate;

    switch (details.layer) {
      case MPEG_LAYER_I:
        details.frameLength = ((12 * bitRate * 1000) / sampleRate + padding) * 4;
        details.sampleCount = LAYER_I_SAMPLES_PER_FRAME;
        break;
      case MPEG_LAYER_II:
      case MPEG_LAYER_III:
        details.frameLength = (144 * bitRate * 1000) / sampleRate + padding;
        details.sampleCount = LAYER_II_OR_III_SAMPLES_PER_FRAME;
        break;
      default:
        throw new MalformedFrameException(ais.getName(), headerLocation, "Layer 0x00");
    }

    //No, we're not going to retry and get as much data as possible in case of an EOF
    ais.skipNBytesBackport((long)details.frameLength - 4); // 4 is the header we've already read
    return details;
  }

  private static int readHeader(AudioInputStream ais) throws IOException {
    return ais.read32bitBE();
  }

  private int resync(AudioInputStream ais) throws IOException {
    for(int skipped = 0; ; skipped++) {
      ais.mark(4);
      int header = readHeader(ais);
      ais.reset();
      if (isMpegFrame(header)) {
        return skipped;
      } else {
        ais.skipNBytesBackport(1);
      }
    }
  }


  /**
   * Checks if the bits 21-31 are set
   */
  private static boolean isMpegFrame(int header) {
    return (header & 0xFFE00000) == 0xFFE00000;
  }

  private static boolean isEndOfFileAhead(AudioInputStream ais) throws IOException {
    if (ais.available() >= 8) {
      String tag = ais.readString(8);
      return tag.startsWith("TAG") // ID3v1 tag
          || tag.startsWith("LYRICSBE") // seems to be a LYRIGSBEGIN sequence with 3-char tags followed by 5-char sizes (in ascii!) and finishing in LYRICS200
          || tag.startsWith("APETAGEX");
    } else {
      return true;
    }
  }

  private int read32bitSynchSafe(AudioInputStream ais) throws IOException {
    return read32bitSynchSafeInt(ais.readNBytesStrict(4));
  }

  private static int read32bitSynchSafeInt(byte[] bytes) {
    return (((bytes[0] << 7 | (bytes[1]&0xff)) << 7) | (bytes[2]&0xff)) << 7 | (bytes[3]&0xff);
  }

  protected static byte[] toSynchSafeBytes(int i) {
    byte b0 = (byte)(i >> 21);
    byte b1 = (byte)(i >> 14 & 0xff);
    byte b2 = (byte)(i >> 7 & 0xff);
    byte b3 = (byte)(i & 0xff);
    return new byte[]{b0, b1, b2, b3};
  }

  static final class FrameDetails {
    int version;
    int layer;
    int numChannels;
    int bitRate;
    int sampleRate;
    int sampleCount;
    int frameLength;
  }

  public static final class MpegInfo implements AudioInfo {
    @Getter
    private final String name;
    private final Map<Integer, Long> sampleCounts = new HashMap<>(); // samplingRate => samples
    private final List<AudioIssue> audioIssues = new ArrayList<>(); // location => bytes skipped
    private boolean incomplete;

    public MpegInfo(String name) {
      this.name = name;
    }

    private void appendFrame(FrameDetails details) {
      sampleCounts.compute(details.sampleRate, (sampleRate, sampleCount) -> (sampleCount == null ? 0 : sampleCount) + details.sampleCount);
    }

    public boolean isEmpty() {
      return sampleCounts.isEmpty();
    }

    /**
     * Sync errors don't have any effect on this flag
     * @return true if the file unexpectedly reached EOF
     */
    public boolean isIncomplete() {
      return incomplete;
    }

    @Override
    public Duration getDuration() {
      double seconds = 0.0;
      for (Entry<Integer, Long> entry : sampleCounts.entrySet()) {
        seconds += entry.getValue() / (double) entry.getKey();
      }
      return AudioInfo.secondsToDuration(seconds);
    }

    public List<AudioIssue> getIssues() {
      return Collections.unmodifiableList(audioIssues);
    }

    public void addIssue(@NonNull AudioIssue issue) {
      audioIssues.add(issue);
    }
  }


  public final class MalformedFrameException extends AudioFormatException {
    public MalformedFrameException(String name, long location, String message) {
      super(name, location, format, message);
    }
  }
}
