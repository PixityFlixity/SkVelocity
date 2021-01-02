package tk.zulfengaming.bungeesk.bungeecord.config;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import tk.zulfengaming.bungeesk.bungeecord.BungeeSkProxy;

import java.io.File;
import java.io.IOException;

public class YamlConfig {

    public BungeeSkProxy instance;

    public File configFile;

    public String configPath;

    private ConfigurationProvider configObject;

    private Configuration loadedConfig;

    public YamlConfig(BungeeSkProxy instanceIn) {
        this.instance = instanceIn;

        this.configPath = instance.getDataFolder() + File.separator;

        this.configFile = new File(configPath + "config.yml");


        instance.log("Loading config into "+ configFile.getPath() + "...");

        try {
            if (!instance.getDataFolder().exists()) {
                instance.getDataFolder().mkdirs();
            }
            if (!configFile.exists()) //noinspection ResultOfMethodCallIgnored
                configFile.createNewFile();

            this.configObject = ConfigurationProvider.getProvider(YamlConfiguration.class);

            this.loadedConfig = configObject.load(configFile);

        } catch (IOException e) {
            instance.error("There was an error getting the config!");

            e.printStackTrace();
        }

    }

    public void save(String node, String value) throws IOException {
        loadedConfig.set(node, value);
        configObject.save(loadedConfig, configFile);

    }

    public String getString(String node) {
        return loadedConfig.getString(node);
    }

    public boolean getBoolean(String node) {
        return loadedConfig.getBoolean(node);
    }

    public int getInt(String node) {
        return loadedConfig.getInt(node);
    }
}
