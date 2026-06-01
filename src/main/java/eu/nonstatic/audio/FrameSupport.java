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

import java.io.IOException;

public interface FrameSupport {

  default int readFrames(double[] buffer) throws IOException {
    return readFrames(buffer, 0, buffer.length);
  }

  int readFrames(double[] buffer, int start, int len) throws IOException;

  int availableFrames() throws IOException;
  long skipFrames(long n) throws IOException;

  boolean markSupported();
  void markFrames(int readlimit);
  void reset() throws IOException;
}