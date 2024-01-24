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
import de.markusbordihn.modsoptimizer.data.GameEnvironment;
import de.markusbordihn.modsoptimizer.service.ModsOptimizerService;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IModLocator;

public class ModLocatorService implements IModLocator {

  public ModLocatorService() {

    // Detect game environment.
    Environment environment = Launcher.INSTANCE.environment();
    Optional<String> launchTarget = environment.getProperty(IEnvironment.Keys.LAUNCHTARGET.get());

    // Setup and initialized Mods Optimizer Service.
    ModsOptimizerService modsOptimizer =
        new ModsOptimizerService(
                FMLPaths.GAMEDIR.get().toFile(),
                FMLPaths.MODSDIR.get().toFile(),
                launchTarget.isPresent() && launchTarget.get().contains("server")
                    ? GameEnvironment.SERVER
                    : GameEnvironment.CLIENT)
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

  @Override
  public List<ModFileOrException> scanMods() {
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
