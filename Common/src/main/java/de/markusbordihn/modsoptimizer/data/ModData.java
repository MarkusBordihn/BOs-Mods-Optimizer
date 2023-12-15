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

import de.markusbordihn.modsoptimizer.Constants;
import de.markusbordihn.modsoptimizer.config.ModsDatabaseConfig;
import de.markusbordihn.modsoptimizer.data.ModFileData.ModEnvironment;
import java.io.File;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ModData {

  private static final String LOG_PREFIX = "[Mod Data]";
  private static final String FILE_EXTENSION = ".jar";

  private static final String OVERVIEW_SEPARATOR =
      "--------------------------------------------------------------------------------------------------------";

  private static final Map<String, Set<ModFileData>> duplicatedModsMap = new HashMap<>();
  private static final Map<String, ModFileData> knownModsMap = new HashMap<>();
  private static final Set<ModFileData> clientModsSet = new HashSet<>();
  private static final Set<ModFileData> serverModsSet = new HashSet<>();
  private static final Set<ModFileData> defaultModsSet = new HashSet<>();

  protected ModData() {}

  public static void parseMods(File modPath) {
    parseMods(modPath, FILE_EXTENSION);
  }

  public static void parseMods(File modPath, String fileExtension) {
    if (modPath == null || !modPath.exists()) {
      Constants.LOG.error("{} ⚠ Unable to find valid mod path: {}", LOG_PREFIX, modPath);
      return;
    }

    // Read mods directory.
    File[] modsFiles = modPath.listFiles();
    if (modsFiles == null) {
      Constants.LOG.error("{} ⚠ Unable to find valid mod files in path: {}", LOG_PREFIX, modPath);
      return;
    }

    // Parsing mods.
    Constants.LOG.info(
        "{} parsing ~{} mods in {} with file extension {} ...",
        LOG_PREFIX,
        modsFiles.length,
        modPath,
        fileExtension);
    for (File modFile : modsFiles) {
      String modFileName = modFile.getName();
      if (modFileName.endsWith(fileExtension)) {
        ModFileData modFileData = readModInfo(modFile);
        if (modFileData != null) {

          // Check for duplicated mods.
          if (knownModsMap.containsKey(modFileData.id())) {
            Constants.LOG.error(
                "{} ⚠ Duplicated mod {} found in {} and {}",
                LOG_PREFIX,
                modFileData.id(),
                modFileData.path(),
                knownModsMap.get(modFileData.id()).path());
            if (!duplicatedModsMap.containsKey(modFileData.id())) {
              duplicatedModsMap.put(modFileData.id(), new HashSet<>());
            }
            duplicatedModsMap.get(modFileData.id()).add(modFileData);
            duplicatedModsMap.get(modFileData.id()).add(knownModsMap.get(modFileData.id()));
          } else {
            knownModsMap.put(modFileData.id(), modFileData);
          }

          // Add mods to environment specific mod list.
          if (modFileData.environment() == ModEnvironment.CLIENT) {
            clientModsSet.add(modFileData);
          } else if (modFileData.environment() == ModEnvironment.SERVER) {
            serverModsSet.add(modFileData);
          } else {
            defaultModsSet.add(modFileData);
          }
        }
      }
    }

    showStats();
    showOverview();
  }

  private static void showStats() {
    if (!duplicatedModsMap.isEmpty()) {
      Constants.LOG.info(
          "{} ⚠ Found {} duplicated mods in {} mod files.",
          LOG_PREFIX,
          duplicatedModsMap.size(),
          knownModsMap.size());
    }
    if (!clientModsSet.isEmpty()) {
      Constants.LOG.info(
          "{} Found {} client mods in {} mod files.",
          LOG_PREFIX,
          clientModsSet.size(),
          knownModsMap.size());
    }
    if (!serverModsSet.isEmpty()) {
      Constants.LOG.info(
          "{} Found {} server mods in {} mod files.",
          LOG_PREFIX,
          serverModsSet.size(),
          knownModsMap.size());
    }
    if (!defaultModsSet.isEmpty()) {
      Constants.LOG.info(
          "{} Found {} default mods in {} mod files.",
          LOG_PREFIX,
          defaultModsSet.size(),
          knownModsMap.size());
    }
  }

  private static void showOverview() {
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String overviewHeader =
        String.format(
            "| %-32s | %-22s | %-8s | %-7s | %-19s |",
            "ID", "VERSION", "TYPE", "ENVIRONMENT", "TIMESTAMP");
    Constants.LOG.info(OVERVIEW_SEPARATOR);
    Constants.LOG.info(overviewHeader);
    Constants.LOG.info(OVERVIEW_SEPARATOR);
    for (ModFileData modFileData : knownModsMap.values()) {
      String modEntry =
          String.format(
              "| %-32s | %-22s | %-8s | %-7s | %-19s |",
              modFileData.id(),
              modFileData.version(),
              modFileData.modType(),
              modFileData.environment(),
              modFileData.timestamp().format(dateTimeFormatter));
      Constants.LOG.info(modEntry);
    }
    Constants.LOG.info(OVERVIEW_SEPARATOR);
  }

  public static ModFileData readModInfo(File modFile) {
    if (modFile == null || !modFile.exists()) {
      return null;
    }
    return readModInfo(modFile.toPath());
  }

  public static Set<ModFileData> getKnownMods() {
    return new HashSet<>(knownModsMap.values());
  }

  public static Set<ModFileData> getClientMods() {
    return new HashSet<>(clientModsSet);
  }

  public static Set<ModFileData> getServerMods() {
    return new HashSet<>(serverModsSet);
  }

  public static Set<ModFileData> getDefaultMods() {
    return new HashSet<>(defaultModsSet);
  }

  public static Map<String, Set<ModFileData>> getDuplicatedMods() {
    return new HashMap<>(duplicatedModsMap);
  }

  public static ModFileData readModInfo(Path modFile) {
    try (JarFile jarFile = new JarFile(modFile.toFile())) {
      // Read manifest
      Manifest manifest = jarFile.getManifest();
      if (manifest == null) {
        Constants.LOG.error("{} ⚠ Unable to read manifest from mod file {}", LOG_PREFIX, modFile);
        return null;
      }

      // Parse mod file data
      ModFileData modFileData = ModFileData.parseModFile(manifest, modFile, jarFile);

      // Check local mods database and update mod environment, if needed.
      if (ModsDatabaseConfig.containsMod(modFileData.id())) {
        ModEnvironment modEnvironment = ModsDatabaseConfig.getModEnvironment(modFileData.id());
        if (modEnvironment != modFileData.environment()) {
          Constants.LOG.info(
              "{} Overwrite mod environment for {} from {} to {}",
              LOG_PREFIX,
              modFileData.id(),
              modFileData.environment(),
              modEnvironment);
          modFileData =
              new ModFileData(
                  modFileData.path(),
                  modFileData.id(),
                  modFileData.modType(),
                  modFileData.name(),
                  modFileData.version(),
                  modEnvironment,
                  modFileData.timestamp());
        }
      }

      // Debug output
      if (ModsDatabaseConfig.isDebugEnabled()) {
        Constants.LOG.info("{} {}", LOG_PREFIX, modFileData);
      }

      return modFileData;
    } catch (Exception e) {
      Constants.LOG.error("{} ⚠ Unable to read mod file {}:", LOG_PREFIX, modFile, e);
    }
    return null;
  }
}
