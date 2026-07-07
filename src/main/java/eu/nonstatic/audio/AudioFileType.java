/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sound.sampled.AudioFileFormat;
import lombok.Getter;

@Getter
public enum AudioFileType {

  AIFF("Aiff", List.of("aif", "aiff", "aifc"), List.of("audio/x-aiff")),
  WAVE("Wave", List.of("wav", "wave"), List.of("audio/wav", "audio/x-wav", "audio/vnd.wav")),
  MP3("MP3", List.of("mp3"), List.of("audio/mpeg")), // before MP2 because of mimeTypeToExtension
  MP2("MP2", List.of("mp2"), List.of("audio/mpeg")),
  FLAC("Flac", List.of("flac"), List.of("audio/flac")),
  DTS("DTS", List.of("dts")),
  APE("APE", List.of("ape", "apl", "mac"), List.of("audio/x-monkeys-audio")),
  OGG("Ogg", List.of("ogg", "oga"), List.of("audio/ogg")),
  XM("XM", List.of("xm"));

  private final String displayName;
  private final List<String> extensions;
  private final List<String> mimeTypes;

  AudioFileType(String displayName, List<String> extensions, List<String> mimeTypes) {
    this.displayName = displayName;
    this.extensions = extensions; // needs to allow contains(null)
    this.mimeTypes = mimeTypes;
  }

  AudioFileType(String displayName, List<String> extensions) {
    this(displayName, extensions, Collections.emptyList());
  }

  public static AudioFileType ofExtension(String extension) {
    Optional<AudioFileType> result = Optional.empty();
    if(isNotEmpty(extension)) {
      String extLower = extension.toLowerCase(Locale.ROOT);
      result = Stream.of(values())
          .filter(aft -> aft.extensions.contains(extLower))
          .findAny();
    }
    return result.orElseThrow(() -> new IllegalArgumentException("No AudioFileType available for extension: " + extension)); // same contract as in valueOf()
  }

  public static List<AudioFileType> ofMimeType(String mimeType) {
    List<AudioFileType> result = List.of();
    if(isNotEmpty(mimeType)) {
      String mimeLower = mimeType.toLowerCase(Locale.ROOT);
      result = Stream.of(values())
          .filter(aft -> aft.mimeTypes.contains(mimeLower))
          .toList();
    }

    if(result.isEmpty()) {
      throw new IllegalArgumentException("No AudioFileType available for mimeType: " + mimeType); // same contract as in valueOf()
    }
    return result;
  }

  public static List<String> extensionToMimeTypes(String extension) {
    return ofExtension(extension).mimeTypes;
  }

  public static String extensionToMimeType(String extension) {
    List<String> mimeTypes = extensionToMimeTypes(extension);
    return mimeTypes.isEmpty() ? null : mimeTypes.get(0);
  }

  public static List<String> mimeTypeToExtensions(String mimeType) {
    return ofMimeType(mimeType).stream().flatMap(aft -> aft.extensions.stream()).toList();
  }

  public static String mimeTypeToExtension(String mimeType) {
    List<String> extensions = mimeTypeToExtensions(mimeType);
    return extensions.isEmpty() ? null : extensions.get(0);
  }

  public AudioFileFormat.Type toAudioFileFormatType() {
    return new AudioFileFormat.Type(displayName.toUpperCase(Locale.ROOT), extensions.get(0));
  }

  private static boolean isNotEmpty(String s) {
    return s != null && !s.isEmpty();
  }
}
