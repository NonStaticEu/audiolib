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

import static java.lang.Character.toLowerCase;

import lombok.NonNull;

/**
 * The musical key of a track: a {@link Note} and a {@link Mode}, plus the
 * confidence of the estimate (Pearson correlation of the chroma with the winning key profile,
 * close to 1.0 for a strong match).
 */
public record Key(@NonNull Note note, @NonNull Mode mode, double confidence) {

  public Key {
    if (confidence < 0.0 || confidence > 1.0) {
      throw new IllegalArgumentException("Invalid confidence: " + confidence);
    }
  }

  public static Key ofSignature(String signature, double confidence) {
    if (signature.isEmpty() || signature.length() > 2) {
      throw new IllegalArgumentException("Invalid key signature: " + signature);
    }
    char symbol = signature.charAt(0);
    Character alteration = signature.length() == 2 ? signature.charAt(1) : null;
    Note note = Note.of(symbol, alteration);
    Mode mode = Character.isLowerCase(symbol) ? Mode.MINOR : Mode.MAJOR;
    return new Key(note, mode, confidence);
  }

  public String signature() {
    String result = Mode.MINOR.equals(mode)
        ? Character.toString(toLowerCase(note.getSymbol()))
        : Character.toString(note.getSymbol());
    return note.getAlteration() != null ? result + note.getAlteration() : result;
  }

  /**
   * Camelot wheel code (used by DJs for harmonic mixing), eg C major = 8B, A minor = 8A.
   * Derived from the circle of fifths rather than a lookup table: each clockwise step around
   * the wheel is a perfect fifth (+7 semitones, inverse of 7 mod 12 is 7), and a minor key
   * shares its number with its relative major (+3 semitones).
   */
  public String camelot() {
    int number = (mode == Mode.MINOR) ? majorNumber((note.ordinal() + 3) % 12) : majorNumber(note.ordinal());
    char letter = (mode == Mode.MINOR) ? 'A' : 'B';
    return number + String.valueOf(letter);
  }

  private static int majorNumber(int tonic) {
    return ((7 + 7 * tonic) % 12) + 1;
  }

  @Override
  public String toString() {
    return signature() + " (" + confidence + ')';
  }
}
