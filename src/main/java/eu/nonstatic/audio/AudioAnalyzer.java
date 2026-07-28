/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio;

import static eu.nonstatic.audio.AudioUtils.timeToFrames;

import edu.princeton.cs.algs4.Complex;
import edu.princeton.cs.algs4.FFT;
import eu.nonstatic.audio.formats.AudioInfo;
import java.io.IOException;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;

/**
 * @see "https://labrosa.ee.columbia.edu/~dpwe/resources/matlab/fingerprint/"
 * @see "https://dali.feld.cvut.cz/ucebna/matlab/toolbox/signal/specgram.html"
 * @see "https://cas.web.cern.ch/cas/Denmark-2010/Caspers/Tektronix%20%20primer%20on%20overlapping%20FFT%20signals%202009%20CAS2010.pdf"
 * @see "https://www.physik.uni-wuerzburg.de/~praktiku/Anleitung/Fremde/ANO14.pdf"
 */
public record AudioAnalyzer(int windowFrames, int overlapFrames) {

  private static final float DEFAULT_OVERLAP_RATIO = 0.5f; // 50% default overlap similarly to specgram in Matlab

  public AudioAnalyzer(AudioFormat af, float windowDuration, float overlapRatio) {
    this(MathUtils.getNearestPowerOfTwo(
            timeToFrames(af.getSampleRate(), windowDuration)), //we are using af.getSampleRate() as the uncompressed frame rate (should be the same thing)
        overlapRatio);
  }

  public AudioAnalyzer(AudioFormat af, float windowDuration) {
    this(af, windowDuration, DEFAULT_OVERLAP_RATIO);
  }

  public AudioAnalyzer(int windowFrames) {
    this(windowFrames, DEFAULT_OVERLAP_RATIO);
  }

  /**
   * windowSize muct be a power of 2 as per the Princeton FFT implementation The higher windowSize, the better the frequency resolution. I'd advice 256 or 512. Caution, windowSize
   * represents a different timespan depending on the sampling rate (eg 512 on 8000Hz is 64ms, 512 on 44100Hz is 11.6 ms)
   */
  public AudioAnalyzer(int windowFrames, float overlapRatio) {
    this(windowFrames, (int) (windowFrames * overlapRatio));
  }

  public AudioAnalyzer {
    if (!MathUtils.isPowerOfTwo(windowFrames)) { // window must be > 0, but this test also ensures it
      throw new IllegalArgumentException("windowFrames is not a power of 2: " + windowFrames); //necessary given our FFT implementation
    } else if (overlapFrames < 0) {
      throw new IllegalArgumentException("overlapFrames can't be negative: " + overlapFrames);
    } else if (overlapFrames >= windowFrames) {
      throw new IllegalArgumentException("overlapFrames must be strictly < windowFrames");
    }

  }

  /**
   * This is NOT the window duration! This is duration of the window without the overlap in seconds
   */
  public float getWindowLapse(float uncompressedFrameRate) {
    return AudioUtils.framesToTime(uncompressedFrameRate, windowFrames - overlapFrames);
  }

  /**
   * Different from Matlab's spectrogram because it counts how many windows are necessary to cover the whole len, even if one of the windows is zero-padded. It seems Matlab only
   * keeps those windows entirely filled: floor((len-windowFrames)/(windowFrames-overlapFrames))
   */
  public int getWindowsCount(int sampleFrames) {
    return 1 + (sampleFrames - overlapFrames - 1) / (windowFrames - overlapFrames); // that is 1+ceil((sampleFrames-windowFrames+(windowsFrames-overlapFrames-1))/(windowFrames-overlapFrames)) == 1 + (sampleFrames - overlapFrames - 1) / (windowFrames - overlapFrames)
  }

  /**
   * Will always return a buffer of size windowFrames so that the number of frequencies of the fft remains constant
   */
  public double[] readWindow(FrameSupport fis) throws IOException {
    if (!fis.markSupported()) { // I can't simply fallback to "no overlap", the number of windows wouldn't match anymore the number that's actually read
      throw new UnsupportedOperationException("Input Stream must support marking");
    }

    double[] buffer = new double[windowFrames];

    if (overlapFrames > 0) {
      fis.markFrames(windowFrames);
      fis.readFrames(buffer);
      fis.reset();
      fis.skipFrames((long)windowFrames - overlapFrames);
    } else {
      fis.readFrames(buffer);
    }

    return buffer;
  }

  /**
   * Like specgram in Matlab
   */
  public Complex[] fft(double[] audioFrames, int start, int len) {
    double[] hanning = hanning(audioFrames, start, len);
    Complex[] complexFrames = ComplexUtils.realToComplex(hanning);
    Complex[] fft = FFT.fft(complexFrames);
    return cleanConjugates(fft); // since the input was real
  }

  public Complex[] fft(double[] audioWindow) {
    return fft(audioWindow, 0, audioWindow.length);
  }

