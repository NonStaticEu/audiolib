package eu.nonstatic.audio.formats.dts;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.formats.AudioInfo;
import eu.nonstatic.audio.formats.wave.WaveInfo;
import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter @RequiredArgsConstructor
public class DtsInfo implements AudioInfo {
  private final String name;
  private final short waveFormat;
  private final short numChannels;
  private final float sampleRate;
  private final short bitsPerSample;
  private final int audioSize;
  private final Duration duration;

  public DtsInfo(WaveInfo infos) {
    this(infos.getName(), infos.getWaveFormat(), infos.getNumChannels(), infos.getSampleRate(), infos.getBitsPerSample(), infos.getAudioSize(), infos.getDuration());
  }

  @Override
  public AudioFileType getType() {
    return AudioFileType.DTS;
  }
}