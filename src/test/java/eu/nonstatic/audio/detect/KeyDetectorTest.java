package eu.nonstatic.audio.detect;

import static eu.nonstatic.audio.detect.Mode.MAJOR;
import static eu.nonstatic.audio.detect.Mode.MINOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.nonstatic.audio.AudioTestBase;
import java.io.IOException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.junit.jupiter.api.Test;

class KeyDetectorTest implements AudioTestBase {

  private static final float SAMPLE_RATE = 44100f;

  // Same Krumhansl-Kessler profiles the detector matches against, kept here as an independent oracle.
  private static final double[] MAJOR_PROFILE = {6.35, 2.23, 3.48, 2.33, 4.38, 4.09, 2.52, 5.19, 2.39, 3.66, 2.29, 2.88};
  private static final double[] MINOR_PROFILE = {6.33, 2.68, 3.52, 5.38, 2.60, 3.53, 2.54, 4.75, 3.98, 2.69, 3.34, 3.17};

  private static final KeyDetector DETECTOR = new KeyDetector();

  @Test
  void mapsSinglePitchToItsPitchClass() {
    int aIndex = argmax(DETECTOR.chroma(sine(440.0, 2.0), SAMPLE_RATE));   // A4
    assertEquals(9, aIndex, "440 Hz should land on pitch class A");

    int cIndex = argmax(DETECTOR.chroma(sine(261.63, 2.0), SAMPLE_RATE)); // C4
    assertEquals(0, cIndex, "261.63 Hz should land on pitch class C");
  }

  @Test
  void matchesEachKeyProfileExactly() {
    for (Note note : Note.values()) {
      assertExactMatch(note, MAJOR, MAJOR_PROFILE);
      assertExactMatch(note, MINOR, MINOR_PROFILE);
    }
  }

  private static void assertExactMatch(Note note, Mode mode, double[] profile) {
    // A chroma equal to a key's own profile must correlate perfectly (r = 1) with that key.
    Key key = DETECTOR.bestMatch(rotate(profile, note));
    assertEquals(note, key.note(), () -> mode + " note " + note);
    assertEquals(mode, key.mode(), () -> mode + " note " + note);
    assertEquals(1.0, key.confidence(), 1e-9);
  }

  @Test
  void computesCamelotCodes() {
    assertEquals("8A", new Key(Note.A, MINOR, 1.0).camelot()); // A minor
    assertEquals("8B", new Key(Note.C, MAJOR, 1.0).camelot()); // C major
    assertEquals("9B", new Key(Note.G, MAJOR, 1.0).camelot()); // G major
    assertEquals("9A", new Key(Note.E, MINOR, 1.0).camelot()); // E minor
    assertEquals("1B", new Key(Note.B, MAJOR, 1.0).camelot()); // B major (wraps around)
  }

  @Test
  void detectsCMajorFromSyntheticChord() {
    // C major triad spread over two octaves (C4 E4 G4 C5 E5 G5), no A energy -> not A minor.
    double[] samples = sine(261.63, 4.0, 329.63, 392.00, 523.25, 659.26, 783.99);

    Key key = DETECTOR.detect(samples, SAMPLE_RATE);
    assertEquals(Note.C, key.note(), "expected C, got " + key);
    assertEquals(MAJOR, key.mode(), "expected major, got " + key);
  }

  @Test
  void throwsOnSilence() {
    double[] silence = new double[(int) SAMPLE_RATE]; // 1 second of zeros
    assertThrows(IllegalArgumentException.class, () -> DETECTOR.detect(silence, SAMPLE_RATE));
  }

  @Test
  void detectsKeyFromRealFile() throws IOException, UnsupportedAudioFileException {
    Key key = DETECTOR.detect(WAVE_URL.openStream());
    assertNotNull(key);
    System.out.println(WAVE_NAME + " -> " + key + " (confidence " + key.confidence() + ')');
  }

  /**
   * Sum of equal-amplitude sine waves (each frequency a pure tone, no harmonics) over the given
   * duration, at {@link #SAMPLE_RATE}.
   */
  private static double[] sine(double firstFrequency, double seconds, double... otherFrequencies) {
    int totalSamples = (int) (SAMPLE_RATE * seconds);
    double[] samples = new double[totalSamples];
    double[] frequencies = prepend(firstFrequency, otherFrequencies);

    for (int i = 0; i < totalSamples; i++) {
      double sum = 0;
      for (double frequency : frequencies) {
        sum += Math.sin(2 * Math.PI * frequency * i / SAMPLE_RATE);
      }
      samples[i] = sum / frequencies.length;
    }
    return samples;
  }

  private static double[] prepend(double head, double[] tail) {
    double[] all = new double[tail.length + 1];
    all[0] = head;
    System.arraycopy(tail, 0, all, 1, tail.length);
    return all;
  }

  private static double[] rotate(double[] profile, Note note) {
    int len = profile.length;
    double[] rotated = new double[len];
    for (int pc = 0; pc < len; pc++) {
      rotated[pc] = profile[((pc - note.ordinal()) % len + len) % len];
    }
    return rotated;
  }

  private static int argmax(double[] values) {
    int best = 0;
    for (int i = 1; i < values.length; i++) {
      if (values[i] > values[best]) {
        best = i;
      }
    }
    return best;
  }
}
