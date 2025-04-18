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
import eu.nonstatic.audio.ape.ApeInfoSupplier;
import eu.nonstatic.audio.dts.DtsInfoSupplier;
import eu.nonstatic.audio.flac.FlacInfoSupplier;
import eu.nonstatic.audio.mpeg.Mp2AudioInfoSupplier;
import eu.nonstatic.audio.mpeg.Mp3AudioInfoSupplier;
import eu.nonstatic.audio.ogg.OggInfoSupplier;
import eu.nonstatic.audio.wave.WaveInfoSupplier;
import eu.nonstatic.audio.xm.XmInfoSupplier;
import java.util.EnumMap;
import lombok.NonNull;

public final class AudioInfoSuppliers {

  private static final EnumMap<AudioFormat, AudioInfoSupplier> AUDIO_INFO_SUPPLIERS = new EnumMap<>(AudioFormat.class); // allows get(null)

  static {
    AUDIO_INFO_SUPPLIERS.put(AudioFormat.AIFF, new AiffInfoSupplier());
    AUDIO_INFO_SUPPLIERS.put(AudioFormat.WAVE, new WaveInfoSupplier());
    AUDIO_INFO_SUPPLIERS.put(AudioFormat.MP3,  new Mp3AudioInfoSupplier());
    AUDIO_INFO_SUPPLIERS.put(AudioFormat.MP2,  new Mp2AudioInfoSupplier());
    AUDIO_INFO_SUPPLIERS.put(AudioFormat.FLAC, new FlacInfoSupplier());
    AUDIO_INFO_SUPPLIERS.put(AudioFormat.OGG, new OggInfoSupplier());
    AUDIO_INFO_SUPPLIERS.put(AudioFormat.DTS, new DtsInfoSupplier());
    AUDIO_INFO_SUPPLIERS.put(AudioFormat.APE, new ApeInfoSupplier());
    AUDIO_INFO_SUPPLIERS.put(AudioFormat.XM, new XmInfoSupplier());
  }

  private AudioInfoSuppliers() {}

  public static AudioInfoSupplier getByFileName(String fileName) {
    String ext = getExt(fileName);
    return getByExtension(ext);
  }

  public static AudioInfoSupplier getByExtension(String extension) throws IllegalArgumentException {
    return AUDIO_INFO_SUPPLIERS.get(AudioFormat.ofExtension(extension));
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
