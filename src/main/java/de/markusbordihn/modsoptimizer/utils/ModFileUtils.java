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

package de.markusbordihn.modsoptimizer.utils;

import de.markusbordihn.modsoptimizer.Constants;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModFileUtils {

  protected static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final String LOG_PREFIX = "[Mod File Utils]";

  protected ModFileUtils() {}

  public static boolean deleteModFile(File file) {
    if (file == null) {
      return false;
    }
    if (file.isDirectory()) {
      log.error(
          "{} ⚠ Was unable to delete mod file {}, because it's an directory!", LOG_PREFIX, file);
      return false;
    }
    Path filePath = file.toPath();
    try {
      return Files.deleteIfExists(filePath);
    } catch (IOException e) {
      log.error("{} ⚠ Was unable to delete mod file {}, because of: {}", LOG_PREFIX, file, e);
      return false;
    }
  }
}
