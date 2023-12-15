/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.ogg;

import eu.nonstatic.audio.AudioIssue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;

public abstract class OggIssues {
  protected final List<AudioIssue> audioIssues = new ArrayList<>(); // location => bytes skipped

  public List<AudioIssue> getIssues() {
    return Collections.unmodifiableList(audioIssues);
  }

  public void addIssue(@NonNull AudioIssue issue) {
    audioIssues.add(issue);
  }


  protected final void into(OggIssues oggIssues) {
    oggIssues.audioIssues.clear();
    oggIssues.audioIssues.addAll(audioIssues);
  }
}