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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.modsoptimizer.data.ModFileData.ModEnvironment;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ModsDatabaseConfigTests {

  @Test
  void testCleanTomlFileWithDuplicateKeys() throws IOException {
    File tempFile = File.createTempFile("test-config", ".toml");
    tempFile.deleteOnExit();

    String tomlContent =
        """
    [Mods]
    test-mod = "client"
    test-mod = "server"
    another-mod = "both"
    """;

    Files.write(tempFile.toPath(), tomlContent.getBytes(StandardCharsets.UTF_8));

    String cleanedToml = ModsDatabaseConfig.cleanTomlFileWithWarnings(tempFile);
    assertNotNull(cleanedToml);
    assertTrue(cleanedToml.contains("test-mod = \"client\""));
    assertFalse(cleanedToml.contains("test-mod = \"server\""));
    assertTrue(cleanedToml.contains("another-mod = \"both\""));
  }

  @Test
  void testGetConfigFile() {
    File modsDatabaseConfigFile = ModsDatabaseConfig.getConfigFile();
    assertTrue(modsDatabaseConfigFile.exists());
  }

  @Test
  void testGetConfig() {
    Map<String, String> modsDatabaseConfig = ModsDatabaseConfig.getConfig();
    assertFalse(modsDatabaseConfig.isEmpty());
  }

  @Test
  void testContainsMod() {
    assertTrue(ModsDatabaseConfig.containsMod("server_side_mod_id"));
    assertTrue(ModsDatabaseConfig.containsMod("client_side_mod_id"));
    assertTrue(ModsDatabaseConfig.containsMod("default_side_mod_id"));
  }

  @Test
  void testGetModEnvironment() {
    assertEquals(ModEnvironment.SERVER, ModsDatabaseConfig.getModEnvironment("server_side_mod_id"));
    assertEquals(ModEnvironment.CLIENT, ModsDatabaseConfig.getModEnvironment("client_side_mod_id"));
    assertEquals(
        ModEnvironment.DEFAULT, ModsDatabaseConfig.getModEnvironment("default_side_mod_id"));
  }
}
