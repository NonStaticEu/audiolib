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
import eu.nonstatic.audio.ogg.OggInfoSupplier.MalformedPacketException;
import eu.nonstatic.audio.ogg.OggInfoSupplier.OggPage;
import eu.nonstatic.audio.ogg.OggInfoSupplier.OggPage.PacketSegment;
import eu.nonstatic.audio.ogg.OggInfoSupplier.UnsupportedCodecException;
import java.io.IOException;

public interface OggCodecReader<I extends OggInfo> {
  String META_BOS_SEGMENT_SIZE = "bos-segment-size";

  OggCodec getCodec();
  byte[] getHeader();
  I readBos(AudioInputStream ais, int serialNumber, PacketSegment segment) throws UnsupportedCodecException, MalformedPacketException, IOException;

  void readPacket(AudioInputStream ais, I oggInfo, OggPage page, PacketSegment next) throws MalformedPacketException, IOException;
}
