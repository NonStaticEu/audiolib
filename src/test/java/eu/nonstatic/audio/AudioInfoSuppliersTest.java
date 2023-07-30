package eu.nonstatic.audio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AudioInfoSuppliersTest {

  @Test
  void should_select_supplier_by_filename() {
    assertEquals(FlacInfoSupplier.class, AudioInfoSuppliers.getByFileName("/tmp/music.flac").getClass());
    assertEquals(AiffInfoSupplier.class, AudioInfoSuppliers.getByFileName("music.AIFF").getClass());
  }

  @Test
  void should_not_select_supplier_by_filename() {
    assertNull(AudioInfoSuppliers.getByFileName("/tmp/music.xyz"));
    assertNull(AudioInfoSuppliers.getByFileName("music.XYZ"));
    assertNull(AudioInfoSuppliers.getByFileName("whatever"));
  }

  @Test
  void should_fail_select_supplier_by_filename_null() {
    assertThrows(NullPointerException.class, () -> AudioInfoSuppliers.getByFileName(null));
  }

  @Test
  void should_select_flac_supplier_by_extension() {
    assertEquals(Mp3InfoSupplier.class, AudioInfoSuppliers.getByExtension("mp3").getClass());
    assertEquals(WaveInfoSupplier.class, AudioInfoSuppliers.getByExtension("WAV").getClass());
  }

  @Test
  void should_not_select_flac_supplier_by_extension() {
    assertNull(AudioInfoSuppliers.getByExtension("XYz"));
    assertNull(AudioInfoSuppliers.getByExtension(null));
  }
}
