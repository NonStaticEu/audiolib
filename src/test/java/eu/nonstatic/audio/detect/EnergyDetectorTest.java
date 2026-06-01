package eu.nonstatic.audio.detect;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class EnergyDetectorTest {

  private final EnergyDetector detector = new EnergyDetector();

  @Test
  void silentInputReturnsNoOnsets() {
    double[] silence = new double[44100]; // 1 second of silence
    List<Integer> onsets = detector.detectOnsets(silence);
    assertTrue(onsets.isEmpty());
  }

  @Test
  void emptyInputReturnsNoOnsets() {
    List<Integer> onsets = detector.detectOnsets(new double[0]);
    assertTrue(onsets.isEmpty());
  }

  @Test
  void detectsPulseTrainAt120Bpm() {
    float sampleRate = 44100f;
    int durationSeconds = 5;
    int totalSamples = (int) (sampleRate * durationSeconds);
    double[] samples = new double[totalSamples];

    // 120 BPM = 2 beats per second = one beat every 22050 samples
    int beatInterval = (int) (sampleRate * 60.0 / 120.0);

    // Place short impulses (click of 200 samples) at regular intervals
    for (int pos = 0; pos < totalSamples; pos += beatInterval) {
      for (int i = 0; i < 200 && (pos + i) < totalSamples; i++) {
        samples[pos + i] = 0.9;
      }
    }

    List<Integer> onsets = detector.detectOnsets(samples);
    // Should detect roughly 10 onsets (2 per second * 5 seconds)
    assertTrue(onsets.size() >= 8, "Expected at least 8 onsets, got " + onsets.size());
  }

  @Test
  void singleBeatReturnsSingleOnset() {
    double[] samples = new double[44100];
    // Single click at the start
    for (int i = 0; i < 200; i++) {
      samples[i] = 0.9;
    }

    List<Integer> onsets = detector.detectOnsets(samples);
    assertEquals(1, onsets.size());
  }

  @Test
  void emptyInputReturnsEmptyEnvelope() {
    assertEquals(0, detector.energyEnvelope(new double[0]).length);
  }

  @Test
  void inputShorterThanWindowReturnsEmptyEnvelope() {
    double[] samples = new double[EnergyDetector.DEFAULT_WINDOW_SIZE - 1];
    assertEquals(0, detector.energyEnvelope(samples).length);
  }

  @Test
  void exactWindowLengthReturnsSingleFrame() {
    double[] samples = new double[EnergyDetector.DEFAULT_WINDOW_SIZE];
    assertEquals(1, detector.energyEnvelope(samples).length);
  }

  @Test
  void envelopeLengthFollowsHopFormula() {
    int window = EnergyDetector.DEFAULT_WINDOW_SIZE;
    int hop = EnergyDetector.DEFAULT_HOP_SIZE;
    double[] samples = new double[window + 4 * hop];

    double[] envelope = detector.energyEnvelope(samples);

    assertEquals(5, envelope.length);
  }

  @Test
  void silentInputProducesZeroEnvelope() {
    double[] samples = new double[44100];

    double[] envelope = detector.energyEnvelope(samples);

    assertTrue(envelope.length > 0);
    assertArrayEquals(new double[envelope.length], envelope, 1e-12);
  }

  @Test
  void constantSignalProducesConstantRms() {
    double[] samples = new double[44100];
    final double level = 0.5;
    for (int i = 0; i < samples.length; i++) {
      samples[i] = level;
    }

    double[] envelope = detector.energyEnvelope(samples);

    assertTrue(envelope.length > 0);
    for (double v : envelope) {
      assertEquals(level, v, 1e-12);
    }
  }

  @Test
  void sineWaveRmsApproximatesAmplitudeOverSqrtTwo() {
    int sampleRate = 44100;
    double[] samples = new double[sampleRate];
    double frequency = 1000.0;
    double amplitude = 1.0;
    for (int i = 0; i < samples.length; i++) {
      samples[i] = amplitude * Math.sin(2 * Math.PI * frequency * i / sampleRate);
    }

    double[] envelope = detector.energyEnvelope(samples);

    // 1024-sample window holds ~23.2 periods of 1 kHz, not an integer count,
    // so RMS lands close to but not exactly on amplitude/sqrt(2).
    double expected = amplitude / Math.sqrt(2);
    for (double v : envelope) {
      assertEquals(expected, v, 5e-3);
    }
  }

  @Test
  void customWindowAndHopShapeEnvelope() {
    EnergyDetector custom = new EnergyDetector(
        4, 2,
        EnergyDetector.DEFAULT_AVERAGE_WINDOW,
        EnergyDetector.DEFAULT_SENSITIVITY,
        EnergyDetector.DEFAULT_MIN_GAP_WINDOWS);
    // 10 samples, window 4, hop 2 → (10 - 4) / 2 + 1 = 4 frames
    double[] samples = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

    double[] envelope = custom.energyEnvelope(samples);

    assertArrayEquals(new double[]{1.0, 1.0, 1.0, 1.0}, envelope, 1e-12);
  }
}
