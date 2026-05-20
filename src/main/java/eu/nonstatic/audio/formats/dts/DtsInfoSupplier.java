/**
 * Audiolib
 * Copyright (C) 2025 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.dts;

import eu.nonstatic.audio.AudioFileType;
import eu.nonstatic.audio.AudioInputStream;
import eu.nonstatic.audio.formats.AudioFormatException;
import eu.nonstatic.audio.formats.AudioInfoException;
import eu.nonstatic.audio.formats.AudioInfoSupplier;
import eu.nonstatic.audio.formats.wave.WaveInfo;
import eu.nonstatic.audio.formats.wave.WaveInfoSupplier;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DtsInfoSupplier implements AudioInfoSupplier<DtsInfo> {

  @Override
  public DtsInfo getInfos(InputStream is, String name) throws AudioInfoException, IOException {
    AudioInputStream ais = new AudioInputStream(is, name);

    ais.mark(4);
    if(WaveInfoSupplier.isRiff(ais)) {
      ais.reset();
      WaveInfo infos = new WaveInfoSupplier().getInfos(ais);
      return new DtsInfo(infos);
    } else {
      throw new AudioInfoException(new AudioFormatException(ais.getName(), 0, AudioFileType.DTS, "DTS format not supported"));
    }
  }
}
