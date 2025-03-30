package eu.nonstatic.audio.xm;

import eu.nonstatic.audio.AudioFormat;
import eu.nonstatic.audio.AudioInfo;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class XmInfo implements AudioInfo {

  private static final int TICKS_PER_BPM_PER_MINUTE = 24;
  public static final int LINES_PER_PATTERN = 64;

  private final String name;
  private final String tracker;
  private final short length; // in patterns
  private final short tempo; // ticks per pattern line
  private final short bpm; // there are bpm * 2/5 ticks per second, that is 24*bpm ticks per minute
  private final short numChannels;
  private final short instruments;

  @Override
  public AudioFormat getFormat() {
    return AudioFormat.XM;
  }

  @Override
  public Duration getDuration() {
    double nanos = (getSongTicks() * ((double)SECONDS_PER_MINUTE * NANOS_PER_SECOND)) / getTicksPerMinute();
    return Duration.ofNanos(Math.round(nanos));
  }

  public int getSongTicks() {
    return length * LINES_PER_PATTERN * tempo;
  }

  public int getTicksPerMinute() {
    return bpm * TICKS_PER_BPM_PER_MINUTE;
  }
}
