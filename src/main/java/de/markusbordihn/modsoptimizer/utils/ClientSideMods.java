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
import de.markusbordihn.modsoptimizer.config.ModsDatabase;
import java.io.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientSideMods {

  public static final String CLIENT_MOD_EXTENSION = ".client";
  protected static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final String LOG_PREFIX = "[Client Side Mod]";

  protected ClientSideMods() {}

  public static int enable(File modPath) {
    int result = 0;
    if (modPath == null || !modPath.exists()) {
      log.error("{} ⚠ Unable to find valid mod path: {}", LOG_PREFIX, modPath);
      return result;
    }
    File[] modsFiles = modPath.listFiles();
    if (modsFiles == null) {
      log.error("{} ⚠ Unable to find valid mod files in path: {}", LOG_PREFIX, modPath);
      return result;
    }
    for (File modFile : modsFiles) {
      String modFileName = modFile.getName();
      if (modFileName.endsWith(CLIENT_MOD_EXTENSION) && isClientSide(modFileName)) {
        File clientFile =
            new File(
                modFile
                    .getAbsoluteFile()
                    .toString()
                    .replace(".jar" + CLIENT_MOD_EXTENSION, ".jar"));
        log.info("{} ✔ Try to enable client side mod {} ...", LOG_PREFIX, modFileName);
        if (clientFile.exists()) {
          if (!ModFileUtils.deleteModFile(modFile)) {
            log.error(
                "{} ⚠ Was unable to remove duplicated client side mod {}!", LOG_PREFIX, modFile);
          } else {
            result++;
          }
        } else if (!modFile.renameTo(clientFile)) {
          log.error("{} ⚠ Was unable to enable client side mod {}!", LOG_PREFIX, modFile);
        } else {
          result++;
        }
      }
    }
    return result;
  }

  public static int disable(File modPath) {
    int result = 0;
    if (modPath == null || !modPath.exists()) {
      log.error("{} ⚠ Unable to find valid mod path: {}", LOG_PREFIX, modPath);
      return result;
    }
    File[] modsFiles = modPath.listFiles();
    if (modsFiles == null) {
      log.error("{} ⚠ Unable to find valid mod files in path: {}", LOG_PREFIX, modPath);
      return result;
    }
    for (File modFile : modsFiles) {
      String modFileName = modFile.getName();
      if (!modFileName.endsWith(CLIENT_MOD_EXTENSION)
          && modFileName.endsWith(".jar")
          && isClientSide(modFileName)) {
        File clientFile = new File(modFile.getAbsoluteFile() + CLIENT_MOD_EXTENSION);
        log.info("{} X Try to disable client side mod {} ...", LOG_PREFIX, modFileName);
        if (clientFile.exists()) {
          if (!ModFileUtils.deleteModFile(modFile)) {
            log.error(
                "{} ⚠ Was unable to remove duplicated client side mod {}!", LOG_PREFIX, modFile);
          } else {
            result++;
          }
        } else if (!modFile.renameTo(clientFile)) {
          log.error("{} ⚠ Was unable to disable client side mod {}!", LOG_PREFIX, modFile);
        } else {
          result++;
        }
      }
    }
    return result;
  }

  public static boolean isClientSide(String name) {
    String shortedModName = ModsDatabase.stripeVersionNumbers(name);
    return ClientSideModsConfig.containsClientSideMode(shortedModName);
  }
}
