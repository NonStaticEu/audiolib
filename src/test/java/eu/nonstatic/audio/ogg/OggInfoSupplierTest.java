/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.ogg;

import eu.nonstatic.audio.AudioFormat;
import eu.nonstatic.audio.AudioFormatException;
import eu.nonstatic.audio.AudioInfoException;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.AudioIssue.Type;
import eu.nonstatic.audio.AudioTestBase;
import eu.nonstatic.audio.FaultyStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OggInfoSupplierTest implements AudioTestBase {

  @Test
  void should_give_infos() throws AudioFormatException, IOException, AudioInfoException {
    OggInfo oggInfo = new OggInfoSupplier().getInfos(OGG_URL.openStream(), OGG_NAME);
    assertEquals(AudioFormat.OGG, oggInfo.getFormat());
    assertEquals(Duration.ofNanos(6104036281L), oggInfo.getDuration());

    OggStreamsInfos oggStreamsInfo = new OggInfoSupplier().getStreamsInfos(OGG_URL.openStream(), OGG_NAME);
    assertFalse(oggStreamsInfo.isIncomplete());
    assertEquals(Duration.ofNanos(6104036281L), oggInfo.getDuration());
  }

  @Test
  void should_handle_truncated_file_no_page() throws IOException {
    byte[] bytes;
    try(InputStream is = OGG_URL.openStream()) {
      bytes = is.readAllBytes();
    }

    try(ByteArrayInputStream incompleteStream = new ByteArrayInputStream(bytes, 0, 500)) {
      AudioInfoException aie = assertThrows(AudioInfoException.class,
          () -> new OggInfoSupplier().getInfos(incompleteStream, OGG_NAME));

      assertEquals(1, aie.getIssues().size());
      assertEquals("Could not find any audio stream containing pages from the 1 found ones at 0: /audio/Example.ogg", aie.getIssues().get(0).getCause().getMessage());
    }
  }

  @Test
  void should_handle_truncated_file_with_page() throws IOException, AudioInfoException {
    byte[] bytes;
    try(InputStream is = OGG_URL.openStream()) {
      bytes = is.readAllBytes();
    }

    int truncateLength = 50000;
    try(ByteArrayInputStream incompleteStream = new ByteArrayInputStream(bytes, 0, truncateLength)) {
      OggInfo infos = new OggInfoSupplier().getInfos(incompleteStream, OGG_NAME);

      List<AudioIssue> issues = infos.getIssues();
      assertEquals(2, issues.size());

      AudioIssue audioIssue0 = issues.get(0);
      assertEquals(Type.OTHER, audioIssue0.getType());
      assertEquals(Map.of(OggCodecReader.META_BOS_SEGMENT_SIZE, 30), audioIssue0.getMetas());

      AudioIssue audioIssue1 = issues.get(1);
      assertEquals(Type.EOF, audioIssue1.getType());
      assertEquals(truncateLength, audioIssue1.getLocation());
    }
  }

  @Test
  void should_throw_read_issue() {
    OggInfoSupplier infoSupplier = new OggInfoSupplier();
    IOException ioe = assertThrows(IOException.class, () -> infoSupplier.getInfos(new FaultyStream(), OGG_NAME));
    assertEquals("reads: 0", ioe.getMessage());
  }

  @Test
  void should_give_ogg_infos_on_incomplete_file() throws IOException, AudioInfoException {
    byte[] bytes;
    try(InputStream is = OGG_URL.openStream()) {
      bytes = is.readAllBytes();
    }

    int incompleteLength = bytes.length - 50000;
    try(ByteArrayInputStream incompleteStream = new ByteArrayInputStream(bytes, 0, incompleteLength)) {

      OggInfo infos = new OggInfoSupplier().getInfos(incompleteStream, OGG_NAME + ":incomplete");
      assertTrue(infos.isIncomplete());

      List<AudioIssue> issues = infos.getIssues();
      assertEquals(2, issues.size()); // bos and EOF
      AudioIssue issue = issues.get(1);
      assertEquals(Type.EOF, issue.getType());
      assertEquals(54793, issue.getLocation());
      assertNull(issue.getMetas());
      assertEquals(EOFException.class, issue.getCause().getClass());
      assertEquals("AudioIssue EOF at 54793", issue.toString());
    }
  }

  @Test
  void should_give_ogg_infos_on_out_of_synch_file() throws IOException, AudioInfoException {
    byte[] bytes;
    try(InputStream is = OGG_URL.openStream()) {
      bytes = is.readAllBytes();
    }

    Duration fullDuration = new OggInfoSupplier().getInfos(new ByteArrayInputStream(bytes), OGG_NAME + ":complete").getDuration();

    int split = 1515, missing = 50;
    ByteBuffer bb = ByteBuffer.allocate(bytes.length - missing);
    bb.put(bytes, 0, split);
    bb.put(bytes, split+missing, bytes.length - missing - split);

    try(ByteArrayInputStream faultyStream = new ByteArrayInputStream(bb.array())) {
      OggInfo infos = new OggInfoSupplier().getInfos(faultyStream, OGG_NAME + ":outofsynch");

      assertFalse(infos.isIncomplete());

      List<AudioIssue> issues = infos.getIssues();
      assertEquals(2, issues.size()); // bos and sync
      AudioIssue issue = issues.get(1);
      assertEquals(Type.SYNC, issue.getType());
      assertEquals(4382, issue.getLocation());
      assertEquals(3313L, issue.getMeta(AudioIssue.META_SKIPPED));
      assertEquals("AudioIssue SYNC at 4382, {skipped=3313}", issue.toString());

      assertEquals(fullDuration, infos.getDuration()); // same duration as the complete one because of the granule position
    }
  }

  @Test
  void should_give_ogg_infos_on_out_of_synch_incomplete_file() throws IOException, AudioInfoException {
    byte[] bytes;
    try(InputStream is = OGG_URL.openStream()) {
      bytes = is.readAllBytes();
    }

    int split = 5000, garbage = 69;
    ByteBuffer bb = ByteBuffer.allocate(split + garbage);
    bb.put(bytes, 0, split);
    for(int i = 0; i < garbage; i++) {
      bb.put((byte)42);
    }

    try(ByteArrayInputStream faultyStream = new ByteArrayInputStream(bb.array())) {
      OggInfo infos = new OggInfoSupplier().getInfos(faultyStream, OGG_NAME + ":outofsyncheof");

      assertTrue(infos.isIncomplete());

      List<AudioIssue> issues = infos.getIssues();
      assertEquals(2, issues.size()); // bos and EOF
      AudioIssue issue = issues.get(1);
      assertEquals(Type.EOF, issue.getType());
      assertEquals(5069, issue.getLocation());
      assertNull(issue.getMetas());
      assertEquals(EOFException.class, issue.getCause().getClass());
      assertEquals("AudioIssue EOF at 5069", issue.toString());
    }
  }
}
