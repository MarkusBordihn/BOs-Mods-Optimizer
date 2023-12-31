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
import de.markusbordihn.modsoptimizer.config.ModsDatabaseConfig;
import de.markusbordihn.modsoptimizer.data.ModData;
import de.markusbordihn.modsoptimizer.utils.ClientSideModsUtils;
import de.markusbordihn.modsoptimizer.utils.DuplicatedModsUtils;
import de.markusbordihn.modsoptimizer.utils.SemanticVersionUtils;
import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;

public class ModLocatorService implements IModLocator {

  private static final File GAME_DIR = FMLPaths.GAMEDIR.get().toFile();
  private static final File MODS_DIR = FMLPaths.MODSDIR.get().toFile();

  public ModLocatorService() {
    long totalStartTime = System.nanoTime();
    long startTime = System.nanoTime();

    // Detect environment.
    Environment environment = Launcher.INSTANCE.environment();
    Optional<String> launchTarget = environment.getProperty(IEnvironment.Keys.LAUNCHTARGET.get());
    boolean isClient = true;
    if (ModsDatabaseConfig.isDebugEnabled()) {
      Constants.LOG.warn("⚠ Debug mode is enabled!");
      SemanticVersionUtils.enableDebug();
    }
    if (ModsDatabaseConfig.isDebugEnabled()
        && !Objects.equals(ModsDatabaseConfig.getDebugForceSide(), "default")) {
      if (Objects.equals(ModsDatabaseConfig.getDebugForceSide(), "server")) {
        Constants.LOG.info("⚠ Forced server side environment ...");
        isClient = false;
      } else if (Objects.equals(ModsDatabaseConfig.getDebugForceSide(), "client")) {
        Constants.LOG.info("⚠ Forced client side environment ...");
      }
    } else {
      if (launchTarget.isPresent()) {
        if (launchTarget.get().contains("server")) {
          isClient = false;
        }
      } else {
        Constants.LOG.warn(
            "⚠ Unable to detect environment will check game dir for additional hints ...");
        File[] gameFiles = GAME_DIR.listFiles();
        if (gameFiles == null) {
          Constants.LOG.warn("⚠ Unable to detect game files in game dir {}", GAME_DIR);
          return;
        }
        for (File gameFile : gameFiles) {
          if (gameFile.getName().contains("server")) {
            isClient = false;
            break;
          }
        }
      }
    }
    Constants.LOG.info(
        "♻ init with game dir {} and mods dir {} for target {} in {} ms.",
        GAME_DIR,
        MODS_DIR,
        isClient ? "CLIENT" : "SERVER",
        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));

    // Re-enable client side mods on client.
    if (isClient) {
      startTime = System.nanoTime();
      Constants.LOG.info("✔ Re-Enable possible client side mods ...");
      int numClientSideModsEnabled = ClientSideModsUtils.enable(MODS_DIR);
      if (numClientSideModsEnabled > 0) {
        Constants.LOG.info(
            "✔ Re-Enabled {} client side mods in {} ms.",
            numClientSideModsEnabled,
            TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
      }
    }

    // Parsing mods data.
    startTime = System.nanoTime();
    Constants.LOG.info("♻ Parsing Mods data ...");
    ModData.parseMods(MODS_DIR, ".jar");
    if (ModData.getKnownMods().isEmpty()) {
      Constants.LOG.error("⚠ Unable to find any mods in {}", MODS_DIR);
      return;
    }
    Constants.LOG.info(
        "♻ Parsed {} mods in {} ms.",
        ModData.getKnownMods().size(),
        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));

    // Check for duplicated mods.
    if (!ModData.getDuplicatedMods().isEmpty()) {
      startTime = System.nanoTime();
      DuplicatedModsUtils.optimize(ModData.getDuplicatedMods());
      Constants.LOG.info(
          "♻ Optimized {} duplicated mods in {} ms.",
          ModData.getDuplicatedMods().size(),
          TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
    } else {
      Constants.LOG.info("✔ No duplicated mods found.");
    }

    // Disable client side mods on ded-server.
    if (!isClient && !ModData.getClientMods().isEmpty()) {
      startTime = System.nanoTime();
      Constants.LOG.info(
          "❌ Disable possible {} client side mods ...", ModData.getClientMods().size());
      int numClientSideModsDisabled = ClientSideModsUtils.disable(ModData.getClientMods());
      if (numClientSideModsDisabled > 0) {
        Constants.LOG.info(
            "❌ Disabled {} client side mods in {} ms.",
            numClientSideModsDisabled,
            TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
      } else {
        Constants.LOG.info("❌ Unable to disable any client side mods.");
      }
    } else if (!isClient) {
      Constants.LOG.info("✔ No client side mods found.");
    } else {
      Constants.LOG.info("✔ Client side mods are enabled.");
    }

    Constants.LOG.info(
        "⏱ Mod Optimizer needs {} ms in total.",
        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - totalStartTime));
  }

  @Override
  public List<IModFile> scanMods() {
    Constants.LOG.debug("scanMods");
    return Collections.emptyList();
  }

  @Override
  public String name() {
    return "adaptive_performance_tweaks_mod_optimizer";
  }

  @Override
  public void scanFile(IModFile modFile, Consumer<Path> pathConsumer) {
    Constants.LOG.debug("scanFile {} {}", modFile, pathConsumer);
  }

  @Override
  public void initArguments(Map<String, ?> arguments) {
    Constants.LOG.debug("initArguments: {}", arguments);
  }

  @Override
  public boolean isValid(IModFile modFile) {
    Constants.LOG.debug("isValid {}", modFile);
    return false;
  }
}
