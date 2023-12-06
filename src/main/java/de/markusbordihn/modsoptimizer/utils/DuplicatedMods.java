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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DuplicatedMods {

  protected static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final String LOG_PREFIX = "[Duplicated Mods]";

  protected DuplicatedMods() {}

  public static int searchDuplicatedMods(File modPath) {
    return searchDuplicatedMods(modPath, ".jar", false);
  }

  public static int searchDuplicatedClientMods(File modPath) {
    return searchDuplicatedMods(modPath, ".jar.client", false);
  }

  public static int searchDuplicatedMods(File modPath, String fileExtension, boolean testMode) {
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
    log.info(
        "{} checking ~{} mods in {} for duplication with file extension {} ...",
        LOG_PREFIX,
        modsFiles.length,
        modPath,
        fileExtension);
    List<File> checkedFiles = new ArrayList<>();
    for (File modFile : modsFiles) {
      String modFileName = modFile.getName();
      if (modFileName.endsWith(fileExtension) && !checkedFiles.contains(modFile)) {
        String simplifiedModName = ModsDatabase.stripeVersionNumbers(modFileName);
        List<File> duplicatedMods = new ArrayList<>();
        for (File modFileToCompare : modsFiles) {
          String modFileToCompareName = modFileToCompare.getName();
          if (modFileToCompareName.endsWith(fileExtension)) {
            String simplifiedModNameToCompare =
                ModsDatabase.stripeVersionNumbers(modFileToCompareName);
            if (modFile != modFileToCompare
                && simplifiedModName.equals(simplifiedModNameToCompare)) {
              duplicatedMods.add(modFileToCompare);
              checkedFiles.add(modFileToCompare);
            }
          }
        }
        if (!duplicatedMods.isEmpty()) {
          duplicatedMods.add(modFile);
          log.info("{} ⚠ Found duplicated Mods: {}", LOG_PREFIX, duplicatedMods);
          log.info("{} ✔ Will keep most recent Mod: {}", LOG_PREFIX, findLatestMod(duplicatedMods));
          archiveDuplicatedMods(duplicatedMods, testMode);
          result++;
        }
      }
    }
    return result;
  }

  public static void archiveDuplicatedMods(List<File> modList, boolean testMode) {
    File newestMod = findLatestMod(modList);
    for (File modFile : modList) {
      if (modFile != newestMod) {
        if (testMode) {
          log.info("{} Would remove duplicated mod {} ...", LOG_PREFIX, modFile);
        } else if (!ModFileUtils.deleteModFile(modFile)) {
          log.error("{} ⚠ Was unable to remove outdated mod {}!", LOG_PREFIX, modFile);
        }
      }
    }
  }

  public static String normalizeVersionNumber(String fileName) {
    String versionNumber = getVersionNumber(fileName);
    String unifiedVersionNumber = versionNumber;
    String[] versionNumberParts = versionNumber.split("\\.");

    // Handle version numbers like 20220709 and 20220517
    if (versionNumberParts.length == 1 && !versionNumberParts[0].isEmpty()) {
      unifiedVersionNumber = String.format("%03d.000.000", Integer.parseInt(versionNumberParts[0]));
    }

    // Handle version numbers like 1.0, 1.1, 1.2 ...
    else if (versionNumberParts.length == 2) {
      unifiedVersionNumber =
          String.format(
              "%03d.%03d.000",
              Integer.parseInt(versionNumberParts[0]), Integer.parseInt(versionNumberParts[1]));
    }

    // Handle version numbers like 1.0.0, 1.1.0, 1.2.0 ...
    else if (versionNumberParts.length == 3) {
      unifiedVersionNumber =
          String.format(
              "%03d.%03d.%03d",
              Integer.parseInt(versionNumberParts[0]),
              Integer.parseInt(versionNumberParts[1]),
              Integer.parseInt(versionNumberParts[2]));
    }

    // Handle version numbers like 1.0.0.0, 1.1.0.0, 1.2.0.0 ...
    else if (versionNumberParts.length == 4) {
      unifiedVersionNumber =
          String.format(
              "%03d.%03d.%03d.%03d",
              Integer.parseInt(versionNumberParts[0]),
              Integer.parseInt(versionNumberParts[1]),
              Integer.parseInt(versionNumberParts[2]),
              Integer.parseInt(versionNumberParts[3]));
    }

    // Handle unknown version number format
    else {
      log.warn(
          "Unable to extract version number from {} got {}, use {} instead!",
          fileName,
          versionNumberParts,
          versionNumber);
    }
    return unifiedVersionNumber;
  }

  public static File findLatestMod(List<File> modList) {
    Map<String, File> modListVersions = new HashMap<>();
    modList.sort(Collections.reverseOrder());

    // Pre-format version numbers per file.
    for (File file : modList) {
      String fileName = file.getName();
      modListVersions.put(normalizeVersionNumber(fileName), file);
    }

    // Sorting files by unified version numbers.
    List<String> sortedModList = new ArrayList<>(modListVersions.keySet());
    sortedModList.sort(Collections.reverseOrder());

    return modListVersions.getOrDefault(sortedModList.get(0), modList.get(0));
  }

  public static String getVersionNumber(String fileName) {
    return fileName
        .replace("1.18.2.jar", "")
        .replace("mc1.18.2", "")
        .replace("[1.18.2]", "")
        .replaceAll("[^0-9.-]", "")
        .replace("1.18-", "")
        .replace("1.18.1-", "")
        .replace("1.18.2-", "")
        .replace("-1.18.2", "")
        .replaceAll("(\\d)-(\\d)", "$1.$2")
        .replace("-", "")
        .replaceAll("\\.$", "")
        .replaceAll("^\\.", "");
  }
}
