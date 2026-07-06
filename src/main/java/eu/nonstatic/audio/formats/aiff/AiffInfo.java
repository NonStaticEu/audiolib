package eu.nonstatic.audio.formats.aiff;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.formats.AudioInfo;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class AiffInfo implements AudioInfo {
  private final String name;
  private final short numChannels;
  private final float sampleRate;
  private final short bitsPerSample;
  private final int numFrames;
  private final String compression;
  private final boolean bigEndian;

  @Override
  public AudioFileType getType() {
    return AudioFileType.AIFF;
  }

  @Override
  public Duration getDuration() {
    return Duration.ofMillis(Math.round((numFrames * 1000.0) / sampleRate));
  }
}