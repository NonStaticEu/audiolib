package eu.nonstatic.audio.formats.flac;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.formats.AudioInfo;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class FlacInfo implements AudioInfo {
  private final String name;
  private final short numChannels;
  private final float sampleRate;
  private final short bitsPerSample;
  private final long numFrames;

  @Override
  public AudioFileType getType() {
    return AudioFileType.FLAC;
  }

  @Override
  public Duration getDuration() {
    return Duration.ofMillis(Math.round((numFrames * 1000.0) / sampleRate));
  }
}