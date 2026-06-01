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

import edu.princeton.cs.algs4.Complex;


public final class ComplexUtils {

  private ComplexUtils() {}

  /**
   * Computes max amplitude of the fft sample
   */
  public static double getMaxMagnitude(Complex[][] matrix) {
    double max = 0.0;
    for (Complex[] complexes : matrix) {
      double amp = getMaxMagnitude(complexes);
      if (amp > max) {
        max = amp;
      }
    }
    return max;
  }

  
  public static double getMaxMagnitude(Complex[] array) {
    double max = 0.0;
    for(int i = 1; i < array.length; i++) { // 1 because the first row is not significant (convolution-wise)
      double amp = array[i].abs(); // magnitude of the sound at a given frequency/slice
      if(amp > max) {
        max = amp;
      }
    }
    return max;
  }
  
  /**
   * Put the time domain data into a complex number with imaginary part as 0
   * len may be > reals.length in which case the array is padded with (complex) zeroes
   */
  public static Complex[] realToComplex(double[] reals, int start, int len) {
    Complex[] result = new Complex[len];
    
    int max = Math.min(reals.length - start, len);
    
    int i = 0;
    for(int j = start; i < max; i++, j++) {
      result[i] = new Complex(reals[j], 0.0);
    }
    
    // Then pad with Complex(0,0) for the remainder of the complex list [max..len] ???
    for(; i < len; i++) {
      result[i] = new Complex(0.0, 0.0);
    }
    
    return result;
  }

  public static Complex[] realToComplex(double[] reals) {
    return realToComplex(reals, 0, reals.length);
  }
  
  public static double[][] abs(Complex[][] matrix) {
    double[][] abs = new double[matrix.length][];
    
    for(int i = 0; i < matrix.length; i++) {
      abs[i] = abs(matrix[i]);
    }
    
    return abs;
  }

  public static double[] abs(Complex[] array) {
    double[] abs = new double[array.length];
    
    for(int i = 0; i < array.length; i++) {
      abs[i] = array[i].abs();
    }
    
    return abs;
  }
}
