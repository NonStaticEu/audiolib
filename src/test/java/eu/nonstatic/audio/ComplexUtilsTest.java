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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.princeton.cs.algs4.Complex;
import org.junit.jupiter.api.Test;

class ComplexUtilsTest {

  @Test
  void should_get_max_magnitude_of_array_ignoring_first_element() {
    Complex[] array = {
        new Complex(100.0, 0.0), // ignored, index 0
        new Complex(3.0, 4.0),   // magnitude 5
        new Complex(1.0, 1.0)    // magnitude sqrt(2)
    };
    assertEquals(5.0, ComplexUtils.getMaxMagnitude(array), 1e-9);
  }

  @Test
  void should_return_zero_max_magnitude_for_single_element_array() {
    Complex[] array = {new Complex(100.0, 0.0)};
    assertEquals(0.0, ComplexUtils.getMaxMagnitude(array), 1e-9);
  }

  @Test
  void should_get_max_magnitude_of_matrix() {
    Complex[][] matrix = {
        {new Complex(0.0, 0.0), new Complex(3.0, 4.0)},
        {new Complex(0.0, 0.0), new Complex(1.0, 1.0)}
    };
    assertEquals(5.0, ComplexUtils.getMaxMagnitude(matrix), 1e-9);
  }

  @Test
  void should_convert_reals_to_complex_array() {
    double[] reals = {1.0, 2.0, 3.0};
    Complex[] complexes = ComplexUtils.realToComplex(reals);
    assertEquals(3, complexes.length);
    assertEquals(1.0, complexes[0].re(), 1e-9);
    assertEquals(0.0, complexes[0].im(), 1e-9);
    assertEquals(2.0, complexes[1].re(), 1e-9);
    assertEquals(3.0, complexes[2].re(), 1e-9);
  }

  @Test
  void should_pad_with_zero_complex_when_len_exceeds_reals_length() {
    double[] reals = {1.0, 2.0};
    Complex[] complexes = ComplexUtils.realToComplex(reals, 0, 4);
    assertEquals(4, complexes.length);
    assertEquals(1.0, complexes[0].re(), 1e-9);
    assertEquals(2.0, complexes[1].re(), 1e-9);
    assertEquals(0.0, complexes[2].re(), 1e-9);
    assertEquals(0.0, complexes[2].im(), 1e-9);
    assertEquals(0.0, complexes[3].re(), 1e-9);
  }

  @Test
  void should_convert_reals_to_complex_with_start_offset() {
    double[] reals = {1.0, 2.0, 3.0, 4.0};
    Complex[] complexes = ComplexUtils.realToComplex(reals, 1, 2);
    assertEquals(2, complexes.length);
    assertEquals(2.0, complexes[0].re(), 1e-9);
    assertEquals(3.0, complexes[1].re(), 1e-9);
  }

  @Test
  void should_compute_abs_of_complex_array() {
    Complex[] array = {new Complex(3.0, 4.0), new Complex(0.0, 0.0)};
    assertArrayEquals(new double[]{5.0, 0.0}, ComplexUtils.abs(array), 1e-9);
  }

  @Test
  void should_compute_abs_of_complex_matrix() {
    Complex[][] matrix = {
        {new Complex(3.0, 4.0)},
        {new Complex(0.0, 5.0)}
    };
    double[][] abs = ComplexUtils.abs(matrix);
    assertArrayEquals(new double[]{5.0}, abs[0], 1e-9);
    assertArrayEquals(new double[]{5.0}, abs[1], 1e-9);
  }
}
