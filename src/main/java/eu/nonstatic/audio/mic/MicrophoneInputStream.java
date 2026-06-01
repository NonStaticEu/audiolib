/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.mic;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import lombok.Getter;

public class MicrophoneInputStream extends InputStream {

  @Getter
  private final AudioFormat audioFormat;
  private final byte[] buffer;

  private TargetDataLine targetDataLine;
  private int readBytes;
  private int cursor;
  
  
  public MicrophoneInputStream() throws IOException {
    this( new MicrophoneAudioFormat());
  }
  
  public MicrophoneInputStream(AudioFormat audioFormat) throws IOException {
    super();
    this.audioFormat = audioFormat;
    this.buffer = new byte[getOneSecondBufferSize()];
    startLine();
  }

  /**
   * @return One second of sound
   */
  private int getOneSecondBufferSize() {
    return (int)(audioFormat.getSampleRate() * audioFormat.getSampleSizeInBits() * audioFormat.getChannels()) / 8;
  }
  
  public void startLine() throws IOException {
    DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

    if (AudioSystem.isLineSupported(dataLineInfo)) {
      try {
        targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        targetDataLine.open(audioFormat);
        targetDataLine.start();
      }
      catch(LineUnavailableException e) {
        throw new IOException(e);
      }
    }
    else {
      throw new IOException("Microphone not supported");
    }
  }

  public void stopLine() {
    targetDataLine.stop();
  }
  
  @Override
  public void close() throws IOException {
    super.close();
    stopLine();
  }

  @Override
  public int read() throws IOException {
    if(cursor >= readBytes) {
      readBytes = targetDataLine.read(buffer, 0, buffer.length);
      cursor = 0;
    }
    return buffer[cursor++];
  }
}
