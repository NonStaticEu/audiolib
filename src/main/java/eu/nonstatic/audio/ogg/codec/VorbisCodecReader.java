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

import eu.nonstatic.audio.AudioInputStream;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.ogg.OggCodec;
import eu.nonstatic.audio.ogg.OggCodecReader;
import eu.nonstatic.audio.ogg.OggInfoSupplier.MalformedPacketException;
import eu.nonstatic.audio.ogg.OggInfoSupplier.OggPage;
import eu.nonstatic.audio.ogg.OggInfoSupplier.UnsupportedCodecException;
import eu.nonstatic.audio.ogg.codec.OggVorbisInfo.SamplingDetails;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class VorbisCodecReader implements OggCodecReader<OggVorbisInfo> {

  private static final int PACKET_TYPE_DATA_MASK = 0x1;
  private static final int PACKET_TYPE_IDENT =   1<<0 | PACKET_TYPE_DATA_MASK; // 1
  private static final int PACKET_TYPE_COMMENT = 1<<1 | PACKET_TYPE_DATA_MASK; // 3
  private static final int PACKET_TYPE_SETUP =   1<<2 | PACKET_TYPE_DATA_MASK; // 5

  public static final String VORBIS_MAGIC = "vorbis";
  private static final byte[] VORBIS_MAGIC_BYTES = VORBIS_MAGIC.getBytes(StandardCharsets.US_ASCII);

  private static final int PACKET_TYPE_LENGTH = 1;
  private static final int HEADER_LENGTH = PACKET_TYPE_LENGTH + VORBIS_MAGIC_BYTES.length;
  private static final byte[] HEADER_BYTES = ByteBuffer.allocate(HEADER_LENGTH)
      .put((byte)PACKET_TYPE_IDENT)
      .put(VORBIS_MAGIC_BYTES)
      .array();

  @Override
  public OggCodec getCodec() {
    return OggCodec.VORBIS;
  }

  /**
   * Contains the ident packet type followed by the vorbis magic value, as per the RFC 5334
   */
  @Override
  public byte[] getHeader() {
    return HEADER_BYTES;
  }

  /**
   * Max of ident lengths here: <a href="https://datatracker.ietf.org/doc/html/rfc5334#page-3">...</a>
   * and <a href="https://datatracker.ietf.org/doc/html/rfc5334#ref-Codecs">...</a>
   */
  @Override
  public OggVorbisInfo readBos(AudioInputStream ais, int serialNumber, OggPage.PacketSegment segment) throws UnsupportedCodecException, IOException {
    AudioIssue sizeIssue = null;
    if(segment.getSize() != 23) {
       sizeIssue = AudioIssue.other(ais.location(), Map.of(META_BOS_SEGMENT_SIZE, segment.getSize()));
      // Just a warning. Maybe things will work out in the end
    }
    SamplingDetails samplingDetails = readIdentificationPacket(ais, serialNumber, segment);
    OggVorbisInfo vorbisInfo = new OggVorbisInfo(ais.getName(), serialNumber, samplingDetails);

    if(sizeIssue != null) {
      vorbisInfo.addIssue(sizeIssue);
    }
    return vorbisInfo;
  }

  @Override
  public void readPacket(AudioInputStream ais, OggVorbisInfo info, OggPage page, OggPage.PacketSegment segment) throws MalformedPacketException, IOException {
    long location = ais.location();
    int packetType = ais.readStrict();

    if((packetType & PACKET_TYPE_DATA_MASK) == 0) {
      readDataPacket(ais, info, segment);
    } else {
      // A comment or setup packet follow an identification packet, we are not checking here if they are all present or in order
      checkVorbisMagic(ais, info.getSerialNumber());
      switch(packetType) {
        case PACKET_TYPE_IDENT:
          throw new IllegalStateException("Ident packet already exists");
        case PACKET_TYPE_COMMENT:
          readCommentPacket(ais, segment);
          break;
        case PACKET_TYPE_SETUP:
          readSetupPacket(ais, segment);
          break;
        default:
          throw new MalformedPacketException(ais.getName(), location, info.getSerialNumber(), "unknown packet type: " + packetType);
      }
    }
  }


  private void readDataPacket(AudioInputStream ais, OggVorbisInfo info, OggPage.PacketSegment segment) throws IOException {
    // TODO for more duration accuracy we could use blocksize mumbo jumbo https://xiph.org/vorbis/doc/Vorbis_I_spec.html#x1-720004.3
    int dataLength = segment.getSize() - PACKET_TYPE_LENGTH;
    ais.skipNBytesBackport(dataLength);
    info.appendBytes(dataLength);
  }

  private static void checkVorbisMagic(AudioInputStream ais, int serialNumber) throws IOException, MalformedPacketException {
    long location = ais.location();
    byte[] header = ais.readNBytes(VORBIS_MAGIC_BYTES.length);
    if(!Arrays.equals(VORBIS_MAGIC_BYTES, header)) {
      throw new MalformedPacketException(ais.getName(), location, serialNumber, "No vorbis tag");
    }
  }

  /**
   * Caution: https://xiph.org/vorbis/doc/Vorbis_I_spec.html#x1-630004.2.2
   * "The bitrate fields are used only as hints.
   * The nominal bitrate field especially may be considerably off in purely VBR streams."
   */
  private SamplingDetails readIdentificationPacket(AudioInputStream ais, int serialNumber, OggPage.PacketSegment segment)
      throws UnsupportedCodecException, IOException {
    long location = ais.location();
    int version = ais.read32bitLE();
    if(version != 0) {
      throw new UnsupportedCodecException(ais.getName(), location, serialNumber, "Unsupported Vorbis version: " + version);
    }
    short numChannels = (short) ais.readStrict();
    int sampleRate = ais.read32bitLE();
    int bitRate = computeBitRate(ais.read32bitLE(), ais.read32bitLE(), ais.read32bitLE());
    readBlockSizes(ais.readStrict()); // blockSizes
    ais.readStrict(); // framingFlag

    return SamplingDetails.builder()
        .version(version)
        .numChannels(numChannels)
        .sampleRate(sampleRate)
        .bitRate(bitRate)
        .build();
  }

  /**
   * All three fields set to the same value implies a fixed rate, or tightly bounded, nearly fixed-rate bitstream
   * Only nominal set implies a VBR or ABR stream that averages the nominal bitrate
   * Maximum and or minimum set implies a VBR bitstream that obeys the bitrate limits
   * None set indicates the encoder does not care to speculate.
   */
  private static int computeBitRate(int max, int nominal, int min) {
    if(nominal != 0) {
      return nominal;
    } else if(max == 0) {
      return min; // if zero, we'll have zero as an unknown bitrate
    } else {
      return max; // if both min and max are non-zero, then let's use max
    }
  }

  private static int[] readBlockSizes(int blockSizes) {
    return new int[]{ blockSizes & 0x0f, blockSizes & 0xf0 };
  }

  private static void readCommentPacket(AudioInputStream ais, OggPage.PacketSegment segment) throws IOException {
    long startLocation = ais.location() - HEADER_LENGTH;

    int vendorLength = ais.read32bitLE();
    ais.skipNBytesBackport(vendorLength); // vendor
    int commentCount = ais.read32bitLE();
    for (int i = 0; i < commentCount; i++) {
      int commentLength = ais.read32bitLE();
      ais.skipNBytesBackport(commentLength); // comment
    }
    ais.readStrict(); // framingFlag

    ais.skipNBytesBackport(segment.getSize() - (ais.location() - startLocation));
  }

  private void readSetupPacket(AudioInputStream ais,OggPage.PacketSegment segment) throws IOException {
    ais.skipNBytesBackport((long)segment.getSize() - HEADER_LENGTH);
  }
}
