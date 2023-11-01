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

import java.time.Duration;
import java.util.List;

public interface AudioInfo {
      int SECONDS_PER_MINUTE = 60;
      long NANOS_PER_SECOND = 1_000_000_000L;

      String getName();
      Duration getDuration();
      List<AudioIssue> getIssues();

      static Duration secondsToDuration(double seconds) {
            return Duration.ofNanos(Math.round(seconds * NANOS_PER_SECOND));
      }
}
