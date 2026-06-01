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

import eu.nonstatic.audio.AudioUtils;
import eu.nonstatic.audio.Sampling;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

public record BpmDetector(double minBpm, double maxBpm) {

  private static final double DEFAULT_MIN_BPM = 60.0;
  private static final double DEFAULT_MAX_BPM = 200.0;

  // Isolates kick/bass energy so the envelope tracks the beat rather than the full mix.
  private static final double LOW_PASS_CUTOFF_HZ = 150.0;
  // Comb-style scoring sums autocorrelation at L, 2L, 3L, 4L. The true beat period
  // gets credit at all its multiples (bar, 2-bar...), while spurious sub-multiple
  // peaks only sum a subset.
  private static final int NUM_HARMONICS = 4;
  // Log-Gaussian prior on tempo. Suppresses octave errors (half/double-tempo) without
  // a hard cutoff — overwhelming evidence at the boundaries can still win.
  private static final double PRIOR_CENTER_BPM = 120.0;
  private static final double PRIOR_SIGMA_LOG2 = 0.5;
  private static final double LN2 = Math.log(2);

  public BpmDetector() {
    this(DEFAULT_MIN_BPM, DEFAULT_MAX_BPM);
  }

  public Bpm detect(InputStream is) throws IOException, UnsupportedAudioFileException {
    return detect(AudioUtils.getMonoInputStream(is));
  }

  public Bpm detect(AudioInputStream ais) throws IOException {
    Sampling sampling = Sampling.of(ais);
    return detect(sampling);
  }

  public Bpm detect(Sampling sampling) {
    return detect(sampling.samples(), sampling.sampleRate());
  }

  public Bpm detect(double[] samples, float sampleRate) {
    double[] filtered = Filters.IIR.lowPass(samples, sampleRate, LOW_PASS_CUTOFF_HZ);

    EnergyDetector energyDetector = new EnergyDetector();
    double[] envelope = energyDetector.energyEnvelope(filtered);
    double frameRate = sampleRate / energyDetector.hopSize();
    return estimateBpm(envelope, frameRate);
  }

  private Bpm estimateBpm(double[] envelope, double frameRate) {
    int minLag = (int) Math.floor(60.0 * frameRate / maxBpm);
    int maxLag = (int) Math.ceil(60.0 * frameRate / minBpm);
    requireEnoughFrames(envelope.length, minLag, maxLag);

    double[] novelty = zeroMeanNovelty(envelope);
    int searchEnd = Math.min(novelty.length - 1, maxLag * NUM_HARMONICS);
    double[] ac = autocorrelation(novelty, searchEnd);
    double[] score = scoreLags(ac, minLag, maxLag, frameRate);
    int bestLag = argmax(score, minLag, maxLag);
    double refinedLag = parabolicRefine(score, bestLag, minLag, maxLag);

    double bpm = 60.0 * frameRate / refinedLag;
    return new Bpm(bpm, periodicity(ac, bestLag), salience(score, bestLag, minLag, maxLag));
  }

  // Normalized autocorrelation at the beat lag: ac[0] is the novelty variance (zero-lag
  // autocorrelation), so ac[bestLag]/ac[0] is the Pearson autocorrelation coefficient at that
  // lag — the fraction of onset energy that recurs on the beat. ~1 for a strong, steady beat,
  // near 0 for weakly periodic material. The differing count normalizations of ac[bestLag] and
  // ac[0] let the ratio edge slightly past 1 at large lags, so we clamp to [0, 1].
  private static double periodicity(double[] ac, int bestLag) {
    double zeroLag = ac[0];
    if (zeroLag <= 0) {
      return 0.0;
    }
    return Math.max(0.0, Math.min(1.0, ac[bestLag] / zeroLag));
  }

