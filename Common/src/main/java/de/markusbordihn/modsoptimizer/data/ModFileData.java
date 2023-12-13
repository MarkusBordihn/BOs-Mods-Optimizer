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

import com.github.zafarkhaja.semver.Version;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.moandjiezana.toml.Toml;
import de.markusbordihn.modsoptimizer.Constants;
import de.markusbordihn.modsoptimizer.utils.SemanticVersionUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public record ModFileData(
    Path path,
    String id,
    ModType modType,
    String name,
    Version version,
    ModEnvironment environment,
    LocalDateTime timestamp) {

  public static final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

  public static final Version EMPTY_VERSION = Version.valueOf("0.0.0");
  public static final String EMPTY_MOD_ID = "unknown_id";
  public static final String EMPTY_MOD_NAME = "Unknown";
  public static final LocalDateTime EMPTY_TIMESTAMP = LocalDateTime.now();

  public static ModFileData parseModFile(Manifest manifest, Path path, JarFile jarFile) {
    ModType modType = getModTypeByFile(manifest, jarFile);
    if (modType == ModType.FORGE) {
      return ModFileData.parseForgeModFile(manifest, path, jarFile);
    } else if (modType == ModType.NEOFORGE) {
      return ModFileData.parseNeoForgeModFile(manifest, path, jarFile);
    } else if (modType == ModType.FABRIC) {
      return ModFileData.parseFabricModFile(manifest, path, jarFile);
    }

    return new ModFileData(
        path,
        EMPTY_MOD_ID,
        modType,
        EMPTY_MOD_NAME,
        EMPTY_VERSION,
        ModEnvironment.DEFAULT,
        EMPTY_TIMESTAMP);
  }

  public static ModFileData parseNeoForgeModFile(Manifest manifest, Path path, JarFile jarFile) {
    ModFileData modFileData = parseForgeModFile(manifest, path, jarFile);
    return new ModFileData(
        modFileData.path(),
        modFileData.id(),
        ModType.NEOFORGE,
        modFileData.name(),
        modFileData.version(),
        modFileData.environment(),
        modFileData.timestamp());
  }

  public static ModFileData parseForgeModFile(Manifest manifest, Path path, JarFile jarFile) {
    String modId = EMPTY_MOD_ID;
    String name = EMPTY_MOD_NAME;
    Version version = EMPTY_VERSION;
    ModEnvironment environment = ModEnvironment.UNKNOWN;
    LocalDateTime timestamp = EMPTY_TIMESTAMP;

    // Parse mods.toml file
    ZipEntry modsFile = jarFile.getEntry("META-INF/mods.toml");
    if (modsFile != null && !modsFile.isDirectory()) {
      try (InputStream inputStream = jarFile.getInputStream(modsFile)) {
        Toml modsToml = new Toml().read(inputStream);
        modId = modsToml.getString("mods[0].modId");
        name = modsToml.getString("mods[0].displayName");

        // Parse version number.
        if (modsToml.getString("mods[0].version") != null
            && !modsToml.getString("mods[0].version").startsWith("${")) {
          version = SemanticVersionUtils.parseVersion(modsToml.getString("mods[0].version"));
        } else if (modsToml.getString("version") != null
            && !modsToml.getString("version").startsWith("${")) {
          Constants.LOG.warn(
              "⚠ The version tag should be placed inside the [[mods]] section of the mods.toml file {}!",
              path);
          version = SemanticVersionUtils.parseVersion(modsToml.getString("version"));
        }

        // Iterate over all dependencies (max. 10) and check the required side for "forge" or
        // "neoforge".
        for (int i = 0; i < 10; i++) {
          String dependencyId = "dependencies." + modId + "[" + i + "]";
          try {
            if (!modsToml.contains(dependencyId)) {
              break;
            }
          } catch (Exception e) {
            break;
          }
          if (modsToml.getString(dependencyId + ".modId").equals("forge")
              || modsToml.getString(dependencyId + ".modId").equals("neoforge")) {
            if (modsToml.getString(dependencyId + ".side") != null) {
              String requiredSide = modsToml.getString(dependencyId + ".side").toLowerCase();
              environment =
                  switch (requiredSide) {
                    case "client" -> ModEnvironment.CLIENT;
                    case "server" -> ModEnvironment.SERVER;
                    case "both" -> ModEnvironment.DEFAULT;
                    default -> environment;
                  };
            } else {
              Constants.LOG.warn(
                  "⚠ Found no side tag inside the {} section of the mods.toml file {}!",
                  dependencyId,
                  path);
            }
            break;
          }
        }

        // Use displayTest for hints of the environment, even it is used very rarely.
        if (environment == ModEnvironment.UNKNOWN) {
          String displayTest = modsToml.getString("mods[0].displayTest");
          if (displayTest != null) {
            environment =
                switch (displayTest) {
                  case "IGNORE_SERVER_VERSION" -> ModEnvironment.SERVER;
                  case "IGNORE_ALL_VERSION" -> ModEnvironment.CLIENT;
                  case "MATCH_VERSION" -> ModEnvironment.DEFAULT;
                  default -> environment;
                };
          }
        }
      } catch (Exception e) {
        Constants.LOG.error("Was unable to read mods file {} from {}:", modsFile, path, e);
      }
    }

    // Add manifest information, if available.
    if (manifest != null && manifest.getMainAttributes() != null) {
      Attributes attributes = manifest.getMainAttributes();
      if (version == null || version.equals(EMPTY_VERSION)) {
        version = SemanticVersionUtils.parseVersion(attributes.getValue("Implementation-Version"));
      }
      if (name == null || name.equals("${file.jarName}")) {
        name = attributes.getValue("Specification-Title");
      }
      timestamp = parseTimestamp(attributes.getValue("Implementation-Timestamp"));
    }

    // Confirm that we have a valid timestamp
    if (timestamp == null || timestamp.equals(EMPTY_TIMESTAMP)) {
      timestamp = parseTimestampFromPath(path);
    }

    return new ModFileData(path, modId, ModType.FORGE, name, version, environment, timestamp);
  }

  private static LocalDateTime parseTimestampFromPath(Path path) {
    try {
      BasicFileAttributes fileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
      if (fileAttributes != null) {
        FileTime fileTime = fileAttributes.creationTime();
        return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
      }
    } catch (IOException e) {
      Constants.LOG.error("Was unable to read file attributes from {}:", path, e);
    }
    return EMPTY_TIMESTAMP;
  }

  private static LocalDateTime parseTimestamp(String timestamp) {
    if (timestamp != null && !timestamp.isEmpty()) {
      try {
        return LocalDateTime.parse(timestamp, dateTimeFormatter);
      } catch (Exception e) {
        Constants.LOG.error("Was unable to parse timestamp {}:", timestamp, e);
      }
    }
    return EMPTY_TIMESTAMP;
  }

  public static ModFileData parseFabricModFile(Manifest manifest, Path path, JarFile jarFile) {
    String id = EMPTY_MOD_ID;
    String name = EMPTY_MOD_NAME;
    Version version = EMPTY_VERSION;
    ModEnvironment environment = ModEnvironment.UNKNOWN;
    LocalDateTime timestamp = EMPTY_TIMESTAMP;

    // Parse fabric.mod.json file
    ZipEntry modsFile = jarFile.getEntry("fabric.mod.json");
    if (modsFile != null && !modsFile.isDirectory()) {
      try (InputStream inputStream = jarFile.getInputStream(modsFile)) {
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(new InputStreamReader(inputStream));
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject != null) {
          id = jsonObject.get("id").getAsString();
          name = jsonObject.get("name").getAsString();

          // Parse version number
          if (jsonObject.get("version") != null
              && !jsonObject.get("version").getAsString().startsWith("${")) {
            version = SemanticVersionUtils.parseVersion(jsonObject.get("version").getAsString());
          }

          // Parse environment
          if (jsonObject.get("environment") != null) {
            String environmentString = jsonObject.get("environment").getAsString();
            environment =
                switch (environmentString) {
                  case "client" -> ModEnvironment.CLIENT;
                  case "server" -> ModEnvironment.SERVER;
                  case "*" -> ModEnvironment.DEFAULT;
                  default -> environment;
                };
          }
        }
      } catch (Exception e) {
        Constants.LOG.error("Was unable to read mods file {} from {}:", modsFile, path, e);
      }
    }

    // Add manifest information, if available.
    if (manifest != null && manifest.getMainAttributes() != null) {
      Attributes attributes = manifest.getMainAttributes();
      if (version == null || version.equals(EMPTY_VERSION)) {
        version = SemanticVersionUtils.parseVersion(attributes.getValue("Implementation-Version"));
      }
      if (name == null || name.equals("${file.jarName}")) {
        name = attributes.getValue("Specification-Title");
      }
      timestamp = parseTimestamp(attributes.getValue("Implementation-Timestamp"));
    }

    // Confirm that we have a valid timestamp
    if (timestamp == null || timestamp.equals(EMPTY_TIMESTAMP)) {
      timestamp = parseTimestampFromPath(path);
    }

    return new ModFileData(path, id, ModType.FABRIC, name, version, environment, timestamp);
  }

  private static ModType getModTypeByFile(Manifest manifest, JarFile jarFile) {
    if (manifest == null || manifest.getMainAttributes() == null) {
      return ModType.UNKNOWN;
    }

    // Simple check for Fabric mods, because they have a Fabric-Gradle-Version or
    // Fabric-Loader-Version attribute.
    Attributes mainAttributes = manifest.getMainAttributes();
    if ((mainAttributes.getValue("Fabric-Gradle-Version") != null
            && !mainAttributes.getValue("Fabric-Gradle-Version").isEmpty())
        || (mainAttributes.getValue("Fabric-Loader-Version") != null
            && !mainAttributes.getValue("Fabric-Loader-Version").isEmpty())) {
      return ModType.FABRIC;
    } else if (mainAttributes.getValue("Implementation-Title") != null
        && mainAttributes.getValue("Implementation-Title").equals("NeoForge")) {
      return ModType.NEOFORGE;
    }

    // File name based check.
    String fileName = jarFile.getName().toLowerCase();
    if (fileName.endsWith(".jar")) {
      if (fileName.contains("-neoforge-")) {
        return ModType.NEOFORGE;
      } else if (fileName.startsWith("-fabric-")) {
        return ModType.FABRIC;
      } else if (fileName.startsWith("-forge-")) {
        return ModType.FORGE;
      }
    }

    // File based check for Forge mods, because they have no specific attribute.
    if (jarFile.getEntry("META-INF/mods.toml") != null) {
      return ModType.FORGE;
    } else if (jarFile.getEntry("META-INF/fabric.mod.json") != null) {
      return ModType.FABRIC;
    }

    // Unknown mod type
    Constants.LOG.warn("⚠ Unable to detect mod type for {}", jarFile.getName());
    return ModType.UNKNOWN;
  }

  public enum ModType {
    FABRIC,
    FORGE,
    NEOFORGE,
    UNKNOWN
  }

  public enum ModEnvironment {
    DEFAULT,
    CLIENT,
    SERVER,
    UNKNOWN
  }
}
