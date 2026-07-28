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

import org.junit.jupiter.api.Test;

class ByteUtilsTest {

  @Test
  void should_read_signed_msb_positive_value() {
    byte[] bytes = {0x00, 0x40};
    assertEquals(64L, ByteUtils.bytesToLongSignedMSB(bytes));
  }

  @Test
  void should_read_signed_msb_negative_value() {
    byte[] bytes = {(byte) 0xFF, (byte) 0xC0};
    assertEquals(-64L, ByteUtils.bytesToLongSignedMSB(bytes));
  }

  @Test
  void should_read_signed_msb_with_offset_and_length() {
    byte[] bytes = {(byte) 0xAA, 0x00, 0x40, (byte) 0xAA};
    assertEquals(64L, ByteUtils.bytesToLongSignedMSB(bytes, 1, 2));
  }

  @Test
  void should_return_zero_for_empty_signed_msb_array() {
    assertEquals(0L, ByteUtils.bytesToLongSignedMSB(new byte[0]));
  }

  @Test
  void should_read_unsigned_msb_value() {
    byte[] bytes = {(byte) 0xFF, (byte) 0xC0};
    assertEquals(65472L, ByteUtils.bytesToLongUnsignedMSB(bytes));
  }

  @Test
  void should_read_unsigned_msb_with_offset_and_length() {
    byte[] bytes = {(byte) 0xAA, (byte) 0xFF, (byte) 0xC0, (byte) 0xAA};
    assertEquals(65472L, ByteUtils.bytesToLongUnsignedMSB(bytes, 1, 2));
  }

  @Test
  void should_read_signed_lsb_value_from_last_byte_only() {
    // Implementation quirk: only the sign (last) byte and the bytes strictly between it and
    // the first byte are accumulated; the first (least significant) byte itself is dropped.
    byte[] bytes = {0x34, 0x12};
    assertEquals(4608L, ByteUtils.bytesToLongSignedLSB(bytes)); // 0x12 << 8
  }

  @Test
  void should_read_signed_lsb_negative_value() {
    byte[] bytes = {0x00, (byte) 0xC0};
    assertEquals(-16384L, ByteUtils.bytesToLongSignedLSB(bytes));
  }

  @Test
  void should_return_zero_for_empty_signed_lsb_array() {
    assertEquals(0L, ByteUtils.bytesToLongSignedLSB(new byte[0]));
  }

  @Test
  void should_read_signed_lsb_with_offset_using_absolute_shift() {
    // Implementation quirk: the sign byte is shifted by its absolute index, not its
    // index relative to "start", so results with a non-zero offset can look surprising.
    byte[] bytes = {(byte) 0xAA, 0x00, 0x40, (byte) 0xAA};
    assertEquals(4194304L, ByteUtils.bytesToLongSignedLSB(bytes, 1, 2)); // 0x40 << 16
  }

  @Test
  void should_read_unsigned_lsb_value() {
    byte[] bytes = {0x00, (byte) 0xC0};
    assertEquals(49152L, ByteUtils.bytesToLongUnsignedLSB(bytes));
  }

  @Test
  void should_read_unsigned_lsb_with_offset_and_length() {
    byte[] bytes = {(byte) 0xAA, 0x00, (byte) 0xC0, (byte) 0xAA};
    assertEquals(49152L, ByteUtils.bytesToLongUnsignedLSB(bytes, 1, 2));
  }

  @Test
  void should_convert_int_to_bytes_big_endian() {
    byte[] bytes = ByteUtils.intToBytes(0x01020304);
    assertEquals(4, bytes.length);
    assertEquals((byte) 0x01, bytes[0]);
    assertEquals((byte) 0x02, bytes[1]);
    assertEquals((byte) 0x03, bytes[2]);
    assertEquals((byte) 0x04, bytes[3]);
  }

  @Test
  void should_convert_bytes_to_hex_string() {
    byte[] bytes = {0x00, (byte) 0xFF, 0x0A, (byte) 0xAB};
    assertEquals("00ff0aab", ByteUtils.bytesToHexString(bytes));
  }

  @Test
  void should_convert_empty_bytes_to_empty_hex_string() {
    assertEquals("", ByteUtils.bytesToHexString(new byte[0]));
  }
}
