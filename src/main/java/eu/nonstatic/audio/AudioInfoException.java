/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio;

import java.util.Collections;
import java.util.List;
import lombok.NonNull;

/**
 * To be thrown when file info cannot be extracted (eg: EOF)
 * the issues list may be used as a summary of issues.
 * Not applicable for framed streams such as mp3 where we might have a partial length
 */
public class AudioInfoException extends AudioException {

  private final List<AudioIssue> issues;

  public AudioInfoException(String name, @NonNull AudioIssue issue) {
    this(name, List.of(issue));
  }

  public AudioInfoException(@NonNull String name, @NonNull List<AudioIssue> issues) {
    super(name, "Cannot retrieve audio infos");
    this.issues = issues;
  }

  public AudioInfoException(AudioFormatException e) {
    this(e.name, AudioIssue.format(e));
  }

  public List<AudioIssue> getIssues() {
    return Collections.unmodifiableList(issues);
  }

  @Override
  public String getMessage() {
    return super.getMessage() + ": " + name;
  }
}
