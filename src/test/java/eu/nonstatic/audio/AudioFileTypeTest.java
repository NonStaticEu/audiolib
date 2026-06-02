package eu.nonstatic.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AudioFileTypeTest {

  @Test
  void should_get_by_extension() {
    assertEquals(AudioFileType.WAVE, AudioFileType.ofExtension("wAv"));
    assertEquals(AudioFileType.WAVE, AudioFileType.ofExtension("Wave"));
    assertEquals(AudioFileType.MP3, AudioFileType.ofExtension("mp3"));
    assertEquals(AudioFileType.MP2, AudioFileType.ofExtension("MP2"));
    assertEquals(AudioFileType.FLAC, AudioFileType.ofExtension("flac"));
  }

  @Test
  void should_fail_on_null_extension() {
    assertThrows(IllegalArgumentException.class, () -> AudioFileType.ofExtension(null));
  }

  @Test
  void should_fail_on_empty_extension() {
    assertThrows(IllegalArgumentException.class, () -> AudioFileType.ofExtension(""));
  }

  @Test
  void should_fail_on_unknown_extension() {
    assertThrows(IllegalArgumentException.class, () -> AudioFileType.ofExtension("foobar"));
  }


  @Test
  void should_get_by_mime_type() {
    assertEquals(List.of(AudioFileType.WAVE), AudioFileType.ofMimeType("AUDIO/WAV"));
    assertEquals(List.of(AudioFileType.WAVE), AudioFileType.ofMimeType("audio/x-wav"));
    assertEquals(List.of(AudioFileType.MP3, AudioFileType.MP2), AudioFileType.ofMimeType("audio/mpeg"));
    assertEquals(List.of(AudioFileType.FLAC), AudioFileType.ofMimeType("audio/flac"));
  }

  @Test
  void should_fail_on_null_mime_type() {
    assertThrows(IllegalArgumentException.class, () -> AudioFileType.ofMimeType(null));
  }

  @Test
  void should_fail_on_empty_mime_type() {
    assertThrows(IllegalArgumentException.class, () -> AudioFileType.ofMimeType(""));
  }

  @Test
  void should_fail_on_unknown_mime_type() {
    assertThrows(IllegalArgumentException.class, () -> AudioFileType.ofMimeType("audio/foobar"));
  }


  @Test
  void should_get_mimes_from_extension() {
    assertEquals(List.of("audio/mpeg"), AudioFileType.extensionToMimeTypes("mp3"));
    assertEquals(List.of("audio/mpeg"), AudioFileType.extensionToMimeTypes("mp2"));
    assertEquals(List.of("audio/wav", "audio/x-wav", "audio/vnd.wav"), AudioFileType.extensionToMimeTypes("wav"));

    assertTrue(AudioFileType.extensionToMimeTypes("xm").isEmpty());

    assertThrows(IllegalArgumentException.class, () -> AudioFileType.extensionToMimeTypes("foobar"));
    assertThrows(IllegalArgumentException.class, () -> AudioFileType.extensionToMimeTypes(null));
  }

  @Test
  void should_get_mime_from_extension() {
    assertEquals("audio/mpeg", AudioFileType.extensionToMimeType("mp3"));
    assertEquals("audio/wav", AudioFileType.extensionToMimeType("wav"));

    assertNull(AudioFileType.extensionToMimeType("xm"));

    assertThrows(IllegalArgumentException.class, () -> AudioFileType.extensionToMimeType("foobar"));
    assertThrows(IllegalArgumentException.class, () -> AudioFileType.extensionToMimeType(null));
  }

  @Test
  void should_get_extensions_from_mime_type() {
    assertEquals(List.of("mp3", "mp2"), AudioFileType.mimeTypeToExtensions("audio/mpeg"));
    assertEquals(List.of("wav", "wave"), AudioFileType.mimeTypeToExtensions("audio/x-wav"));
    assertEquals(List.of("flac"), AudioFileType.mimeTypeToExtensions("audio/flac"));
    assertThrows(IllegalArgumentException.class, () -> AudioFileType.mimeTypeToExtensions("audio/foobar"));
    assertThrows(IllegalArgumentException.class, () -> AudioFileType.mimeTypeToExtensions(null));
  }

  @Test
  void should_get_extension_from_mime_type() {
    assertEquals("mp3", AudioFileType.mimeTypeToExtension("audio/mpeg"));
    assertEquals("wav", AudioFileType.mimeTypeToExtension("audio/x-wav"));
    assertEquals("flac", AudioFileType.mimeTypeToExtension("audio/flac"));
    assertThrows(IllegalArgumentException.class, () -> AudioFileType.mimeTypeToExtension("audio/foobar"));
    assertThrows(IllegalArgumentException.class, () -> AudioFileType.mimeTypeToExtension(null));
  }
}
