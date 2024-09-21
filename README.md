# Audiolib
A simple java audio file info lib

## Supported formats
* Wave
* Flac
* Aiff
* MP3
* MP2
* Ogg Vorbis
* XM

## Usage

If you already know the file type to analyze, say, a FLAC, you should directly use the dedicated info supplier:
`new FlacInfoSupplier().getInfos(...)`
There's always 3 methods available:
* `getInfos(File file)`
* `getInfos(Path path)`
* `getInfos(InputStream is, String name)`

If you don't know the file type in advance (say you're analyzing mixed mixed audio files from a folder tree), you can retrieve any AudioInfoSupplier implementation from the static methods `AudioInfoSuppliers.getByFileName() `or `AudioInfoSuppliers.getByExtension()`. 
At this time there is no introspection of the file (using magic strings/numbers or alike)

Each implementation of AudioInfoSupplier returns a dedicated info file (eg: FlacInfo for FlacInfoSupplier#getInfos) containing format-specific properties,
but there are always 3 common ones accessible through: getName(), getDuration(), getIssues()
Issues are the recoverable ones (eg: loss of sync in a MP3).

Else the `getInfos()` implementations of each `AudioInfoSupplier` are likely to throw
* IOException: stream red issues, etc)
* AudioInfoException: specific format-related unrecoverable errors FIXME


# LICENSE
This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
