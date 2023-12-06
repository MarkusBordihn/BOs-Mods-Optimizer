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

package de.markusbordihn.modsoptimizer.services;

import cpw.mods.modlauncher.Environment;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import de.markusbordihn.modsoptimizer.Constants;
import de.markusbordihn.modsoptimizer.utils.ClientSideMods;
import de.markusbordihn.modsoptimizer.utils.ClientSideModsConfig;
import de.markusbordihn.modsoptimizer.utils.DuplicatedMods;
import de.markusbordihn.modsoptimizer.utils.ModData;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModLocatorService implements IModLocator {

  protected static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final File GAME_DIR = FMLPaths.GAMEDIR.get().toFile();
  private static final File MODS_DIR = FMLPaths.MODSDIR.get().toFile();

  public ModLocatorService() {
    ClientSideModsConfig.prepareConfigFile();

    Environment environment = Launcher.INSTANCE.environment();
    Optional<String> launchTarget = environment.getProperty(IEnvironment.Keys.LAUNCHTARGET.get());
    boolean isClient = true;
    if (launchTarget.isPresent()) {
      if (launchTarget.get().contains("server")) {
        isClient = false;
      }
    } else {
      log.warn(
          "{} ⚠ Unable to detect environment will check game dir for additional hints ...",
          Constants.LOG_PREFIX);
      File[] gameFiles = GAME_DIR.listFiles();
      if (gameFiles == null) {
        log.warn("{} ⚠ Unable to detect game files in game dir {}", Constants.LOG_PREFIX, GAME_DIR);
        return;
      }
      for (File gameFile : gameFiles) {
        if (gameFile.getName().contains("server")) {
          isClient = false;
          break;
        }
      }
    }

    log.info(
        "{} ♻ init with game dir {} and mods dir {} for target {}",
        Constants.LOG_PREFIX,
        GAME_DIR,
        MODS_DIR,
        isClient ? "CLIENT" : "SERVER");

    log.info("{} ♻ Collecting Mods data ...", Constants.LOG_PREFIX);
    ModData.parseMods(MODS_DIR, ".jar");

    log.info("{} ♻ Optimizing Duplicated Mods ...", Constants.LOG_PREFIX);
    long start = System.nanoTime();
    int numDuplicatedMods = DuplicatedMods.searchDuplicatedMods(MODS_DIR);
    if (numDuplicatedMods > 0) {
      log.info(
          "{} Removed {} duplicated mods in {} ms.",
          Constants.LOG_PREFIX,
          numDuplicatedMods,
          TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
    }

    if (isClient) {
      start = System.nanoTime();
      log.info("{} ✔ Re-Enable possible client side mods ...", Constants.LOG_PREFIX);
      int numClientSideModsEnabled = ClientSideMods.enable(MODS_DIR);
      if (numClientSideModsEnabled > 0) {
        log.info(
            "{} ✔ Re-Enabled {} possible client side mods in {} ms.",
            Constants.LOG_PREFIX,
            numClientSideModsEnabled,
            TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
      }
    } else {
      start = System.nanoTime();
      log.info("{} X Disable possible client side mods ...", Constants.LOG_PREFIX);
      int numClientSideModsDisabled = ClientSideMods.disable(MODS_DIR);
      if (numClientSideModsDisabled > 0) {
        DuplicatedMods.searchDuplicatedClientMods(MODS_DIR);
        log.info(
            "{} X Disabled {} possible client side mods in {} ms.",
            Constants.LOG_PREFIX,
            numClientSideModsDisabled,
            TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
      }
    }
  }

  @Override
  public List<IModFile> scanMods() {
    log.debug("scanMods");
    return Collections.emptyList();
  }

  @Override
  public String name() {
    return "adaptive_performance_tweaks_mod_optimizer";
  }

  @Override
  public void scanFile(IModFile modFile, Consumer<Path> pathConsumer) {
    log.debug("scanFile {} {}", modFile, pathConsumer);
  }

  @Override
  public void initArguments(Map<String, ?> arguments) {
    log.debug("initArguments: {}", arguments);
  }

  @Override
  public boolean isValid(IModFile modFile) {
    log.debug("isValid {}", modFile);
    return false;
  }
}
