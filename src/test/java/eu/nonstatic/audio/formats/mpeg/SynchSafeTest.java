package eu.nonstatic.audio.formats.mpeg;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SynchSafeTest {

  @Test
  void should_encode_synch_safe_bytes() {
    assertArrayEquals(new byte[]{9, -74, 100, 119}, SynchSafe.toBytes(19772023));
  }

  @Test
  void should_decode_synch_safe_bytes() {
    assertEquals(19772023, SynchSafe.toInt(new byte[]{9, -74, 100, 119}));
  }
}