  // Peak salience: how much the winning lag dominates the next-best competing peak. The main lobe
  // around bestLag is skipped (its shoulders aren't distinct candidates); the meaningful rivals —
  // half/double tempo — sit far away, so a guard proportional to the search range leaves them in.
  // salience = 1 - secondBest/bestScore: ~1 when the tempo is unambiguous, ~0 when a rival period
  // scores almost as high. Clamped to [0, 1] since competing scores can be negative or near-equal.
  private static double salience(double[] score, int bestLag, int minLag, int maxLag) {
    double bestScore = score[bestLag];
    if (bestScore <= 0) {
      return 0.0;
    }
    int guard = Math.max(1, (maxLag - minLag) / 50);
    double secondBest = Double.NEGATIVE_INFINITY;
    for (int lag = minLag; lag <= maxLag; lag++) {
      if (Math.abs(lag - bestLag) <= guard) {
        continue;
      }
      secondBest = Math.max(secondBest, score[lag]);
    }
    if (secondBest == Double.NEGATIVE_INFINITY) {
      return 1.0;
    }
    return Math.max(0.0, Math.min(1.0, 1.0 - secondBest / bestScore));
  }

  private static void requireEnoughFrames(int n, int minLag, int maxLag) {
    if (n < 4 || maxLag < minLag + 2 || maxLag >= n) {
      throw new IllegalArgumentException("Audio too short to determine BPM");
    }
  }

  // Onset novelty: half-wave rectified energy difference, zero-mean.
  private static double[] zeroMeanNovelty(double[] envelope) {
    int n = envelope.length;
    double[] novelty = new double[n];
    double sum = 0;
    for (int i = 1; i < n; i++) {
      double v = Math.max(0, envelope[i] - envelope[i - 1]);
      novelty[i] = v;
      sum += v;
    }
    double mean = sum / n;
    for (int i = 0; i < n; i++) {
      novelty[i] -= mean;
    }
    return novelty;
  }

  // Normalized autocorrelation of `novelty` for lags in [0, maxLag], so the comb sum
  // is comparable across candidates. ac[0] is the variance, used as the periodicity denominator.
  private static double[] autocorrelation(double[] novelty, int maxLag) {
    int n = novelty.length;
    double[] ac = new double[maxLag + 1];
    for (int lag = 0; lag <= maxLag; lag++) {
      ac[lag] = correlateAtLag(novelty, lag) / (n - lag);
    }
    return ac;
  }

  private static double correlateAtLag(double[] novelty, int lag) {
    double s = 0;
    int limit = novelty.length - lag;
    for (int f = 0; f < limit; f++) {
      s += novelty[f] * novelty[f + lag];
    }
    return s;
  }

  private double[] scoreLags(double[] ac, int minLag, int maxLag, double frameRate) {
    double[] score = new double[maxLag + 1];
    for (int lag = minLag; lag <= maxLag; lag++) {
      double bpm = 60.0 * frameRate / lag;
      score[lag] = combSum(ac, lag) * priorWeight(bpm);
    }
    return score;
  }

  private static double combSum(double[] ac, int lag) {
    double s = 0;
    for (int k = 1; k <= NUM_HARMONICS; k++) {
      int l = k * lag;
      if (l < ac.length) {
        s += ac[l];
      }
    }
    return s;
  }

  private static double priorWeight(double bpm) {
    double dLog = Math.log(bpm / PRIOR_CENTER_BPM) / LN2;
    return Math.exp(-(dLog * dLog) / (2 * PRIOR_SIGMA_LOG2 * PRIOR_SIGMA_LOG2));
  }

  private static int argmax(double[] score, int from, int to) {
    int best = from;
    double bestScore = score[from];
    for (int i = from + 1; i <= to; i++) {
      if (score[i] > bestScore) {
        bestScore = score[i];
        best = i;
      }
    }
    return best;
  }

  // Parabolic interpolation around the peak for sub-frame precision.
  private static double parabolicRefine(double[] score, int bestLag, int minLag, int maxLag) {
    if (bestLag <= minLag || bestLag >= maxLag) {
      return bestLag;
    }
    double y0 = score[bestLag - 1];
    double y1 = score[bestLag];
    double y2 = score[bestLag + 1];
    double denom = y0 - 2 * y1 + y2;
    if (Math.abs(denom) <= 1e-12) {
      return bestLag;
    }
    return bestLag + 0.5 * (y0 - y2) / denom;
  }
}
