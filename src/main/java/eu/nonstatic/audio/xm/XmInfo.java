package eu.nonstatic.audio.xm;

import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.AudioInfo;
import java.time.Duration;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class XmInfo implements AudioInfo {

  private static final int TICKS_PER_BPM_PER_MINUTE = 24;
  public static final int LINES_PER_PATTERN = 64;

  private final String name;
  short length; // in patterns
  short tempo; // ticks per pattern line
  short bpm; // there are bpm * 2/5 ticks per second, that is 24*bpm ticks per minute
  short channels;
  short instruments;

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


  @Override
  public List<AudioIssue> getIssues() {
    return List.of();
  }
}
