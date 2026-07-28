package eu.nonstatic.audio.detect;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class FiltersTest {

  private static final float SAMPLE_RATE = 44100.f;
  private static final int SAMPLES = (int) (2 * SAMPLE_RATE); // 2 seconds
  private static final int SETTLED = SAMPLES / 2;             // measure RMS past the transient
  private static final double BUTTERWORTH_Q = 1.0 / Math.sqrt(2.0);

  // Reference RMS of a unit-amplitude sine (amplitude / sqrt(2)).
  private static final double UNIT_SINE_RMS = 1.0 / Math.sqrt(2.0);


  static class IIR {
    @Test
    void lowPassPassesLowAttenuatesHigh() {
      double low = rms(Filters.IIR.lowPass(sine(100, 1.0), SAMPLE_RATE, 1000), SETTLED);
      double high = rms(Filters.IIR.lowPass(sine(10000, 1.0), SAMPLE_RATE, 1000), SETTLED);

      assertTrue(low > 0.9 * UNIT_SINE_RMS, "100 Hz should pass nearly unchanged, got " + low);
      assertTrue(high < 0.2 * UNIT_SINE_RMS, "10 kHz should be strongly attenuated, got " + high);
      assertTrue(low > high);
    }

    @Test
    void lowPassPassesDc() {
      double[] out = Filters.IIR.lowPass(constant(0.5), SAMPLE_RATE, 1000);
      assertEquals(0.5, out[out.length - 1], 1e-3);
    }


    @Test
    void HighPassPassesHighAttenuatesLow() {
      double high = rms(Filters.IIR.highPass(sine(10000, 1.0), SAMPLE_RATE, 1000), SETTLED);
      double low = rms(Filters.IIR.highPass(sine(100, 1.0), SAMPLE_RATE, 1000), SETTLED);

      assertTrue(high > 0.9 * UNIT_SINE_RMS, "10 kHz should pass nearly unchanged, got " + high);
      assertTrue(low < 0.2 * UNIT_SINE_RMS, "100 Hz should be strongly attenuated, got " + low);
      assertTrue(high > low);
    }

    @Test
    void HighPassRejectsDc() {
      double[] out = Filters.IIR.highPass(constant(0.5), SAMPLE_RATE, 1000);
      assertEquals(0.0, rms(out, SETTLED), 1e-6);
    }

    @Test
    void BandPassPassesMidAttenuatesEdges() {
      double mid = rms(Filters.IIR.bandPass(sine(1000, 1.0), SAMPLE_RATE, 500, 2000), SETTLED);
      double low = rms(Filters.IIR.bandPass(sine(50, 1.0), SAMPLE_RATE, 500, 2000), SETTLED);
      double high = rms(Filters.IIR.bandPass(sine(15000, 1.0), SAMPLE_RATE, 500, 2000), SETTLED);

      assertTrue(mid > 0.5 * UNIT_SINE_RMS, "1 kHz should sit in the band, got " + mid);
      assertTrue(low < 0.3 * UNIT_SINE_RMS, "50 Hz should be attenuated, got " + low);
      assertTrue(high < 0.3 * UNIT_SINE_RMS, "15 kHz should be attenuated, got " + high);
      assertTrue(mid > low);
      assertTrue(mid > high);
    }

    @Test
    void BandStopAttenuatesMidPassesEdges() {
      double mid = rms(Filters.IIR.bandStop(sine(1000, 1.0), SAMPLE_RATE, 200, 5000), SETTLED);
      double low = rms(Filters.IIR.bandStop(sine(50, 1.0), SAMPLE_RATE, 200, 5000), SETTLED);
      double high = rms(Filters.IIR.bandStop(sine(15000, 1.0), SAMPLE_RATE, 200, 5000), SETTLED);

      // The first-order high-pass caps below unity near Nyquist, so the upper edge passes less
      // cleanly than the lower one; the meaningful property is that the mid is notched far below both.
      assertTrue(low > 0.8 * UNIT_SINE_RMS, "50 Hz should pass, got " + low);
      assertTrue(high > 0.5 * UNIT_SINE_RMS, "15 kHz should pass, got " + high);
      assertTrue(mid < 0.5 * low, "1 kHz should be in the rejected band, mid=" + mid + " low=" + low);
      assertTrue(mid < 0.5 * high, "1 kHz should be in the rejected band, mid=" + mid + " high=" + high);
    }

    @Test
    void BandStopPassesDc() {
      double[] out = Filters.IIR.bandStop(constant(0.5), SAMPLE_RATE, 200, 5000);
      assertEquals(0.5, out[out.length - 1], 1e-3);
    }


    @Test
    void filtersReturnNewArrayAndDoNotMutateInput() {
      double[] input = sine(440, 1.0);
      double[] copy = input.clone();

      double[][] outputs = {
          Filters.IIR.lowPass(input, SAMPLE_RATE, 1000),
          Filters.IIR.highPass(input, SAMPLE_RATE, 1000),
          Filters.IIR.bandPass(input, SAMPLE_RATE, 500, 2000),
          Filters.IIR.bandStop(input, SAMPLE_RATE, 200, 5000),
      };

      assertArrayEquals(copy, input, "input must not be mutated");
      for (double[] out : outputs) {
        assertEquals(input.length, out.length);
        assertNotSame(out, input);
      }
    }

    @Test
    void filtersHandleEmptyInput() {
      double[] empty = new double[0];
      assertEquals(0, Filters.IIR.lowPass(empty, SAMPLE_RATE, 1000).length);
      assertEquals(0, Filters.IIR.highPass(empty, SAMPLE_RATE, 1000).length);
      assertEquals(0, Filters.IIR.bandPass(empty, SAMPLE_RATE, 500, 2000).length);
      assertEquals(0, Filters.IIR.bandStop(empty, SAMPLE_RATE, 200, 5000).length);
    }
  }




  static class RBJ {
    @Test
    void biquadLowPassPassesLowAttenuatesHigh() {
      double low = rms(Filters.RBJ.lowPass(sine(100, 1.0), SAMPLE_RATE, 1000, BUTTERWORTH_Q), SETTLED);
      double high = rms(Filters.RBJ.lowPass(sine(10000, 1.0), SAMPLE_RATE, 1000, BUTTERWORTH_Q), SETTLED);

      assertTrue(low > 0.9 * UNIT_SINE_RMS, "100 Hz should pass nearly unchanged, got " + low);
      assertTrue(high < 0.2 * UNIT_SINE_RMS, "10 kHz should be strongly attenuated, got " + high);
      assertTrue(low > high);
    }

    @Test
    void biquadLowPassPassesDc() {
      double[] out = Filters.RBJ.lowPass(constant(0.5), SAMPLE_RATE, 1000, BUTTERWORTH_Q);
      assertEquals(0.5, out[out.length - 1], 1e-3);
    }

    @Test
    void highPassPassesHighAttenuatesLow() {
      double high = rms(Filters.RBJ.highPass(sine(10000, 1.0), SAMPLE_RATE, 1000, BUTTERWORTH_Q), SETTLED);
      double low = rms(Filters.RBJ.highPass(sine(100, 1.0), SAMPLE_RATE, 1000, BUTTERWORTH_Q), SETTLED);

      assertTrue(high > 0.9 * UNIT_SINE_RMS, "10 kHz should pass nearly unchanged, got " + high);
      assertTrue(low < 0.2 * UNIT_SINE_RMS, "100 Hz should be strongly attenuated, got " + low);
      assertTrue(high > low);
    }

    @Test
    void highPassRejectsDc() {
      double[] out = Filters.RBJ.highPass(constant(0.5), SAMPLE_RATE, 1000, BUTTERWORTH_Q);
      assertEquals(0.0, rms(out, SETTLED), 1e-6);
    }

    @Test
    void bandPassPassesCenterAttenuatesEdges() {
      double center = rms(Filters.RBJ.bandPass(sine(1000, 1.0), SAMPLE_RATE, 1000, 1.0), SETTLED);
      double low = rms(Filters.RBJ.bandPass(sine(100, 1.0), SAMPLE_RATE, 1000, 1.0), SETTLED);
      double high = rms(Filters.RBJ.bandPass(sine(10000, 1.0), SAMPLE_RATE, 1000, 1.0), SETTLED);

      // Constant 0 dB peak gain -> unity at the center frequency.
      assertEquals(UNIT_SINE_RMS, center, 0.05);
      assertTrue(center > low, "center should beat 100 Hz, center=" + center + " low=" + low);
      assertTrue(center > high, "center should beat 10 kHz, center=" + center + " high=" + high);
    }

    @Test
    void bandPassRejectsDc() {
      double[] out = Filters.RBJ.bandPass(constant(0.5), SAMPLE_RATE, 1000, 1.0);
      assertEquals(0.0, rms(out, SETTLED), 1e-6);
    }

    @Test
    void bandStopAttenuatesCenterPassesEdges() {
      double center = rms(Filters.RBJ.bandStop(sine(1000, 1.0), SAMPLE_RATE, 1000, 2.0), SETTLED);
      double low = rms(Filters.RBJ.bandStop(sine(100, 1.0), SAMPLE_RATE, 1000, 2.0), SETTLED);
      double high = rms(Filters.RBJ.bandStop(sine(10000, 1.0), SAMPLE_RATE, 1000, 2.0), SETTLED);

      // Notch has an exact null at the center frequency.
      assertTrue(center < 0.1 * UNIT_SINE_RMS, "center should be notched, got " + center);
      assertTrue(low > 0.9 * UNIT_SINE_RMS, "100 Hz should pass, got " + low);
      assertTrue(high > 0.9 * UNIT_SINE_RMS, "10 kHz should pass, got " + high);
    }

    @Test
    void bandStopPassesDc() {
      double[] out = Filters.RBJ.bandStop(constant(0.5), SAMPLE_RATE, 1000, 2.0);
      assertEquals(0.5, out[out.length - 1], 1e-3);
    }


    @Test
    void filtersReturnNewArrayAndDoNotMutateInput() {
      double[] input = sine(440, 1.0);
      double[] copy = input.clone();

      double[][] outputs = {
          Filters.RBJ.lowPass(input, SAMPLE_RATE, 1000, BUTTERWORTH_Q),
          Filters.RBJ.highPass(input, SAMPLE_RATE, 1000, BUTTERWORTH_Q),
          Filters.RBJ.bandPass(input, SAMPLE_RATE, 1000, 1.0),
          Filters.RBJ.bandStop(input, SAMPLE_RATE, 1000, 2.0),
      };

      assertArrayEquals(copy, input, "input must not be mutated");
      for (double[] out : outputs) {
        assertEquals(input.length, out.length);
        assertNotSame(out, input);
      }
    }

    @Test
    void filtersHandleEmptyInput() {
      double[] empty = new double[0];
      assertEquals(0, Filters.RBJ.lowPass(empty, SAMPLE_RATE, 1000, BUTTERWORTH_Q).length);
      assertEquals(0, Filters.RBJ.highPass(empty, SAMPLE_RATE, 1000, BUTTERWORTH_Q).length);
      assertEquals(0, Filters.RBJ.bandPass(empty, SAMPLE_RATE, 1000, 1.0).length);
      assertEquals(0, Filters.RBJ.bandStop(empty, SAMPLE_RATE, 1000, 2.0).length);
    }
  }






  private static double[] sine(double frequency, double amplitude) {
    double[] samples = new double[SAMPLES];
    for (int i = 0; i < SAMPLES; i++) {
      samples[i] = amplitude * Math.sin(2 * Math.PI * frequency * i / SAMPLE_RATE);
    }
    return samples;
  }

  private static double[] constant(double level) {
    double[] samples = new double[SAMPLES];
    Arrays.fill(samples, level);
    return samples;
  }

  private static double rms(double[] x, int from) {
    double sum = 0;
    for (int i = from; i < x.length; i++) {
      sum += x[i] * x[i];
    }
    return Math.sqrt(sum / (x.length - from));
  }
}
