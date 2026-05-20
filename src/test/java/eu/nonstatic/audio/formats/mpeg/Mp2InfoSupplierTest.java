/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.mpeg;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.formats.AudioInfoException;
import eu.nonstatic.audio.AudioTestBase;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class Mp2InfoSupplierTest implements AudioTestBase {

  @Test
  void should_give_infos() throws IOException, AudioInfoException {
    MpegInfo mpegInfo = new Mp2AudioInfoSupplier().getInfos(MP2_URL.openStream(), MP2_NAME);
    assertEquals(AudioFileType.MP2, mpegInfo.getType());
    assertFalse(mpegInfo.isIncomplete());
    assertEquals(Duration.ofNanos(11102040816L), mpegInfo.getDuration());
  }

  // Please refer to Mp3InfoSupplierTest for all other exceptional test cases
}
