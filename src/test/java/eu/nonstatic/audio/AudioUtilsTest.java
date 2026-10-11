package eu.nonstatic.audio;

import static eu.nonstatic.audio.formats.mpeg.MpegAudioInfoSupplier.ID3_FOOTER_LENGTH;
import static eu.nonstatic.audio.formats.mpeg.MpegAudioInfoSupplier.ID3_HEADER_LENGTH;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.nonstatic.audio.formats.mpeg.MpegAudioInfoSupplier;
import eu.nonstatic.audio.formats.mpeg.SynchSafe;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AudioUtilsTest implements AudioTestBase {

  /**
   * Well over mp3spi's hardcoded 4096001 bytes mark limit, which is what makes
   * AudioSystem.getAudioInputStream(File) blow up with "Resetting to invalid mark" on mp3s
   * carrying a big embedded cover.
   */
  private static final int OVERSIZED_TAG_SIZE = 5_000_000;

  private static final byte[] PAYLOAD = {'Y', 'O', 'L', 'O'};


  @Test
  void should_not_skip_anything_when_there_is_no_tag() throws IOException {
    byte[] bytes = {'Y', 'O', 'L', 'O', 1, 2, 3, 4, 5, 6, 7, 8};
    var is = new AudioInputStream(bytes, null);
    assertEquals(0L, MpegAudioInfoSupplier.skipID3v2(is));
    assertArrayEquals(bytes, is.readAllBytes());
  }

  @Test
  void should_not_skip_anything_when_the_stream_is_shorter_than_the_magic() throws IOException {
    byte[] bytes = {'I', 'D'};
    try (var is = new AudioInputStream(bytes, null)) {
      assertThrows(EOFException.class, () -> MpegAudioInfoSupplier.skipID3v2(is));
    }
  }

  @Test
  void should_throw_on_truncated_tag_header() throws IOException {
    try (var is = new AudioInputStream(new byte[]{'I', 'D', '3', 3, 0, 0}, null)) {
      assertThrows(EOFException.class, () -> MpegAudioInfoSupplier.skipID3v2(is));
    }
  }

  @Test
  void should_throw_on_truncated_tag() throws IOException {
    byte[] bytes = new byte[ID3_HEADER_LENGTH + 100];
    System.arraycopy(id3v2Header(1234, false), 0, bytes, 0, 10);
    try (var is = new AudioInputStream(bytes, null)) {
      assertThrows(EOFException.class, () -> MpegAudioInfoSupplier.skipID3v2(is));
    }
  }

  @Test
  void should_skip_tag() throws IOException {
    var is = new AudioInputStream(prependTagToAudio(1234, false, PAYLOAD), null);
    assertEquals(ID3_HEADER_LENGTH + 1234L, MpegAudioInfoSupplier.skipID3v2(is));
    assertArrayEquals(PAYLOAD, is.readAllBytes());
  }

  @Test
  void should_skip_tag_and_footer() throws IOException {
    var is = new AudioInputStream(prependTagToAudio(1234, true, PAYLOAD), null);
    assertEquals(ID3_HEADER_LENGTH + 1234L + ID3_FOOTER_LENGTH, MpegAudioInfoSupplier.skipID3v2(is));
    assertArrayEquals(PAYLOAD, is.readAllBytes());
  }

  @Test
  void should_keep_location_in_sync_on_audio_input_stream() throws IOException {
    try (var ais
             = new AudioInputStream(new ByteArrayInputStream(prependTagToAudio(1234, false, PAYLOAD)), "tagged")) {
      assertEquals(ID3_HEADER_LENGTH + 1234L, MpegAudioInfoSupplier.skipID3v2(ais));
      assertEquals(ID3_HEADER_LENGTH + 1234L, ais.location());
    }
  }

  @Test
  void should_leave_location_untouched_when_there_is_no_tag() throws IOException {
    try (AudioInputStream ais
             = new AudioInputStream(new ByteArrayInputStream(PAYLOAD), "untagged")) {
      assertEquals(0L, MpegAudioInfoSupplier.skipID3v2(ais));
      assertEquals(0L, ais.location());
    }
  }


  @Test
  void should_open_file_with_oversized_id3v2_tag(@TempDir Path tempDir) throws Exception {
    byte[] plain;
    try(InputStream is = WAVE_URL.openStream()) {
      plain = is.readAllBytes();
    }
    byte[] tagged = prependTagToAudio(OVERSIZED_TAG_SIZE, false, plain);

    // No AudioFileReader knows what to make of the "ID3" magic, hence the need to get rid of the tag first
    assertThrows(UnsupportedAudioFileException.class, () -> AudioSystem.getAudioInputStream(new ByteArrayInputStream(tagged)));

    AudioFormat expectedFormat;
    int expectedBytes;
    try (javax.sound.sampled.AudioInputStream ais = AudioUtils.getAudioInputStream(new ByteArrayInputStream(plain))) {
      expectedFormat = ais.getFormat();
      expectedBytes = ais.readAllBytes().length;
    }

    try (javax.sound.sampled.AudioInputStream ais = AudioUtils.getAudioInputStream(new ByteArrayInputStream(tagged))) {
      assertEquals(expectedFormat.toString(), ais.getFormat().toString());
      assertEquals(expectedBytes, ais.readAllBytes().length);
    }
  }

  private static byte[] id3v2Header(int framesSize, boolean footer) {
    int footerSize = footer ? ID3_FOOTER_LENGTH : 0;
    return ByteBuffer.allocate(ID3_HEADER_LENGTH + framesSize + footerSize)
      .put((byte)'I').put((byte)'D').put((byte)'3')
      .put((byte)3).put((byte)0) // ID3v2.3.0
      .put((byte) (footer ? 0x10 : 0x00))
      .put(SynchSafe.toBytes(framesSize))
      .put(new byte[framesSize])
      .put(new byte[footerSize])
      .array();
  }

  private static byte[] prependTagToAudio(int framesSize, boolean footer, byte[] audioBytes) {
    byte[] id3v2Header = id3v2Header(framesSize, footer);
    return ByteBuffer.allocate(id3v2Header.length + audioBytes.length)
        .put(id3v2Header)
        .put(audioBytes)
        .array();
  }
}
