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

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public class FrameInputStream extends BufferedInputStream implements FrameSupport {

  private final int frameSize;


  public FrameInputStream(AudioInputStream ais) {
    this(ais, ais.getFormat().getFrameSize()); //taking the frameSize only works with uncompressed audio
    
    AudioFormat af = ais.getFormat();
    if(af.getFrameRate() != af.getSampleRate()) {
      throw new IllegalArgumentException("The stream must be uncompressed (frameRate: " + af.getFrameRate() + ", sampleRate: " + af.getSampleRate());
    }
    
    if(af.getChannels() != 1) {
      throw new IllegalArgumentException("Illegal number of channels: " + af.getChannels());
    }
  }

  public FrameInputStream(InputStream is, int frameSize) {
    super(is);
    this.frameSize = frameSize;
  }
  

  @Override
  public int readFrames(double[] buffer, int start, int len) throws IOException {
    int count = 0;

    byte[] frameBuffer = new byte[frameSize];
    
    try {
      for(int i = 0; i < len; i++) {
        read(frameBuffer);
        // Rebuilding frame as double
        long l = ByteUtils.bytesToLongSignedLSB(frameBuffer); // assuming we're working with SIGNED PCM WAV's (LSB byte order)
        buffer[start+i] = l;
        count++;
      }
    }
    catch(EOFException e) {
      // Nothing. means we're done too
    }
    return count;
  }

  
  @Override
  public int availableFrames() throws IOException {
    return available() / frameSize;
  }

  @Override
  public long skipFrames(long n) throws IOException {
    return skip(n * frameSize);
  }

  @Override
  public synchronized void markFrames(int readlimit) {
    mark(readlimit * frameSize);
  }
}