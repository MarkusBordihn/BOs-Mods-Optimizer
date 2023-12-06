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
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModData {

  protected static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final String LOG_PREFIX = "[Mod Data]";

  protected  ModData() {}

  public static void parseMods(File modPath, String fileExtension) {
    if (modPath == null || !modPath.exists()) {
      log.error("{} ⚠ Unable to find valid mod path: {}", LOG_PREFIX, modPath);
      return;
    }
    File[] modsFiles = modPath.listFiles();
    if (modsFiles == null) {
      log.error("{} ⚠ Unable to find valid mod files in path: {}", LOG_PREFIX, modPath);
      return;
    }
    log.info(
        "{} parsing ~{} mods in {} with file extension {} ...",
        LOG_PREFIX,
        modsFiles.length,
        modPath,
        fileExtension);
    for (File modFile : modsFiles) {
      String modFileName = modFile.getName();
      if (modFileName.endsWith(fileExtension)) {
        readModInfo(modFile.toPath());
      }
    }
  }

  private static void readModInfo(Path modFile) {
    log.info("{} Found mod file {}", LOG_PREFIX, modFile);
  }
}
