/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.mpeg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import eu.nonstatic.audio.AudioFormatException;
import eu.nonstatic.audio.AudioInfoException;
import eu.nonstatic.audio.AudioTestBase;
import eu.nonstatic.audio.mpeg.MpegAudioInfoSupplier.MpegInfo;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class Mp2InfoSupplierTest implements AudioTestBase {

  @Test
  void should_give_infos() throws AudioFormatException, IOException, AudioInfoException {
    MpegInfo mpegInfo = new Mp2AudioInfoSupplier().getInfos(MP2_URL.openStream(), MP2_NAME);
    assertFalse(mpegInfo.isIncomplete());
    assertEquals(Duration.ofNanos(11102040816L), mpegInfo.getDuration());
  }

  // Please refer to Mp3InfoSupplierTest for all other exceptional test cases
}
