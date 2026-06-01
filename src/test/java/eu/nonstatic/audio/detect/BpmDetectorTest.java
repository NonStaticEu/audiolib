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

    assertThrows(IllegalArgumentException.class,
        () -> BPM_DETECTOR.detect(samples, 44100f));
  }
}
