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

import de.markusbordihn.modsoptimizer.Constants;
import de.markusbordihn.modsoptimizer.data.GameEnvironment;
import de.markusbordihn.modsoptimizer.service.ModsOptimizerService;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModLocatorService {

  protected static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  public ModLocatorService() {
    log.info("ModLocatorService");
  }

  public void init() {

    // Detect game environment.
    FabricLoaderImpl fabricLoader = FabricLoaderImpl.INSTANCE;
    if (fabricLoader == null) {
      log.error("Fabric Loader is not available!");
      return;
    }
    EnvType envType = FabricLoaderImpl.INSTANCE.getEnvironmentType();
    GameEnvironment gameEnvironment = GameEnvironment.UNKNOWN;
    if (envType == EnvType.SERVER) {
      gameEnvironment = GameEnvironment.SERVER;
    } else if (envType == EnvType.CLIENT) {
      gameEnvironment = GameEnvironment.CLIENT;
    }

    // Setup and initialized Mods Optimizer Service.
    ModsOptimizerService modsOptimizer =
        new ModsOptimizerService(
                fabricLoader.getGameDir().toFile(),
                fabricLoader.getModsDirectory(),
                gameEnvironment)
            .init();

    // Re-enable client side mods on client.
    modsOptimizer.enableClientSideMods();

    // Parsing mods data.
    modsOptimizer.parseMods();

    // Check for duplicated mods.
    modsOptimizer.optimizeDuplicatedMods();

    // Disable client side mods on ded-server.
    modsOptimizer.disableClientSideMods();

    // Record total time.
    Constants.LOG.info(
        "⏱ Mod Optimizer needs {} ms in total.",
        TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - modsOptimizer.getTotalStartTime()));
  }
}
