/*
 * Copyright 2022 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.modsoptimizer.data;

import com.github.zafarkhaja.semver.Version;
import java.nio.file.Path;
import java.time.LocalDateTime;

public record ModFileData(
    Path path,
    String id,
    ModType modType,
    String name,
    Version version,
    ModEnvironment environment,
    LocalDateTime timestamp) {

  public static final Version EMPTY_VERSION = Version.valueOf("0.0.0");
  public static final String EMPTY_MOD_ID = "unknown_id";
  public static final String EMPTY_MOD_NAME = "Unknown";
  public static final LocalDateTime EMPTY_TIMESTAMP = LocalDateTime.now();

  public enum ModType {
    FABRIC,
    FORGE,
    NEOFORGE,
    MIXED,
    QUILT,
    UNKNOWN
  }

  public enum ModEnvironment {
    DEFAULT,
    CLIENT,
    SERVER,
    SERVICE,
    LIBRARY,
    LANGUAGE_PROVIDER,
    DATA_PACK,
    UNKNOWN
  }
}
