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

package de.markusbordihn.modsoptimizer.config;

import com.google.gson.JsonObject;
import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import de.markusbordihn.modsoptimizer.Constants;
import de.markusbordihn.modsoptimizer.data.ModFileData.ModEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModsDatabaseConfig {

  public static final Path CONFIG_PATH =
      Paths.get("").toAbsolutePath().resolve("config").resolve(Constants.MOD_ID);

  public static final String ALLOW_REMOTE_DATABASE = "allowRemoteDatabase";
  public static final String DEBUG_ENABLED = "debugEnabled";
  public static final String DEBUG_FORCE_SIDE = "debugForceSide";
  public static final String CONFIG_FILE_NAME = "config.toml";
  private static final Map<String, String> modsMap = new HashMap<>();
  private static boolean allowRemoteDatabase = true;
  private static boolean debugEnabled = false;
  private static String debugForceSide = "default";

  static {
    File configFile = getConfigFile();
    if (configFile == null || !configFile.exists()) {
      configFile = createConfigFile(configFile);
    }
    readConfigFile(configFile);

    if (allowRemoteDatabase) {
      ModsDatabaseUpdater.updateFromRemoteIfNeeded();
    }
    JsonObject json = ModsDatabaseUpdater.getModsDatabase();
    modsMap.putAll(ModsDatabaseUpdater.getSortedModDatabaseMap(json));

    JsonObject overrideJson = ModsDatabaseUpdater.getModsDatabaseOverride();
    Set<String> overrideKeys = ModsDatabaseUpdater.getSortedModDatabaseSet(overrideJson);
    if (!overrideKeys.isEmpty()) {
      Map<String, String> overrides = ModsDatabaseUpdater.getSortedModDatabaseMap(overrideJson);
      for (Map.Entry<String, String> entry : overrides.entrySet()) {
        String modId = entry.getKey();
        String newType = entry.getValue();
        String oldType = modsMap.get(modId);
        if (oldType != null && !oldType.equals(newType)) {
          Constants.LOG.info(
              "{} Override: {} changed from {} to {}", Constants.MOD_NAME, modId, oldType, newType);
        } else if (oldType == null) {
          Constants.LOG.info("{} Override: {} set to {}", Constants.MOD_NAME, modId, newType);
        }
      }
      modsMap.putAll(overrides);
    }

    Constants.LOG.info(
        "{} Mods Database Config File loaded with {} mods client: {}, server: {}, default: {}{}",
        Constants.MOD_NAME,
        modsMap.size(),
        modsMap.values().stream().filter(modType -> modType.equals("client")).count(),
        modsMap.values().stream().filter(modType -> modType.equals("server")).count(),
        modsMap.values().stream().filter(modType -> modType.equals("default")).count(),
        overrideKeys.isEmpty() ? "." : " (" + overrideKeys.size() + " overrides).");
  }

  protected ModsDatabaseConfig() {}

  public static String getConfigFileName() {
    return CONFIG_FILE_NAME;
  }

  public static Map<String, String> getConfig() {
    return modsMap;
  }

  public static boolean isDebugEnabled() {
    return debugEnabled;
  }

  public static String getDebugForceSide() {
    return debugForceSide;
  }

  public static boolean containsMod(String modId) {
    return modsMap.containsKey(modId);
  }

  public static ModEnvironment getModEnvironment(String modId) {
    String modType = modsMap.get(modId);
    if ("client".equals(modType)) {
      return ModEnvironment.CLIENT;
    } else if ("server".equals(modType)) {
      return ModEnvironment.SERVER;
    }
    return ModEnvironment.BOTH;
  }

  public static String cleanTomlFileWithWarnings(File file) throws IOException {
    Set<String> seenKeys = new HashSet<>();
    StringBuilder cleanedToml = new StringBuilder();
    int lineNumber = 0;

    for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
      lineNumber++;
      String trimmed = line.trim();

      // Keep comments and empty lines
      if (trimmed.startsWith("#") || !trimmed.contains("=")) {
        cleanedToml.append(line).append("\n");
        continue;
      }

      String[] keyValue = trimmed.split("=", 2);
      if (keyValue.length < 2) {
        cleanedToml.append(line).append("\n");
        continue;
      }

      String key = keyValue[0].trim();
      if (seenKeys.contains(key)) {
        Constants.LOG.warn(
            "⚠ Duplicate key '{}' found on line {} in config file {}. This entry was ignored.",
            key,
            lineNumber,
            file.getName());
        continue;
      }

      seenKeys.add(key);
      cleanedToml.append(line).append("\n");
    }

    return cleanedToml.toString();
  }

  private static void readConfigFile(File file) {
    if (file == null) {
      file = getConfigFile();
    }
    if (file == null || !file.exists() || !file.canWrite() || !file.canRead()) {
      Constants.LOG.error("⚠ Unable to load config file {}!", file);
      return;
    }

    Constants.LOG.info("Loading Mods Database Config File from {}", file);
    try {
      String cleanedToml = cleanTomlFileWithWarnings(file);
      Map<String, Object> config = new Toml().read(cleanedToml).toMap();

      if (config.containsKey("Database")) {
        Object databaseObject = config.get("Database");
        if (databaseObject instanceof Map<?, ?> database) {
          Object allowRemoteDatabaseValue = database.get(ALLOW_REMOTE_DATABASE);
          if (allowRemoteDatabaseValue instanceof String stringValue) {
            allowRemoteDatabase = Boolean.parseBoolean(stringValue);
          }
        }
      }

      // Read debug options from config file.
      if (config.containsKey("Debug")) {
        Object debugObject = config.get("Debug");
        if (debugObject instanceof Map<?, ?> debugMap) {
          Object debugEnabledValue = debugMap.get(DEBUG_ENABLED);
          if (debugEnabledValue instanceof String stringValue) {
            debugEnabled = Boolean.parseBoolean(stringValue);
          }
          Object debugForceSideValue = debugMap.get(DEBUG_FORCE_SIDE);
          if (debugForceSideValue instanceof String stringValue) {
            debugForceSide = stringValue;
          }
        }
      }
    } catch (Exception exception) {
      Constants.LOG.error("There was an error, loading the config file {}:", file, exception);
    }
  }

  private static void appendFileHeader(StringBuilder stringBuilder) {
    stringBuilder
        .append("# This file was auto-generated by ")
        .append(Constants.MOD_NAME)
        .append("\n")
        .append("# Last update: ")
        .append(LocalDateTime.now())
        .append("\n");
  }

  private static File createConfigFile(File file) {
    Constants.LOG.info("Creating Mods Database Config File under {}", file);

    // Add default header
    StringBuilder textContent = new StringBuilder();
    appendFileHeader(textContent);

    // Prepare toml writer.
    OutputStream outputStream = new ByteArrayOutputStream();
    TomlWriter tomlWriter = new TomlWriter.Builder().build();

    // Add database options.
    Map<String, String> databaseOptions = new HashMap<>();
    databaseOptions.put(ALLOW_REMOTE_DATABASE, allowRemoteDatabase ? "true" : "false");
    try {
      tomlWriter.write(databaseOptions, outputStream);
      textContent.append("\n[Database]\n").append(outputStream);
    } catch (Exception exception) {
      Constants.LOG.error(
          "There was an error, adding the database options to the config file {}:",
          file,
          exception);
      return null;
    }

    // Define debug options.
    Map<String, String> debugOptions = new HashMap<>();
    debugOptions.put(DEBUG_ENABLED, debugEnabled ? "true" : "false");
    debugOptions.put(DEBUG_FORCE_SIDE, debugForceSide);
    outputStream = new ByteArrayOutputStream();
    try {
      tomlWriter.write(debugOptions, outputStream);
      textContent.append("\n[Debug]\n").append(outputStream);
    } catch (Exception exception) {
      Constants.LOG.error(
          "There was an error, adding the debug options to the config file {}:", file, exception);
      return null;
    }

    // Write config file.
    try {
      Files.writeString(file.toPath(), textContent, StandardOpenOption.CREATE_NEW);
    } catch (Exception exception) {
      Constants.LOG.error("There was an error, writing the config file to {}:", file, exception);
      return null;
    }

    return file;
  }

  public static File getConfigFile() {
    Path path = getConfigDirectory();
    if (path != null) {
      return path.resolve(getConfigFileName()).toFile();
    }
    return null;
  }

  private static Path getConfigDirectory() {
    Path resultPath = null;
    try {
      resultPath = Files.createDirectories(CONFIG_PATH);
    } catch (Exception exception) {
      Constants.LOG.error(
          "There was an error, creating the config directory {}:", CONFIG_PATH, exception);
    }
    return resultPath;
  }
}
