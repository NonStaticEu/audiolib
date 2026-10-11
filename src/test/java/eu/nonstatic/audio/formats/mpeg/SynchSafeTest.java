/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Audiolib is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Audiolib. If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.mpeg;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SynchSafeTest {

  @Test
  void should_encode_synch_safe_bytes() {
    assertArrayEquals(new byte[]{9, -74, 100, 119}, SynchSafe.toBytes(19772023));
  }

  @Test
  void should_decode_synch_safe_bytes() {
    assertEquals(19772023, SynchSafe.toInt(new byte[]{9, -74, 100, 119}));
  }
}
