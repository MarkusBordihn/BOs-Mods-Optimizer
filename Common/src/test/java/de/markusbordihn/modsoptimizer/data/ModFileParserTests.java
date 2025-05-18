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

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ModFileParserTests {

  @Test
  void testStandardTimestamp() {
    assertEquals(
        LocalDateTime.of(2024, 12, 8, 3, 10, 9),
        ModFileParser.parseTimestamp("2024-12-08T03:10:09+0000"));
  }

  @Test
  void testNoneStandardTimestamp() {
    assertEquals(
        LocalDateTime.of(2024, 12, 8, 0, 19, 01),
        ModFileParser.parseTimestamp("2024-12-08-00:19:01"));
  }

  @Test
  void testNanoTimestamp() {
    LocalDateTime result = ModFileParser.parseTimestamp("2024-12-08T03:10:09.753051715");
    assertEquals(2024, result.getYear());
    assertEquals(753051715, result.getNano());
  }

  @Test
  void testNanoZonedTimestamp() {
    assertEquals(
        LocalDateTime.of(2024, 12, 8, 3, 10, 9, 753051715),
        ModFileParser.parseTimestamp("2024-12-08T03:10:09.753051715Z"));
  }

  @Test
  void testInvalidTimestamp() {
    assertEquals(ModFileData.EMPTY_TIMESTAMP, ModFileParser.parseTimestamp("invalid"));
  }

  @Test
  void testNullTimestamp() {
    assertEquals(ModFileData.EMPTY_TIMESTAMP, ModFileParser.parseTimestamp(null));
  }
}
