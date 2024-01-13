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

package de.markusbordihn.modsoptimizer.service;

import de.markusbordihn.modsoptimizer.Constants;
import de.markusbordihn.modsoptimizer.config.ModsDatabaseConfig;
import de.markusbordihn.modsoptimizer.data.GameEnvironment;
import de.markusbordihn.modsoptimizer.data.ModData;
import de.markusbordihn.modsoptimizer.utils.ClientSideModsUtils;
import de.markusbordihn.modsoptimizer.utils.DuplicatedModsUtils;
import de.markusbordihn.modsoptimizer.utils.SemanticVersionUtils;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ModsOptimizerService {

  private final File gameDir;
  private final File modsDir;

  private final GameEnvironment environment;
  private final long totalStartTime;
  private GameEnvironment gameEnvironment;

  public ModsOptimizerService(File gameDir, File modsDir) {
    this(gameDir, modsDir, GameEnvironment.UNKNOWN);
  }

  public ModsOptimizerService(File gameDir, File modsDir, GameEnvironment environment) {
    this.gameDir = gameDir;
    this.modsDir = modsDir;
    this.environment = environment;
    this.gameEnvironment = environment;
    this.totalStartTime = System.nanoTime();
  }

  public ModsOptimizerService init() {
    Constants.LOG.info("{} ♻ Init ...", Constants.LOG_PREFIX);
    Constants.LOG.info("Game Directory: {}", this.gameDir);
    Constants.LOG.info("Mods Directory: {}", this.modsDir);
    Constants.LOG.info("Game Environment: {}", this.environment);

    // Record start time.
    long startTime = System.nanoTime();

    // Enable debug mode, if requested.
    if (ModsDatabaseConfig.isDebugEnabled()) {
      Constants.LOG.warn("⚠ Debug mode is enabled!");
      SemanticVersionUtils.enableDebug();
    }

    // Change environment, if requested.
    GameEnvironment gameEnvironment = this.environment;
    if (ModsDatabaseConfig.isDebugEnabled()
        && !Objects.equals(ModsDatabaseConfig.getDebugForceSide(), "default")) {
      if (Objects.equals(ModsDatabaseConfig.getDebugForceSide(), "server")) {
        Constants.LOG.info("⚠ Forced server side environment ...");
        gameEnvironment = GameEnvironment.SERVER;
      } else if (Objects.equals(ModsDatabaseConfig.getDebugForceSide(), "client")) {
        Constants.LOG.info("⚠ Forced client side environment ...");
        gameEnvironment = GameEnvironment.CLIENT;
      }
    }

    // Verify environment.
    if (gameEnvironment == GameEnvironment.UNKNOWN) {
      Constants.LOG.warn(
          "⚠ Unable to detect environment will check game dir for additional hints ...");
      File[] gameFiles = gameDir.listFiles();
      if (gameFiles == null) {
        Constants.LOG.warn("⚠ Unable to detect game files in game dir {}", gameDir);
      } else {
        for (File gameFile : gameFiles) {
          if (gameFile.getName().contains("server")) {
            Constants.LOG.info("⚠ Detected server side environment file ...");
            gameEnvironment = GameEnvironment.SERVER;
            break;
          }
        }
      }
    }

    // Set game environment for further processing.
    this.gameEnvironment = gameEnvironment;

    Constants.LOG.info(
        "♻ init with game dir {} and mods dir {} for target {} in {} ms.",
        gameDir,
        modsDir,
        gameEnvironment,
        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));

    return this;
  }

  public void enableClientSideMods() {
    if (this.gameEnvironment != GameEnvironment.CLIENT) {
      return;
    }
    long startTime = System.nanoTime();
    Constants.LOG.info("✔ Re-Enable possible client side mods ...");
    int numClientSideModsEnabled = ClientSideModsUtils.enable(modsDir);
    if (numClientSideModsEnabled > 0) {
      Constants.LOG.info(
          "✔ Re-Enabled {} client side mods in {} ms.",
          numClientSideModsEnabled,
          TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
    }
  }

  public void disableClientSideMods() {
    if (this.gameEnvironment == GameEnvironment.CLIENT) {
      Constants.LOG.info("✔ Client side mods are enabled.");
      return;
    } else if (ModData.getClientMods().isEmpty()) {
      Constants.LOG.warn("✔ No mods for client-side checks found!");
      return;
    } else if (this.gameEnvironment != GameEnvironment.SERVER) {
      Constants.LOG.warn("✔ Unknown environment {} for client-side checks!", this.gameEnvironment);
      return;
    }

    long startTime = System.nanoTime();
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
  }

  public void parseMods() {
    long startTime = System.nanoTime();
    Constants.LOG.info("♻ Parsing Mods data ...");
    ModData.parseMods(modsDir, ".jar");
    if (ModData.getKnownMods().isEmpty()) {
      Constants.LOG.error("⚠ Unable to find any mods in {}", modsDir);
      return;
    }
    Constants.LOG.info(
        "♻ Parsed {} mods in {} ms.",
        ModData.getKnownMods().size(),
        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
  }

  public void optimizeDuplicatedMods() {
    if (!ModData.getDuplicatedMods().isEmpty()) {
      long startTime = System.nanoTime();
      DuplicatedModsUtils.optimize(ModData.getDuplicatedMods());
      Constants.LOG.info(
          "♻ Optimized {} duplicated mods in {} ms.",
          ModData.getDuplicatedMods().size(),
          TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
    } else {
      Constants.LOG.info("✔ No duplicated mods found.");
    }
  }

  public long getTotalStartTime() {
    return this.totalStartTime;
  }
}
