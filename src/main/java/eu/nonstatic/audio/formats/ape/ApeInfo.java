package eu.nonstatic.audio.formats.ape;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.formats.AudioInfo;
import java.time.Duration;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class ApeInfo implements AudioInfo {
  private final String name;
  private final short version;
  private final short compressionType;
  private final short numChannels;
  private final float sampleRate;
  private final short bitsPerSample;
  private final int finalFrameBlocks;
  private final int blocksPerFrame;
  private final int numFrames;

  @Override
  public AudioFileType getType() {
    return AudioFileType.APE;
  }

  @Override
  public Duration getDuration() {
    int numSamples = finalFrameBlocks;
    if(numFrames > 1) {
      numSamples += blocksPerFrame * (numFrames-1);
    }
    return Duration.ofNanos((long) (1_000_000_000L * numSamples / sampleRate));
  }
}