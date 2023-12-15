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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Those samples are taken from here:
 * https://en.wikipedia.org/wiki/Synthesizer
 * https://commons.wikimedia.org/wiki/File:Amplitudenmodulation.ogg
 * https://en.m.wikipedia.org/wiki/File:Example.ogg
 * https://modarchive.org/index.php?request=view_by_moduleid&query=149252 (by rez)
 */
public interface AudioTestBase {

  String AIFF_NAME = "/audio/Arpeggio.aiff";
  String WAVE_NAME = "/audio/Amplitudenmodulation.wav";
  String MP2_NAME  = "/audio/Moog-juno-303-example.mp2";
  String MP3_NAME  = "/audio/Moog-juno-303-example.mp3";
  String FLAC_NAME = "/audio/Filtered_envelope_sawtooth_moog.flac";
  String OGG_NAME  = "/audio/Example.ogg";
  String XM_NAME = "/audio/unreeeal_superhero_3.xm";

  URL AIFF_URL = AudioTestBase.class.getResource(AIFF_NAME);
  URL WAVE_URL = AudioTestBase.class.getResource(WAVE_NAME);
  URL MP2_URL = AudioTestBase.class.getResource(MP2_NAME);
  URL MP3_URL = AudioTestBase.class.getResource(MP3_NAME);
  URL FLAC_URL = AudioTestBase.class.getResource(FLAC_NAME);
  URL OGG_URL = AudioTestBase.class.getResource(OGG_NAME);
  URL XM_URL = AudioTestBase.class.getResource(XM_NAME);


  static Path copyFileContents(URL url, Path file) throws IOException {
    try(InputStream mp3Stream  = url.openStream()) {
      Files.copy(mp3Stream, file, StandardCopyOption.REPLACE_EXISTING);
    }
    return file;
  }
}
