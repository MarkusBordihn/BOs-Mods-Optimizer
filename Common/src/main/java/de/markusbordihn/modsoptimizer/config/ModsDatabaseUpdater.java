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

  private static final Path CONFIG_DIR =
      Paths.get("").toAbsolutePath().resolve("config").resolve(Constants.MOD_ID);
  private static final Path LOCAL_FILE = CONFIG_DIR.resolve("mods-database.json");
  private static final String REMOTE_URL =
      "https://raw.githubusercontent.com/MarkusBordihn/BOs-Mods-Optimizer/1.18.2/Common/src/main/resources/mods-database.json";
  private static final int CACHE_MAX_AGE_HOURS = 24;

  private static final Set<String> VALID_KEYS = Set.of("client", "server", "both");

  public static void updateFromRemoteIfNeeded() {
    try {
      Files.createDirectories(CONFIG_DIR);

      if (Files.exists(LOCAL_FILE)) {
        Instant lastModified = Files.getLastModifiedTime(LOCAL_FILE).toInstant();
        if (isManuallyModified(LOCAL_FILE)) {
          Constants.LOG.info(
              "{} 🛑 Local mods-database.json was modified by user, skipping remote update.",
              LOG_PREFIX);
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
        Files.copy(inputStream, LOCAL_FILE, StandardCopyOption.REPLACE_EXISTING);
        String newHash = calculateSha256(LOCAL_FILE);
        Files.writeString(
            LOCAL_FILE.resolveSibling("mods-database.json.sha256"),
            newHash,
            StandardCharsets.UTF_8);
        Constants.LOG.info(
            "{} ✅ Fetched remote mods-database.json and saved SHA-256 hash.", LOG_PREFIX);
      }

    } catch (IOException e) {
      Constants.LOG.warn(
          "{} ⚠ Failed to update mods-database.json: {}", LOG_PREFIX, e.getMessage());
    }
  }

  public static JsonObject getModsDatabase() {
    // Read local mods-database.json
    if (Files.exists(LOCAL_FILE)) {
      try (InputStream inputStream = Files.newInputStream(LOCAL_FILE)) {
        JsonObject jsonObject = parseAndValidate(inputStream);
        if (jsonObject != null) return jsonObject;
      } catch (IOException e) {
        Constants.LOG.warn(
            "{} ⚠ Failed to read local mods-database.json: {}", LOG_PREFIX, e.getMessage());
      }
    }

    // Read fallback mods-database.json
    try (InputStream inputStream =
        ModsDatabaseUpdater.class.getClassLoader().getResourceAsStream("mods-database.json")) {
      if (inputStream != null) {
        JsonObject jsonObject = parseAndValidate(inputStream);
        if (jsonObject != null) return jsonObject;
      }
    } catch (IOException e) {
      Constants.LOG.warn(
          "{} ⚠ Failed to read fallback mods-database.json: {}", LOG_PREFIX, e.getMessage());
    }

    Constants.LOG.error("{} ❌ Could not load any valid mods-database.json!", LOG_PREFIX);
    return new JsonObject();
  }

  public static Map<String, String> getSortedModDatabaseMap(JsonObject jsonObject) {
    Map<String, String> modIdMap = new TreeMap<>();
    addEntriesToMap(jsonObject, "client", "client", modIdMap);
    addEntriesToMap(jsonObject, "server", "server", modIdMap);
    addEntriesToMap(jsonObject, "both", "default", modIdMap);
    return modIdMap;
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
              "{} ⚠ Key '{}' missing or invalid in mods-database.json", LOG_PREFIX, key);
          return null;
        }
      }
      return jsonObject;
    } catch (Exception e) {
      Constants.LOG.warn("{} ⚠ Failed to parse mods-database.json: {}", LOG_PREFIX, e.getMessage());
      return null;
    }
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
