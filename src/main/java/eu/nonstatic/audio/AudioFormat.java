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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum AudioFormat {

  AIFF("aif", "aiff"),
  WAVE("wav", "wave"),
  MP3("mp3"),
  MP2("mp2"),
  FLAC("flac");

  private final List<String> extensions;

  AudioFormat(String... extensions) {
    this.extensions = Collections.unmodifiableList(Arrays.asList(extensions)); // needs to allow contains(null)
  }

  public static AudioFormat ofExtension(String extension) {
    String extensionLower = toLowerCase(extension);
    return Stream.of(values())
        .filter(format -> format.extensions.contains(extensionLower))
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException("No audio info available for extension: " + extension)); // same contract as in valueOf()
  }

  private static String toLowerCase(String ext) {
    return ext != null ? ext.toLowerCase(Locale.ROOT) : null;
  }
}
