/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.ogg;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.formats.AudioFormatEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class OggInfo extends AudioFormatEx implements Cloneable {

  protected OggInfo(String name, Encoding encoding, float sampleRate, int sampleSizeInBits, int channels, int frameSize, float frameRate, boolean bigEndian, int serialNumber) {
    super(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
    this.name = name;
    this.serialNumber = serialNumber;
  }


  private final String name;
  private final int serialNumber;

  @Getter(AccessLevel.NONE)
  protected boolean granuled;
  protected long firstGranule;
  protected long lastGranule; // duration  = (last-first)/samplerate
  protected boolean incomplete;
  protected List<AudioIssue> issues = new ArrayList<>(); // location => bytes skipped


  public String getName() {
    return name + ':' + serialNumber;
  }

  @Override
  public AudioFileType getType() {
    return AudioFileType.OGG;
  }

  public abstract OggCodec getCodec();

  public List<AudioIssue> getIssues() {
    return Collections.unmodifiableList(issues);
  }

  protected void addIssue(@NonNull AudioIssue issue) {
    issues.add(issue);
  }

  protected void updateGranulePos(long granulePos) {
    if(!granuled) {
      firstGranule = granulePos;
      granuled = true;
    }
    lastGranule = granulePos;
  }
  public abstract boolean isEmpty();


  @Override
  public OggInfo clone() {
    try {
      OggInfo clone = (OggInfo) super.clone();
      clone.issues = new ArrayList<>(clone.issues);
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}