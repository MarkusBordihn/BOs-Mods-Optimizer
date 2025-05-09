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
              "🛑 Local mods-database.json was modified by user, skipping remote update.");
          return;
        }

        if (lastModified.plus(Duration.ofHours(CACHE_MAX_AGE_HOURS)).isAfter(Instant.now())) {
          Constants.LOG.info(
              "⏳ Local mods-database.json is still fresh (last update: {}).", lastModified);
          return;
        }

        Constants.LOG.info("🔄 Local mods-database.json is outdated, fetching from remote …");
      }

      try (InputStream in = new URL(REMOTE_URL).openStream()) {
        Files.copy(in, LOCAL_FILE, StandardCopyOption.REPLACE_EXISTING);
        String newHash = calculateSha256(LOCAL_FILE);
        Files.writeString(
            LOCAL_FILE.resolveSibling("mods-database.json.sha256"),
            newHash,
            StandardCharsets.UTF_8);
        Constants.LOG.info("✅ Updated mods-database.json and saved SHA-256 hash.");
      }

    } catch (IOException e) {
      Constants.LOG.warn("⚠ Failed to update mods-database.json: {}", e.getMessage());
    }
  }

  public static JsonObject getModsDatabase() {
    // Read local mods-database.json
    if (Files.exists(LOCAL_FILE)) {
      try (InputStream in = Files.newInputStream(LOCAL_FILE)) {
        JsonObject json = parseAndValidate(in);
        if (json != null) return json;
      } catch (IOException e) {
        Constants.LOG.warn("⚠ Failed to read local mods-database.json: {}", e.getMessage());
      }
    }

    // Read fallback mods-database.json
    try (InputStream in =
        ModsDatabaseUpdater.class.getClassLoader().getResourceAsStream("mods-database.json")) {
      if (in != null) {
        JsonObject json = parseAndValidate(in);
        if (json != null) return json;
      }
    } catch (IOException e) {
      Constants.LOG.warn("⚠ Failed to read fallback mods-database.json: {}", e.getMessage());
    }

    Constants.LOG.error("❌ Could not load any valid mods-database.json!");
    return new JsonObject();
  }

  public static Map<String, String> getSortedModDatabaseMap(JsonObject json) {
    Map<String, String> modIdMap = new TreeMap<>();
    if (json.has("client"))
      json.getAsJsonArray("client").forEach(e -> modIdMap.put(e.getAsString(), "client"));
    if (json.has("server"))
      json.getAsJsonArray("server").forEach(e -> modIdMap.put(e.getAsString(), "server"));
    if (json.has("both"))
      json.getAsJsonArray("both").forEach(e -> modIdMap.put(e.getAsString(), "default"));
    return modIdMap;
  }

  private static JsonObject parseAndValidate(InputStream in) {
    try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
      JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
      for (String key : VALID_KEYS) {
        if (!json.has(key) || !json.get(key).isJsonArray()) {
          Constants.LOG.warn("⚠ Key '{}' missing or invalid in mods-database.json", key);
          return null;
        }
      }
      return json;
    } catch (Exception e) {
      Constants.LOG.warn("⚠ Failed to parse mods-database.json: {}", e.getMessage());
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
        Constants.LOG.info("🛑 mods-database.json was modified (SHA-256 hash mismatch)");
      }
      return modified;
    } catch (IOException e) {
      Constants.LOG.warn("⚠ Failed to check hash for {}: {}", path.getFileName(), e.getMessage());
      return false;
    }
  }

  private static String calculateSha256(Path file) throws IOException {
    try (InputStream fis = Files.newInputStream(file)) {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] buffer = new byte[8192];
      int bytesRead;
      while ((bytesRead = fis.read(buffer)) != -1) {
        digest.update(buffer, 0, bytesRead);
      }
      byte[] hashBytes = digest.digest();
      StringBuilder sb = new StringBuilder();
      for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new IOException("Unable to calculate SHA-256 hash", e);
    }
  }
}
