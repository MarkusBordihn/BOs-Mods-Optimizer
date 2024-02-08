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
import de.markusbordihn.modsoptimizer.data.ModFileData.ModEnvironment;
import de.markusbordihn.modsoptimizer.data.ModFileData.ModType;
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

public class ModFileParser {

  public static final String MANIFEST_AUTOMATIC_MODULE_NAME = "Automatic-Module-Name";
  public static final String MANIFEST_IMPLEMENTATION_VERSION = "Implementation-Version";
  public static final String MANIFEST_IMPLEMENTATION_TIMESTAMP = "Implementation-Timestamp";
  public static final String MANIFEST_IMPLEMENTATION_TITLE = "Implementation-Title";
  public static final String MANIFEST_SPECIFICATION_TITLE = "Specification-Title";
  public static final String MANIFEST_FML_MOD_TYPE = "FMLModType";
  public static final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

  protected ModFileParser() {}

  private static ModType getModTypeByFile(Manifest manifest, JarFile jarFile) {

    // File name based check.
    String fileName = jarFile.getName().toLowerCase();
    if (fileName.endsWith(".jar")) {
      if (fileName.contains("-neoforge-") || fileName.endsWith("-neoforge.jar")) {
        return ModType.NEOFORGE;
      } else if (fileName.startsWith("-fabric-") || fileName.endsWith("-fabric.jar")) {
        return ModType.FABRIC;
      } else if (fileName.startsWith("-forge-") || fileName.endsWith("-forge.jar")) {
        return ModType.FORGE;
      } else if (fileName.startsWith("-quilt-") || fileName.endsWith("-quilt.jar")) {
        return ModType.QUILT;
      }
    }

    // Simple check for Mixed mods and other special cases.
    if (jarFile.getEntry("META-INF/mods.toml") != null
        && jarFile.getEntry("fabric.mod.json") != null) {
      return ModType.MIXED;
    }

    // Simple check for Fabric mods, because they have a Fabric-Gradle-Version or
    // Fabric-Loader-Version attribute.
    if (manifest != null && manifest.getMainAttributes() != null) {
      Attributes mainAttributes = manifest.getMainAttributes();
      if ((mainAttributes.getValue("Fabric-Gradle-Version") != null
              && !mainAttributes.getValue("Fabric-Gradle-Version").isEmpty())
          || (mainAttributes.getValue("Fabric-Loader-Version") != null
              && !mainAttributes.getValue("Fabric-Loader-Version").isEmpty())) {
        if (jarFile.getEntry("fabric.mod.json") != null) {
          return ModType.FABRIC;
        } else if (jarFile.getEntry("quilt.mod.json") != null) {
          return ModType.QUILT;
        } else if (jarFile.getEntry("META-INF/mods.toml") != null
            || jarFile.getEntry("META-INF/services/cpw.mods.modlauncher.api.ITransformationService")
                != null) {
          return ModType.MIXED;
        }
      } else if (hasAttributeValue(ModFileParser.MANIFEST_IMPLEMENTATION_TITLE, mainAttributes)
          && mainAttributes
              .getValue(ModFileParser.MANIFEST_IMPLEMENTATION_TITLE)
              .equals("NeoForge")) {
        return ModType.NEOFORGE;
      } else if (hasAttributeValue(ModFileParser.MANIFEST_FML_MOD_TYPE, mainAttributes)) {
        return ModType.FORGE;
      }
    }

    // File based check for data packs, Forge and Fabric mods.
    if (jarFile.getEntry("META-INF/mods.toml") != null) {
      return ModType.FORGE;
    } else if (jarFile.getEntry("fabric.mod.json") != null) {
      return ModType.FABRIC;
    } else if (jarFile.getEntry("quilt.mod.json") != null) {
      return ModType.QUILT;
    }

    // Unknown mod type
    Constants.LOG.warn(
        "⚠ Unable to detect mod type for {} with manifest {}",
        jarFile.getName(),
        manifest != null ? manifest.getMainAttributes() : null);
    return ModType.UNKNOWN;
  }

