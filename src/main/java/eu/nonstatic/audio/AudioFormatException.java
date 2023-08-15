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

import lombok.Getter;

@Getter
public class AudioFormatException extends AudioException {

  private final long location;
  private final AudioFormat format;

  public AudioFormatException(String name, long location, AudioFormat format, String message) {
    super(name, message);
    this.location = location;
    this.format = format;
  }

  public AudioFormatException(String name, long location, AudioFormat format, String message, Throwable cause) {
    super(name, message, cause);
    this.location = location;
    this.format = format;
  }

  @Override
  public String getMessage() {
    return super.getMessage() + " at " + location + ": " + name;
  }
}
