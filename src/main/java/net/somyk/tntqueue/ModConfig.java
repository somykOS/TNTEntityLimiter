package net.somyk.tntqueue;

import net.fabricmc.loader.api.FabricLoader;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.file.Path;

import static net.somyk.tntqueue.TntQueue.*;

public class ModConfig {

  public static final String maxPrimedTntAmount = "maxPrimedTntAmount";
  public static final String maxQueueSize = "maxQueueSize";
  private static final Path CONFIG_PATH =
      FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".yml").toFile().toPath();
  private static final YamlFile config = new YamlFile(CONFIG_PATH.toString());

  static {
    try {
      initializeConfig();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void load() {}

  private static void initializeConfig() throws IOException {
    if (!config.exists()) {
      config.createNewFile();
      LOGGER.info("Config has been created: {}", CONFIG_PATH);
    } else {
      LOGGER.info("Loaded {}", CONFIG_PATH);
    }
    config.loadWithComments();
    config.addDefault(maxPrimedTntAmount, 3);
    config.addDefault(maxQueueSize, 100);
    config.save();
  }

  public static int getIntegerValue(String key) {
    return getConfig().getInt(key);
  }

  public static void setValue(String key, Object newValue) {
    YamlFile localConfig = getConfig();
    localConfig.set(key, newValue);
    saveConfig(localConfig);
  }

  private static YamlFile getConfig() {
    try {
      config.loadWithComments();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    return config;
  }

  private static void saveConfig(YamlFile localConfig) {
    try {
      localConfig.save();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }
}
