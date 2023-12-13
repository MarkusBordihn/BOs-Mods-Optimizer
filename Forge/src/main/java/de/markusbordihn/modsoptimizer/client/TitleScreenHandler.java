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

package de.markusbordihn.modsoptimizer.client;

import de.markusbordihn.modsoptimizer.Constants;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;

public class TitleScreenHandler {

  private static boolean showed = false;

  protected TitleScreenHandler() {}

  public static void handleInitScreenEvent(ScreenEvent.InitScreenEvent.Post event) {
    if (event.getScreen() instanceof TitleScreen && !showed) {
      Constants.LOG.debug("Found title screen for InitScreenEvent: {}", event.getScreen());
      logMessage();
    }
  }

  public static void handleScreenOpenEvent(ScreenOpenEvent event) {
    if (event.getScreen() instanceof TitleScreen && !showed) {
      Constants.LOG.debug("Found title screen for ScreenOpenEvent: {}", event.getScreen());
      logMessage();
    }
  }

  public static void logMessage() {
    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    Constants.LOG.info(
        "{} ⏲ Client took about {} sec to start ...",
        Constants.LOG_PREFIX,
        (System.currentTimeMillis() - runtimeMXBean.getStartTime()) / 1000f);
    showed = true;
  }
}
