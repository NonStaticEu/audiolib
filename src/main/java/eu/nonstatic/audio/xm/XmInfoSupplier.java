package eu.nonstatic.audio.xm;

import eu.nonstatic.audio.AudioFormat;
import eu.nonstatic.audio.AudioFormatException;
import eu.nonstatic.audio.AudioInfoException;
import eu.nonstatic.audio.AudioInfoSupplier;
import eu.nonstatic.audio.AudioInputStream;
import eu.nonstatic.audio.AudioIssue;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XmInfoSupplier implements AudioInfoSupplier<XmInfo> {

  protected static final String XM_HEADER = "Extended Module: ";
  private static final int MODULE_NAME_LENGTH = 20;
  private static final int MODULE_PADDING_VALUE = 0x00;
  private static final int TRACKER_NAME_LENGTH = 20;
  private static final int TRACKER_PADDING_VALUE = 0x20;

  /**
   * <a href="https://github.com/milkytracker/MilkyTracker/blob/master/resources/reference/xm-form.txt">...</a>
   * <a href="https://www.celersms.com/doc/XM_file_format.pdf">...</a>
   * <a href="https://gist.github.com/loveemu/737ace92f08b439a416adc829ae2aa76">...</a>
   * <a href="https://milkytracker.org/docs/FT2.pdf">...</a>
   *
   * Caution: this assumes there is no tempo change along the track
   */
  @Override
  public XmInfo getInfos(InputStream is, String name) throws AudioInfoException, IOException {
    AudioInputStream ais = new AudioInputStream(is, name);
    try {
      return getInfos(ais);
    } catch(AudioFormatException e) {
      throw new AudioInfoException(e);
    } catch(EOFException e){
      throw new AudioInfoException(name, AudioIssue.eof(ais.location(), e));
    }
  }

  private XmInfo getInfos(AudioInputStream ais) throws IOException, AudioFormatException {
    if(!ais.readString(XM_HEADER.length()).equals(XM_HEADER)) {
      throw new AudioFormatException(ais.getName(), MODULE_PADDING_VALUE, AudioFormat.XM, "No XM header");
    }

    String moduleName = readPaddedString(ais, MODULE_NAME_LENGTH, MODULE_PADDING_VALUE);

    long location = ais.location();
    int oneA = ais.readStrict();
    if(oneA != 0x1a) {
      throw new AudioFormatException(ais.getName(), location, AudioFormat.XM, "No 0x1A t pos 20");
    }

    String trackerName = readPaddedString(ais, TRACKER_NAME_LENGTH, TRACKER_PADDING_VALUE); // trackerName
    log.info("Reading module: {} written on {}", moduleName, trackerName);

    short version = ais.read16bitLE();
    if(version < 0x104) {
      throw new AudioFormatException(ais.getName(), location, AudioFormat.XM, "Unsupported XM version: " + Integer.toHexString(version));
    }
    ais.location(); // offsetStart
    ais.read32bitLE(); // headerSize
    short songLength = ais.read16bitLE();
    ais.read16bitLE(); // restartPos
    short channels = ais.read16bitLE();
    ais.read16bitLE(); // patterns
    short instruments = ais.read16bitLE();
    ais.read16bitLE(); // flags
    short tempo = ais.read16bitLE();
    short bpm = ais.read16bitLE();

    return XmInfo.builder()
        .name(moduleName)
        .length(songLength)
        .bpm(bpm)
        .tempo(tempo)
        .numChannels(channels)
        .instruments(instruments)
        .build();
  }


  private static String readPaddedString(AudioInputStream ais, int length, int padding) throws IOException {
    int nameLength;
    byte[] paddedName = ais.readNBytesStrict(length);
    for (nameLength = length; nameLength > MODULE_PADDING_VALUE; nameLength--) {
      if(paddedName[nameLength-1] != padding) {
        break;
      }
    }
    return new String(paddedName, MODULE_PADDING_VALUE, nameLength, StandardCharsets.US_ASCII);
  }
}
