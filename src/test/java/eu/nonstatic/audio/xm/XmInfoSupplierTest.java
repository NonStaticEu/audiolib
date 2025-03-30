/**
 * Audiolib
 * Copyright (C) 2022 NonStatic
 *
 * This file is part of cuelib.
 * Audiolib is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with . If not, see <https://www.gnu.org/licenses/>.
 */
package eu.nonstatic.audio.xm;

import eu.nonstatic.audio.AudioFormat;
import eu.nonstatic.audio.AudioInfoException;
import eu.nonstatic.audio.AudioIssue;
import eu.nonstatic.audio.AudioIssue.Type;
import eu.nonstatic.audio.AudioTestBase;
import eu.nonstatic.audio.FaultyStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmInfoSupplierTest implements AudioTestBase {

  XmInfoSupplier infoSupplier = new XmInfoSupplier();

  @Test
  void should_give_infos() throws IOException, AudioInfoException {
    XmInfo xmInfo = infoSupplier.getInfos(XM_URL.openStream(), XM_NAME);
    assertEquals(AudioFormat.XM, xmInfo.getFormat());
    assertEquals("FastTracker v2.00", xmInfo.getTracker());
    assertEquals(Duration.ofMillis(127500L), xmInfo.getDuration());
    assertTrue(xmInfo.getIssues().isEmpty());
  }

  @Test
  void should_throw_read_issue() {
    IOException ioe = assertThrows(IOException.class, () -> infoSupplier.getInfos(new FaultyStream(), XM_NAME));
    assertEquals("reads: 0", ioe.getMessage());
  }

  @Test
  void should_throw_on_eof() {
    ByteBuffer bb = ByteBuffer.allocate(37)
      .put(XmInfoSupplier.XM_HEADER.getBytes())
      .put("Killargh Track name!".getBytes(StandardCharsets.US_ASCII)); // 20 chars
      // and nothing else

    ByteArrayInputStream bais = new ByteArrayInputStream(bb.array());
    AudioInfoException iae = assertThrows(AudioInfoException.class, () -> infoSupplier.getInfos(bais, XM_NAME));
    assertEquals(1, iae.getIssues().size());
    AudioIssue issue = iae.getIssues().get(0);
    assertEquals(Type.EOF, issue.getType());
    assertEquals(37, issue.getLocation());
    assertEquals(EOFException.class, issue.getCause().getClass());
  }
}
