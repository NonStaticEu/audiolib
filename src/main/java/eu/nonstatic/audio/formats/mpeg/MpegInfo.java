package eu.nonstatic.audio.formats.mpeg;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.AudioIssue.Type;
import eu.nonstatic.audio.formats.AudioInfo;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;

public final class MpegInfo implements AudioInfo {

  @Getter
  private final String name;
  @Getter
  private final AudioFileType type;
  private final Map<Integer, Long> sampleCounts; // samplingRate => samples
  private final List<AudioIssue> audioIssues; // location => bytes skipped
  @Getter
  private final boolean incomplete; // Sync errors don't have any effect on this flag. true if the file unexpectedly reached EOF

  public MpegInfo(String name, AudioFileType type, Map<Integer, Long> sampleCounts, List<AudioIssue> audioIssues) {
    this.name = name;
    this.type = type;
    this.sampleCounts = Collections.unmodifiableMap(sampleCounts);
    this.audioIssues = Collections.unmodifiableList(audioIssues);
    this.incomplete = audioIssues.stream().anyMatch(issue -> Type.EOF.equals(issue.getType()));
  }

  @Override
  public float getSampleRate() {
    return getApproxSampleRate();
  }

  @Override
  public Duration getDuration() {
    double seconds = 0.0;
    for (Entry<Integer, Long> entry : sampleCounts.entrySet()) {
      seconds += entry.getValue() / (double) entry.getKey();
    }
    return AudioInfo.secondsToDuration(seconds);
  }

  public int getApproxSampleRate() {
    return sampleCounts.entrySet()
        .stream().max(Entry.comparingByValue())
        .map(Entry::getKey)
        .orElse(0);
  }

  @Override
  public List<AudioIssue> getIssues() {
    return audioIssues;
  }
}