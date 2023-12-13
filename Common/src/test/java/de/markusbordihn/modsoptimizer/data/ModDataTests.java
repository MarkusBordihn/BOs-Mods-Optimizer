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
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.modsoptimizer.data.ModFileData.ModEnvironment;
import de.markusbordihn.modsoptimizer.data.ModFileData.ModType;
import java.io.File;
import org.junit.jupiter.api.Test;

class ModDataTests {

  private final File testModFiles = new File("src/test/resources/testfile/mods");
  private final File testModClientSampleFiles =
      new File("src/test/resources/testfile/mods_sample/client");
  private final File testModServerSampleFiles =
      new File("src/test/resources/testfile/mods_sample/server");
  private final File testModBothSampleFiles =
      new File("src/test/resources/testfile/mods_sample/both");

  @Test
  void testModPathExists() {
    System.out.printf("Test Mod Files: %s\n", testModFiles);
    assertTrue (testModFiles.exists());
  }

  @Test
  void testReadModInfo_NeoForge() {
    File neoForgeModFile01 = new File(testModFiles, "neoforge_test_mod_01.jar");
    ModFileData neoModFileData01 = ModData.readModInfo(neoForgeModFile01);
    assertEquals(ModType.NEOFORGE, neoModFileData01.modType());
    assertEquals(ModEnvironment.DEFAULT, neoModFileData01.environment());
  }

  @Test
  void testReadModInfo_Forge() {
    File forgeModFile01 = new File(testModFiles, "forge_test_mod_01.jar");
    ModFileData modFileData01 = ModData.readModInfo(forgeModFile01);
    assertEquals(ModType.FORGE, modFileData01.modType());
    assertEquals(ModEnvironment.DEFAULT, modFileData01.environment());

    File forgeModFile02 = new File(testModFiles, "forge_test_mod_02.jar");
    ModFileData modFileData02 = ModData.readModInfo(forgeModFile02);
    assertEquals(ModType.FORGE, modFileData02.modType());
    assertEquals(ModEnvironment.DEFAULT, modFileData02.environment());

    File forgeModFile03 = new File(testModFiles, "forge_test_mod_03.jar");
    ModFileData modFileData03 = ModData.readModInfo(forgeModFile03);
    assertEquals(ModType.FORGE, modFileData03.modType());
    assertEquals(ModEnvironment.DEFAULT, modFileData03.environment());

    File forgeModFile04 = new File(testModFiles, "forge_test_mod_04.jar");
    ModFileData modFileData04 = ModData.readModInfo(forgeModFile04);
    assertEquals(ModType.FORGE, modFileData04.modType());
    assertEquals(ModEnvironment.DEFAULT, modFileData04.environment());
  }

  @Test
  void testReadModInfo_Fabric() {
    File fabricModFile01 = new File(testModFiles, "fabric_test_mod_01.jar");
    ModFileData fabricModFileData01 = ModData.readModInfo(fabricModFile01);
    assertEquals(ModType.FABRIC, fabricModFileData01.modType());
    assertEquals(ModEnvironment.DEFAULT, fabricModFileData01.environment());
  }

  @Test
  void testReadModInfo_NeoForge_Samples() {
    File sampleModFile01 =
        new File(testModClientSampleFiles, "appleskin-neoforge-mc1.20.2-2.5.1.jar");
    ModFileData sampleModFileData01 = ModData.readModInfo(sampleModFile01);
    assertEquals(ModType.NEOFORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData01.environment());
  }

  @Test
  void testReadModInfo_Forge_Samples() {
    File sampleModFile01 =
        new File(testModClientSampleFiles, "ImmediatelyFast-Forge-1.2.8+1.20.4.jar");
    ModFileData sampleModFileData01 = ModData.readModInfo(sampleModFile01);
    assertEquals(ModType.FORGE, sampleModFileData01.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData01.environment());

    File sampleModFile02 =
        new File(testModClientSampleFiles, "physics-mod-3.0.11-mc-1.20.4-forge.jar");
    ModFileData sampleModFileData02 = ModData.readModInfo(sampleModFile02);
    assertEquals(ModType.FORGE, sampleModFileData02.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData02.environment());

    File sampleModFile03 = new File(testModServerSampleFiles, "letmedespawn-1.1.1.jar");
    ModFileData sampleModFileData03 = ModData.readModInfo(sampleModFile03);
    assertEquals(ModType.FORGE, sampleModFileData03.modType());
    assertEquals(ModEnvironment.SERVER, sampleModFileData03.environment());

    File sampleModFile04 =
        new File(testModClientSampleFiles, "3dskinlayers-forge-1.5.3-mc1.19.3.jar");
    ModFileData sampleModFileData04 = ModData.readModInfo(sampleModFile04);
    assertEquals(ModType.FORGE, sampleModFileData04.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData04.environment());

    File sampleModFile05 = new File(testModBothSampleFiles, "easy_mob_farm_1.20.1-6.5.0.jar");
    ModFileData sampleModFileData05 = ModData.readModInfo(sampleModFile05);
    assertEquals(ModType.FORGE, sampleModFileData05.modType());
    assertEquals(ModEnvironment.DEFAULT, sampleModFileData05.environment());

    File sampleModFile06 = new File(testModBothSampleFiles, "awesomedungeon-2.0.11.jar");
    ModFileData sampleModFileData06 = ModData.readModInfo(sampleModFile06);
    assertEquals(ModType.FORGE, sampleModFileData06.modType());
    assertEquals(ModEnvironment.DEFAULT, sampleModFileData06.environment());

    File sampleModFile07 =
        new File(testModClientSampleFiles, "BetterAdvancements-1.18.2-0.2.0.146.jar");
    ModFileData sampleModFileData07 = ModData.readModInfo(sampleModFile07);
    assertEquals(ModType.FORGE, sampleModFileData07.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData07.environment());

    File sampleModFile08 = new File(testModBothSampleFiles, "Botania-1.18.2-435.jar");
    ModFileData sampleModFileData08 = ModData.readModInfo(sampleModFile08);
    assertEquals(ModType.FORGE, sampleModFileData08.modType());
    assertEquals(ModEnvironment.UNKNOWN, sampleModFileData08.environment());

    File sampleModFile09 = new File(testModBothSampleFiles, "SoL-Carrot-1.18.1-1.12.0.jar");
    ModFileData sampleModFileData09 = ModData.readModInfo(sampleModFile09);
    assertEquals(ModType.FORGE, sampleModFileData09.modType());
    assertEquals(ModEnvironment.UNKNOWN, sampleModFileData09.environment());

    File sampleModFile10 = new File(testModClientSampleFiles, "rubidium-0.6.4.jar");
    ModFileData sampleModFileData10 = ModData.readModInfo(sampleModFile10);
    assertEquals(ModType.FORGE, sampleModFileData10.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData10.environment());
  }

  @Test
  void testReadModInfo_Fabric_Samples() {
    File sampleModFile01 =
        new File(testModClientSampleFiles, "physics-mod-3.0.11-mc-1.20.4-fabric.jar");
    ModFileData sampleModFileData01 = ModData.readModInfo(sampleModFile01);
    assertEquals(ModType.FABRIC, sampleModFileData01.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData01.environment());

    File sampleModFile02 =
        new File(testModClientSampleFiles, "3dskinlayers-fabric-1.5.6-mc1.20.2.jar");
    ModFileData sampleModFileData02 = ModData.readModInfo(sampleModFile02);
    assertEquals(ModType.FABRIC, sampleModFileData02.modType());
    assertEquals(ModEnvironment.CLIENT, sampleModFileData02.environment());
  }
}
