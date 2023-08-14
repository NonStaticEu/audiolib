/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.nonstatic.audio.AudioIssue.Type;
import eu.nonstatic.audio.Mp3InfoSupplier.Mp3Info;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class Mp3InfoSupplierTest implements AudioTestBase {

  @Test
  void should_give_infos() throws AudioFormatException, IOException, AudioInfoException {
    Mp3Info mp3Info = new Mp3InfoSupplier().getInfos(MP3_URL.openStream(), MP3_NAME);
    assertFalse(mp3Info.isIncomplete());
    assertEquals(Duration.ofNanos(11154285714L), mp3Info.getDuration());
  }

  @Test
  void should_handle_truncated_file() {
    Mp3InfoSupplier infoSupplier = new Mp3InfoSupplier();
    ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[]{0, 0, 0, 73, 68, 51, 4}); // leading zeroes for findData
    EOFException eofe = assertThrows(EOFException.class, () -> infoSupplier.getInfos(emptyStream, MP3_NAME));
    assertEquals("location: 7", eofe.getMessage());
  }

  @Test
  void should_throw_no_frames() {
    Mp3InfoSupplier infoSupplier = new Mp3InfoSupplier();
    ByteArrayInputStream noFrameStream = new ByteArrayInputStream(new byte[]{-1, -5, 80, 0, 42, 42, 42});
    AudioFormatException afe = assertThrows(AudioFormatException.class, () -> infoSupplier.getInfos(noFrameStream, MP3_NAME));
    assertEquals("Could not find a single frame: /audio/Moog-juno-303-example.mp3", afe.getMessage());
  }

  @Test
  void should_throw_read_issue() {
    Mp3InfoSupplier infoSupplier = new Mp3InfoSupplier();
    IOException ioe = assertThrows(IOException.class, () -> infoSupplier.getInfos(new FaultyStream(), MP3_NAME));
    assertEquals("reads: 0", ioe.getMessage());
  }

  @Test
  void should_give_mp3_infos_on_incomplete_file() throws AudioFormatException, IOException, AudioInfoException {
    byte[] bytes;
    try(InputStream is = MP3_URL.openStream()) {
      bytes = is.readAllBytes();
    }

    Duration fullDuration = new Mp3InfoSupplier().getInfos(new ByteArrayInputStream(bytes), MP3_NAME + ":complete").getDuration();

    int incompleteLength = bytes.length - 50;
    try(ByteArrayInputStream incompleteStream = new ByteArrayInputStream(bytes, 0, incompleteLength)) {
      Mp3Info incompleteInfos = new Mp3InfoSupplier().getInfos(incompleteStream, MP3_NAME + ":incomplete");

      assertTrue(incompleteInfos.isIncomplete());
      List<AudioIssue> issues = incompleteInfos.getIssues();
      assertEquals(1, issues.size());
      AudioIssue issue = issues.get(0);
      assertEquals(Type.EOF, issue.getType());
      assertEquals(incompleteLength, issue.getLocation());
      assertNull(issue.getMetas());
      assertEquals(EOFException.class, issue.getCause().getClass());
      assertEquals("AudioIssue EOF at 210419", issue.toString());

      Duration incompleteDuration = incompleteInfos.getDuration();
      assertEquals(Duration.ofNanos(11128163265L), incompleteDuration);
      // That's one Layer III frame less
      assertEquals(Math.round(1152*(1_000_000_000.0)/44100), fullDuration.minus(incompleteDuration).toNanos());
    }
  }

  @Test
  void should_give_mp3_infos_on_out_of_synch_file() throws AudioFormatException, IOException, AudioInfoException {
    byte[] bytes;
    try(InputStream is = MP3_URL.openStream()) {
      bytes = is.readAllBytes();
    }

    Duration fullDuration = new Mp3InfoSupplier().getInfos(new ByteArrayInputStream(bytes), MP3_NAME + ":complete").getDuration();

    int split = 1515, missing = 50;
    ByteBuffer bb = ByteBuffer.allocate(bytes.length - missing);
    bb.put(bytes, 0, split);
    bb.put(bytes, split+missing, bytes.length - missing - split);

    try(ByteArrayInputStream faultyStream = new ByteArrayInputStream(bb.array())) {
      Mp3Info incompleteInfos = new Mp3InfoSupplier().getInfos(faultyStream, MP3_NAME + ":outofsynch");

      assertFalse(incompleteInfos.isIncomplete());

      List<AudioIssue> issues = incompleteInfos.getIssues();
      assertEquals(1, issues.size());
      AudioIssue issue = issues.get(0);
      assertEquals(Type.SYNC, issue.getType());
      assertEquals(1930, issue.getLocation());
      assertEquals(359L, issue.getMeta(AudioIssue.META_SKIPPED));
      assertEquals("AudioIssue SYNC at 1930, {skipped=359}", issue.toString());

      Duration incompleteDuration = incompleteInfos.getDuration();
      assertEquals(Duration.ofNanos(11128163265L), incompleteDuration);
      // That's one Layer III frame less
      assertEquals(Math.round(1152*(1_000_000_000.0)/44100), fullDuration.minus(incompleteDuration).toNanos());
    }
  }

  @Test
  void should_give_mp3_infos_on_out_of_synch_eof_file() throws AudioFormatException, IOException, AudioInfoException {
    byte[] bytes;
    try(InputStream is = MP3_URL.openStream()) {
      bytes = is.readAllBytes();
    }

    int split = 253, garbage = 69;
    ByteBuffer bb = ByteBuffer.allocate(split + garbage);
    bb.put(bytes, 0, split);
    for(int i = 0; i < garbage; i++) {
      bb.put((byte)42);
    }

    try(ByteArrayInputStream faultyStream = new ByteArrayInputStream(bb.array())) {
      Mp3Info incompleteInfos = new Mp3InfoSupplier().getInfos(faultyStream, MP3_NAME + ":outofsyncheof");

      assertTrue(incompleteInfos.isIncomplete());

      List<AudioIssue> issues = incompleteInfos.getIssues();
      assertEquals(1, issues.size());
      AudioIssue issue = issues.get(0);
      assertEquals(Type.EOF, issue.getType());
      assertEquals(321, issue.getLocation());
      assertNull(issue.getMetas());
      assertEquals(EOFException.class, issue.getCause().getClass());
      assertEquals("AudioIssue EOF at 321", issue.toString());
    }
  }

  @Test
  void should_encode_synch_safe_bytes() {
    assertArrayEquals(new byte[]{9, -74, 100, 119}, Mp3InfoSupplier.toSynchSafeBytes(19772023));
  }
}
