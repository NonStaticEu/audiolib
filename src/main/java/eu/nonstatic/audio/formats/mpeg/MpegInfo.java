/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.mpeg;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.AudioIssue.Type;
import eu.nonstatic.audio.formats.AudioFormatEx;
import eu.nonstatic.audio.formats.AudioInfo;
import lombok.AccessLevel;
import lombok.Getter;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class MpegInfo extends AudioFormatEx {

  @Getter
  private final String name;
  @Getter
  private final AudioFileType type;
  @Getter(AccessLevel.PACKAGE)
  private final Map<Short, Integer> channelsCount; // numChannels => frames
  @Getter(AccessLevel.PACKAGE)
  private final Map<Integer, Long> sampleCounts; // samplingRate => samples
  @Getter
  private final boolean incomplete; // Sync errors don't have any effect on this flag. true if the file unexpectedly reached EOF
  private final List<AudioIssue> issues;

  public MpegInfo(String name, AudioFileType type, Map<Short, Integer> channelsCount, Map<Integer, Long> sampleCounts, List<AudioIssue> issues) {
    super(new Encoding(type.name()), approxSampleRate(sampleCounts),
            -1, approxNumChannels(channelsCount), -1, frameRate(channelsCount, sampleCounts), true);
    this.name = name;
    this.type = type;
    this.channelsCount = channelsCount;
    this.sampleCounts = Collections.unmodifiableMap(sampleCounts);
    this.incomplete = issues.stream().anyMatch(issue -> Type.EOF.equals(issue.getType()));
    this.issues = Collections.unmodifiableList(issues);
  }

  @Override
  public Duration getDuration() {
    return AudioInfo.secondsToDuration(getSeconds(sampleCounts));
  }

  private static double getSeconds(Map<Integer, Long> sampleCounts) {
    double seconds = 0.0;
    for (Entry<Integer, Long> entry : sampleCounts.entrySet()) {
      seconds += entry.getValue() / (double) entry.getKey();
    }
    return seconds;
  }

  private static float frameRate(Map<Short, Integer> channelsCount, Map<Integer, Long> sampleCounts) {
    int frames = channelsCount.values().stream().mapToInt(Integer::intValue).sum();
    double seconds = getSeconds(sampleCounts);
    return seconds != 0d ? (float) (frames / seconds) : 0;
  }

  private static short approxNumChannels(Map<Short, Integer> channelsCount) {
    return channelsCount.entrySet()
        .stream().max(Entry.comparingByValue())
        .map(Entry::getKey)
        .orElse((short) 0);
  }

  private static int approxSampleRate(Map<Integer, Long> sampleCounts) {
    return sampleCounts.entrySet()
        .stream().max(Entry.comparingByValue())
        .map(Entry::getKey)
        .orElse(0);
  }

  @Override
  public List<AudioIssue> getIssues() {
      return issues;
  }
}