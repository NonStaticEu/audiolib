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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

public final class OggStreamsInfos extends OggIssues implements Iterable<OggInfo> {

  // serial number => infos
  private final Map<Integer, OggInfo> oggInfos = new LinkedHashMap<>(); // linked cause we'll be getting the first audio stream later on

  // true if the file unexpectedly reached EOF or if we didn't reach the end-of-stream page for every stream
  // Sync errors don't have any effect on this flag
  @Getter
  boolean incomplete;

  public int size() {
    return oggInfos.size();
  }

  public boolean isEmpty() {
    return oggInfos.isEmpty();
  }

  public OggInfo get(int serialNumber) {
    return oggInfos.get(serialNumber);
  }

  void put(int serialNumber, OggInfo oggInfo) {
    if (oggInfos.get(serialNumber) != null) {
      throw new IllegalArgumentException("serialNumber is already associated: " + serialNumber);
    } else {
      oggInfos.put(serialNumber, oggInfo);
    }
  }

  public boolean contains(int serialNumber) {
    return oggInfos.containsKey(serialNumber);
  }

  public Stream<OggInfo> stream() {
    return oggInfos.values().stream();
  }

  @Override
  public Iterator<OggInfo> iterator() {
    Iterator<OggInfo> it = oggInfos.values().iterator();
    return new Iterator<>() {
      @Override public boolean hasNext() {
        return it.hasNext();
      }
      @Override public OggInfo next() {
        return it.next();
      }
    };
  }

  public Map<Integer, OggInfo> getOggInfos() {
    return Collections.unmodifiableMap(oggInfos);
  }

  public Map<Integer, OggInfo> getOggInfos(OggCodec.Type type) {
    return oggInfos.entrySet()
        .stream()
        .filter(e -> e.getValue().getCodec().getType().equals(type))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }
}