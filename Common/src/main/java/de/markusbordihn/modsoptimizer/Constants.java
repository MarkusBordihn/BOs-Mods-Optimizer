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

package de.markusbordihn.modsoptimizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Constants {

  // General Mod definitions
  public static final String LOG_NAME = "Mods Optimizer";
  public static final String LOG_PREFIX = "[Mods Optimizer]";
  public static final String MOD_ID = "mods_optimizer";
  public static final String MOD_NAME = "Mods Optimizer";
  public static final String MINECRAFT_VERSION = "1.20.2";

  // Logger
  public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

  // Config Prefix
  public static final String CONFIG_ID = "adaptive_performance_tweaks";
  public static final String CONFIG_ID_PREFIX = CONFIG_ID + "/";

  private Constants() {}
}
