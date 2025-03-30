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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class AudioInfos {

  private AudioInfos() {}

  public static AudioInfo get(Path path) throws AudioInfoException, IOException {
    return AudioInfoSuppliers.getByFileName(path.getFileName().toString()).getInfos(path);
  }

  public static AudioInfo get(File file) throws AudioInfoException, IOException {
    return AudioInfoSuppliers.getByFileName(file.getName()).getInfos(file);
  }
}
