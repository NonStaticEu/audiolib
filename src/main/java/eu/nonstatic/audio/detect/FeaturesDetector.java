package eu.nonstatic.audio.detect;

import eu.nonstatic.audio.AudioUtils;
import eu.nonstatic.audio.Sampling;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

public class FeaturesDetector {

  private final BpmDetector bpmDetector = new BpmDetector();
  private final KeyDetector keyDetector = new KeyDetector();

  public Features detect(InputStream is) throws IOException, UnsupportedAudioFileException {
    return detect(AudioUtils.getMonoInputStream(is));
  }

  public Features detect(AudioInputStream ais) throws IOException {
    Sampling sampling = Sampling.mono(ais);
    return detect(sampling);
  }

  public Features detect(Sampling sampling) {
    return new Features(bpmDetector.detect(sampling), keyDetector.detect(sampling));
  }
}
