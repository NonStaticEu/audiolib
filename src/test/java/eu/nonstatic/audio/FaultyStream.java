/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio;

import java.io.IOException;
import java.io.InputStream;

public class FaultyStream extends InputStream {

  final InputStream is;
  final int afterReads;
  int readCount;

  public FaultyStream() {
    this(null, 0);
  }

  public FaultyStream(InputStream is, int afterReads) {
    this.is = is;
    this.afterReads = afterReads;
  }

  @Override
  public int read() throws IOException {
    if (readCount >= afterReads) {
      throw new IOException("reads: " + readCount);
    } else {
      int read = is.read();
      readCount++;
      return read;
    }
  }

  @Override
  public void mark(int readlimit) {
    // nope
  }

  @Override
  public void reset() throws IOException {
    // nope
  }

  @Override
  public boolean markSupported() {
    return true; // to avoid wrapping in a BufferedInputStream
  }
}
