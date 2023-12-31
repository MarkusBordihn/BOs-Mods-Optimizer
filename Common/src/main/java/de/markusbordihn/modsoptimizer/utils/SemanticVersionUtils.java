/**
 * Copyright 2022 Markus Bordihn
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package de.markusbordihn.modsoptimizer.utils;

import com.github.zafarkhaja.semver.Version;
import de.markusbordihn.modsoptimizer.Constants;
import java.util.regex.Pattern;

public class SemanticVersionUtils {

  public static final Version EMPTY_VERSION = Version.valueOf("0.0.0");
  public static final Pattern MC_NAME_VERSION_PATTERN =
      Pattern.compile("[+_-]?(mc|minecraft)[1,2]{1,2}\\.\\d{1,2}\\.?[0-9x]?");
  public static final Pattern FORGE_VERSION_PATTERN = Pattern.compile("[+_-]?(forge)[+_-]?");
  public static final Pattern BUILD_VERSION_PATTERN = Pattern.compile("[_.-]?(build)[_.-]?");
  public static final Pattern LEADING_ZEROS_PATTERN = Pattern.compile("\\.0+\\d");
  public static final Pattern RELEASE_VERSION_PATTERN = Pattern.compile("[+_-]?(release)[+_-]?");
  public static final Pattern SNAPSHOT_VERSION_PATTERN = Pattern.compile("[+_-]?(snapshot)[+_-]?");
  public static final Pattern VERSION_CLEANUP_DOUBLE_PATTERN =
      Pattern.compile("([._+\\-\\s])[._+\\-\\s]");
  public static final Pattern VERSION_CLEANUP_START_PATTERN = Pattern.compile("(^[._+\\-\\s])");
  public static final Pattern VERSION_CLEANUP_END_PATTERN = Pattern.compile("([._+\\-\\s])$");
  public static final Pattern VERSION_PATTERN_1 = Pattern.compile("^\\d+$");
  public static final Pattern VERSION_PATTERN_2 = Pattern.compile("^\\d+\\.\\d+$");
  public static final Pattern VERSION_PATTERN_3 =
      Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.[a-z0-9]+$");
  public static final Pattern VERSION_PATTERN_4 = Pattern.compile("^\\d+\\.\\d+\\+[A-Za-z0-9]+$");
  public static final Pattern VERSION_PATTERN_5 =
      Pattern.compile("^\\d+\\.\\d+\\.\\d+_[A-Za-z0-9]+$");
  public static final Pattern VERSION_PATTERN_6 =
      Pattern.compile("^\\d+\\.\\d+(\\.\\d+)?-\\d+\\.\\d+\\.\\d+\\.\\d+$");
  public static final Pattern VERSION_PATTERN_7 =
      Pattern.compile("^\\d+\\.\\d+(\\.\\d+)?-\\d+\\.\\d+\\.\\d+$");
  public static final Pattern VERSION_PATTERN_8 = Pattern.compile("^\\d+\\.\\d+-\\d+$");
  public static final Pattern VERSION_PATTERN_9 = Pattern.compile("^\\d+\\.\\d+\\.[a-z]+$");
  private static boolean debugEnabled = false;

  protected SemanticVersionUtils() {}

  public static void enableDebug() {
    debugEnabled = true;
  }

  public static void disableDebug() {
    debugEnabled = false;
  }

  public static Version parseVersion(String version) {
    return parseVersion(version, EMPTY_VERSION);
  }

  public static Version parseVersion(String versionNumber, Version defaultVersion) {
    if (versionNumber != null && !versionNumber.isEmpty()) {
      try {
        return Version.valueOf(versionNumber);
      } catch (Exception e) {
        if (debugEnabled) {
          Constants.LOG.debug(
              "No valid semantic version {}, will try to normalize version.", versionNumber);
        }
      }
    }

    // Try to normalize version number, because of wrong formatted version numbers.
    String normalizedVersion = normalizeVersion(versionNumber);
    try {
      return Version.valueOf(normalizedVersion);
    } catch (Exception e) {
      if (debugEnabled) {
        Constants.LOG.error(
            "Unable to parse version {} or {}, because of: {}",
            versionNumber,
            normalizedVersion,
            e);
      }
    }

    return defaultVersion;
  }

  public static String normalizeVersion(String version) {
    if (version == null || version.isEmpty()) {
      return "";
    }
    // Make sure that version is lower case.
    version = version.toLowerCase();

    // Clean up version string.
    version = removeUnnecessaryVersionParts(version);
    version = removeLeadingZeros(version);

    // Normalize typical wrongly formatted version numbers.
    if (VERSION_PATTERN_1.matcher(version).matches()) {
      return version + ".0.0";
    }
    if (VERSION_PATTERN_2.matcher(version).matches()) {
      return version + ".0";
    }
    if (VERSION_PATTERN_3.matcher(version).matches()) {
      return version.substring(0, version.lastIndexOf('.'))
          + "-"
          + version.substring(version.lastIndexOf('.') + 1);
    }
    if (VERSION_PATTERN_4.matcher(version).matches()) {
      return version.substring(0, version.lastIndexOf('+'))
          + "."
          + version.substring(version.lastIndexOf('+') + 1);
    }
    if (VERSION_PATTERN_5.matcher(version).matches()) {
      return version.substring(0, version.lastIndexOf('_'))
          + "-"
          + version.substring(version.lastIndexOf('_') + 1);
    }
    if (VERSION_PATTERN_6.matcher(version).matches()) {
      version = version.substring(version.lastIndexOf('-') + 1);
      return version.substring(0, version.lastIndexOf('.'))
          + "-"
          + version.substring(version.lastIndexOf('.') + 1);
    }
    if (VERSION_PATTERN_7.matcher(version).matches()) {
      return version.substring(version.lastIndexOf('-') + 1);
    }
    if (VERSION_PATTERN_8.matcher(version).matches()) {
      return version.replace('-', '.');
    }
    if (VERSION_PATTERN_9.matcher(version).matches()) {
      String lastVersionPart = version.substring(version.lastIndexOf('.') + 1);
      lastVersionPart = Character.getNumericValue(lastVersionPart.charAt(0)) + "";
      return version.substring(0, version.lastIndexOf('.')) + '.' + lastVersionPart;
    }

    return version;
  }

  static String removeUnnecessaryVersionParts(String version) {
    if (version == null || version.isEmpty()) {
      return "";
    }
    if (version.startsWith(Constants.MINECRAFT_VERSION)) {
      version = version.substring(Constants.MINECRAFT_VERSION.length());
    }
    if (MC_NAME_VERSION_PATTERN.matcher(version).find()) {
      version = version.replaceAll(MC_NAME_VERSION_PATTERN.pattern(), "");
    }
    if (FORGE_VERSION_PATTERN.matcher(version).find()) {
      version = version.replaceAll(FORGE_VERSION_PATTERN.pattern(), "");
    }
    if (BUILD_VERSION_PATTERN.matcher(version).find()) {
      version = version.replaceAll(BUILD_VERSION_PATTERN.pattern(), "-");
    }
    if (RELEASE_VERSION_PATTERN.matcher(version).find()) {
      version = version.replaceAll(RELEASE_VERSION_PATTERN.pattern(), "");
    }
    if (SNAPSHOT_VERSION_PATTERN.matcher(version).find()) {
      version = version.replaceAll(SNAPSHOT_VERSION_PATTERN.pattern(), "-");
    }

    // Remove double dashes, dots, underscores and spaces with a single version.
    version = version.replaceAll(VERSION_CLEANUP_START_PATTERN.pattern(), "");
    version = version.replaceAll(VERSION_CLEANUP_END_PATTERN.pattern(), "");
    version = version.replaceAll(VERSION_CLEANUP_DOUBLE_PATTERN.pattern(), "$1");

    return version;
  }

  static String removeLeadingZeros(String version) {
    if (version == null || version.isEmpty()) {
      return "";
    }
    if (version.startsWith("0.")) {
      version = version.substring(2);
    }
    if (version.startsWith("0")) {
      version = version.substring(1);
    }
    if (LEADING_ZEROS_PATTERN.matcher(version).find()) {
      String[] parts = version.split("\\.");
      for (int i = 0; i < parts.length; i++) {
        parts[i] = removeLeadingZerosFromPart(parts[i]);
      }
      return String.join(".", parts);
    }
    return version;
  }

  static String removeLeadingZerosFromPart(String part) {
    return part.replaceFirst("^0+(?!$)", "");
  }
}
