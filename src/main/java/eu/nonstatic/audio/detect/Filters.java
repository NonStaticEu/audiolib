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

/**
 * The biquad filters (lowPass, highPass, bandPass, bandStop), each driven by a frequency and a
 * quality factor Q, are second-order RBJ-cookbook sections. Simpler first-order IIR variants are
 * also provided: lowPass and highPass (one cutoff), and bandPassFirstOrder / bandStopFirstOrder
 * (a low and a high edge frequency).
 *
 * @see "https://www.w3.org/TR/audio-eq-cookbook/"
 */
public final class Filters {

  private Filters() {}

  /**
   * First order IIR filters
   */
  public static final class IIR {

    private IIR() {}

    public static double[] lowPass(double[] samples, float sampleRate, double cutoffHz) {
      return lowPass(samples, 0, samples.length, sampleRate, cutoffHz);
    }

    // First-order IIR low-pass: y[n] = y[n-1] + α (x[n] - y[n-1])
    public static double[] lowPass(double[] samples, int start, int len, float sampleRate, double cutoffHz) {
      double rc = 1.0 / (2.0 * Math.PI * cutoffHz);
      double dt = 1.0 / sampleRate;
      double alpha = dt / (rc + dt);
      double[] out = new double[len];
      double y = 0;
      for (int i = 0; i < len; i++) {
        y += alpha * (samples[start+i] - y);
        out[i] = y;
      }
      return out;
    }

    public static double[] highPass(double[] samples, float sampleRate, double cutoffHz) {
      return highPass(samples, 0, samples.length, sampleRate, cutoffHz);
    }

    // First-order IIR high-pass: y[n] = α (y[n-1] + x[n] - x[n-1]),  α = rc / (rc + dt)
    public static double[] highPass(double[] samples, int start, int len, float sampleRate, double cutoffHz) {
      double rc = 1.0 / (2.0 * Math.PI * cutoffHz);
      double dt = 1.0 / sampleRate;
      double alpha = rc / (rc + dt);
      double[] out = new double[len];
      double prevX = 0, prevY = 0;
      for (int i = 0; i < len; i++) {
        double x = samples[start+i];
        double y = alpha * (prevY + x - prevX);
        out[i] = y;
        prevX = x;
        prevY = y;
      }
      return out;
    }

    public static double[] bandPass(double[] samples, float sampleRate, double lowCutoffHz, double highCutoffHz) {
      return bandPass(samples, 0, samples.length, sampleRate, lowCutoffHz, highCutoffHz);
    }

    // First-order band-pass: high-pass at lowCutoffHz cascaded into low-pass at highCutoffHz.
    public static double[] bandPass(double[] samples, int start, int len, float sampleRate, double lowCutoffHz, double highCutoffHz) {
      return lowPass(highPass(samples, start, len, sampleRate, lowCutoffHz), sampleRate, highCutoffHz);
    }

    public static double[] bandStop(double[] samples, float sampleRate, double lowCutoffHz, double highCutoffHz) {
      return bandStop(samples, 0, samples.length, sampleRate, lowCutoffHz, highCutoffHz);
    }

    // First-order band-stop: content below lowCutoffHz (low-pass) summed with above highCutoffHz (high-pass).
    public static double[] bandStop(double[] samples, int start, int len, float sampleRate, double lowCutoffHz, double highCutoffHz) {
      double[] low = lowPass(samples, start, len, sampleRate, lowCutoffHz);
      double[] high = highPass(samples, start, len, sampleRate, highCutoffHz);
      double[] out = new double[len];
      for (int i = 0; i < len; i++) {
        out[i] = low[i] + high[i];
      }
      return out;
    }
  }

  /**
   * Second order RBJ biquad filters
   */
  public static final class RBJ {

    private RBJ() {}

    public static double[] lowPass(double[] samples, float sampleRate, double cutoffHz, double q) {
      return lowPass(samples, 0, samples.length, sampleRate, cutoffHz, q);
    }

    // Second-order RBJ biquad low-pass: passes frequencies below cutoffHz, passes DC.
    public static double[] lowPass(double[] samples, int start, int len, float sampleRate, double cutoffHz, double q) {
      double w0 = 2.0 * Math.PI * cutoffHz / sampleRate;
      double cos = Math.cos(w0);
      double alpha = Math.sin(w0) / (2.0 * q);
      return biquad(samples, start, len, (1 - cos) / 2, 1 - cos, (1 - cos) / 2, 1 + alpha, -2 * cos, 1 - alpha);
    }

    public static double[] highPass(double[] samples, float sampleRate, double cutoffHz, double q) {
      return highPass(samples, 0, samples.length, sampleRate, cutoffHz, q);
    }

    // Second-order RBJ biquad high-pass: rejects DC, passes frequencies above cutoffHz.
    public static double[] highPass(double[] samples, int start, int len, float sampleRate, double cutoffHz, double q) {
      double w0 = 2.0 * Math.PI * cutoffHz / sampleRate;
      double cos = Math.cos(w0);
      double alpha = Math.sin(w0) / (2.0 * q);
      return biquad(samples, start, len, (1 + cos) / 2, -(1 + cos), (1 + cos) / 2, 1 + alpha, -2 * cos, 1 - alpha);
    }

    public static double[] bandPass(double[] samples, float sampleRate, double centerHz, double q) {
      return bandPass(samples, 0, samples.length, sampleRate, centerHz, q);
    }

    // Second-order RBJ biquad band-pass (constant 0 dB peak gain): passes a band around centerHz.
    public static double[] bandPass(double[] samples, int start, int len, float sampleRate, double centerHz, double q) {
      double w0 = 2.0 * Math.PI * centerHz / sampleRate;
      double cos = Math.cos(w0);
      double alpha = Math.sin(w0) / (2.0 * q);
      return biquad(samples, start, len, alpha, 0, -alpha, 1 + alpha, -2 * cos, 1 - alpha);
    }

    public static double[] bandStop(double[] samples, float sampleRate, double centerHz, double q) {
      return bandStop(samples, 0, samples.length, sampleRate, centerHz, q);
    }

    // Second-order RBJ biquad band-stop (notch): rejects a band around centerHz, passes the rest.
    public static double[] bandStop(double[] samples, int start, int len, float sampleRate, double centerHz, double q) {
      double w0 = 2.0 * Math.PI * centerHz / sampleRate;
      double cos = Math.cos(w0);
      double alpha = Math.sin(w0) / (2.0 * q);
      return biquad(samples, start, len, 1, -2 * cos, 1, 1 + alpha, -2 * cos, 1 - alpha);
    }


    // Direct Form I biquad: y[n] = (b0/a0)x[n] + (b1/a0)x[n-1] + (b2/a0)x[n-2] - (a1/a0)y[n-1] - (a2/a0)y[n-2]
    private static double[] biquad(double[] samples, int start, int len, double b0, double b1, double b2, double a0, double a1, double a2) {
      double nb0 = b0 / a0, nb1 = b1 / a0, nb2 = b2 / a0, na1 = a1 / a0, na2 = a2 / a0;
      double[] out = new double[len];
      double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
      for (int i = 0; i < len; i++) {
        double x = samples[start+i];
        double y = nb0 * x + nb1 * x1 + nb2 * x2 - na1 * y1 - na2 * y2;
        out[i] = y;
        x2 = x1;
        x1 = x;
        y2 = y1;
        y1 = y;
      }
      return out;
    }
  }
}
