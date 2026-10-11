/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Audiolib is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Audiolib. If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.ui;

import static eu.nonstatic.audio.AudioUtils.getOneSecondFrames;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.princeton.cs.algs4.Complex;
import eu.nonstatic.audio.AudioAnalyzer;
import eu.nonstatic.audio.AudioUtils;
import eu.nonstatic.audio.FixtureLoader;
import eu.nonstatic.audio.FrameInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.junit.jupiter.api.Test;

class AphexTest {

  private static final String FILE_NAME = "Mi-1 = -aSn=1NDi[n] [Sj C{i}Fij[n - 1] + [Fexti[[n-1]].wav.gz";

  private static final long DEMON_START_TIME = 5 * 60 + 27; //the demon face starts at 5:27
  private static final int DEMON_DURATION = 10; //and lasts 10 seconds

  private static final int CHUNK_SIZE = 2048; //number of samples per chunk; will be transposed to a number of frequencies

  public static void main(String... args) throws IOException, UnsupportedAudioFileException {
    try(InputStream is = FixtureLoader.getResourceAsStream("/audio/" + FILE_NAME);
        AudioInputStream mis = AudioUtils.getMonoInputStream(is);
        FrameInputStream fis = new FrameInputStream(mis)) {

      //go to the "demon face" start
      int oneSecondFrames = getOneSecondFrames(mis.getFormat());
      fis.skipFrames(DEMON_START_TIME * oneSecondFrames); //we may also use skip() with the amount of bytes in the mono "reference"
      
      long start = System.currentTimeMillis();
      Complex[][] fftSample = new AudioAnalyzer(CHUNK_SIZE, 0).fftSliding(fis, DEMON_DURATION * oneSecondFrames);
      long end = System.currentTimeMillis();
      System.out.println("Duration: " + (end - start) + " ms");
      
      new SpectrumVisualizer(fftSample, true);
    }
  }

  @Test
  void test() {
    assertTrue(true);
  }
}
