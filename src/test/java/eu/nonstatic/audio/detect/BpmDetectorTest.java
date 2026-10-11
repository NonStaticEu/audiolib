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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BpmDetectorTest {

  private static final BpmDetector BPM_DETECTOR = new BpmDetector();

  @ParameterizedTest
  @ValueSource(ints = {90, 120, 140})
  void detectsKnownBpmFromSyntheticSignal(int expectedBpm) {
    float sampleRate = 44100f;
    int durationSeconds = 10;
    int totalSamples = (int) (sampleRate * durationSeconds);
    double[] samples = new double[totalSamples];

    int beatInterval = (int) (sampleRate * 60.0 / expectedBpm);

    // Generate click track
    for (int pos = 0; pos < totalSamples; pos += beatInterval) {
      for (int i = 0; i < 200 && (pos + i) < totalSamples; i++) {
        samples[pos + i] = 0.9;
      }
    }

    Bpm detectedBpm = BPM_DETECTOR.detect(samples, sampleRate);
    assertEquals(expectedBpm, detectedBpm.estimate(), 1.0,
        "Expected ~" + expectedBpm + " BPM, got " + detectedBpm);
    double periodicity = detectedBpm.periodicity();
    assertTrue(periodicity > 0.5 && periodicity <= 1.0,
        "Clean click track should yield high periodicity, got " + periodicity);
    double salience = detectedBpm.salience();
    assertTrue(salience > 0.5 && salience <= 1.0,
        "Clean click track should yield unambiguous tempo, got salience " + salience);
  }

  @Test
  void throwsOnInsufficientBeats() {
    double[] samples = new double[44100];
    // Single click — not enough for BPM
    for (int i = 0; i < 200; i++) {
      samples[i] = 0.9;
    }

    assertThrows(NotEnoughAudioException.class,
        () -> BPM_DETECTOR.detect(samples, 44100f));
  }
}
