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
import org.junit.jupiter.api.Test;

class ModDataTests {

  private final File testModFiles = new File("src/test/resources/testfile/mods");
  private final File testModBothSampleFiles =
      new File("src/test/resources/testfile/mods_sample/both");
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
  void testModPathExists() {
    System.out.printf("Test Mod Files: %s\n", testModFiles);
    assertTrue(testModFiles.exists());
  }

  @Test
  void testReadModInfo_NeoForge() {
    ModFileData neoModFileData01 = ModData.readModInfo(testModFiles, "neoforge_test_mod_01.jar");
    assertEquals(ModType.NEOFORGE, neoModFileData01.modType());
    assertEquals(ModEnvironment.DEFAULT, neoModFileData01.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, neoModFileData01.id());
  }

  @Test
  void testReadModInfo_Forge() {
    ModFileData modFileData01 = ModData.readModInfo(testModFiles, "forge_test_mod_01.jar");
    assertEquals(ModType.FORGE, modFileData01.modType());
    assertEquals(ModEnvironment.DEFAULT, modFileData01.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, modFileData01.id());

    ModFileData modFileData02 = ModData.readModInfo(testModFiles, "forge_test_mod_02.jar");
    assertEquals(ModType.FORGE, modFileData02.modType());
    assertEquals(ModEnvironment.DEFAULT, modFileData02.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, modFileData02.id());

    ModFileData modFileData03 = ModData.readModInfo(testModFiles, "forge_test_mod_03.jar");
    assertEquals(ModType.FORGE, modFileData03.modType());
    assertEquals(ModEnvironment.DEFAULT, modFileData03.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, modFileData03.id());

    ModFileData modFileData04 = ModData.readModInfo(testModFiles, "forge_test_mod_04.jar");
    assertEquals(ModType.FORGE, modFileData04.modType());
    assertEquals(ModEnvironment.DEFAULT, modFileData04.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, modFileData04.id());
  }

  @Test
  void testReadModInfo_Fabric() {
    ModFileData fabricModFileData01 = ModData.readModInfo(testModFiles, "fabric_test_mod_01.jar");
    assertEquals(ModType.FABRIC, fabricModFileData01.modType());
    assertEquals(ModEnvironment.DEFAULT, fabricModFileData01.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, fabricModFileData01.id());
  }

  @Test
  void testReadModInfo_Quilt() {
    ModFileData quiltModFileData01 = ModData.readModInfo(testModFiles, "quilt_test_mod_01.jar");
    assertEquals(ModType.QUILT, quiltModFileData01.modType());
    assertEquals(ModEnvironment.CLIENT, quiltModFileData01.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, quiltModFileData01.id());
  }

