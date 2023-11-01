/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.ogg;

import eu.nonstatic.audio.AudioInputStream;
import eu.nonstatic.audio.ogg.OggInfoSupplier.UnsupportedCodecException;
import eu.nonstatic.audio.ogg.codec.VorbisCodecReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OggCodecReaders {

  private OggCodecReaders() {}

  private static final Map<OggCodec, OggCodecReader<?>> SUPPORTED_READERS = Stream.of(
    new VorbisCodecReader()
    // add more here as needed
  ).collect(Collectors.toMap(OggCodecReader::getCodec, Function.identity()));


  public static final int MAX_HEADER_SIZE = SUPPORTED_READERS.values()
      .stream()
      .map(OggCodecReader::getHeader)
      .mapToInt(header -> header.length)
      .max()
      .orElse(0);

  public static boolean isSupported(OggCodec codec) {
    return SUPPORTED_READERS.get(codec) != null;
  }

  public static OggCodecReader get(OggCodec codec) throws UnsupportedCodecException {
    OggCodecReader<?> reader = SUPPORTED_READERS.get(codec);
    if(reader != null) {
      return reader;
    } else { // isn't supposed to happen since we are only getting back readers for existing ogg infos
      throw new UnsupportedCodecException(null, -1, -1, "Unsupported requested codec: " + codec);
    }
  }

  public static OggCodecReader get(AudioInputStream ais, int serialNumber) throws IOException, UnsupportedCodecException {
    long location = ais.location();
    ais.mark(MAX_HEADER_SIZE);
    byte[] paddedHeader = ais.readNBytes(MAX_HEADER_SIZE);

    for (OggCodecReader<?> reader : SUPPORTED_READERS.values()) {
      byte[] codecHeader = reader.getHeader();
      if (Arrays.equals(codecHeader, 0, codecHeader.length, paddedHeader, 0, codecHeader.length)) {
        ais.reset();
        ais.skipNBytesBackport(codecHeader.length);
        return reader;
      }
    }
    throw new UnsupportedCodecException(ais.getName(), location, serialNumber, "Unrecognized header: " + new String(paddedHeader, StandardCharsets.US_ASCII));
  }
}
