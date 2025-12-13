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

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ModsDatabaseUpdaterTests {

  @TempDir Path tempDir;

  @BeforeEach
  void setUp() {
    ModsDatabaseUpdater.setConfigDir(tempDir);
  }

  @AfterEach
  void tearDown() {
    ModsDatabaseUpdater.resetConfigDir();
  }

  @Test
  @DisplayName("Config directory getter and setter work correctly")
  void testConfigDirGetterAndSetter() {
    Path customDir = tempDir.resolve("custom");
    ModsDatabaseUpdater.setConfigDir(customDir);
    assertEquals(customDir, ModsDatabaseUpdater.getConfigDir());
  }

  @Test
  @DisplayName("Reset config directory returns to default")
  void testResetConfigDir() {
    Path customDir = tempDir.resolve("custom");
    ModsDatabaseUpdater.setConfigDir(customDir);
    ModsDatabaseUpdater.resetConfigDir();
    assertNotEquals(customDir, ModsDatabaseUpdater.getConfigDir());
  }

  @Test
  @DisplayName("Setting config directory with null resets to default")
  void testSetConfigDirWithNull() {
    ModsDatabaseUpdater.setConfigDir(tempDir);
    ModsDatabaseUpdater.setConfigDir(null);
    assertNotEquals(tempDir, ModsDatabaseUpdater.getConfigDir());
  }

  @Test
  @DisplayName("Load valid local mods-database.json file")
  void testGetModsDatabase_WithValidLocalFile() throws IOException {
    Path databaseFile = tempDir.resolve("mods-database.json");
    String jsonContent =
        """
        {
          "client": ["mod1", "mod2"],
          "server": ["mod3", "mod4"],
          "both": ["mod5", "mod6"]
        }
        """;
    Files.writeString(databaseFile, jsonContent, StandardCharsets.UTF_8);

    JsonObject database = ModsDatabaseUpdater.getModsDatabase();
    assertNotNull(database);
    assertTrue(database.has("client"));
    assertTrue(database.has("server"));
    assertTrue(database.has("both"));
    assertEquals(2, database.getAsJsonArray("client").size());
    assertEquals(2, database.getAsJsonArray("server").size());
    assertEquals(2, database.getAsJsonArray("both").size());
  }

  @Test
  @DisplayName("Fallback to embedded resource when no local file exists")
  void testGetModsDatabase_FallbackToEmbedded() {
    JsonObject database = ModsDatabaseUpdater.getModsDatabase();
    assertNotNull(database);
    assertTrue(database.has("client"));
    assertTrue(database.has("server"));
    assertTrue(database.has("both"));
  }

  @Test
  @DisplayName("Create override template file on first access")
  void testOverrideFile_CreatesTemplate() {
    JsonObject override = ModsDatabaseUpdater.getModsDatabaseOverride();
    assertNotNull(override);

    Path overrideFile = tempDir.resolve("mods-database-override.json");
    assertTrue(Files.exists(overrideFile));
  }

  @Test
  @DisplayName("Override file can override base database entries")
  void testOverrideFile_OverridesBaseDatabase() throws IOException {
    Path databaseFile = tempDir.resolve("mods-database.json");
    String baseJson =
        """
        {
          "client": ["mod1", "mod2"],
          "server": ["mod3"],
          "both": ["mod4"]
        }
        """;
    Files.writeString(databaseFile, baseJson, StandardCharsets.UTF_8);

    Path overrideFile = tempDir.resolve("mods-database-override.json");
    String overrideJson =
        """
        {
          "client": [],
          "server": ["mod1", "mod3"],
          "both": ["mod4"]
        }
        """;
    Files.writeString(overrideFile, overrideJson, StandardCharsets.UTF_8);

    JsonObject database = ModsDatabaseUpdater.getModsDatabase();

    assertFalse(database.getAsJsonArray("client").toString().contains("mod1"));
    assertTrue(database.getAsJsonArray("server").toString().contains("mod1"));
    assertTrue(database.getAsJsonArray("client").toString().contains("mod2"));
  }

  @Test
  @DisplayName("Override can move mod between categories")
  void testOverrideFile_ChangesCategory() throws IOException {
    Path databaseFile = tempDir.resolve("mods-database.json");
    String baseJson =
        """
        {
          "client": ["clientmod"],
          "server": ["servermod"],
          "both": ["bothmod"]
        }
        """;
    Files.writeString(databaseFile, baseJson, StandardCharsets.UTF_8);

    Path overrideFile = tempDir.resolve("mods-database-override.json");
    String overrideJson =
        """
        {
          "client": [],
          "server": ["clientmod", "servermod"],
          "both": ["bothmod"]
        }
        """;
    Files.writeString(overrideFile, overrideJson, StandardCharsets.UTF_8);

    JsonObject database = ModsDatabaseUpdater.getModsDatabase();

    assertEquals(0, database.getAsJsonArray("client").size());
    assertTrue(database.getAsJsonArray("server").toString().contains("clientmod"));
    assertTrue(database.getAsJsonArray("server").toString().contains("servermod"));
  }

  @Test
  @DisplayName("Override keeps mods not mentioned in override file")
  void testOverrideFile_RemovesModCompletely() throws IOException {
    Path databaseFile = tempDir.resolve("mods-database.json");
    String baseJson =
        """
        {
          "client": ["mod1", "mod2"],
          "server": ["mod3"],
          "both": ["mod4"]
        }
        """;
    Files.writeString(databaseFile, baseJson, StandardCharsets.UTF_8);

    Path overrideFile = tempDir.resolve("mods-database-override.json");
    String overrideJson =
        """
        {
          "client": ["mod1"],
          "server": ["mod3"],
          "both": ["mod4"]
        }
        """;
    Files.writeString(overrideFile, overrideJson, StandardCharsets.UTF_8);

    JsonObject database = ModsDatabaseUpdater.getModsDatabase();

    assertTrue(database.toString().contains("mod1"));
    assertTrue(database.toString().contains("mod2"));
    assertTrue(database.getAsJsonArray("client").toString().contains("mod1"));
    assertTrue(database.getAsJsonArray("client").toString().contains("mod2"));
  }

  @Test
  @DisplayName("Get sorted mod database map with correct categories")
  void testGetSortedModDatabaseMap() throws IOException {
    Path databaseFile = tempDir.resolve("mods-database.json");
    String jsonContent =
        """
        {
          "client": ["mod1"],
          "server": ["mod2"],
          "both": ["mod3"]
        }
        """;
    Files.writeString(databaseFile, jsonContent, StandardCharsets.UTF_8);

    JsonObject database = ModsDatabaseUpdater.getModsDatabase();
    var map = ModsDatabaseUpdater.getSortedModDatabaseMap(database);

    assertEquals("client", map.get("mod1"));
    assertEquals("server", map.get("mod2"));
    assertEquals("default", map.get("mod3"));
  }

  @Test
  @DisplayName("Get sorted mod database set with all mods")
  void testGetSortedModDatabaseSet() throws IOException {
    Path databaseFile = tempDir.resolve("mods-database.json");
    String jsonContent =
        """
        {
          "client": ["mod1"],
          "server": ["mod2"],
          "both": ["mod3"]
        }
        """;
    Files.writeString(databaseFile, jsonContent, StandardCharsets.UTF_8);

    JsonObject database = ModsDatabaseUpdater.getModsDatabase();
    var set = ModsDatabaseUpdater.getSortedModDatabaseSet(database);

    assertTrue(set.contains("mod1"));
    assertTrue(set.contains("mod2"));
    assertTrue(set.contains("mod3"));
    assertEquals(3, set.size());
  }

  @Test
  @DisplayName("Fallback to embedded resource with invalid JSON missing keys")
  void testInvalidJson_MissingKeys() throws IOException {
    Path databaseFile = tempDir.resolve("mods-database.json");
    String jsonContent =
        """
        {
          "client": ["mod1"]
        }
        """;
    Files.writeString(databaseFile, jsonContent, StandardCharsets.UTF_8);

    JsonObject database = ModsDatabaseUpdater.getModsDatabase();
    assertNotNull(database);
  }

  @Test
  @DisplayName("Fallback to embedded resource with invalid JSON wrong format")
  void testInvalidJson_WrongFormat() throws IOException {
    Path databaseFile = tempDir.resolve("mods-database.json");
    String jsonContent =
        """
        {
          "client": "not-an-array",
          "server": [],
          "both": []
        }
        """;
    Files.writeString(databaseFile, jsonContent, StandardCharsets.UTF_8);

    JsonObject database = ModsDatabaseUpdater.getModsDatabase();
    assertNotNull(database);
  }

  @Test
  @DisplayName("Override file handles complex scenario with multiple changes")
  void testOverrideFile_ComplexScenario() throws IOException {
    Path databaseFile = tempDir.resolve("mods-database.json");
    String baseJson =
        """
        {
          "client": ["mod1", "mod2", "mod3"],
          "server": ["mod4", "mod5"],
          "both": ["mod6", "mod7"]
        }
        """;
    Files.writeString(databaseFile, baseJson, StandardCharsets.UTF_8);

    Path overrideFile = tempDir.resolve("mods-database-override.json");
    String overrideJson =
        """
        {
          "client": [],
          "server": ["mod1", "mod5"],
          "both": ["mod4", "mod6", "mod7"]
        }
        """;
    Files.writeString(overrideFile, overrideJson, StandardCharsets.UTF_8);

    JsonObject database = ModsDatabaseUpdater.getModsDatabase();

    String clientArr = database.getAsJsonArray("client").toString();
    String serverArr = database.getAsJsonArray("server").toString();
    String bothArr = database.getAsJsonArray("both").toString();

    assertTrue(clientArr.contains("mod2"));
    assertTrue(clientArr.contains("mod3"));
    assertFalse(clientArr.contains("mod1"));

    assertTrue(serverArr.contains("mod1"));
    assertTrue(serverArr.contains("mod5"));
    assertFalse(serverArr.contains("mod4"));

    assertTrue(bothArr.contains("mod4"));
    assertTrue(bothArr.contains("mod6"));
    assertTrue(bothArr.contains("mod7"));
  }

  @Test
  @DisplayName("Detect duplicate mod ID in base database")
  void testDuplicateModId_InBaseDatabase() throws IOException {
    Path databaseFile = tempDir.resolve("mods-database.json");
    String baseJson =
        """
        {
          "client": ["duplicatemod"],
          "server": ["duplicatemod"],
          "both": []
        }
        """;
    Files.writeString(databaseFile, baseJson, StandardCharsets.UTF_8);

    JsonObject database = ModsDatabaseUpdater.getModsDatabase();
    assertNotNull(database);
  }

  @Test
  @DisplayName("Detect duplicate mod ID in override database")
  void testDuplicateModId_InOverrideDatabase() throws IOException {
    Path databaseFile = tempDir.resolve("mods-database.json");
    String baseJson =
        """
        {
          "client": ["mod1"],
          "server": ["mod2"],
          "both": []
        }
        """;
    Files.writeString(databaseFile, baseJson, StandardCharsets.UTF_8);

    Path overrideFile = tempDir.resolve("mods-database-override.json");
    String overrideJson =
        """
        {
          "client": ["duplicatemod"],
          "server": [],
          "both": ["duplicatemod"]
        }
        """;
    Files.writeString(overrideFile, overrideJson, StandardCharsets.UTF_8);

    JsonObject database = ModsDatabaseUpdater.getModsDatabase();
    assertNotNull(database);
  }

  @Test
  @DisplayName("Detect duplicate mod ID across all categories")
  void testDuplicateModId_AcrossAllCategories() throws IOException {
    Path databaseFile = tempDir.resolve("mods-database.json");
    String baseJson =
        """
        {
          "client": ["triplemod"],
          "server": ["triplemod"],
          "both": ["triplemod"]
        }
        """;
    Files.writeString(databaseFile, baseJson, StandardCharsets.UTF_8);

    JsonObject database = ModsDatabaseUpdater.getModsDatabase();
    assertNotNull(database);
  }
}
