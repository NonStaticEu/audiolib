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

public final class MathUtils {

  private MathUtils() {}

  public static boolean isPowerOfTwo(int i) {
    return (i != 0) && ((i & (i - 1)) == 0);
  }

  public static int getNearestPowerOfTwo(int i) {
    return 1 << (int)Math.round(Math.log(i) / Math.log(2)); //remember a^x = e^(x.ln(a))
  }

  public static int getSuperiorPowerOfTwo(int i) {
    return 1 << (int)Math.ceil(Math.log(i) / Math.log(2));
  }
  
  public static double max(double[][] matrix) {
    double max = Double.NEGATIVE_INFINITY;
    
    for(double[] array : matrix) {
      double arrayMax = max(array);
      if(arrayMax > max) {
        max = arrayMax;
      }
    }
    
    return max;
  }

  public static double max(double[] array) {
    if(array.length == 0) {
      throw new IllegalArgumentException("Empty array");
    }
    
    double max = Double.NEGATIVE_INFINITY;
    for(double d : array) {
      if(d > max) {
        max = d;
      }
    }
    
    return max;
  }
  
  public static void max(double v, double[][] matrix) {
    for(double[] array : matrix) {
      max(v, array);
    }
  }
  
  public static void max(double v, double[] array) {
    for(int i = 0; i < array.length; i++) {
      if(v > array[i]) {
        array[i] = v;
      }
    }
  }
  
  public static void log(double[][] matrix) {
    for(double[] array : matrix) {
      log(array);
    }
  }
  
  public static void log(double[] array) {
    for(int i = 0; i < array.length; i++) {
      array[i] = Math.log(array[i]);
    }
  }
  
  public static double mean(double[][] matrix) {
    double s = 0.0;
    int n = 0;
    for(double[] array : matrix) {
      for(double d : array) {
        s += d;
      }
      n += array.length;
    }
    return s/n;
  }
  
  public static void minus(double[][] matrix, double v) {
    for(double[] array : matrix) {
      minus(array, v);
    }
  }
  
  public static void minus(double[] array, double v) {
    for(int i = 0; i < array.length; i++) {
      array[i] -= v;
    }
  }
  
  /**
   * @param number
   * @param fuzz how much to dampen, better for distributaion if it's an odd number
   * @return
   */
  public static int dampen(int number, int fuzz) {
    return fuzz * ((number + fuzz/2)/fuzz); //could also be number - number%fuzz but it's not distributed AROUND number
  }
}