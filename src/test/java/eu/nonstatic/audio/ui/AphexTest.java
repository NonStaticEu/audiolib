package eu.nonstatic.audio.ui;

import static eu.nonstatic.audio.AudioUtils.getOneSecondFrames;

import edu.princeton.cs.algs4.Complex;
import eu.nonstatic.audio.AudioAnalyzer;
import eu.nonstatic.audio.AudioUtils;
import eu.nonstatic.audio.FixtureLoader;
import eu.nonstatic.audio.FrameInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

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
}
