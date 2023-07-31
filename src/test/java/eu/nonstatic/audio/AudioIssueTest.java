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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.nonstatic.audio.AudioIssue.Type;
import java.io.EOFException;
import java.io.Serializable;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AudioIssueTest {

  @Test
  void should_build_sync() {
    AudioIssue issue = AudioIssue.sync(42, 69);
    assertEquals(Type.SYNC, issue.getType());
    assertEquals(42L, issue.getLocation());
    assertNull(issue.getCause());
    assertEquals(1, issue.getMetas().size());
    assertEquals(69L, issue.getMeta(AudioIssue.META_SKIPPED));
  }

  @Test
  void should_build_eof() {
    EOFException exception = new EOFException("This is the end, my friend");
    AudioIssue issue = AudioIssue.eof(77, exception);
    assertEquals(Type.EOF, issue.getType());
    assertEquals(77L, issue.getLocation());
    assertSame(exception, issue.getCause());
    assertNull(issue.getMetas());
  }

  @Test
  void should_not_build_eof() {
    assertThrows(NullPointerException.class, () -> AudioIssue.eof(77, null));
  }

  @Test
    void should_build_other() {
    RuntimeException exception = new RuntimeException("Dr. Claw was here");
    AudioIssue issue = AudioIssue.other(33, exception);
    assertEquals(Type.OTHER, issue.getType());
    assertEquals(33L, issue.getLocation());
    assertSame(exception, issue.getCause());
    assertNull(issue.getMetas());
  }

  @Test
  void should_build_other_2() {
    Map<String, Serializable> metas = Map.of("foo", "bar");
    AudioIssue issue = AudioIssue.other(33, metas);
    assertEquals(Type.OTHER, issue.getType());
    assertEquals(33L, issue.getLocation());
    assertNull(issue.getCause());
    assertEquals(metas, issue.getMetas());
    assertNotSame(metas, issue.getMetas());
  }

  @Test
  void should_build_other_with_empty_metas() {
    AudioIssue issue = AudioIssue.other(1337, Map.of());
    assertEquals(Type.OTHER, issue.getType());
    assertEquals(1337L, issue.getLocation());
    assertNull(issue.getCause());
    assertTrue(issue.getMetas().isEmpty());
  }

  @Test
  void should_not_build_other() {
    assertThrows(NullPointerException.class, () -> AudioIssue.other(33, (Throwable) null));
  }
}