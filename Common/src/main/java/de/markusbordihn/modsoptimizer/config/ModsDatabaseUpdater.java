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
import com.google.gson.JsonParser;
import de.markusbordihn.modsoptimizer.Constants;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ModsDatabaseUpdater {

  private static final String LOG_PREFIX = "[Mods Database Updater]";

  private static final Path DEFAULT_CONFIG_DIR =
      Paths.get("").toAbsolutePath().resolve("config").resolve(Constants.MOD_ID);
  private static final String REMOTE_URL =
      "https://raw.githubusercontent.com/MarkusBordihn/BOs-Mods-Optimizer/1.18.2/Common/src/main/resources/mods-database.json";
  private static final int CACHE_MAX_AGE_HOURS = 48;
  private static final String MODS_DATABASE_FILE = "mods-database.json";
  private static final String KEY_CLIENT = "client";
  private static final String KEY_SERVER = "server";
  private static final String KEY_BOTH = "both";
  private static final Set<String> VALID_KEYS = Set.of(KEY_CLIENT, KEY_SERVER, KEY_BOTH);
  private static Path configDir = DEFAULT_CONFIG_DIR;

  public static Path getConfigDir() {
    return configDir;
  }

  public static void setConfigDir(Path customConfigDir) {
    configDir = customConfigDir != null ? customConfigDir : DEFAULT_CONFIG_DIR;
  }

  public static void resetConfigDir() {
    configDir = DEFAULT_CONFIG_DIR;
  }

  private static Path getLocalFile() {
    return configDir.resolve(MODS_DATABASE_FILE);
  }

  private static Path getOverrideFile() {
    return configDir.resolve("mods-database-override.json");
  }

  public static void updateFromRemoteIfNeeded() {
    Path localFile = getLocalFile();
    try {
      Files.createDirectories(configDir);

      if (Files.exists(localFile)) {
        Instant lastModified = Files.getLastModifiedTime(localFile).toInstant();
        if (isManuallyModified(localFile)) {
          Constants.LOG.info(
              "{} 🛑 Local mods-database.json was modified by user, skipping remote update.",
              LOG_PREFIX);
          String currentHash = calculateSha256(localFile);
          Files.writeString(
              localFile.resolveSibling("mods-database.json.sha256"),
              currentHash,
              StandardCharsets.UTF_8);
          return;
        }

        if (lastModified.plus(Duration.ofHours(CACHE_MAX_AGE_HOURS)).isAfter(Instant.now())) {
          Constants.LOG.info(
              "{} ⏳ Local mods-database.json is still fresh (last update: {}).",
              LOG_PREFIX,
              lastModified);
          return;
        }

        Constants.LOG.info(
            "{} 🔄 Local mods-database.json is outdated, fetching from remote …", LOG_PREFIX);
      }

      try (InputStream inputStream = new URL(REMOTE_URL).openStream()) {
        Files.copy(inputStream, localFile, StandardCopyOption.REPLACE_EXISTING);
        String newHash = calculateSha256(localFile);
        Files.writeString(
            localFile.resolveSibling("mods-database.json.sha256"), newHash, StandardCharsets.UTF_8);
        Constants.LOG.info(
            "{} ✅ Fetched remote mods-database.json and saved SHA-256 hash.", LOG_PREFIX);
      }

    } catch (IOException e) {
      Constants.LOG.warn(
          "{} ⚠ Failed to update mods-database.json: {}", LOG_PREFIX, e.getMessage());
    }
  }

  public static JsonObject getModsDatabase() {
    Path localFile = getLocalFile();
    JsonObject baseDatabase = new JsonObject();

    if (Files.exists(localFile)) {
      try (InputStream inputStream = Files.newInputStream(localFile)) {
        JsonObject jsonObject = parseAndValidate(inputStream);
        if (jsonObject != null) {
          baseDatabase = jsonObject;
        }
      } catch (IOException e) {
        Constants.LOG.warn(
            "{} ⚠ Failed to read local mods-database.json: {}", LOG_PREFIX, e.getMessage());
      }
    }

    if (baseDatabase.size() == 0) {
      try (InputStream inputStream =
          ModsDatabaseUpdater.class.getClassLoader().getResourceAsStream("mods-database.json")) {
        if (inputStream != null) {
          JsonObject jsonObject = parseAndValidate(inputStream);
          if (jsonObject != null) {
            baseDatabase = jsonObject;
          }
        }
      } catch (IOException e) {
        Constants.LOG.warn(
            "{} ⚠ Failed to read fallback mods-database.json: {}", LOG_PREFIX, e.getMessage());
      }
    }

    if (baseDatabase.size() == 0) {
      Constants.LOG.error("{} ❌ Could not load any valid mods-database.json!", LOG_PREFIX);
      return new JsonObject();
    }

    // Validate base database for duplicates
    validateDuplicateModIds(baseDatabase, "mods-database.json");

    // Apply overrides
    JsonObject overrideDatabase = getModsDatabaseOverride();
    if (overrideDatabase.size() > 0) {
      validateDuplicateModIds(overrideDatabase, "mods-database-override.json");
      baseDatabase = applyOverrides(baseDatabase, overrideDatabase);
    }

    return baseDatabase;
  }

  public static JsonObject getModsDatabaseOverride() {
    Path overrideFile = getOverrideFile();
    if (!Files.exists(overrideFile)) {
      createOverrideTemplate();
      return new JsonObject();
    }

    try (InputStream inputStream = Files.newInputStream(overrideFile)) {
      JsonObject jsonObject = parseAndValidate(inputStream);
      if (jsonObject != null) {
        return jsonObject;
      }
    } catch (IOException e) {
      Constants.LOG.warn(
          "{} ⚠ Failed to read mods-database-override.json: {}", LOG_PREFIX, e.getMessage());
    }

    return new JsonObject();
  }

  private static void createOverrideTemplate() {
    try {
      Files.createDirectories(configDir);
      Path overrideFile = getOverrideFile();
      String template =
"""
{
  "description": "Override entries from mods-database.json. This file is never modified automatically.",
  "server": [
    "server-override-mod-id"
  ],
  "client": [
    "client-override-mod-id"
  ],
  "both": [
    "both-override-mod-id"
  ]
}
""";
      Files.writeString(overrideFile, template, StandardCharsets.UTF_8);
      Constants.LOG.info("{} ✅ Created mods-database-override.json template.", LOG_PREFIX);
    } catch (IOException e) {
      Constants.LOG.warn(
          "{} ⚠ Failed to create mods-database-override.json template: {}",
          LOG_PREFIX,
          e.getMessage());
    }
  }

  public static Map<String, String> getSortedModDatabaseMap(JsonObject jsonObject) {
    Map<String, String> modIdMap = new TreeMap<>();
    addEntriesToMap(jsonObject, "client", "client", modIdMap);
    addEntriesToMap(jsonObject, "server", "server", modIdMap);
    addEntriesToMap(jsonObject, "both", "default", modIdMap);
    return modIdMap;
  }

  public static Set<String> getSortedModDatabaseSet(JsonObject jsonObject) {
    Set<String> modIdSet = new java.util.TreeSet<>();
    if (jsonObject.has("client")) {
      jsonObject.getAsJsonArray("client").forEach(e -> modIdSet.add(e.getAsString()));
    }
    if (jsonObject.has("server")) {
      jsonObject.getAsJsonArray("server").forEach(e -> modIdSet.add(e.getAsString()));
    }
    if (jsonObject.has("both")) {
      jsonObject.getAsJsonArray("both").forEach(e -> modIdSet.add(e.getAsString()));
    }
    return modIdSet;
  }

  private static void addEntriesToMap(
      JsonObject jsonObject, String key, String value, Map<String, String> map) {
    if (jsonObject.has(key)) {
      jsonObject
          .getAsJsonArray(key)
          .forEach(jsonElement -> map.put(jsonElement.getAsString(), value));
    }
  }

  private static JsonObject parseAndValidate(InputStream inputStream) {
    try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
      JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
      for (String key : VALID_KEYS) {
        if (!jsonObject.has(key) || !jsonObject.get(key).isJsonArray()) {
          Constants.LOG.warn(
              "{} ⚠ Key '{}' missing or invalid in mods database JSON", LOG_PREFIX, key);
          return null;
        }
      }
      return jsonObject;
    } catch (Exception e) {
      Constants.LOG.warn("{} ⚠ Failed to parse mods database JSON: {}", LOG_PREFIX, e.getMessage());
      return null;
    }
  }

  private static void validateDuplicateModIds(JsonObject jsonObject, String fileName) {
    Map<String, String> modIdToCategory = new java.util.HashMap<>();

    for (String category : VALID_KEYS) {
      if (jsonObject.has(category) && jsonObject.get(category).isJsonArray()) {
        jsonObject
            .getAsJsonArray(category)
            .forEach(
                element -> {
                  String modId = element.getAsString();
                  if (modIdToCategory.containsKey(modId)) {
                    Constants.LOG.error(
                        "{} ❌ Duplicate mod ID '{}' found in both '{}' and '{}' in {}",
                        LOG_PREFIX,
                        modId,
                        modIdToCategory.get(modId),
                        category,
                        fileName);
                  } else {
                    modIdToCategory.put(modId, category);
                  }
                });
      }
    }
  }

  private static JsonObject applyOverrides(JsonObject baseDatabase, JsonObject overrideDatabase) {
    JsonObject mergedDatabase = new JsonObject();

    // Collect all mod IDs from override database
    Set<String> overrideModIds = new java.util.HashSet<>();
    for (String category : VALID_KEYS) {
      if (overrideDatabase.has(category) && overrideDatabase.get(category).isJsonArray()) {
        overrideDatabase
            .getAsJsonArray(category)
            .forEach(element -> overrideModIds.add(element.getAsString()));
      }
    }

    // Copy base database entries excluding those in override
    for (String category : VALID_KEYS) {
      com.google.gson.JsonArray mergedArray = new com.google.gson.JsonArray();

      if (baseDatabase.has(category) && baseDatabase.get(category).isJsonArray()) {
        baseDatabase
            .getAsJsonArray(category)
            .forEach(
                element -> {
                  String modId = element.getAsString();
                  if (!overrideModIds.contains(modId)) {
                    mergedArray.add(modId);
                  }
                });
      }

      // Add override entries
      if (overrideDatabase.has(category) && overrideDatabase.get(category).isJsonArray()) {
        overrideDatabase
            .getAsJsonArray(category)
            .forEach(element -> mergedArray.add(element.getAsString()));
      }

      mergedDatabase.add(category, mergedArray);
    }

    return mergedDatabase;
  }

  private static boolean isManuallyModified(Path path) {
    Path hashFile = path.resolveSibling(path.getFileName() + ".sha256");
    if (!Files.exists(path) || !Files.exists(hashFile)) {
      return false;
    }

    try {
      String expectedHash = Files.readString(hashFile, StandardCharsets.UTF_8).trim();
      String actualHash = calculateSha256(path);
      boolean modified = !expectedHash.equals(actualHash);
      if (modified) {
        Constants.LOG.info(
            "{} 🛑 mods-database.json was modified (SHA-256 hash mismatch)", LOG_PREFIX);
      }
      return modified;
    } catch (IOException e) {
      Constants.LOG.warn(
          "{} ⚠ Failed to check hash for {}: {}", LOG_PREFIX, path.getFileName(), e.getMessage());
      return false;
    }
  }

  private static String calculateSha256(Path file) throws IOException {
    try (InputStream inputStream = Files.newInputStream(file)) {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        digest.update(buffer, 0, bytesRead);
      }
      byte[] hashBytes = digest.digest();
      StringBuilder stringBuilder = new StringBuilder();
      for (byte b : hashBytes) {
        stringBuilder.append(String.format("%02x", b));
      }
      return stringBuilder.toString();
    } catch (Exception e) {
      throw new IOException(LOG_PREFIX + " Unable to calculate SHA-256 hash for " + file, e);
    }
  }
}
