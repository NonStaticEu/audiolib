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

import java.util.LinkedList;
import java.util.List;

public record EnergyDetector(int windowSize, int hopSize, int averageWindow, double sensitivity, int minGapWindows) {

  public static final int DEFAULT_WINDOW_SIZE = 1024;
  // Hop < window gives overlapping frames, raising envelope time resolution and
  // letting autocorrelation lock onto the true period rather than a window-quantized one.
  public static final int DEFAULT_HOP_SIZE = 256;
  public static final int DEFAULT_AVERAGE_WINDOW = 43;
  public static final double DEFAULT_SENSITIVITY = 1.4;
  public static final int DEFAULT_MIN_GAP_WINDOWS = 9;

  public EnergyDetector() {
    this(DEFAULT_WINDOW_SIZE, DEFAULT_HOP_SIZE, DEFAULT_AVERAGE_WINDOW, DEFAULT_SENSITIVITY, DEFAULT_MIN_GAP_WINDOWS);
  }

  public List<Integer> detectOnsets(double[] samples) {
    double[] energy = energyEnvelope(samples);
    int numFrames = energy.length;
    if (numFrames == 0) {
      return List.of();
    }

    var onsets = new LinkedList<Integer>();
    int lastOnsetWindow = -minGapWindows - 1;
    int halfWindow = averageWindow / 2;

    for (int w = 0; w < numFrames; w++) {
      int start = Math.max(0, w - halfWindow);
      int end = Math.min(numFrames, w + halfWindow + 1);
      double avg = 0;
      for (int j = start; j < end; j++) {
        avg += energy[j];
      }
      avg /= (end - start);

      if (energy[w] > sensitivity * avg && (w - lastOnsetWindow) >= minGapWindows) {
        onsets.add(w * hopSize);
        lastOnsetWindow = w;
      }
    }

    return onsets;
  }

  public double[] energyEnvelope(double[] samples) {
    if (samples.length < windowSize) {
      return new double[0];
    }
    int numFrames = (samples.length - windowSize) / hopSize + 1;
    double[] energy = new double[numFrames];
    for (int w = 0; w < numFrames; w++) {
      double sum = 0;
      int offset = w * hopSize;
      for (int i = 0; i < windowSize; i++) {
        double s = samples[offset + i];
        sum += s * s;
      }
      energy[w] = Math.sqrt(sum / windowSize);
    }
    return energy;
  }
}