  public static ModFileData parseModFile(Manifest manifest, Path path, JarFile jarFile) {
    ModType modType = getModTypeByFile(manifest, jarFile);
    if (modType == ModType.FORGE) {
      return ModFileParser.parseForgeModFile(manifest, path, jarFile);
    } else if (modType == ModType.NEOFORGE) {
      return ModFileParser.parseNeoForgeModFile(manifest, path, jarFile);
    } else if (modType == ModType.FABRIC) {
      return ModFileParser.parseFabricModFile(manifest, path, jarFile);
    } else if (modType == ModType.QUILT) {
      return ModFileParser.parseQuiltModFile(manifest, path, jarFile);
    } else if (modType == ModType.MIXED) {
      return ModFileParser.parseMixedModFile(manifest, path, jarFile);
    }

    Constants.LOG.error(
        "⚠ Found unknown mod type {} for mod file {} with manifest {}!",
        modType,
        jarFile.getName(),
        manifest != null ? manifest.getMainAttributes() : null);

    return new ModFileData(
        path,
        ModFileData.EMPTY_MOD_ID,
        modType,
        ModFileData.EMPTY_MOD_NAME,
        ModFileData.EMPTY_VERSION,
        ModEnvironment.DEFAULT,
        ModFileData.EMPTY_TIMESTAMP);
  }

