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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AudoInfoSuppliersTest {

  @Test
  void should_select_supplier_by_filename() {
    assertEquals(FlacInfoSupplier.class, AudioInfoSuppliers.getByFileName("/tmp/music.flac").getClass());
    assertEquals(AiffInfoSupplier.class, AudioInfoSuppliers.getByFileName("music.AIFF").getClass());
  }

  @Test
  void should_not_select_supplier_by_filename() {
    IllegalArgumentException iae1 = assertThrows(IllegalArgumentException.class, () -> AudioInfoSuppliers.getByFileName("/tmp/music.xyz"));
    assertEquals("No audio format available for extension: xyz", iae1.getMessage());

    IllegalArgumentException iae2 = assertThrows(IllegalArgumentException.class, () -> AudioInfoSuppliers.getByFileName("music.XYZ"));
    assertEquals("No audio format available for extension: XYZ", iae2.getMessage());

    IllegalArgumentException iae3 = assertThrows(IllegalArgumentException.class, () -> AudioInfoSuppliers.getByFileName("whatever"));
    assertEquals("No audio format available for extension: null", iae3.getMessage());
  }

  @Test
  void should_fail_select_supplier_by_filename_null() {
    assertThrows(NullPointerException.class, () -> AudioInfoSuppliers.getByFileName(null));
  }

  @Test
  void should_select_supplier_by_extension() {
    assertEquals(Mp3AudioInfoSupplier.class, AudioInfoSuppliers.getByExtension("mp3").getClass());
    assertEquals(Mp2AudioInfoSupplier.class, AudioInfoSuppliers.getByExtension("mp2").getClass());
    assertEquals(WaveInfoSupplier.class, AudioInfoSuppliers.getByExtension("WAV").getClass());
  }

  @Test
  void should_not_select_supplier_by_extension() {
    IllegalArgumentException iae1 = assertThrows(IllegalArgumentException.class, () -> AudioInfoSuppliers.getByExtension("XYz"));
    assertEquals("No audio format available for extension: XYz", iae1.getMessage());

    IllegalArgumentException iae2 = assertThrows(IllegalArgumentException.class, () -> AudioInfoSuppliers.getByExtension(null));
    assertEquals("No audio format available for extension: null", iae2.getMessage());
  }
}
