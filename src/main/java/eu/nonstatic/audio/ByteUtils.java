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

public final class ByteUtils {

  private ByteUtils() {}

  /**
   * MSB / Big endian case (RIFX)
   */
  public static long bytesToLongSignedMSB(byte[] frameBuffer, int start, int len) {
    long l = 0;
    if(frameBuffer.length > 0) {
      l = frameBuffer[start]; //first byte remains signed
      for(int r = 1; r < len; r++) {
        l = (l << 8) + (frameBuffer[start + r] & 0xffL);
      }
    }
    return l;
  }

  public static long bytesToLongSignedMSB(byte[] frameBuffer) {
    return bytesToLongSignedMSB(frameBuffer, 0, frameBuffer.length);
  }

  /**
   * MSB / Big endian case (RIFX)
   */
  public static long bytesToLongUnsignedMSB(byte[] frameBuffer, int start, int len) {
    long l = 0;
    for(int r = 0; r < len; r++) {
      l = (l << 8) + (frameBuffer[start + r] & 0xffL);
    }
    return l;
  }

  public static long bytesToLongUnsignedMSB(byte[] frameBuffer) {
    return bytesToLongUnsignedMSB(frameBuffer, 0, frameBuffer.length);
  }

  
  /**
   * LSB / Little endian case (WAV/RIFF)
   */
  public static long bytesToLongSignedLSB(byte[] frameBuffer, int start, int len) {
    long l = 0;
    if(frameBuffer.length > 0) {
      int last = start+len-1;
      l = (long) frameBuffer[last] << (8 * last); //last byte remains signed
      for(int r = len-2; r > 0; r--) {
        l += (frameBuffer[start + r] & 0xffL) << (8 * r);
      }
    }
    return l;
  }

  public static long bytesToLongSignedLSB(byte[] frameBuffer) {
    return bytesToLongSignedLSB(frameBuffer, 0, frameBuffer.length);
  }

  /**
   * LSB / Little endian case (WAV/RIFF)
   */
  public static long bytesToLongUnsignedLSB(byte[] frameBuffer, int start, int len) {
    long l = 0;
    for(int r = len-1; r >= 0; r--) {
      l += (frameBuffer[start + r] & 0xffL) << (8 * r);
    }
    return l;
  }

  public static long bytesToLongUnsignedLSB(byte[] frameBuffer) {
    return bytesToLongUnsignedLSB(frameBuffer, 0, frameBuffer.length);
  }
  
  public static byte[] intToBytes(int value) {
    return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)value};
  }
  
  public static String bytesToHexString(byte[] buf) {
    StringBuilder strbuf = new StringBuilder(buf.length * 2);
    for (byte b : buf) {
      if ((b & 0xff) < 0x10) {
        strbuf.append('0');
      }
      strbuf.append(Integer.toString(b & 0xff, 16));
    }
    return strbuf.toString();
  }

}
