/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Audiolib is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Audiolib. If not, see <https://www.gnu.org/licenses/>.
 */
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
