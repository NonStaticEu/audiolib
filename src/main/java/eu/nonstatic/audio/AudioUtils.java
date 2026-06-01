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
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public final class AudioUtils {

  private AudioUtils() {}

  public static int getOneSecondBytes(AudioFormat af) {
    return (int)(af.getSampleRate() * af.getSampleSizeInBits() * af.getChannels()) / 8;
  }
  
  public static int getOneSecondFrames(AudioFormat af) {
    return (int)(af.getSampleRate() * af.getChannels());
  }


  /**
   * Caution, sampleRate != frameRate in compressed formats
   * @param frameRate
   * @param frames
   * @return time in seconds
   */
  public static float framesToTime(float frameRate, int frames) {
    return frames / frameRate;
  }

  /**
   * Caution, sampleRate != frameRate in compressed formats
   * @param frameRate
   * @param time in seconds
   * @return number of frames
   */
  public static int timeToFrames(float frameRate, float time) {
    return (int)(time * frameRate);
  }

  
  
  public static AudioFormat toStandardStereoPcmFormat(AudioFormat sourceFormat) {
    return new AudioFormat(Encoding.PCM_SIGNED, //tells whether samples are signed, forcing to PCM SIGNED for decoding (usually 8bit PCM wavs are unsigned, and there can also be Float or µLaw wav's)
                           sourceFormat.getSampleRate(),
                           16, //bits per sample
                           2, //stereo
                           4, //frame size (16*2/8)
                           sourceFormat.getSampleRate(), //frame rate == same as sample rate in pcm
                           false, //little endian in pcm
                           sourceFormat.properties());
  }

  /**
   * Useful when the input is not a PCM/WAV (eg MP3/OGG)
   */
  public static AudioInputStream getStereoInputStream(AudioInputStream ais) {
    AudioFormat targetFormat = toStandardStereoPcmFormat(ais.getFormat());
    return AudioSystem.getAudioInputStream(targetFormat, ais); // will return ais itself if the the in/out formats are the same
  }

  public static AudioFormat toSignedMonoPcmFormat(AudioFormat sourceFormat) {
    return new AudioFormat(Encoding.PCM_SIGNED, //tells whether samples are signed, forcing to PCM SIGNED for decoding (usually 8bit PCM wavs are unsigned, and there can also be Float or µLaw wav's)
                           sourceFormat.getSampleRate(),
                           16, //bits per sample
                           1, //mono
                           2, //frame size (16/8)
                           sourceFormat.getSampleRate(), //frame rate == sample rate in pcm
                           false, //little endian in pcm
                           sourceFormat.properties());
  }

  public static AudioInputStream getMonoInputStream(InputStream is) throws UnsupportedAudioFileException, IOException {
    return getMonoInputStream(AudioSystem.getAudioInputStream(ensureBuffered(is)));
  }

  private static InputStream ensureBuffered(InputStream is) {
    if(is.markSupported()) {
      return is;
    } else {
      return new BufferedInputStream(is);
    }
  }

  /**
   * Useful when only one channel has to be analyzed
   */
  public static AudioInputStream getMonoInputStream(AudioInputStream ais) {
    // Need to make a double conversion if we're not coming from a straight pcm (eg: ogg/mp3)
    if(ais.getFormat().getChannels() > 1) {
      ais = getStereoInputStream(ais); // Going to uncompressed stereo; will return itself if no conversion is needed
    }

    AudioFormat targetFormat = toSignedMonoPcmFormat(ais.getFormat());
    return AudioSystem.getAudioInputStream(targetFormat, ais); // will return ais itself if the the in/out formats are the same
  }
}