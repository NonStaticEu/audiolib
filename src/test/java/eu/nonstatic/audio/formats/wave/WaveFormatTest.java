/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.wave;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class WaveFormatTest {

  @Test
  void should_expose_value() {
    assertEquals((short) 0x0001, WaveFormat.PCM.getValue());
    assertEquals((short) 0xFFFE, WaveFormat.EXTENSIBLE.getValue());
  }

  @Test
  void should_find_format_by_value() {
    assertEquals(WaveFormat.PCM, WaveFormat.ofValue((short) 0x0001));
    assertEquals(WaveFormat.MPEGLAYER3, WaveFormat.ofValue((short) 0x0055));
    assertEquals(WaveFormat.EXTENSIBLE, WaveFormat.ofValue((short) 0xFFFE));
  }

  @Test
  void should_return_null_for_unknown_value() {
    assertNull(WaveFormat.ofValue((short) 0x2222));
  }
}
