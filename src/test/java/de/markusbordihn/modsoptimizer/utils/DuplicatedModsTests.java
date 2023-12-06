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

package de.markusbordihn.modsoptimizer.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.modsoptimizer.data.TestData;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class DuplicatedModsTests {

  private final String testFilePath = "src/test/resources/testfile/duplicates";
  private final File testFiles = new File(testFilePath);

  private final String twoTestFilePath = "src/test/resources/testfile/two_duplicates";
  private final File twoTestFiles = new File(twoTestFilePath);

  @Test
  void testFindLatestMod() {
    System.out.printf("Test Mod Files: %s\n", testFiles);
    List<File> testFileList =
        new ArrayList<>(Arrays.asList(Objects.requireNonNull(testFiles.listFiles())));
    File result = DuplicatedMods.findLatestMod(testFileList);
    assertEquals("adaptive_performance_tweaks_items_1.18.1-2.20.9.jar", result.getName());
  }

  @Test
  void getVersionNumber() {
    assertEquals(
        "3.5.16", DuplicatedMods.getVersionNumber(TestData.modListDuplicatesSpecific01[0]));
    assertEquals(
        "3.5.11", DuplicatedMods.getVersionNumber(TestData.modListDuplicatesSpecific01[1]));

    assertEquals(
        "20220709", DuplicatedMods.getVersionNumber(TestData.modListDuplicatesSpecific02[0]));
    assertEquals(
        "20220517", DuplicatedMods.getVersionNumber(TestData.modListDuplicatesSpecific02[1]));

    assertEquals("1.0.6", DuplicatedMods.getVersionNumber(TestData.modListDuplicatesSpecific03[0]));
    assertEquals("1.0.5", DuplicatedMods.getVersionNumber(TestData.modListDuplicatesSpecific03[1]));

    assertEquals("71.1", DuplicatedMods.getVersionNumber(TestData.modListDuplicatesSpecific04[0]));
    assertEquals("70", DuplicatedMods.getVersionNumber(TestData.modListDuplicatesSpecific04[1]));

    assertEquals(
        "3.4.1.131", DuplicatedMods.getVersionNumber(TestData.modListDuplicatesSpecific05[0]));
    assertEquals(
        "3.4.0.124", DuplicatedMods.getVersionNumber(TestData.modListDuplicatesSpecific05[1]));

    assertEquals("1.4.2", DuplicatedMods.getVersionNumber(TestData.modListDuplicatesSpecific06[0]));
    assertEquals("1.4.0", DuplicatedMods.getVersionNumber(TestData.modListDuplicatesSpecific06[1]));
  }

  @Test
  void normalizeVersionNumber() {
    assertEquals(
        "003.005.016",
        DuplicatedMods.normalizeVersionNumber(TestData.modListDuplicatesSpecific01[0]));
    assertEquals(
        "003.005.011",
        DuplicatedMods.normalizeVersionNumber(TestData.modListDuplicatesSpecific01[1]));

    assertEquals(
        "20220709.000.000",
        DuplicatedMods.normalizeVersionNumber(TestData.modListDuplicatesSpecific02[0]));
    assertEquals(
        "20220517.000.000",
        DuplicatedMods.normalizeVersionNumber(TestData.modListDuplicatesSpecific02[1]));

    assertEquals(
        "001.000.006",
        DuplicatedMods.normalizeVersionNumber(TestData.modListDuplicatesSpecific03[0]));
    assertEquals(
        "001.000.005",
        DuplicatedMods.normalizeVersionNumber(TestData.modListDuplicatesSpecific03[1]));

    assertEquals(
        "071.001.000",
        DuplicatedMods.normalizeVersionNumber(TestData.modListDuplicatesSpecific04[0]));
    assertEquals(
        "070.000.000",
        DuplicatedMods.normalizeVersionNumber(TestData.modListDuplicatesSpecific04[1]));

    assertEquals(
        "003.004.001.131",
        DuplicatedMods.normalizeVersionNumber(TestData.modListDuplicatesSpecific05[0]));
    assertEquals(
        "003.004.000.124",
        DuplicatedMods.normalizeVersionNumber(TestData.modListDuplicatesSpecific05[1]));

    assertEquals(
        "001.004.002",
        DuplicatedMods.normalizeVersionNumber(TestData.modListDuplicatesSpecific06[0]));
    assertEquals(
        "001.004.000",
        DuplicatedMods.normalizeVersionNumber(TestData.modListDuplicatesSpecific06[1]));
  }

  @Test
  void testSearchDuplicatedMods() {

    System.out.printf("Two Test Mod Files: %s\n", twoTestFiles);
    int two_result = DuplicatedMods.searchDuplicatedMods(twoTestFiles, ".jar", true);
    assertTrue(two_result > 0);

    System.out.printf("Test Mod Files: %s\n", testFiles);
    int result = DuplicatedMods.searchDuplicatedMods(testFiles, ".jar", true);
    assertTrue(result > 0);
  }
}
