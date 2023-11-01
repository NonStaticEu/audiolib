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

import eu.nonstatic.audio.aiff.AiffInfoSupplier;
import eu.nonstatic.audio.flac.FlacInfoSupplier;
import eu.nonstatic.audio.mpeg.Mp2AudioInfoSupplier;
import eu.nonstatic.audio.mpeg.Mp3AudioInfoSupplier;
import eu.nonstatic.audio.wave.WaveInfoSupplier;
import java.util.EnumMap;
import lombok.NonNull;

public final class AudioInfoSuppliers {

  private static final EnumMap<AudioFormat, StreamInfoSupplier> STREAM_INFO_SUPPLIERS = new EnumMap<>(AudioFormat.class); // allows get(null)

  static {
    STREAM_INFO_SUPPLIERS.put(AudioFormat.AIFF, new AiffInfoSupplier());
    STREAM_INFO_SUPPLIERS.put(AudioFormat.WAVE, new WaveInfoSupplier());
    STREAM_INFO_SUPPLIERS.put(AudioFormat.MP3,  new Mp3AudioInfoSupplier());
    STREAM_INFO_SUPPLIERS.put(AudioFormat.MP2,  new Mp2AudioInfoSupplier());
    STREAM_INFO_SUPPLIERS.put(AudioFormat.FLAC, new FlacInfoSupplier());
  }

  private AudioInfoSuppliers() {}

  public static StreamInfoSupplier getByFileName(String fileName) {
    String ext = getExt(fileName);
    return getByExtension(ext);
  }

  public static StreamInfoSupplier getByExtension(String extension) throws IllegalArgumentException {
    return STREAM_INFO_SUPPLIERS.get(AudioFormat.ofExtension(extension));
  }

  private static String getExt(@NonNull String fileName) {
    String ext = null;
    int dot = fileName.lastIndexOf('.');
    if (dot >= 0) {
      ext = fileName.substring(dot + 1);
    }
    return ext;
  }
}
