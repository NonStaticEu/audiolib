/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Audiolib is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Audiolib. If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.detect;

/**
 * A tempo estimate in beats per minute, with two complementary quality measures, each in [0, 1]:
 *
 * <ul>
 *   <li>{@code periodicity} — the normalized autocorrelation of the onset-novelty signal at the
 *       detected beat period (the fraction of onset energy that recurs on the beat). Close to 1.0
 *       for a strong, steady beat and near 0.0 for weakly periodic or rubato material. Mirrors
 *       {@code Key.confidence}. Note it stays high even when the chosen tempo is an octave off
 *       (half/double), since the signal is still strongly periodic there.</li>
 *   <li>{@code salience} — how much the winning tempo dominates its nearest competing peak (best
 *       vs. next-best comb score). Close to 1.0 when the tempo is unambiguous, near 0.0 when a
 *       rival period (typically half/double tempo) scores almost as high. This is what flags
 *       octave ambiguity that {@code periodicity} cannot.</li>
 * </ul>
 */
public record Bpm(double estimate, double periodicity, double salience) {

  public int intValue() {
    return Math.toIntExact(Math.round(estimate));
  }
}
