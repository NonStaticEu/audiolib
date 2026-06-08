/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.detect;

import java.util.Objects;
import lombok.Getter;

/**
 * Those are the notes of the piano keyboard
 * The flat notes are not present
 */
@Getter
public enum Note {
  C('C'),
  C_SHARP('C', Note.SHARP),
  D('D'),
  D_SHARP('D', Note.SHARP),
  E('E'),
  F('F'),
  F_SHARP('F', Note.SHARP),
  G('G'),
  G_SHARP('G', Note.SHARP),
  A('A'),
  A_SHARP('A', Note.SHARP),
  B('B');

  static final char SHARP = '♯'; // dièse in FR
  static final char FLAT = '♭'; // bémol in FR

  private final char symbol;
  private final Character alteration;

  Note(char symbol, Character alteration) {
    this.symbol = Character.toUpperCase(symbol);
    this.alteration = alteration;
  }

  Note(char symbol) {
    this(symbol, null);
  }

  public static Note of(char symbol, Character alteration) {
    if(alteration != null) {
      if(alteration == '#') {
        alteration = SHARP;
      } else if(alteration == 'b') {
        alteration = FLAT;
      }
    }

    symbol = Character.toUpperCase(symbol);
    for (Note value : values()) {
      if (value.symbol == symbol && Objects.equals(value.alteration, alteration)) {
        return value;
      }
    }
    throw new IllegalArgumentException("Invalid note: " + symbol + " / " + alteration);
  }
}
