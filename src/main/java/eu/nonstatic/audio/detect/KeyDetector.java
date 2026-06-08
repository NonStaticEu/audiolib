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

import edu.princeton.cs.algs4.Complex;
import eu.nonstatic.audio.AudioAnalyzer;
import eu.nonstatic.audio.AudioUtils;
import eu.nonstatic.audio.Sampling;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Detects the musical key of a track using the Krumhansl-Schmuckler key-finding algorithm:
 * a 12-bin chroma vector is built from a windowed FFT of the whole track, then correlated
 * against the 24 major/minor key profiles; the best match wins.
 *
 * @see "https://en.wikipedia.org/wiki/Krumhansl%E2%80%93Schmuckler_key-finding_algorithm"
 * @see "Krumhansl, C. L. (1990). Cognitive Foundations of Musical Pitch."
 */
public record KeyDetector(int windowFrames, double minFrequency, double maxFrequency) {

  // 8192 frames gives ~5.4 Hz resolution at 44.1 kHz, enough to resolve semitones in the
  // harmonically rich mid-range. Must be a power of two (AudioAnalyzer / FFT requirement).
  private static final int DEFAULT_WINDOW_FRAMES = 8192;
  // Focus on fundamentals and low harmonics (roughly C2..C7); higher bins mostly add noise.
  private static final double DEFAULT_MIN_FREQUENCY = 65.0;
  private static final double DEFAULT_MAX_FREQUENCY = 2100.0;

  private static final int PITCH_CLASSES = 12;
  private static final double A4_FREQUENCY = 440.0;
  private static final int A4_MIDI = 69;
  private static final double LOG2 = Math.log(2);

  // Krumhansl-Kessler key profiles (index 0 = note), perceived stability of each scale degree.
  private static final double[] MAJOR_PROFILE = {6.35, 2.23, 3.48, 2.33, 4.38, 4.09, 2.52, 5.19, 2.39, 3.66, 2.29, 2.88};
  private static final double[] MINOR_PROFILE = {6.33, 2.68, 3.52, 5.38, 2.60, 3.53, 2.54, 4.75, 3.98, 2.69, 3.34, 3.17};

  public KeyDetector() {
    this(DEFAULT_WINDOW_FRAMES, DEFAULT_MIN_FREQUENCY, DEFAULT_MAX_FREQUENCY);
  }

  public Key detect(InputStream is) throws IOException, UnsupportedAudioFileException {
    return detect(AudioUtils.getMonoInputStream(is));
  }

  public Key detect(AudioInputStream ais) throws IOException {
    Sampling sampling = Sampling.of(ais);
    return detect(sampling);
  }

  public Key detect(Sampling sampling) {
    return detect(sampling.samples(), sampling.start(), sampling.length(), sampling.sampleRate());
  }

  public Key detect(double[] samples, float sampleRate) {
    return detect(samples, 0, samples.length, sampleRate);
  }

  private Key detect(double[] samples, int start, int length, float sampleRate) {
    return bestMatch(chroma(samples, start, length, sampleRate));
  }

  public double[] chroma(double[] samples, float sampleRate) {
    return chroma(samples, 0, samples.length, sampleRate);
  }

  /**
   * Builds a 12-bin chroma vector: for every FFT bin in [minFrequency, maxFrequency] its
   * magnitude is folded into the pitch class of its frequency. Octaves of the same note thus
   * accumulate together.
   */
  public double[] chroma(double[] samples, int start, int len, float sampleRate) {
    AudioAnalyzer analyzer = new AudioAnalyzer(windowFrames); // 50% overlap by default
    int hop = windowFrames - analyzer.overlapFrames();

    double[] chroma = new double[PITCH_CLASSES];
    for (int s = start; s + windowFrames <= start+len; s += hop) {
      // fft applies the Hanning window, runs the FFT and drops the conjugate
      // mirror; the remaining bin i carries the magnitude of frequency (i+1)*sampleRate/N.
      Complex[] spectrum = analyzer.fft(samples, s, windowFrames);
      for (int i = 0; i < spectrum.length; i++) {
        double frequency = (i + 1) * sampleRate / windowFrames;
        if (frequency < minFrequency || frequency > maxFrequency) {
          continue;
        }
        chroma[pitchClass(frequency)] += spectrum[i].abs();
      }
    }
    return chroma;
  }

  /**
   * Correlates the chroma against all 24 key profiles and returns the best-matching key.
   */
  public Key bestMatch(double[] chroma) {
    if (isSilent(chroma)) {
      throw new IllegalArgumentException("No tonal content to determine key");
    }

    int bestTonic = 0;
    Mode bestMode = Mode.MAJOR;
    double bestScore = Double.NEGATIVE_INFINITY;

    for (int tonic = 0; tonic < PITCH_CLASSES; tonic++) {
      double majorScore = correlation(chroma, rotate(MAJOR_PROFILE, tonic));
      if (majorScore > bestScore) {
        bestScore = majorScore;
        bestTonic = tonic;
        bestMode = Mode.MAJOR;
      }
      double minorScore = correlation(chroma, rotate(MINOR_PROFILE, tonic));
      if (minorScore > bestScore) {
        bestScore = minorScore;
        bestTonic = tonic;
        bestMode = Mode.MINOR;
      }
    }
    return new Key(Note.values()[bestTonic], bestMode, bestScore);
  }

  // MIDI pitch -> pitch class (0 = C). A4 (440 Hz, MIDI 69) maps to pitch class 9 (A).
  private static int pitchClass(double frequency) {
    int midi = (int) Math.round(A4_MIDI + PITCH_CLASSES * Math.log(frequency / A4_FREQUENCY) / LOG2);
    return ((midi % PITCH_CLASSES) + PITCH_CLASSES) % PITCH_CLASSES;
  }

  // Rotates a note-relative profile so that index `note` becomes the note of the candidate key.
  private static double[] rotate(double[] profile, int tonic) {
    double[] rotated = new double[PITCH_CLASSES];
    for (int pc = 0; pc < PITCH_CLASSES; pc++) {
      rotated[pc] = profile[((pc - tonic) % PITCH_CLASSES + PITCH_CLASSES) % PITCH_CLASSES];
    }
    return rotated;
  }

  // Pearson correlation coefficient between two equally-sized vectors.
  private static double correlation(double[] a, double[] b) {
    int n = a.length;
    double meanA = mean(a);
    double meanB = mean(b);

    double covariance = 0, varA = 0, varB = 0;
    for (int i = 0; i < n; i++) {
      double da = a[i] - meanA;
      double db = b[i] - meanB;
      covariance += da * db;
      varA += da * da;
      varB += db * db;
    }
    return covariance / Math.sqrt(varA * varB);
  }

  private static double mean(double[] values) {
    double sum = 0;
    for (double v : values) {
      sum += v;
    }
    return sum / values.length;
  }

  private static boolean isSilent(double[] chroma) {
    for (double v : chroma) {
      if (v != 0.0) {
        return false;
      }
    }
    return true;
  }
}