  @Test
  void testReadModInfo_NeoForge_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readModInfo(testModClientSampleFiles, "appleskin-neoforge-mc1.20.2-2.5.1.jar");
    assertEquals(ModType.NEOFORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData01.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData01.id());
  }

  @Test
  void testReadModInfo_Forge_Default_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readModInfo(testModBothSampleFiles, "easy_mob_farm_1.20.1-6.5.0.jar");
    assertEquals(ModType.FORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.DEFAULT, sampleModFileData01.environment());
    assertEquals("easy_mob_farm", sampleModFileData01.id());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData01.id());

    ModFileData sampleModFileData02 =
        ModData.readModInfo(testModBothSampleFiles, "awesomedungeon-2.0.11.jar");
    assertEquals(ModType.FORGE, sampleModFileData02.modType());
    assertEquals(ModEnvironment.DEFAULT, sampleModFileData02.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData02.id());

    ModFileData sampleModFileData03 =
        ModData.readModInfo(testModBothSampleFiles, "AttributeFix-Forge-1.18.2-14.0.2.jar");
    assertEquals(ModType.FORGE, sampleModFileData03.modType());
    assertEquals(ModEnvironment.DEFAULT, sampleModFileData03.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData03.id());
  }

  @Test
  void testReadModInfo_Forge_Unknown_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readModInfo(testModBothSampleFiles, "Botania-1.18.2-435.jar");
    assertEquals(ModType.FORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.UNKNOWN, sampleModFileData01.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData01.id());

    ModFileData sampleModFileData02 =
        ModData.readModInfo(testModBothSampleFiles, "SoL-Carrot-1.18.1-1.12.0.jar");
    assertEquals(ModType.FORGE, sampleModFileData02.modType());
    assertEquals(ModEnvironment.UNKNOWN, sampleModFileData02.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData02.id());
  }

  @Test
  void testReadModInfo_Forge_Server_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readModInfo(testModServerSampleFiles, "letmedespawn-1.1.1.jar");
    assertEquals(ModType.FORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.SERVER, sampleModFileData01.environment());
    assertEquals("letmedespawn", sampleModFileData01.id());
  }

  @Test
  void testReadModInfo_Forge_Client_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readModInfo(testModClientSampleFiles, "ImmediatelyFast-Forge-1.2.8+1.20.4.jar");
    assertEquals(ModType.FORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData01.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData01.id());

    ModFileData sampleModFileData02 =
        ModData.readModInfo(testModClientSampleFiles, "physics-mod-3.0.11-mc-1.20.4-forge.jar");
    assertEquals(ModType.FORGE, sampleModFileData02.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData02.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData02.id());

    ModFileData sampleModFileData03 =
        ModData.readModInfo(testModClientSampleFiles, "3dskinlayers-forge-1.5.3-mc1.19.3.jar");
    assertEquals(ModType.FORGE, sampleModFileData03.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData03.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData03.id());

    ModFileData sampleModFileData04 =
        ModData.readModInfo(testModClientSampleFiles, "BetterAdvancements-1.18.2-0.2.0.146.jar");
    assertEquals(ModType.FORGE, sampleModFileData04.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData04.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData04.id());

    ModFileData sampleModFileData05 =
        ModData.readModInfo(testModClientSampleFiles, "rubidium-0.6.4.jar");
    assertEquals(ModType.FORGE, sampleModFileData05.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData05.environment());
    assertNotEquals(ModFileData.EMPTY_MOD_ID, sampleModFileData05.id());
  }

  @Test
  void testReadModInfo_Quilt_Client_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readModInfo(testModClientSampleFiles, "effective-2.1.1+1.19.2.jar");
    assertEquals(ModType.QUILT, sampleModFileData01.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData01.environment());
    assertEquals("effective", sampleModFileData01.id());
  }

  @Test
  void testReadModInfo_Forge_Language_Provider_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readModInfo(testModLibrarySampleFiles, "gml-4.0.9-all.jar");
    assertEquals(ModType.FORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.LANGUAGE_PROVIDER, sampleModFileData01.environment());
    assertEquals("org.groovymc.gml", sampleModFileData01.id());
  }

  @Test
  void testReadModInfo_Forge_Library_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readModInfo(testModLibrarySampleFiles, "kotlinforforge-4.9.0-all.jar");
    assertEquals(ModType.FORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.LIBRARY, sampleModFileData01.environment());
    assertEquals("thedarkcolour.kotlinforforge", sampleModFileData01.id());
  }

  @Test
  void testReadModInfo_Mixed_Service_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readModInfo(testModServiceSampleFiles, "Connector-1.0.0-beta.36+1.20.1.jar");
    assertEquals(ModType.MIXED, sampleModFileData01.modType());
    assertEquals(ModEnvironment.SERVICE, sampleModFileData01.environment());
    assertEquals("dev.su5ed.sinytra.connector", sampleModFileData01.id());
  }

  @Test
  void testReadModInfo_Fabric_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readModInfo(testModClientSampleFiles, "physics-mod-3.0.11-mc-1.20.4-fabric.jar");
    assertEquals(ModType.FABRIC, sampleModFileData01.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData01.environment());
    assertEquals("physicsmod", sampleModFileData01.id());

    ModFileData sampleModFileData02 =
        ModData.readModInfo(testModClientSampleFiles, "3dskinlayers-fabric-1.5.6-mc1.20.2.jar");
    assertEquals(ModType.FABRIC, sampleModFileData02.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData02.environment());
    assertEquals("skinlayers", sampleModFileData02.id());
  }

  @Test
  void testReadModInfo_Datapack_Samples() {
    ModFileData sampleModFileData01 =
        ModData.readModInfo(testModDatapackSampleFiles, "explorify-v1.3.0-mc1.20.jar");
    assertEquals(ModType.MIXED, sampleModFileData01.modType());
    assertEquals(ModEnvironment.DATA_PACK, sampleModFileData01.environment());
    assertEquals("explorify", sampleModFileData01.id());

    ModFileData sampleModFileData02 =
        ModData.readModInfo(testModDatapackSampleFiles, "SmidgeonOBliss-1.19.2-1.3.2.jar");
    assertEquals(ModType.FORGE, sampleModFileData02.modType());
    assertEquals(ModEnvironment.DATA_PACK, sampleModFileData02.environment());
    assertEquals("sob", sampleModFileData02.id());
  }
}
