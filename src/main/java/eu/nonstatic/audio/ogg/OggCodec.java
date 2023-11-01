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

import lombok.Getter;

@Getter
public enum OggCodec {
  DAALA(Type.VIDEO),    // https://wiki.xiph.org/Daala
  DIRAC(Type.VIDEO),    // https://wiki.xiph.org/OggDirac
  FLAC(Type.AUDIO),     // https://xiph.org/flac/ogg_mapping.html
  THEORA(Type.VIDEO),   // https://wiki.xiph.org/Theora
  VORBIS(Type.AUDIO),   // https://wiki.xiph.org/Vorbis
  CELT(Type.AUDIO),
  CMML(Type.MARKUP),
  JNG(Type.IMAGE),
  KATE(Type.OVERLAY),
  MIDI(Type.AUDIO),
  MNG(Type.IMAGE),
  OPUS(Type.AUDIO),     // https://wiki.xiph.org/OpusFAQ
  PCM(Type.AUDIO),
  PNG(Type.IMAGE),
  SPEEX(Type.AUDIO),    // https://wiki.xiph.org/Speex
  YUV4MPEG(Type.VIDEO);

  private final Type type;

  OggCodec(Type type) {
    this.type = type;
  }

  public enum Type {
    AUDIO,
    VIDEO,
    IMAGE,
    OVERLAY, // karaoke, lyrics...
    MARKUP   // deprecated codec enabling textual search in an ogg file
  }
}
