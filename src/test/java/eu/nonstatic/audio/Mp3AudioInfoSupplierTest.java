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
import eu.nonstatic.audio.MpegAudioInfoSupplier.FrameDetails;
import eu.nonstatic.audio.MpegAudioInfoSupplier.MpegInfo;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class Mp3AudioInfoSupplierTest implements AudioTestBase {

  @Test
  void should_give_infos() throws AudioFormatException, IOException, AudioInfoException {
    MpegInfo mpegInfo = new Mp3AudioInfoSupplier().getInfos(MP3_URL.openStream(), MP3_NAME);
    assertFalse(mpegInfo.isIncomplete());
    assertEquals(Duration.ofNanos(11154285714L), mpegInfo.getDuration());
  }

  @Test
  void should_handle_truncated_file() {
    MpegAudioInfoSupplier infoSupplier = new Mp3AudioInfoSupplier();
    ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[]{0, 0, 0, 73, 68, 51, 4}); // leading zeroes for findData
    EOFException eofe = assertThrows(EOFException.class, () -> infoSupplier.getInfos(emptyStream, MP3_NAME));
    assertEquals("location: 7", eofe.getMessage());
  }

  @Test
  void should_throw_no_frames() {
    MpegAudioInfoSupplier infoSupplier = new Mp3AudioInfoSupplier();
    ByteArrayInputStream noFrameStream = new ByteArrayInputStream(new byte[]{-1, -5, 80, 0, 42, 42, 42});
    AudioFormatException afe = assertThrows(AudioFormatException.class, () -> infoSupplier.getInfos(noFrameStream, MP3_NAME));
    assertEquals("Could not find a single frame at 0: /audio/Moog-juno-303-example.mp3", afe.getMessage());
  }

  @Test
  void should_throw_read_issue() {
    MpegAudioInfoSupplier infoSupplier = new Mp3AudioInfoSupplier();
    IOException ioe = assertThrows(IOException.class, () -> infoSupplier.getInfos(new FaultyStream(), MP3_NAME));
    assertEquals("reads: 0", ioe.getMessage());
  }

  @Test
  void should_give_mp3_infos_on_incomplete_file() throws AudioFormatException, IOException, AudioInfoException {
    byte[] bytes;
    try(InputStream is = MP3_URL.openStream()) {
      bytes = is.readAllBytes();
    }

    Duration fullDuration = new Mp3AudioInfoSupplier().getInfos(new ByteArrayInputStream(bytes), MP3_NAME + ":complete").getDuration();

    int incompleteLength = bytes.length - 50;
    try(ByteArrayInputStream incompleteStream = new ByteArrayInputStream(bytes, 0, incompleteLength)) {
      MpegInfo incompleteInfos = new Mp3AudioInfoSupplier().getInfos(incompleteStream, MP3_NAME + ":incomplete");

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

    Duration fullDuration = new Mp3AudioInfoSupplier().getInfos(new ByteArrayInputStream(bytes), MP3_NAME + ":complete").getDuration();

    int split = 1515, missing = 50;
    ByteBuffer bb = ByteBuffer.allocate(bytes.length - missing);
    bb.put(bytes, 0, split);
    bb.put(bytes, split+missing, bytes.length - missing - split);

    try(ByteArrayInputStream faultyStream = new ByteArrayInputStream(bb.array())) {
      MpegInfo incompleteInfos = new Mp3AudioInfoSupplier().getInfos(faultyStream, MP3_NAME + ":outofsynch");

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
      MpegInfo incompleteInfos = new Mp3AudioInfoSupplier().getInfos(faultyStream, MP3_NAME + ":outofsyncheof");

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
  void should_seek_frame_after_malformed_one() throws AudioFormatException, IOException, AudioInfoException {
    byte[] bytes;
    try(InputStream is = MP3_URL.openStream()) {
      bytes = is.readAllBytes();
    }

    MpegAudioInfoSupplier infoSupplier = new Mp3AudioInfoSupplier();
    AudioInputStream aisComplete = new AudioInputStream(new ByteArrayInputStream(bytes), MP3_NAME + ":complete");
    infoSupplier.skipID3v2(aisComplete);
    long framesLocation = aisComplete.location();
    FrameDetails frame0 = infoSupplier.readFrame(aisComplete);
    aisComplete.close();

    int faultLocation = (int)framesLocation + frame0.frameLength; // we will have a malformed frame from the second frame's header.

    ByteBuffer bb = ByteBuffer.wrap(bytes);
    int header1 = bb.getInt(faultLocation);
    bb.putInt(faultLocation, header1 & 0xfff9ffff); //so layer is 00, but that has an impact on bitrate calculation, so that''s the type of error we'll get.

    MpegInfo infos = infoSupplier.getInfos(new ByteArrayInputStream(bytes), MP3_NAME + ":malformedframe");
    assertFalse(infos.isIncomplete());

    List<AudioIssue> issues = infos.getIssues();
    assertTrue(issues.size() >= 2); // there should be a FRAME one and a SYNC one after, maybe more if this was not an actual frame
    AudioIssue issue1 = issues.get(0);
    assertEquals(Type.FORMAT, issue1.getType());
    assertEquals(253, issue1.getLocation());
    assertEquals("Cannot handle bitrate for index d at 253: /audio/Moog-juno-303-example.mp3:malformedframe", issue1.getCause().getMessage());

    assertEquals(Type.SYNC, issues.get(1).getType());
  }

  @Test
  void should_encode_synch_safe_bytes() {
    assertArrayEquals(new byte[]{9, -74, 100, 119}, MpegAudioInfoSupplier.toSynchSafeBytes(19772023));
  }
}
