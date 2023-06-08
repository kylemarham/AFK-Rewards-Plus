package net.wavecraft.afkrewardsplus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AfkRewardsPlus extends JavaPlugin {
    private static AfkRewardsPlus instance;
    private Map<Location, Material> outlineBlocks;
    private Map<UUID, List<RewardTimer>> playerTimers;

    // Retrieve the coordinates map
    private FileConfiguration config;
    private File configFile;
    private String prefix;
    private AfkLocationManager afkLocationManager;

    @Override
    public void onEnable() {
        instance = this;
        outlineBlocks = new HashMap<>();
        playerTimers = new HashMap<>();

        // Load the configuration file
        reloadConfig();
        config = getConfig(); // Assign the plugin's configuration to the config object

        // Register the event listener
        getServer().getPluginManager().registerEvents(new AfkListener(this), this);

        getLogger().info("AfkRewardsPlus has been enabled!");

        afkLocationManager = new AfkLocationManager();

        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }

        prefix = config.getString("prefix");

        Map<String, Location> coordinates = loadCoordinates(config);
        AfkCommand afkCommand = new AfkCommand(prefix, config, coordinates, this, configFile, afkLocationManager);
        getCommand("afk").setExecutor(afkCommand);
    }

    @Override
    public void onDisable() {
        // Stop all active timers when the plugin is disabled
        for (List<RewardTimer> timers : playerTimers.values()) {
            for (RewardTimer timer : timers) {
                timer.stop();
            }
        }

        // Save the config file on plugin disable
        saveAfkConfig();

        getLogger().info("AfkRewardsPlus has been disabled!");
    }

    private void saveAfkConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AfkRewardsPlus getInstance() {
        return instance;
    }

    private Map<String, Location> loadCoordinates(FileConfiguration config) {
        Map<String, Location> coordinates = new HashMap<>();
        List<String> coordinatesList = config.getStringList("afk_coordinates");

        for (int i = 0; i < coordinatesList.size(); i++) {
            String coordinateString = coordinatesList.get(i);
            String[] coordinateValues = coordinateString.split(",");

            if (coordinateValues.length == 3) {
                double x = Double.parseDouble(coordinateValues[0].trim());
                double y = Double.parseDouble(coordinateValues[1].trim());
                double z = Double.parseDouble(coordinateValues[2].trim());

                Location location = new Location(getServer().getWorlds().get(0), x, y, z);
                coordinates.put("corner" + (i + 1), location);
            }
        }

        return coordinates;
    }

    public Map<Location, Material> getOutlineBlocks() {
        return outlineBlocks;
    }

    public Map<UUID, List<RewardTimer>> getPlayerTimers() {
        return playerTimers;
    }
}
