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
import de.markusbordihn.modsoptimizer.data.ModFileData;
import java.util.Map;
import java.util.Set;

public class DuplicatedModsUtils {

  protected DuplicatedModsUtils() {}

  public static void optimize(Map<String, Set<ModFileData>> duplicatedMods) {
    if (duplicatedMods == null || duplicatedMods.isEmpty()) {
      return;
    }
    Constants.LOG.info("♻ Optimizing Duplicated Mods ...");
    for (Map.Entry<String, Set<ModFileData>> duplicatedMod : duplicatedMods.entrySet()) {
      String modName = duplicatedMod.getKey();
      Set<ModFileData> modFiles = duplicatedMod.getValue();

      // Find latest mod file, based on the version number.
      ModFileData latestModFile = null;
      for (ModFileData modFile : modFiles) {
        if (latestModFile == null || modFile.version().greaterThan(latestModFile.version())) {
          latestModFile = modFile;
        } else if (modFile.version().equals(latestModFile.version())) {
          String modFileName = modFile.path().getFileName().toString().toLowerCase();
          String latestModFileName = latestModFile.path().getFileName().toString().toLowerCase();
          // Favor mod files without copy / kopie in the file name and shorter file names.
          if ((latestModFileName.contains("copy") && !modFileName.contains("copy"))
              || (latestModFileName.contains("kopie") && !modFileName.contains("kopie"))
              || (latestModFileName.length() > modFileName.length())) {
            latestModFile = modFile;
          }
        }
      }

      // Show warning and keep latest mod file.
      Constants.LOG.warn(
          "⚠ Found {} duplicated Mods with mod id {}: {}", modFiles.size(), modName, modFiles);
      Constants.LOG.info("✔ Will keep most recent Mod: {}", latestModFile);

      // Archive all other mod files.
      for (ModFileData modFile : modFiles) {
        if (modFile != latestModFile && !ModFileUtils.deleteModFile(modFile.path())) {
          Constants.LOG.error("⚠ Was unable to remove outdated mod {}!", modFile);
        }
      }
    }
  }
}
