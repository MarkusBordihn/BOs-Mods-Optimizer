package de.markusbordihn.modsoptimizer.platform;

import de.markusbordihn.modsoptimizer.Constants;
import de.markusbordihn.modsoptimizer.platform.services.IPlatformHelper;
import java.util.ServiceLoader;

public class Services {
  public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

  protected Services() {}

  public static <T> T load(Class<T> clazz) {

    final T loadedService =
        ServiceLoader.load(clazz)
            .findFirst()
            .orElseThrow(
                () -> new NullPointerException("Failed to load service for " + clazz.getName()));
    Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
    return loadedService;
  }
}
