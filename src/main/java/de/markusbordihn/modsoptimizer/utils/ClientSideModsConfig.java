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

import de.markusbordihn.modsoptimizer.Constants;
import de.markusbordihn.modsoptimizer.config.ModsDatabase;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientSideModsConfig {

  protected static final String CONFIG_FILENAME = "client_side_mods.list";
  protected static final String CONFIG_FILE_VERSION_HEADER = "## Client Side Mods - Version:";
  protected static final String CONFIG_FILE_HEADER =
      "## Disable existing entries by comment out like # client-side-mod-1.18.2.jar";
  protected static final String CONFIG_FILE_TEXT = "client side mods config file";
  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  protected ClientSideModsConfig() {}

  public static boolean containsClientSideMode(String name) {
    return clientSideMods.contains(name);
  }

  public static void prepareConfigFile() {
    File configFile = getConfigFile();
    boolean shouldLoadConfigFile;
    if (configFile == null) {
      configFile = createConfigFile(configFile);
      shouldLoadConfigFile = false;
    } else {
      shouldLoadConfigFile = !updateConfigFile(configFile);
    }

    // Only re-load config file if the content was not able to already processed before.
    if (shouldLoadConfigFile) {
      loadingConfigFile(configFile);
    }
  }

  private static File createConfigFile(File file) {
    if (file == null) {
      file = getDefaultConfigFile();
    }
    if (file == null || file.exists()) {
      log.error("⚠ Unable to create {} {}!", CONFIG_FILE_TEXT, file);
      return null;
    }
    StringBuilder textContent = new StringBuilder();

    // Config File header
    textContent.append(getConfigFileVersionHeader()).append(System.lineSeparator());
    textContent.append(CONFIG_FILE_HEADER).append(System.lineSeparator());
    textContent.append(System.lineSeparator());

    // Add default mod List
    for (String clientSideMod : ModsDatabase.getClientSideModsList()) {
      textContent.append(clientSideMod).append(System.lineSeparator());
    }

    // Create config file
    try {
      log.info("Writing {} {} ...", CONFIG_FILE_TEXT, file);
      Files.writeString(file.toPath(), textContent, StandardOpenOption.CREATE_NEW);
    } catch (IOException e) {
      log.error("⚠ Unable to write {} {}, because of: {}", CONFIG_FILE_TEXT, file, e);
    }
    return file;
  }  private static Set<String> clientSideMods =
      parseClientSideMods(ModsDatabase.getClientSideModsList());

  private static boolean updateConfigFile(File file) {
    if (file == null) {
      file = getDefaultConfigFile();
    }
    if (file == null || !file.exists() || !file.canWrite() || !file.canRead()) {
      log.error("⚠ Unable to update {} {}!", CONFIG_FILE_TEXT, file);
      return false;
    }

    List<String> content = new ArrayList<>();
    try {
      content = Files.readAllLines(file.toPath());
    } catch (IOException e) {
      log.error("⚠ Unable to load {} {}, because of: {}", CONFIG_FILE_TEXT, file, e);
    }
    if (!content.isEmpty() && content.get(0).contains(getConfigFileVersionHeader())) {
      log.info("✔ No need to update {} {}", CONFIG_FILE_TEXT, file);
      parseClientSideMods(content);
      return true;
    }
    log.info("♻ Update {} {}: {}", CONFIG_FILE_TEXT, file, content);

    // Update or adding file config header
    if (!content.isEmpty()
        && (content.get(0).isEmpty() || content.get(0).contains(CONFIG_FILE_VERSION_HEADER))) {
      content.set(0, getConfigFileVersionHeader());
    } else {
      content.add(0, "");
      content.add(0, CONFIG_FILE_HEADER);
      content.add(0, getConfigFileVersionHeader());
    }

    // Update mod list
    for (String clientSideMod : ModsDatabase.getClientSideModsList()) {
      boolean skipEntry = false;
      for (String existingClientSideMod : content) {
        if (existingClientSideMod.contains(clientSideMod)) {
          skipEntry = true;
          break;
        }
      }
      if (!skipEntry) {
        log.info("- Adding missing client side mod entry: {}", clientSideMod);
        content.add(clientSideMod);
      }
    }
    parseClientSideMods(content);

    // Update config file.
    StringBuilder textContent = new StringBuilder();
    for (String contentLine : content) {
      textContent.append(contentLine).append(System.lineSeparator());
    }
    try {
      log.info("Save updated {} {} ...", CONFIG_FILE_TEXT, file);
      Files.writeString(file.toPath(), textContent);
    } catch (IOException e) {
      log.error("⚠ Unable to write {} {}, because of: {}", CONFIG_FILE_TEXT, file, e);
    }

    return true;
  }

  private static void loadingConfigFile(File file) {
    if (file == null) {
      file = getDefaultConfigFile();
    }
    if (file == null || !file.exists() || !file.canWrite() || !file.canRead()) {
      log.error("⚠ Unable to load {} {}!", CONFIG_FILE_TEXT, file);
      return;
    }
    log.info("Loading {} {} ...", CONFIG_FILE_TEXT, file);

    List<String> content = new ArrayList<>();
    try {
      content = Files.readAllLines(file.toPath());
    } catch (IOException e) {
      log.error("⚠ Unable to load {} {}, because of: {}", CONFIG_FILE_TEXT, file, e);
    }

    parseClientSideMods(content);
  }

  private static Set<String> parseClientSideMods(List<String> modsList) {
    List<String> clientSideModsList = new ArrayList<>();
    int numberOfSkippedEntries = 0;
    for (String modEntry : modsList) {
      if (!modEntry.isBlank() && !modEntry.startsWith("#")) {
        // Remove blank spaces and new lines for the final list.
        clientSideModsList.add(modEntry.replace("\n", "").replace("\r", "").trim());
      } else {
        log.debug("X Skipping mod entry {}", modEntry);
        numberOfSkippedEntries++;
      }
    }

    // Store aggregated list of client side modes, without version numbers to make them compatible
    // across versions and variants.
    log.info(
        "✔ Processing {} client sides mods with {} skipped entries ...",
        clientSideModsList.size(),
        numberOfSkippedEntries);
    clientSideMods =
        new HashSet<>(clientSideModsList.stream().map(ModsDatabase::stripeVersionNumbers).toList());
    return clientSideMods;
  }

  private static File getConfigFile() {
    File file = getDefaultConfigFile();
    if (file != null && file.exists()) {
      return file;
    }
    return null;
  }

  private static File getDefaultConfigFile() {
    Path path = getConfigDirectory();
    if (path != null) {
      return path.resolve(CONFIG_FILENAME).toFile();
    }
    return null;
  }

  private static Path getConfigDirectory() {
    Path resultPath;
    try {
      resultPath =
          FileUtils.getOrCreateDirectory(
              FMLPaths.CONFIGDIR.get().resolve(Constants.MOD_ID), Constants.MOD_ID);
    } catch (Exception exception) {
      log.error("There was an error, creating the directory:", exception);
      return null;
    }
    return resultPath;
  }

  private static String getConfigFileVersionHeader() {
    return CONFIG_FILE_VERSION_HEADER + " " + ModsDatabase.VERSION;
  }


}
