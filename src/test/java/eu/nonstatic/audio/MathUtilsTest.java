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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MathUtilsTest {

  @Test
  void should_detect_power_of_two() {
    assertTrue(MathUtils.isPowerOfTwo(1));
    assertTrue(MathUtils.isPowerOfTwo(2));
    assertTrue(MathUtils.isPowerOfTwo(1024));
  }

  @Test
  void should_reject_non_power_of_two() {
    assertFalse(MathUtils.isPowerOfTwo(0));
    assertFalse(MathUtils.isPowerOfTwo(3));
    assertFalse(MathUtils.isPowerOfTwo(1023));
  }

  @Test
  void should_get_nearest_power_of_two() {
    assertEquals(8, MathUtils.getNearestPowerOfTwo(9));
    assertEquals(8, MathUtils.getNearestPowerOfTwo(7));
    assertEquals(1, MathUtils.getNearestPowerOfTwo(1));
  }

  @Test
  void should_get_superior_power_of_two() {
    assertEquals(16, MathUtils.getSuperiorPowerOfTwo(9));
    assertEquals(8, MathUtils.getSuperiorPowerOfTwo(8));
    assertEquals(1, MathUtils.getSuperiorPowerOfTwo(1));
  }

  @Test
  void should_get_max_of_array() {
    assertEquals(5.0, MathUtils.max(new double[]{1.0, 5.0, -3.0, 2.0}));
  }

  @Test
  void should_throw_on_max_of_empty_array() {
    assertThrows(IllegalArgumentException.class, () -> MathUtils.max(new double[0]));
  }

  @Test
  void should_get_max_of_matrix() {
    double[][] matrix = {{1.0, 2.0}, {5.0, -3.0}, {0.0, 4.0}};
    assertEquals(5.0, MathUtils.max(matrix));
  }

  @Test
  void should_raise_array_values_below_v_up_to_v() {
    // Despite the name, this raises values that are lower than v up to v (a floor, not a cap).
    double[] array = {1.0, 5.0, 3.0};
    MathUtils.max(4.0, array);
    assertEquals(4.0, array[0]);
    assertEquals(5.0, array[1]);
    assertEquals(4.0, array[2]);
  }

  @Test
  void should_raise_matrix_values_below_v_up_to_v() {
    double[][] matrix = {{1.0, 5.0}, {6.0, 2.0}};
    MathUtils.max(4.0, matrix);
    assertEquals(4.0, matrix[0][0]);
    assertEquals(5.0, matrix[0][1]);
    assertEquals(6.0, matrix[1][0]);
    assertEquals(4.0, matrix[1][1]);
  }

  @Test
  void should_apply_log_to_array() {
    double[] array = {1.0, Math.E};
    MathUtils.log(array);
    assertEquals(0.0, array[0], 1e-9);
    assertEquals(1.0, array[1], 1e-9);
  }

  @Test
  void should_apply_log_to_matrix() {
    double[][] matrix = {{1.0, Math.E}};
    MathUtils.log(matrix);
    assertEquals(0.0, matrix[0][0], 1e-9);
    assertEquals(1.0, matrix[0][1], 1e-9);
  }

  @Test
  void should_compute_mean_of_matrix() {
    double[][] matrix = {{1.0, 2.0}, {3.0, 4.0}};
    assertEquals(2.5, MathUtils.mean(matrix));
  }

  @Test
  void should_return_nan_mean_for_empty_matrix() {
    assertTrue(Double.isNaN(MathUtils.mean(new double[0][0])));
  }

  @Test
  void should_subtract_from_array() {
    double[] array = {5.0, 3.0, 1.0};
    MathUtils.minus(array, 1.0);
    assertEquals(4.0, array[0]);
    assertEquals(2.0, array[1]);
    assertEquals(0.0, array[2]);
  }

  @Test
  void should_subtract_from_matrix() {
    double[][] matrix = {{5.0, 3.0}, {1.0, 0.0}};
    MathUtils.minus(matrix, 1.0);
    assertEquals(4.0, matrix[0][0]);
    assertEquals(2.0, matrix[0][1]);
    assertEquals(0.0, matrix[1][0]);
    assertEquals(-1.0, matrix[1][1]);
  }

  @Test
  void should_dampen_number_to_nearest_fuzz_multiple() {
    assertEquals(10, MathUtils.dampen(11, 5));
    assertEquals(10, MathUtils.dampen(12, 5));
    assertEquals(15, MathUtils.dampen(13, 5));
    assertEquals(0, MathUtils.dampen(0, 5));
  }
}
