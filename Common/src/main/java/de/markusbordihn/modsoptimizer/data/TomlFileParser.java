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

import com.moandjiezana.toml.Toml;
import de.markusbordihn.modsoptimizer.Constants;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class TomlFileParser {

  private TomlFileParser() {}

  public static Toml readTomlFile(JarFile jarFile, Path path) {
    return readTomlFile(jarFile, path, false);
  }

  public static Toml tryReadTomlFile(JarFile jarFile, Path path) {
    return readTomlFile(jarFile, path, true);
  }

  public static Toml readTomlFile(JarFile jarFile, Path path, boolean ignoreErrors) {
    String normalizedPath = normalizePath(path);
    ZipEntry modsFile = jarFile.getEntry(normalizedPath);
    if (modsFile != null && !modsFile.isDirectory()) {
      try (InputStream inputStream = jarFile.getInputStream(modsFile)) {
        return new Toml().read(inputStream);
      } catch (Exception e) {
        if (!ignoreErrors) {
          Constants.LOG.error("Error reading TOML file {} from {}: {}", path, jarFile, e);
        }
      }
    } else if (!ignoreErrors) {
      Constants.LOG.error("TOML file {} not found in {}", normalizedPath, jarFile);
    }
    return new Toml();
  }

  private static String normalizePath(Path path) {
    return path.toString().replace("\\", "/");
  }
}
