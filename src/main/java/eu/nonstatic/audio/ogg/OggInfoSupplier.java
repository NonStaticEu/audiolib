/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.ogg;

import eu.nonstatic.audio.AudioFormat;
import eu.nonstatic.audio.AudioFormatException;
import eu.nonstatic.audio.AudioInfoException;
import eu.nonstatic.audio.AudioInfoSupplier;
import eu.nonstatic.audio.AudioInputStream;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.ogg.OggCodec.Type;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OggInfoSupplier implements AudioInfoSupplier<OggInfo> {

  private static final String OGG_PAGE_TAG = "OggS";
  private static final int OGG_PAGE_TAG_LENGTH = OGG_PAGE_TAG.length();
  private static final int OGG_VERSION = 0;

  /**
   * <a href="https://cloudinary.com/guides/video-formats/ogg-format-an-in-depth-look#ogg-2">...</a>
   * <a href="https://xiph.org/ogg/doc/oggstream.html">...</a>
   * <a href="https://xiph.org/vorbis/doc/Vorbis_I_spec.html#x1-630004.2.2">...</a>
   * <a href="https://xiph.org/ogg/doc/rfc3533.txt">...</a>
   * <a href="https://datatracker.ietf.org/doc/html/rfc5334#section-4">...</a>
   *
   * In short:
   * - Audio/video physical streams are made of one or several interleaved logical bitstreams, each split in pages.
   *   - Pages contain a serial number telling which streams it is part of.
   *   - Pages also contain granule pos which tell where we are in the stream.
   *   - Pages are divided in packets
   *     - Packets are either of types data, or, in a vorbis format: ident (sampling rate, channels, bitrate), comment, or "setup"
   *     - There is one ident at the beginning of a stream but when streaming from a radio for example, there will be more
   */
  public OggInfo getInfos(InputStream is, String name) throws AudioInfoException, IOException {
    try {
      OggStreamsInfos streamInfos = getStreamsInfos(is, name);

      OggInfo oggInfo = streamInfos.getOggInfos(Type.AUDIO)
          .values()
          .stream()
          .filter(info -> !info.isEmpty())
          .findFirst()
          .orElseThrow(() -> {
            String message = String.format("Could not find any audio stream containing pages from the %d found ones", streamInfos.size());
            return new AudioFormatException(name, 0, AudioFormat.OGG, message);
          });

      // appending the global issues to a copy
      OggInfo copy = oggInfo.copy();
      copy.audioIssues.addAll(streamInfos.audioIssues);
      return copy;
    } catch(AudioFormatException e) {
      throw new AudioInfoException(e);
    }
  }

  public OggStreamsInfos getStreamsInfos(InputStream is, String name) throws AudioFormatException, IOException {
    AudioInputStream ais = new AudioInputStream(is, name);
    OggStreamsInfos streamsInfos = new OggStreamsInfos();
    while(!readPagesWithResync(ais, streamsInfos));
    streamsInfos.incomplete = streamsInfos.incomplete || streamsInfos.stream().anyMatch(OggInfo::isIncomplete);
    if (streamsInfos.isEmpty()) {
      throw new AudioFormatException(name, 0, AudioFormat.OGG, "Could not find a single page");
    }
    return streamsInfos;
  }

  private boolean readPagesWithResync(AudioInputStream ais, OggStreamsInfos streamsInfos) throws AudioFormatException, IOException {
    boolean stop = false;

    try {
      readPages(ais, streamsInfos); // reads till a page is malformed, or EOF

      if (isEndOfFileAhead(ais)) {
        stop = true;
      } else {
        long locationBeforeResync = ais.location();
        int skipped = findPage(ais);
        if(skipped >= 0) {
          streamsInfos.addIssue(AudioIssue.sync(locationBeforeResync, skipped));
          log.info("Resync after skipping {} bytes", skipped);
        } else {
          stop = true;
        }
      }
    } catch(EOFException e) { // either from a page decoding or from a resync
      stop = true;

      log.warn("End of file reached, incomplete page: {}", ais.getName());
      streamsInfos.incomplete = true;
      streamsInfos.addIssue(AudioIssue.eof(ais.location(), e));
    }
    return stop;
  }

  private void readPages(AudioInputStream ais, OggStreamsInfos streamsInfos) throws AudioFormatException, IOException {
    try {
      while (readPage(ais, streamsInfos)) {
        /* nothing */
      }
    } catch (MalformedPageException e) {
      log.warn("Page is malformed at {}, will seek till next one", e.getLocation());
      streamsInfos.addIssue(AudioIssue.format(e));
    } catch (MalformedPacketException e) {
      long location = e.getLocation();
      OggInfo oggInfo = streamsInfos.get(e.getSerialNumber());
      if(oggInfo != null) {
        log.warn("Packet is malformed at {}, will seek till next one", location);
        oggInfo.addIssue(AudioIssue.format(e));
      } else { // problem during bos reading, or encountered serial not declared in any bos
        streamsInfos.addIssue(AudioIssue.other(location, e));
        throw new AudioFormatException(ais.getName(), location, AudioFormat.OGG, "serialNumber-bos issue", e);
      }
    }
  }

  /**
   * @param ais
   * @param streamsInfos
   * @return false if there is not a page at the stream's location
   * @throws UnsupportedCodecException
   * @throws IOException read error or EOF while skipping over a page
   */
  private boolean readPage(AudioInputStream ais, OggStreamsInfos streamsInfos)
      throws MalformedPageException, MalformedPacketException, UnsupportedCodecException, IOException {
    ais.mark(OGG_PAGE_TAG_LENGTH);

    try {
      String pattern = readCapturePattern(ais);
      if (!isOggCapturePattern(pattern)) {
        ais.reset();
        return false;
      }
    } catch(EOFException e) { // Means we reached the EOF just after the end of the previous page. Our job is done.
      return false;
    }

    long location = ais.location();
    int oggVersion = ais.readStrict();
    if(oggVersion != OGG_VERSION) {
      throw new MalformedPageException(ais.getName(), location, "Unrecognized Ogg version: " + oggVersion);
    }
    int headerType = ais.readStrict();

    OggPage page = OggPage.builder()
        .oggVersion(oggVersion)
        .headerType(headerType)
        .freshPacket((headerType & 0x1) == 0)
        .firstPage((headerType & 0x2) != 0) // bos: beginning of stream, means we're expecting an ident packet next
        .lastPage((headerType & 0x4) != 0) // eos: end of stream, means there shouldn't be any page after it

        .granulePos(ais.read64bitLE())
        .serialNumber(ais.read32bitLE())

        .pageSeqNumber(ais.read32bitLE())
        .checksum(ais.read32bitLE())
        .build();


    Iterator<OggPage.PacketSegment> segmentIterator = readPacketSizes(ais).iterator();
    int serialNumber = page.serialNumber;
    OggCodecReader reader;
    OggInfo oggInfo;
    if(page.firstPage) {
      if (streamsInfos.contains(serialNumber)) {
        throw new MalformedPacketException(ais.getName(), location, serialNumber, "Serial Number declared twice: " + serialNumber);
      }
      reader = OggCodecReaders.get(ais, serialNumber);
      log.info("Stream with serial {} uses the {} codec", serialNumber, reader.getCodec());
      oggInfo = reader.readBos(ais, serialNumber, segmentIterator.next());
      streamsInfos.put(serialNumber, oggInfo);
    } else {
      oggInfo = streamsInfos.get(serialNumber);
      if(oggInfo != null) {
        reader = OggCodecReaders.get(oggInfo.getCodec());
      } else { // if someone forged a stream with a serialNumber that isn't bound to a bos
        throw new MalformedPacketException(ais.getName(), location, serialNumber, "Undeclared serial number: " + serialNumber);
      }
    }

    oggInfo.updateGranulePos(page.granulePos);

    try {
      while (segmentIterator.hasNext()) {
        reader.readPacket(ais, oggInfo, page, segmentIterator.next());
      }
      if (page.lastPage) { // create a reader.readEos method if needed
        oggInfo.incomplete = false;
      }
    } catch(EOFException e) {
      oggInfo.incomplete = true;
      throw e;
    }
    return true;
  }

  private static List<OggPage.PacketSegment> readPacketSizes(AudioInputStream ais) throws IOException {
    int pageSegments = ais.readStrict();
    List<OggPage.PacketSegment> segments = new ArrayList<>();
    int packetSize = 0;
    int lacingValue;
    for (int i = 0; i < pageSegments; i++) {
      lacingValue = ais.readStrict();
      packetSize += lacingValue;
      if(lacingValue < 255) { // packet lacing completed
        segments.add(new OggPage.PacketSegment(packetSize, true));
        packetSize = 0;
      }
    }
    if(packetSize > 0) {
      segments.add(new OggPage.PacketSegment(packetSize, false));
    }
    return segments;
  }

  private static String readCapturePattern(AudioInputStream ais) throws IOException {
    return ais.readString(OGG_PAGE_TAG_LENGTH);
  }

  private boolean isEndOfFileAhead(AudioInputStream ais) throws IOException {
    return ais.available() == 0;
  }

  private int findPage(AudioInputStream ais) throws IOException {
    for(int skipped = 0; ; skipped++) {
      ais.mark(OGG_PAGE_TAG_LENGTH);
      String pattern = readCapturePattern(ais);
      if (isOggCapturePattern(pattern)) {
        ais.reset();
        return skipped;
      } else {
        ais.skipNBytesBackport(1);
      }
    }
  }


  private boolean isOggCapturePattern(String header) {
    return OGG_PAGE_TAG.equals(header);
  }



  @Getter @Builder
  public static final class OggPage {

    final int oggVersion;
    final int headerType;
    final boolean freshPacket;
    final boolean firstPage;
    final boolean lastPage;

    final long granulePos;
    final int serialNumber;
    final int pageSeqNumber;
    final int checksum;
    final List<PacketSegment> segments;


    @Getter
    @AllArgsConstructor
    public static final class PacketSegment {

      final int size;
      final boolean finished;
    }
  }


  public static final class MalformedPageException extends AudioFormatException {
    public MalformedPageException(String name, long location, String message) {
      super(name, location, AudioFormat.OGG, message);
    }
  }

  @Getter
  public static final class MalformedPacketException extends AudioFormatException {
    private final int serialNumber;
    public MalformedPacketException(String name, long location, int serialNumber, String message) {
      super(name, location, AudioFormat.OGG, message);
      this.serialNumber = serialNumber;
    }
  }

  @Getter
  public static final class UnsupportedCodecException extends AudioFormatException {
    private final int serialNumber;
    public UnsupportedCodecException(String name, long location, int serialNumber, String message) {
      super(name, location, AudioFormat.OGG, message);
      this.serialNumber = serialNumber;
    }
  }
}