  /**
   * Performs a Hanning Window
   *
   * @see "https://en.wikipedia.org/wiki/Window_function"
   * @see "https://en.wikipedia.org/wiki/Spectral_leakage"
   * @see "https://en.wikipedia.org/wiki/Hann_function"
   * @see "https://www.edn.com/electronics-news/4383713/Windowing-Functions-Improve-FFT-Results-Part-I"
   * @see "https://www.physik.uni-wuerzburg.de/~praktiku/Anleitung/Fremde/ANO14.pdf"
   *
   */
  public static double[] hanning(double[] audioBuffer, int start, int len) {
    len = Math.min(audioBuffer.length - start, len);
    double[] result = Arrays.copyOfRange(audioBuffer, start, start+len);
    int N = len - 1;
    for (int i = 0, n = 0; i < len; i++, n++) {
      result[i] = result[i] * 0.5 * (1.0 - Math.cos(2.0 * Math.PI * n / N));
    }
    return result;
  }

  public double[] hanning(double[] audioBuffer) {
    return hanning(audioBuffer, 0, audioBuffer.length);
  }

  /**
   * Since the input is only real data (audio), we can get rid of the "mirrors" (conjugates) in the frequency world
   * <p>
   * The first bin in the FFT is DC (0 Hz), that is the average of the input, the second bin is Fs / N, where Fs is the sample rate and N is the size of the FFT. The next bin is 2
   * * Fs / N. To express this in general terms, the nth bin is n * Fs / N. When N is even, the bin at N / 2 is the energy at the Nyquist frequency which is also not useful in
   * practical applications
   * <p>
   * This algorithm is different from Matlab's specgram as it used to truncate to the first window/2 + 1 points for window even and (window + 1)/2 for nfft odd We're keping only
   * the last (window-1)/2 bins here thus having a Pi phase shift and no DC(0), neither Nyquist freq whilst keeping the bins in increasing freq order.
   *
   * @see "https://stackoverflow.com/questions/4364823/how-to-get-frequency-from-fft-result/4371627#4371627"
   * @see "https://dsp.stackexchange.com/questions/4825/why-is-the-fft-mirrored"
   * @see "https://math.stackexchange.com/questions/129804/fft-characteristics"
   * @see "https://dsp.stackexchange.com/questions/14765/adequate-representation-of-frequency-domain-amplitude-magnitude-of-fft-of-a-sign"
   * @see "https://music.columbia.edu/cmc/musicandcomputers/popups/chapter3/xbit_3_3.php"
   */
  public Complex[] cleanConjugates(Complex[] fftWindow) {
    int length = (fftWindow.length - 1) / 2; // that way we never keep the Nyquist freq
    int start = fftWindow.length - 1;

    Complex[] result = new Complex[length];
    for (int i = 0; i < length; i++) {
      result[i] = fftWindow[start - i];
    }
    return result;
  }

  /**
   * Reads one window and performs its fft
   */
  public Complex[] fftWindow(FrameSupport fis) throws IOException {
    double[] audioWindow = readWindow(fis);
    return fft(audioWindow);
  }

  /**
   * @return Complex[number of windows][windowSize-length frequencies] FFT changes the original signal is a set of sine waves. In order for that basis to describe all the possible
   * inputs it needs to be able to represent amplitude (real) and phase (imaginary)
   */
  @Deprecated
  public Complex[][] fftSliding(double[] audioBuffer) {
    return fftSliding(audioBuffer, 0, audioBuffer.length);
  }

  @Deprecated
  public Complex[][] fftSliding(double[] audioBuffer, int start, int len) {
    int windowCount = getWindowsCount(len);

    // When turning into frequency domain, we'll need complex numbers
    Complex[][] fftBuffer = new Complex[windowCount][];

    // For each window
    for (int i = 0, windowStart = start; i < windowCount; i++, windowStart += (windowFrames - overlapFrames)) {
      // Perform FFT analysis on the window
      fftBuffer[i] = fft(audioBuffer, windowStart, windowFrames); // the last chunk may have a smaller length than windowFrames but it will be padded with realToComplex()
    }
    return fftBuffer;
  }

  /**
   * Reads a number of doubles, split into enough windows and performs the ftt on them
   */
  public Complex[][] fftSliding(FrameSupport fis, int frames) throws IOException {
    int windowCount = getWindowsCount(frames);

    // When turning into frequency domain we'll need complex numbers
    Complex[][] fftBuffer = new Complex[windowCount][];

    // For each window
    for (int i = 0; i < windowCount; i++) {
      // Perform FFT analysis on the window:
      fftBuffer[i] = fftWindow(fis);
    }

    return fftBuffer;
  }

  /**
   * @param fis
   * @param ai
   * @return the complete stream's FFT
   */
  public Complex[][] fftSliding(FrameSupport fis, AudioInfo ai) throws IOException {
    return fftSliding(fis, ai.getFrameCount());
  }
}