  public static ModFileData parseMixedModFile(Manifest manifest, Path path, JarFile jarFile) {
    String modId = ModFileData.EMPTY_MOD_ID;
    String name = ModFileData.EMPTY_MOD_NAME;
    Version version = ModFileData.EMPTY_VERSION;
    ModEnvironment environment = ModEnvironment.UNKNOWN;
    LocalDateTime timestamp = ModFileData.EMPTY_TIMESTAMP;

    ModFileData forgeModFileData = parseForgeModFile(manifest, path, jarFile);
    ModFileData fabricModFileData = parseFabricModFile(manifest, path, jarFile);

    // Check for forge mods data.
    modId = forgeModFileData.id();
    name = forgeModFileData.name();
    version = forgeModFileData.version();
    environment = forgeModFileData.environment();
    timestamp = forgeModFileData.timestamp();

    // Check for fabric mods data.
    if (modId == null || modId.isEmpty() || modId.equals(ModFileData.EMPTY_MOD_ID)) {
      modId = fabricModFileData.id();
      name = fabricModFileData.name();
      version = fabricModFileData.version();
      environment = fabricModFileData.environment();
      timestamp = fabricModFileData.timestamp();
    }

    // Check for quilt mods data.
    if (modId == null || modId.isEmpty() || modId.equals(ModFileData.EMPTY_MOD_ID)) {
      ModFileData quiltModFileData = parseQuiltModFile(manifest, path, jarFile);
      modId = quiltModFileData.id();
      name = quiltModFileData.name();
      version = quiltModFileData.version();
      environment = quiltModFileData.environment();
      timestamp = quiltModFileData.timestamp();
    }

    // Check for service environment mods.
    if (jarFile.getEntry("META-INF/services/cpw.mods.modlauncher.api.ITransformationService")
        != null) {
      environment = ModEnvironment.SERVICE;
    }

    // Check if jar file only contains a 'assets', 'data' directory and maybe a 'meta-inf' or
    // 'things' directory, ignore other files which are not a directory to detect data packs.
    if (environment == ModEnvironment.UNKNOWN) {
      boolean isDataPack = false;
      for (ZipEntry zipEntry : jarFile.stream().toList()) {
        if (zipEntry.isDirectory()) {
          if (zipEntry.getName().startsWith("assets/")
              || zipEntry.getName().startsWith("data/")
              || zipEntry.getName().startsWith("META-INF/")
              || zipEntry.getName().startsWith("things/")) {
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
    }

    return new ModFileData(path, modId, ModType.MIXED, name, version, environment, timestamp);
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
    String modId = ModFileData.EMPTY_MOD_ID;
    String name = ModFileData.EMPTY_MOD_NAME;
    Version version = ModFileData.EMPTY_VERSION;
    ModEnvironment environment = ModEnvironment.UNKNOWN;
    LocalDateTime timestamp = ModFileData.EMPTY_TIMESTAMP;

    // Parse mods.toml file
    ZipEntry modsFile = jarFile.getEntry("META-INF/mods.toml");
    if (modsFile != null && !modsFile.isDirectory()) {
      try (InputStream inputStream = jarFile.getInputStream(modsFile)) {
        Toml modsToml = new Toml().read(inputStream);
        String modsPrefix = "mods[0].";
        String modsVersionId = modsPrefix + "version";
        modId = modsToml.getString(modsPrefix + "modId");
        name = modsToml.getString(modsPrefix + "displayName");

        // Use modLoader for data pack detection.
        if (modsToml.getString("modLoader") != null
            && modsToml.getString("modLoader").equalsIgnoreCase("lowcodefml")) {
          environment = ModEnvironment.DATA_PACK;
        }

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
        if (environment == ModEnvironment.UNKNOWN) {
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
        Constants.LOG.error("⚠ Was unable to read mods file {} from {}:", modsFile, path, e);
      }
    } else {
      Constants.LOG.warn("⚠ Found no META-INF/mods.toml file for {}!", path);
    }

    // Add manifest information, if available.
    if (manifest != null && manifest.getMainAttributes() != null) {
      Attributes attributes = manifest.getMainAttributes();

      // Get mod version from manifest, if available.
      if (version == null
          || version.equals(ModFileData.EMPTY_VERSION)
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
      if (modId == null || modId.isEmpty() || modId.equals(ModFileData.EMPTY_MOD_ID)) {
        if (hasAttributeValue(MANIFEST_AUTOMATIC_MODULE_NAME, attributes)) {
          modId =
              attributes.getValue(MANIFEST_AUTOMATIC_MODULE_NAME).replace(" ", "-").toLowerCase();
        } else if (hasAttributeValue(MANIFEST_IMPLEMENTATION_TITLE, attributes)) {
          modId =
              attributes.getValue(MANIFEST_IMPLEMENTATION_TITLE).replace(" ", "-").toLowerCase();
        }
      }

      // Get fml mod type, if available.
      if (environment == ModEnvironment.UNKNOWN
          && hasAttributeValue(MANIFEST_FML_MOD_TYPE, attributes)) {
        String fmlModType = attributes.getValue(MANIFEST_FML_MOD_TYPE);
        switch (fmlModType) {
          case "LIBRARY", "GAMELIBRARY" -> environment = ModEnvironment.LIBRARY;
          case "LANGPROVIDER" -> environment = ModEnvironment.LANGUAGE_PROVIDER;
          case "MOD" -> {
            // Ignore MOD, because we already have a default environment.
          }
          default ->
              Constants.LOG.warn("⚠ Found unknown fml mod type {} for {}!", fmlModType, path);
        }
      }
      timestamp = parseTimestamp(attributes.getValue(MANIFEST_IMPLEMENTATION_TIMESTAMP));
    }

    // Confirm that we have a valid timestamp
    if (timestamp == null || timestamp.equals(ModFileData.EMPTY_TIMESTAMP)) {
      timestamp = parseTimestampFromPath(path);
    }

    // Confirm that we have a valid mod id
    if (modId == null || modId.isEmpty() || modId.equals(ModFileData.EMPTY_MOD_ID)) {
      if (environment == ModEnvironment.LIBRARY) {
        modId = "library-" + UUID.randomUUID();
      } else if (environment == ModEnvironment.LANGUAGE_PROVIDER) {
        modId = "language-provider-" + UUID.randomUUID();
      } else if (environment == ModEnvironment.DATA_PACK) {
        modId = "data-pack-" + UUID.randomUUID();
      } else {
        Constants.LOG.error("⚠ Found no valid modId for {}!", path);
        modId = "unknown-" + UUID.randomUUID();
      }
    }

    return new ModFileData(path, modId, ModType.FORGE, name, version, environment, timestamp);
  }

  public static ModFileData parseQuiltModFile(Manifest manifest, Path path, JarFile jarFile) {
    String id = ModFileData.EMPTY_MOD_ID;
    String name = ModFileData.EMPTY_MOD_NAME;
    Version version = ModFileData.EMPTY_VERSION;
    ModEnvironment environment = ModEnvironment.UNKNOWN;
    LocalDateTime timestamp = ModFileData.EMPTY_TIMESTAMP;

    // Parse fabric.mod.json file
    ZipEntry modsFile = jarFile.getEntry("quilt.mod.json");
    if (modsFile != null && !modsFile.isDirectory()) {
      try (InputStream inputStream = jarFile.getInputStream(modsFile)) {
        JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        // Parse quilt Loader data
        if (jsonObject != null && jsonObject.get("quilt_loader") != null) {
          JsonObject quiltLoaderObject = jsonObject.get("quilt_loader").getAsJsonObject();
          if (quiltLoaderObject != null) {
            id = quiltLoaderObject.get("id").getAsString();

            // Parse version number
            if (quiltLoaderObject.get("version") != null
                && !quiltLoaderObject.get("version").getAsString().startsWith("${")) {
              version =
                  SemanticVersionUtils.parseVersion(quiltLoaderObject.get("version").getAsString());
            }

            // Parse meta data
            if (quiltLoaderObject.get("metadata") != null) {
              JsonObject metadataObject = quiltLoaderObject.get("metadata").getAsJsonObject();
              if (metadataObject != null) {
                name = metadataObject.get("name").getAsString();
              }
            }
          }
        }

        // Parse Minecraft data and environment
        if (jsonObject != null && jsonObject.get("minecraft") != null) {
          JsonObject minecraftObject = jsonObject.get("minecraft").getAsJsonObject();
          if (minecraftObject != null && minecraftObject.get("environment") != null) {
            String environmentString = minecraftObject.get("environment").getAsString();
            environment =
                switch (environmentString) {
                  case "client" -> ModEnvironment.CLIENT;
                  case "dedicated_server" -> ModEnvironment.SERVER;
                  case "*" -> ModEnvironment.DEFAULT;
                  default -> environment;
                };
          }
        }

      } catch (Exception e) {
        Constants.LOG.error("Was unable to read mods file {} from {}:", modsFile, path, e);
      }
    } else {
      Constants.LOG.warn("⚠ Found no quilt.mod.json file for {}!", path);
    }

    // Add manifest information, if available.
    if (manifest != null && manifest.getMainAttributes() != null) {
      Attributes attributes = manifest.getMainAttributes();
      if (version == null
          || version.equals(ModFileData.EMPTY_VERSION)
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
    if (timestamp == null || timestamp.equals(ModFileData.EMPTY_TIMESTAMP)) {
      timestamp = parseTimestampFromPath(path);
    }

    return new ModFileData(path, id, ModType.QUILT, name, version, environment, timestamp);
  }

  public static ModFileData parseFabricModFile(Manifest manifest, Path path, JarFile jarFile) {
    String id = ModFileData.EMPTY_MOD_ID;
    String name = ModFileData.EMPTY_MOD_NAME;
    Version version = ModFileData.EMPTY_VERSION;
    ModEnvironment environment = ModEnvironment.UNKNOWN;
    LocalDateTime timestamp = ModFileData.EMPTY_TIMESTAMP;

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
    } else {
      Constants.LOG.warn("⚠ Found no fabric.mod.json file for {}!", path);
    }

    // Add manifest information, if available.
    if (manifest != null && manifest.getMainAttributes() != null) {
      Attributes attributes = manifest.getMainAttributes();
      if (version == null
          || version.equals(ModFileData.EMPTY_VERSION)
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
    if (timestamp == null || timestamp.equals(ModFileData.EMPTY_TIMESTAMP)) {
      timestamp = parseTimestampFromPath(path);
    }

    return new ModFileData(path, id, ModType.FABRIC, name, version, environment, timestamp);
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
    return ModFileData.EMPTY_TIMESTAMP;
  }

  private static LocalDateTime parseTimestamp(String timestamp) {
    if (timestamp != null && !timestamp.isEmpty()) {
      try {
        return LocalDateTime.parse(timestamp, dateTimeFormatter);
      } catch (Exception e) {
        Constants.LOG.warn("Was unable to parse timestamp {}:", timestamp);
      }
    }
    return ModFileData.EMPTY_TIMESTAMP;
  }

  private static boolean hasAttributeValue(String name, Attributes attributes) {
    String value = attributes.getValue(name);
    return value != null && !value.isEmpty();
  }
}
