package eu.nonstatic.audio.detect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class KeyNoteTest {

  // Note.of

  @Test
  void testNoteOfBasic() {
    assertEquals(Note.C, Note.of('C', null));
    assertEquals(Note.D, Note.of('D', null));
    assertEquals(Note.A, Note.of('a', null)); // lowercase normalised
  }

  @Test
  void testNoteOfSharpUnicode() {
    assertEquals(Note.C_SHARP, Note.of('C', Note.SHARP));
    assertEquals(Note.F_SHARP, Note.of('F', Note.SHARP));
  }

  @Test
  void testNoteOfSharpAscii() {
    assertEquals(Note.C_SHARP, Note.of('C', Note.SHARP));
    assertEquals(Note.G_SHARP, Note.of('G', '#'));
  }

  @Test
  void testNoteOfFlatAscii() {
    assertThrows(IllegalArgumentException.class, () -> Note.of('C', Note.FLAT));
  }

  @Test
  void testNoteOfInvalid() {
    assertThrows(IllegalArgumentException.class, () -> Note.of('X', null));
  }

  // Key.signature

  @Test
  void testSignatureMajorNoAlteration() {
    Key key = new Key(Note.C, Mode.MAJOR, 0.9);
    assertEquals("C", key.signature());
  }

  @Test
  void testSignatureMinorNoAlteration() {
    Key key = new Key(Note.A, Mode.MINOR, 0.8);
    assertEquals("a", key.signature());
  }

  @Test
  void testSignatureMajorSharp() {
    Key key = new Key(Note.F_SHARP, Mode.MAJOR, 0.7);
    assertEquals("F" + Note.SHARP, key.signature());
  }

  @Test
  void testSignatureMinorSharp() {
    Key key = new Key(Note.C_SHARP, Mode.MINOR, 0.6);
    assertEquals("c" + Note.SHARP, key.signature());
  }

  // Key.ofSignature

  @Test
  void testOfSignatureMajor() {
    Key key = Key.ofSignature("C", 0.9);
    assertEquals(Note.C, key.note());
    assertEquals(Mode.MAJOR, key.mode());
    assertEquals(0.9, key.confidence(), 1e-9);
  }

  @Test
  void testOfSignatureMinor() {
    Key key = Key.ofSignature("a", 0.8);
    assertEquals(Note.A, key.note());
    assertEquals(Mode.MINOR, key.mode());
  }

  @Test
  void testOfSignatureMajorSharp() {
    Key key = Key.ofSignature("F" + Note.SHARP, 0.75);
    assertEquals(Note.F_SHARP, key.note());
    assertEquals(Mode.MAJOR, key.mode());
  }

  @Test
  void testOfSignatureMinorSharp() {
    Key key = Key.ofSignature("c" + Note.SHARP, 0.65);
    assertEquals(Note.C_SHARP, key.note());
    assertEquals(Mode.MINOR, key.mode());
  }

  @Test
  void testOfSignatureRoundTrip() {
    for (Note note : Note.values()) {
      for (Mode mode : Mode.values()) {
        Key original = new Key(note, mode, 0.5);
        Key parsed = Key.ofSignature(original.signature(), 0.5);
        assertEquals(original.note(), parsed.note());
        assertEquals(original.mode(), parsed.mode());
      }
    }
  }

  @Test
  void testOfSignatureEmptyThrows() {
    assertThrows(IllegalArgumentException.class, () -> Key.ofSignature("", 0.5));
  }

  @Test
  void testOfSignatureTooLongThrows() {
    assertThrows(IllegalArgumentException.class, () -> Key.ofSignature("ABC", 0.5));
  }

  @Test
  void testKeyInvalidConfidenceThrows() {
    assertThrows(IllegalArgumentException.class, () -> new Key(Note.C, Mode.MAJOR, -0.1));
    assertThrows(IllegalArgumentException.class, () -> new Key(Note.C, Mode.MAJOR, 1.1));
  }
}
