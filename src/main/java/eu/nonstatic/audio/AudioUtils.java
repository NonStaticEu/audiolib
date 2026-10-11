/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of audiolib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Audiolib is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Audiolib. If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio;

import eu.nonstatic.audio.formats.mpeg.MpegAudioInfoSupplier;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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
  public static AudioInputStream getStereoInputStream(javax.sound.sampled.AudioInputStream ais) {
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

  public static AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
    return getAudioInputStream(url.openStream());
  }

  public static AudioInputStream getAudioInputStream(Path path) throws UnsupportedAudioFileException, IOException {
    return getAudioInputStream(Files.newInputStream(path));
  }

  public static AudioInputStream getAudioInputStream(InputStream is) throws UnsupportedAudioFileException, IOException {
    var ais = new eu.nonstatic.audio.AudioInputStream(is, null);
    /*
     * Skips any leading ID3v2 tag because mp3spi's MpegAudioFileReader marks the stream with a hardcoded 4096001 bytes limit,
     * then JLayer's Bitstream.loadID3v2() reads the whole tag into memory. A tag bigger than that limit (easily reached with an embedded cover) invalidates the mark
     * and makes the subsequent reset(). Getting rid of the tag first works around that issue.
     */
    MpegAudioInfoSupplier.skipID3v2(ais);
    return AudioSystem.getAudioInputStream(ais);
  }

  public static AudioInputStream getMonoInputStream(InputStream is) throws UnsupportedAudioFileException, IOException {
    return getMonoInputStream(getAudioInputStream(is));
  }

  /**
   * Useful when only one channel has to be analyzed
   */
  public static AudioInputStream getMonoInputStream(javax.sound.sampled.AudioInputStream ais) {
    // Need to make a double conversion if we're not coming from a straight pcm (eg: ogg/mp3)
    if(ais.getFormat().getChannels() > 1) {
      ais = getStereoInputStream(ais); // Going to uncompressed stereo; will return itself if no conversion is needed
    }

    AudioFormat targetFormat = toSignedMonoPcmFormat(ais.getFormat());
    return AudioSystem.getAudioInputStream(targetFormat, ais); // will return ais itself if the the in/out formats are the same
  }
}