package eu.nonstatic.audio.formats.wave;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.formats.AudioInfo;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter @Builder
public final class WaveInfo implements AudioInfo {

  private final @NonNull String name;
  private final short waveFormat;
  private final short numChannels;
  private final float sampleRate;
  private final short bitsPerSample;
  private final int audioSize;

  @Override
  public AudioFileType getType() {
    return AudioFileType.WAVE;
  }

  @Override
  public Duration getDuration() {
    return Duration.ofMillis(Math.round((audioSize * 8 * 1000.0) / (numChannels * sampleRate * bitsPerSample)));
  }
}
