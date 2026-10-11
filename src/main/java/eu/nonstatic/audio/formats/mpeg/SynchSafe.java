/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Audiolib is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Audiolib. If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.formats.mpeg;

/**
 * <a href="https://phoxis.org/2010/05/08/synch-safe/">...</a>
 * <a href="http://en.wikipedia.org/wiki/Synchsafe">...</a>
 */
public class SynchSafe {

  private SynchSafe() {}

  public static int toInt(byte[] bytes) {
    return (((bytes[0] << 7 | (bytes[1]&0xff)) << 7) | (bytes[2]&0xff)) << 7 | (bytes[3]&0xff);
  }

  public static byte[] toBytes(int i) {
    byte b0 = (byte)(i >> 21);
    byte b1 = (byte)(i >> 14 & 0xff);
    byte b2 = (byte)(i >> 7 & 0xff);
    byte b3 = (byte)(i & 0xff);
    return new byte[]{b0, b1, b2, b3};
  }

}
