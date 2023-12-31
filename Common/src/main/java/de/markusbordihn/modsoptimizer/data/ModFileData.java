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
import java.util.UUID;
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
  public static final String MANIFEST_AUTOMATIC_MODULE_NAME = "Automatic-Module-Name";
  public static final String MANIFEST_IMPLEMENTATION_VERSION = "Implementation-Version";
  public static final String MANIFEST_IMPLEMENTATION_TIMESTAMP = "Implementation-Timestamp";
  public static final String MANIFEST_IMPLEMENTATION_TITLE = "Implementation-Title";
  public static final String MANIFEST_SPECIFICATION_TITLE = "Specification-Title";
  public static final String MANIFEST_FML_MOD_TYPE = "FMLModType";

  public static ModFileData parseModFile(Manifest manifest, Path path, JarFile jarFile) {
    ModType modType = getModTypeByFile(manifest, jarFile);
    if (modType == ModType.FORGE) {
      return ModFileData.parseForgeModFile(manifest, path, jarFile);
    } else if (modType == ModType.NEOFORGE) {
      return ModFileData.parseNeoForgeModFile(manifest, path, jarFile);
    } else if (modType == ModType.FABRIC) {
      return ModFileData.parseFabricModFile(manifest, path, jarFile);
    } else if (modType == ModType.MIXED) {
      return ModFileData.parseMixedModFile(manifest, path, jarFile);
    }

    Constants.LOG.error(
        "⚠ Found unknown mod type {} for mod file {} with manifest {}!",
        modType,
        jarFile.getName(),
        manifest != null ? manifest.getMainAttributes() : null);

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

  public static ModFileData parseMixedModFile(Manifest manifest, Path path, JarFile jarFile) {
    String modId = EMPTY_MOD_ID;
    String name = EMPTY_MOD_NAME;
    Version version = EMPTY_VERSION;
    ModEnvironment environment = ModEnvironment.UNKNOWN;
    LocalDateTime timestamp = EMPTY_TIMESTAMP;

    ModFileData forgeModFileData = parseForgeModFile(manifest, path, jarFile);
    ModFileData fabricModFileData = parseFabricModFile(manifest, path, jarFile);

    if (forgeModFileData != null) {
      modId = forgeModFileData.id();
      name = forgeModFileData.name();
      version = forgeModFileData.version();
      environment = forgeModFileData.environment();
      timestamp = forgeModFileData.timestamp();
    } else if (fabricModFileData != null) {
      modId = fabricModFileData.id();
      name = fabricModFileData.name();
      version = fabricModFileData.version();
      environment = fabricModFileData.environment();
      timestamp = fabricModFileData.timestamp();
    }

    // Check if jar file only contains a 'data' directory and maybe a 'meta-inf' directory, ignore
    // other files which are not a directory to detect data packs.
    boolean isDataPack = false;
    for (ZipEntry zipEntry : jarFile.stream().toList()) {
      if (zipEntry.isDirectory()) {
        if (zipEntry.getName().startsWith("data/") || zipEntry.getName().startsWith("META-INF/")) {
          isDataPack = true;
        } else {
          isDataPack = false;
          break;
        }
      }
    }
    if (isDataPack) {
      environment = ModEnvironment.DATA_PACK;
    }

    return new ModFileData(path, modId, ModType.MIXED, name, version, environment, timestamp);
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
        String modsPrefix = "mods[0].";
        String modsVersionId = modsPrefix + "version";
        modId = modsToml.getString(modsPrefix + "modId");
        name = modsToml.getString(modsPrefix + "displayName");

        // Parse version number.
        if (modsToml.getString(modsVersionId) != null
            && !modsToml.getString(modsVersionId).startsWith("${")) {
          version = SemanticVersionUtils.parseVersion(modsToml.getString(modsVersionId));
        } else if (modsToml.getString("version") != null
            && !modsToml.getString("version").startsWith("${")) {
          Constants.LOG.warn(
              "⚠ The version tag should be placed inside the [[mods]] section of the mods.toml file {}!",
              path);
          version = SemanticVersionUtils.parseVersion(modsToml.getString("version"));
        }

        // Iterate over all dependencies (max. 10) and check the required side for "forge" or
        // "neoforge", we don't care about other dependencies yet.
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

      // Get mod version from manifest, if available.
      if (version == null
          || version.equals(EMPTY_VERSION)
              && hasAttributeValue(MANIFEST_IMPLEMENTATION_VERSION, attributes)) {
        version =
            SemanticVersionUtils.parseVersion(attributes.getValue(MANIFEST_IMPLEMENTATION_VERSION));
      }

      // Get mod name from manifest, if available.
      if (name == null
          || name.equals("${file.jarName}")
              && hasAttributeValue(MANIFEST_SPECIFICATION_TITLE, attributes)) {
        name = attributes.getValue(MANIFEST_SPECIFICATION_TITLE);
      }

      // Get mod id from manifest, if available.
      if (modId == null || modId.isEmpty() || modId.equals(EMPTY_MOD_ID)) {
        if (hasAttributeValue(MANIFEST_AUTOMATIC_MODULE_NAME, attributes)) {
          modId =
              attributes.getValue(MANIFEST_AUTOMATIC_MODULE_NAME).replace(" ", "-").toLowerCase();
        } else if (hasAttributeValue(MANIFEST_IMPLEMENTATION_TITLE, attributes)) {
          modId =
              attributes.getValue(MANIFEST_IMPLEMENTATION_TITLE).replace(" ", "-").toLowerCase();
        }
      }

      // Get fml mod type, if available.
      if (hasAttributeValue(MANIFEST_FML_MOD_TYPE, attributes)) {
        String fmlModType = attributes.getValue(MANIFEST_FML_MOD_TYPE);
        switch (fmlModType) {
          case "LIBRARY", "GAMELIBRARY" -> environment = ModEnvironment.LIBRARY;
          case "LANGPROVIDER" -> environment = ModEnvironment.LANGUAGE_PROVIDER;
          case "MOD" -> {
            // Ignore MOD, because we already have a default environment.
          }
          default -> Constants.LOG.warn(
              "⚠ Found unknown fml mod type {} for {}!", fmlModType, path);
        }
      }
      timestamp = parseTimestamp(attributes.getValue(MANIFEST_IMPLEMENTATION_TIMESTAMP));
    }

    // Confirm that we have a valid timestamp
    if (timestamp == null || timestamp.equals(EMPTY_TIMESTAMP)) {
      timestamp = parseTimestampFromPath(path);
    }

    // Confirm that we have a valid mod id
    if (modId == null || modId.isEmpty() || modId.equals(EMPTY_MOD_ID)) {
      if (environment == ModEnvironment.LIBRARY) {
        modId = "library-" + UUID.randomUUID();
      } else if (environment == ModEnvironment.LANGUAGE_PROVIDER) {
        modId = "language-provider-" + UUID.randomUUID();
      } else {
        Constants.LOG.error("⚠ Found no valid modId for {}!", path);
        modId = "unknown-" + UUID.randomUUID();
      }
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
        Constants.LOG.warn("Was unable to parse timestamp {}:", timestamp);
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
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
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
      if (version == null
          || version.equals(EMPTY_VERSION)
              && hasAttributeValue(MANIFEST_IMPLEMENTATION_VERSION, attributes)) {
        version =
            SemanticVersionUtils.parseVersion(attributes.getValue(MANIFEST_IMPLEMENTATION_VERSION));
      }
      if (name == null
          || name.equals("${file.jarName}")
              && hasAttributeValue(MANIFEST_SPECIFICATION_TITLE, attributes)) {
        name = attributes.getValue(MANIFEST_SPECIFICATION_TITLE);
      }
      timestamp = parseTimestamp(attributes.getValue(MANIFEST_IMPLEMENTATION_TIMESTAMP));
    }

    // Confirm that we have a valid timestamp
    if (timestamp == null || timestamp.equals(EMPTY_TIMESTAMP)) {
      timestamp = parseTimestampFromPath(path);
    }

    return new ModFileData(path, id, ModType.FABRIC, name, version, environment, timestamp);
  }

  private static ModType getModTypeByFile(Manifest manifest, JarFile jarFile) {
    if (manifest == null || manifest.getMainAttributes() == null) {
      // File based check for data packs, because they need no manifest.
      if (jarFile.getEntry("META-INF/mods.toml") != null
          && jarFile.getEntry("fabric.mod.json") != null) {
        return ModType.MIXED;
      }
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
    } else if (hasAttributeValue(MANIFEST_IMPLEMENTATION_TITLE, mainAttributes)
        && mainAttributes.getValue(MANIFEST_IMPLEMENTATION_TITLE).equals("NeoForge")) {
      return ModType.NEOFORGE;
    } else if (hasAttributeValue(MANIFEST_FML_MOD_TYPE, mainAttributes)) {
      return ModType.FORGE;
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
    if (jarFile.getEntry("META-INF/mods.toml") != null
        && jarFile.getEntry("fabric.mod.json") != null) {
      return ModType.MIXED;
    } else if (jarFile.getEntry("META-INF/mods.toml") != null) {
      return ModType.FORGE;
    } else if (jarFile.getEntry("fabric.mod.json") != null) {
      return ModType.FABRIC;
    }

    // Unknown mod type
    Constants.LOG.warn(
        "⚠ Unable to detect mod type for {} with manifest {}", jarFile.getName(), mainAttributes);
    return ModType.UNKNOWN;
  }

  public static boolean hasAttributeValue(String name, Attributes attributes) {
    String value = attributes.getValue(name);
    return value != null && !value.isEmpty();
  }

  public enum ModType {
    FABRIC,
    FORGE,
    NEOFORGE,
    MIXED,
    UNKNOWN
  }

  public enum ModEnvironment {
    DEFAULT,
    CLIENT,
    SERVER,
    LIBRARY,
    LANGUAGE_PROVIDER,
    DATA_PACK,
    UNKNOWN
  }
}
