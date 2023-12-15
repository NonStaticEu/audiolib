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

import java.io.EOFException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class AudioIssue implements Serializable {

  public static final String META_SKIPPED = "skipped";
  public static final String META_MESSAGE = "message";

  private final long location;
  private final Type type;
  private final Throwable cause;
  private final Map<String, Serializable> metas;

  private AudioIssue(long location, Type type, Throwable cause) {
    this(location, type, cause, null);
  }

  private AudioIssue(long location, Type type, Throwable cause, Map<String, Serializable> metas) {
    this.location = location;
    this.type = Objects.requireNonNull(type);
    this.cause = cause;
    this.metas = metas != null ? Collections.unmodifiableMap(metas) : null;
  }

  public Object getMeta(String key) {
    return metas.get(key);
  }

  @Override
  public String toString() {
    String details = Optional.ofNullable(metas)
        .filter(map -> !map.isEmpty())
        .map(map -> ", " + map)
        .orElse("");
    return AudioIssue.class.getSimpleName() + ' ' + type + " at " + location + details;
  }

  public static AudioIssue sync(long location, long skipped) {
    return new AudioIssue(location, Type.SYNC, null,
        Map.of(META_SKIPPED, skipped) // skipped before sync recovery
    );
  }

  public static AudioIssue format(@NonNull AudioFormatException exception) {
    return new AudioIssue(exception.getLocation(), Type.FORMAT, exception);
  }

  public static AudioIssue eof(long location, @NonNull EOFException exception) {
    return new AudioIssue(location, Type.EOF, exception);
  }

  public static AudioIssue other(long location, @NonNull Throwable throwable) {
    return new AudioIssue(location, Type.OTHER, throwable);
  }

  public static AudioIssue other(long location, Map<String, Serializable> metas) {
    return new AudioIssue(location, Type.OTHER, null, metas);
  }


  public enum Type {
    SYNC, FORMAT, EOF, OTHER
  }
}
