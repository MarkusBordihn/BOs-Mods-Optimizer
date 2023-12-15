/**
 * Copyright 2022 Markus Bordihn
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.markusbordihn.modsoptimizer.utils;

import de.markusbordihn.modsoptimizer.Constants;
import de.markusbordihn.modsoptimizer.data.ModFileData;
import java.io.File;
import java.util.Set;

public class ClientSideModsUtils {

  public static final String CLIENT_MOD_EXTENSION = ".client";
  private static final String LOG_PREFIX = "[Client Side Mod]";

  protected ClientSideModsUtils() {}

  public static int enable(File modPath) {
    int result = 0;
    if (modPath == null || !modPath.exists()) {
      Constants.LOG.error("{} unable to find valid mod path: {}", LOG_PREFIX, modPath);
      return result;
    }
    File[] modsFiles = modPath.listFiles();
    for (File modFile : modsFiles) {
      String modFileName = modFile.getName();
      if (modFileName.endsWith(CLIENT_MOD_EXTENSION)) {
        File clientFile =
            new File(
                modFile
                    .getAbsoluteFile()
                    .toString()
                    .replace(".jar" + CLIENT_MOD_EXTENSION, ".jar"));
        Constants.LOG.info("{} ✔ Try to enable client side mod {} ...", LOG_PREFIX, modFileName);
        if (clientFile.exists()) {
          if (!ModFileUtils.deleteModFile(modFile)) {
            Constants.LOG.error(
                "{} ⚠ Was unable to remove duplicated client side mod {}!", LOG_PREFIX, modFile);
          } else {
            Constants.LOG.info("{} ✔ Removed duplicated client side mod {}!", LOG_PREFIX, modFile);
            result++;
          }
        } else if (!modFile.renameTo(clientFile)) {
          Constants.LOG.error("{} ⚠ Was unable to enable client side mod {}!", LOG_PREFIX, modFile);
        } else {
          Constants.LOG.info("{} ✔ Enabled client side mod {}!", LOG_PREFIX, modFileName);
          result++;
        }
      }
    }
    return result;
  }

  public static int disable(Set<ModFileData> modFiles) {
    int result = 0;
    if (modFiles == null || modFiles.isEmpty()) {
      return result;
    }
    for (ModFileData modFileData : modFiles) {
      if (modFileData.environment() == ModFileData.ModEnvironment.CLIENT) {
        File modFile = modFileData.path().toFile();
        File clientFile = new File(modFile.getAbsoluteFile() + CLIENT_MOD_EXTENSION);
        Constants.LOG.info(
            "{} ❌ Try to disable client side mod {} ...", LOG_PREFIX, modFileData.id());
        if (clientFile.exists()) {
          if (!ModFileUtils.deleteModFile(clientFile)) {
            Constants.LOG.error(
                "{} ⚠ Was unable to remove client side mod {} with {}!",
                LOG_PREFIX,
                modFileData.id(),
                clientFile);
          } else {
            Constants.LOG.info(
                "{} ✔ Removed duplicated client side mod {} with {}!",
                LOG_PREFIX,
                modFileData.id(),
                clientFile);
            result++;
          }
        } else if (!modFile.renameTo(clientFile)) {
          Constants.LOG.error(
              "{} ⚠ Was unable to disable client side mod {}!", LOG_PREFIX, modFile);
        } else {
          Constants.LOG.info("{} ✔ Disabled client side mod {}!", LOG_PREFIX, modFileData.id());
          result++;
        }
      } else {
        Constants.LOG.info(
            "{} ❌ Skip wrongly client side mod {} with {}!",
            LOG_PREFIX,
            modFileData.id(),
            modFileData.environment());
      }
    }
    return result;
  }
}
