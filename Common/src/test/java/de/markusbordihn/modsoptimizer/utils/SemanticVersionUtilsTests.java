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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.github.zafarkhaja.semver.Version;
import de.markusbordihn.modsoptimizer.Constants;
import de.markusbordihn.modsoptimizer.data.VersionTestData;
import org.junit.jupiter.api.Test;

class SemanticVersionUtilsTests {

  @Test
  void testParseVersion() {
    for (String semanticVersion : VersionTestData.versionList) {
      Version version = SemanticVersionUtils.parseVersion(semanticVersion);
      Constants.LOG.info("Parsed version {} to {}", semanticVersion, version);
      assertNotEquals(SemanticVersionUtils.EMPTY_VERSION, version);
    }
  }

  @Test
  void testRemoveUnnecessaryVersionParts() {
    assertEquals("1.2", SemanticVersionUtils.removeUnnecessaryVersionParts("1.2+mc1.18.x"));
  }

  @Test
  void testRemoveLeadingZeros() {
    assertEquals("1.2.3", SemanticVersionUtils.removeLeadingZeros("01.02.03"));
    assertEquals("1.2.3", SemanticVersionUtils.removeLeadingZeros("1.02.03"));
    assertEquals("1.2.3", SemanticVersionUtils.removeLeadingZeros("1.2.03"));
    assertEquals("1.2.3", SemanticVersionUtils.removeLeadingZeros("1.2.3"));
    assertEquals("1.2.0", SemanticVersionUtils.removeLeadingZeros("1.2.0"));
    assertEquals("1.0.3", SemanticVersionUtils.removeLeadingZeros("1.0.3"));
    assertEquals("2.3", SemanticVersionUtils.removeLeadingZeros("0.2.3"));
    assertEquals("1.2.3", SemanticVersionUtils.removeLeadingZeros("01.02.03"));
    assertEquals("9.0+22", SemanticVersionUtils.removeLeadingZeros("9.0+22"));
  }
}
