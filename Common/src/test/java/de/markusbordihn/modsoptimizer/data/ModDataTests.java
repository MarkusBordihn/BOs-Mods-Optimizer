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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.modsoptimizer.data.ModFileData.ModEnvironment;
import de.markusbordihn.modsoptimizer.data.ModFileData.ModType;
import java.io.File;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModDataTests {

  private final File testModFiles = new File("src/test/resources/testfile/mods");
  private final File testModBothSampleFiles =
      new File("src/test/resources/testfile/mods_sample/both");
  private final File testModBrokenSampleFiles =
      new File("src/test/resources/testfile/mods_sample/broken");
  private final File testModClientSampleFiles =
      new File("src/test/resources/testfile/mods_sample/client");
  private final File testModDatapackSampleFiles =
      new File("src/test/resources/testfile/mods_sample/datapack");
  private final File testModLibrarySampleFiles =
      new File("src/test/resources/testfile/mods_sample/library");
  private final File testModServerSampleFiles =
      new File("src/test/resources/testfile/mods_sample/server");
  private final File testModServiceSampleFiles =
      new File("src/test/resources/testfile/mods_sample/service");

  @Test
  @DisplayName("Test mod path exists and is accessible")
  void testModPathExists() {
    System.out.printf("Test Mod Files: %s%n", testModFiles);
    assertTrue(testModFiles.exists());
  }

  @Test
  @DisplayName("Read raw NeoForge mod info correctly")
  void testReadRawModInfo_NeoForge() {
    ModFileData neoModFileData01 = ModData.readRawModInfo(testModFiles, "neoforge_test_mod_01.jar");
    assertEquals(ModType.NEOFORGE, neoModFileData01.modType());
    assertEquals(ModEnvironment.BOTH, neoModFileData01.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, neoModFileData01.id());
  }

  @Test
  @DisplayName("Read raw Forge mod info from multiple test files")
  void testReadRawModInfo_Forge() {
    ModFileData modFileData01 = ModData.readRawModInfo(testModFiles, "forge_test_mod_01.jar");
    assertEquals(ModType.FORGE, modFileData01.modType());
    assertEquals(ModEnvironment.BOTH, modFileData01.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, modFileData01.id());

    ModFileData modFileData02 = ModData.readRawModInfo(testModFiles, "forge_test_mod_02.jar");
    assertEquals(ModType.FORGE, modFileData02.modType());
    assertEquals(ModEnvironment.BOTH, modFileData02.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, modFileData02.id());

    ModFileData modFileData03 = ModData.readRawModInfo(testModFiles, "forge_test_mod_03.jar");
    assertEquals(ModType.FORGE, modFileData03.modType());
    assertEquals(ModEnvironment.BOTH, modFileData03.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, modFileData03.id());

    ModFileData modFileData04 = ModData.readRawModInfo(testModFiles, "forge_test_mod_04.jar");
    assertEquals(ModType.FORGE, modFileData04.modType());
    assertEquals(ModEnvironment.BOTH, modFileData04.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, modFileData04.id());
  }

  @Test
  @DisplayName("Read raw Fabric mod info correctly")
  void testReadRawModInfo_Fabric() {
    ModFileData fabricModFileData01 =
        ModData.readRawModInfo(testModFiles, "fabric_test_mod_01.jar");
    assertEquals(ModType.FABRIC, fabricModFileData01.modType());
    assertEquals(ModEnvironment.BOTH, fabricModFileData01.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, fabricModFileData01.id());
  }

  @Test
  @DisplayName("Read raw Quilt mod info correctly")
  void testReadRawModInfo_Quilt() {
    ModFileData quiltModFileData01 = ModData.readRawModInfo(testModFiles, "quilt_test_mod_01.jar");
    assertEquals(ModType.QUILT, quiltModFileData01.modType());
    assertEquals(ModEnvironment.CLIENT, quiltModFileData01.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, quiltModFileData01.id());
  }

  @Test
  @DisplayName("Read raw broken mod samples with mixed type")
  void testReadRawModInfo_Broken_Samples() {
    ModFileData sampleModfileData01 =
        ModData.readRawModInfo(
            testModBrokenSampleFiles, "HopoBetterRuinedPortals-[1.19-1.19.3]-1.3.3.jar");
    assertEquals(ModType.MIXED, sampleModfileData01.modType());
    assertEquals(ModEnvironment.DATA_PACK, sampleModfileData01.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModfileData01.id());
  }

  @Test
  @DisplayName("Read raw NeoForge mod samples correctly")
  void testReadRawModInfo_NeoForge_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readRawModInfo(testModClientSampleFiles, "appleskin-neoforge-mc1.20.2-2.5.1.jar");
    assertEquals(ModType.NEOFORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData01.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData01.id());

    ModFileData sampleModFileData02 =
        ModData.readRawModInfo(
            testModBothSampleFiles, "JustEnoughProfessions-neoforge-1.21.1-4.0.4.jar");
    assertEquals(ModType.NEOFORGE, sampleModFileData02.modType());
    assertEquals(ModEnvironment.BOTH, sampleModFileData02.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData02.id());

    ModFileData sampleModFileData03 =
        ModData.readRawModInfo(
            testModBothSampleFiles, "supplementaries-1.21-3.0.30-beta-neoforge.jar");
    assertEquals(ModType.NEOFORGE, sampleModFileData03.modType());
    assertEquals(ModEnvironment.BOTH, sampleModFileData03.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData03.id());
  }

  @Test
  @DisplayName("Read raw Forge default mod samples correctly")
  void testReadRawModInfo_Forge_Default_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readRawModInfo(testModBothSampleFiles, "easy_mob_farm_1.20.1-6.5.0.jar");
    assertEquals(ModType.FORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.BOTH, sampleModFileData01.environment());
    assertEquals("easy_mob_farm", sampleModFileData01.id());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData01.id());

    ModFileData sampleModFileData02 =
        ModData.readRawModInfo(testModBothSampleFiles, "awesomedungeon-2.0.11.jar");
    assertEquals(ModType.FORGE, sampleModFileData02.modType());
    assertEquals(ModEnvironment.BOTH, sampleModFileData02.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData02.id());

    ModFileData sampleModFileData03 =
        ModData.readRawModInfo(testModBothSampleFiles, "AttributeFix-Forge-1.18.2-14.0.2.jar");
    assertEquals(ModType.FORGE, sampleModFileData03.modType());
    assertEquals(ModEnvironment.UNKNOWN, sampleModFileData03.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData03.id());

    ModFileData sampleModFileData04 =
        ModData.readRawModInfo(testModBothSampleFiles, "JustEnoughProfessions-1.18.2-1.3.0.jar");
    assertEquals(ModType.FORGE, sampleModFileData04.modType());
    assertEquals(ModEnvironment.BOTH, sampleModFileData04.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData04.id());
  }

  @Test
  @DisplayName("Read raw Forge mods with unknown environment correctly")
  void testReadRawModInfo_Forge_Unknown_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readRawModInfo(testModBothSampleFiles, "Botania-1.18.2-435.jar");
    assertEquals(ModType.FORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.UNKNOWN, sampleModFileData01.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData01.id());

    ModFileData sampleModFileData02 =
        ModData.readRawModInfo(testModBothSampleFiles, "SoL-Carrot-1.18.1-1.12.0.jar");
    assertEquals(ModType.FORGE, sampleModFileData02.modType());
    assertEquals(ModEnvironment.BOTH, sampleModFileData02.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData02.id());
  }

  @Test
  @DisplayName("Read raw Forge server-side mod samples correctly")
  void testReadRawModInfo_Forge_Server_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readRawModInfo(testModServerSampleFiles, "letmedespawn-1.1.1.jar");
    assertEquals(ModType.FORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.SERVER, sampleModFileData01.environment());
    assertEquals("letmedespawn", sampleModFileData01.id());
  }

  @Test
  @DisplayName("Read raw Forge client-side mod samples correctly")
  void testReadRawModInfo_Forge_Client_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readRawModInfo(testModClientSampleFiles, "ImmediatelyFast-Forge-1.2.8+1.20.4.jar");
    assertEquals(ModType.FORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData01.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData01.id());

    // Includes wrongly side="both" in mods.toml
    ModFileData sampleModFileData02 =
        ModData.readRawModInfo(testModClientSampleFiles, "physics-mod-3.0.11-mc-1.20.4-forge.jar");
    assertEquals(ModType.FORGE, sampleModFileData02.modType());
    assertEquals(ModEnvironment.BOTH, sampleModFileData02.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData02.id());

    ModFileData sampleModFileData03 =
        ModData.readRawModInfo(testModClientSampleFiles, "3dskinlayers-forge-1.5.3-mc1.19.3.jar");
    assertEquals(ModType.FORGE, sampleModFileData03.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData03.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData03.id());

    ModFileData sampleModFileData04 =
        ModData.readRawModInfo(testModClientSampleFiles, "BetterAdvancements-1.18.2-0.2.0.146.jar");
    assertEquals(ModType.FORGE, sampleModFileData04.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData04.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData04.id());

    ModFileData sampleModFileData05 =
        ModData.readRawModInfo(testModClientSampleFiles, "rubidium-0.6.4.jar");
    assertEquals(ModType.FORGE, sampleModFileData05.modType());
    assertEquals(ModEnvironment.UNKNOWN, sampleModFileData05.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData05.id());
  }

  @Test
  @DisplayName("Read raw Quilt client-side mod samples correctly")
  void testReadRawModInfo_Quilt_Client_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readRawModInfo(testModClientSampleFiles, "effective-2.1.1+1.19.2.jar");
    assertEquals(ModType.QUILT, sampleModFileData01.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData01.environment());
    assertEquals("effective", sampleModFileData01.id());
  }

  @Test
  @DisplayName("Read raw Forge language provider mod samples correctly")
  void testReadRawModInfo_Forge_Language_Provider_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readRawModInfo(testModLibrarySampleFiles, "gml-4.0.9-all.jar");
    assertEquals(ModType.FORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.LANGUAGE_PROVIDER, sampleModFileData01.environment());
    assertEquals("org.groovymc.gml", sampleModFileData01.id());
  }

  @Test
  @DisplayName("Read raw Forge library mod samples correctly")
  void testReadRawModInfo_Forge_Library_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readRawModInfo(testModLibrarySampleFiles, "kotlinforforge-4.9.0-all.jar");
    assertEquals(ModType.FORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.LIBRARY, sampleModFileData01.environment());
    assertEquals("thedarkcolour.kotlinforforge", sampleModFileData01.id());
  }

  @Test
  @DisplayName("Read raw mixed service mod samples correctly")
  void testReadRawModInfo_Mixed_Service_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readRawModInfo(testModServiceSampleFiles, "Connector-1.0.0-beta.36+1.20.1.jar");
    assertEquals(ModType.MIXED, sampleModFileData01.modType());
    assertEquals(ModEnvironment.SERVICE, sampleModFileData01.environment());
    assertEquals("dev.su5ed.sinytra.connector", sampleModFileData01.id());
  }

  @Test
  @DisplayName("Read raw Fabric mod samples correctly")
  void testReadRawModInfo_Fabric_Samples() {
    // Includes wrongly side="both" in mods.toml
    ModFileData sampleModFileData01 =
        ModData.readRawModInfo(testModClientSampleFiles, "physics-mod-3.0.11-mc-1.20.4-fabric.jar");
    assertEquals(ModType.FABRIC, sampleModFileData01.modType());
    assertEquals(ModEnvironment.BOTH, sampleModFileData01.environment());
    assertEquals("physicsmod", sampleModFileData01.id());

    ModFileData sampleModFileData02 =
        ModData.readRawModInfo(testModClientSampleFiles, "3dskinlayers-fabric-1.5.6-mc1.20.2.jar");
    assertEquals(ModType.FABRIC, sampleModFileData02.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData02.environment());
    assertEquals("skinlayers", sampleModFileData02.id());
  }

  @Test
  @DisplayName("Read raw datapack mod samples correctly")
  void testReadRawModInfo_Datapack_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readRawModInfo(testModDatapackSampleFiles, "explorify-v1.3.0-mc1.20.jar");
    assertEquals(ModType.MIXED, sampleModFileData01.modType());
    assertEquals(ModEnvironment.DATA_PACK, sampleModFileData01.environment());
    assertEquals("explorify", sampleModFileData01.id());

    ModFileData sampleModFileData02 =
        ModData.readRawModInfo(testModDatapackSampleFiles, "SmidgeonOBliss-1.19.2-1.3.2.jar");
    assertEquals(ModType.FORGE, sampleModFileData02.modType());
    assertEquals(ModEnvironment.DATA_PACK, sampleModFileData02.environment());
    assertEquals("sob", sampleModFileData02.id());

    ModFileData sampleModFileData03 =
        ModData.readRawModInfo(testModDatapackSampleFiles, "Explorify v1.6.2 f10-48.jar");
    assertEquals(ModType.MIXED, sampleModFileData03.modType());
    assertEquals(ModEnvironment.DATA_PACK, sampleModFileData03.environment());
    assertEquals("explorify", sampleModFileData03.id());
  }
}
