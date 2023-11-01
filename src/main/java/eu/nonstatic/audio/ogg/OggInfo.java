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

import eu.nonstatic.audio.AudioInfo;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public abstract class OggInfo extends OggIssues implements AudioInfo {

  protected OggInfo(String name, int serialNumber) {
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


  public String getName() {
    return name + ':' + serialNumber;
  }
  public abstract OggCodec getCodec();

  protected void updateGranulePos(long granulePos) {
    if(!granuled) {
      firstGranule = granulePos;
      granuled = true;
    }
    lastGranule = granulePos;
  }
  public abstract boolean isEmpty();



  protected abstract OggInfo copy();

  protected final void into(OggInfo oggInfo) {
    oggInfo.granuled = granuled;
    oggInfo.firstGranule = firstGranule;
    oggInfo.lastGranule = lastGranule;
    oggInfo.incomplete = incomplete;
    super.into(oggInfo);
  }
}