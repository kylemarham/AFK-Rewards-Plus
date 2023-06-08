package net.wavecraft.afkrewardsplus;

import org.bukkit.*;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.boss.BarColor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AfkCommand implements CommandExecutor {
    private String prefix;
    private FileConfiguration config;
    private List<Location> coordinates;
    private final AfkRewardsPlus plugin;
    private final Map<String, Reward> rewards;
    private Map<Player, RewardTimer> rewardTimers = new HashMap<>();

    private CommandSender sender;
    private File configFile;

    private final AfkLocationManager afkLocationManager;

    public AfkCommand(String prefix, FileConfiguration config, Map<String, Location> coordinates, AfkRewardsPlus plugin, File configFile, AfkLocationManager afkLocationManager) {
        this.prefix = prefix;
        this.config = config;
        this.coordinates = new ArrayList<>(coordinates.values());
        this.plugin = plugin;
        this.rewards = loadRewards();
        this.configFile = configFile;
        this.afkLocationManager = afkLocationManager;

        loadConfig(config);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        AfkRewardsPlus plugin = AfkRewardsPlus.getInstance();

        this.sender = sender;

        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                if (!player.hasPermission("afkrewardsplus.afk")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                    return true;
                }

                Location originalLocation = afkLocationManager.getPlayerLocation(player);

                if (RewardTimer.isActive(player)) {
                    player.sendMessage(ChatColor.YELLOW + "You are already AFK!");
                    return true;
                } else {

                    if (config.contains("warpcoords")) {
                        double x = config.getDouble("warpcoords.x");
                        double y = config.getDouble("warpcoords.y");
                        double z = config.getDouble("warpcoords.z");
                        double pitch = config.getDouble("warpcoords.pitch");
                        double yaw = config.getDouble("warpcoords.yaw");
                        double rotation = config.getDouble("warpcoords.rotation");

                        originalLocation = player.getLocation();
                        afkLocationManager.setPlayerLocation(player, originalLocation);

                        Location warpLocation = new Location(player.getWorld(), x, y, z, (float) yaw, (float) pitch);
                        player.teleport(warpLocation);
                        createBossBars(config);

                        player.sendMessage(ChatColor.GREEN + "You have been teleported to the AFK warp location.");
                    } else {
                        player.sendMessage(ChatColor.RED + "No AFK warp location has been set.");
                    }

                }

            } else if (args.length == 1) {
                String subCommand = args[0].toLowerCase();

                if (subCommand.equals("setwarp")) {
                    Location location = player.getLocation();
                    int x = location.getBlockX();
                    int y = location.getBlockY();
                    int z = location.getBlockZ();
                    float pitch = location.getPitch();
                    float yaw = location.getYaw();
                    float rotation = (float) player.getLocation().getY();

                    config.set("warpcoords.x", x);
                    config.set("warpcoords.y", y);
                    config.set("warpcoords.z", z);
                    config.set("warpcoords.pitch", pitch);
                    config.set("warpcoords.yaw", yaw);
                    config.set("warpcoords.rotation", rotation);

                    saveConfig();

                    player.sendMessage(ChatColor.GREEN + "Warp coordinates have been set.");

                    return true;
                } else if (subCommand.equals("pos1")) {
                    Location playerLocation = player.getLocation();
                    coordinates.set(0, playerLocation);
                    saveCoordinatesToConfig(); // Save coordinates to the configuration file
                    player.sendMessage(ChatColor.GREEN + "Position 1 has been set to your current location.");
                    return true;
                } else if (subCommand.equals("pos2")) {
                    Location playerLocation = player.getLocation();
                    coordinates.set(1, playerLocation);

                    saveCoordinatesToConfig(); // Save coordinates to the configuration file
                    player.sendMessage(ChatColor.GREEN + "Position 2 has been set to your current location.");
                    return true;
                } else if (subCommand.equals("reload")) {
                    loadConfig();
                    player.sendMessage(prefix + " Config reloaded.");
                    return true;
                }
            }
        }

        return false;
    }

    private void saveCoordinatesToConfig() {
        List<String> coordinatesList = new ArrayList<>();
        for (Location location : coordinates) {
            double x = location.getX();
            double y = location.getY();
            double z = location.getZ();
            String coordinate = x + "," + y + "," + z;
            coordinatesList.add(coordinate);
        }
        config.set("afk_coordinates", coordinatesList);
        saveConfig();
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        coordinates = stringListToCoordinates(config.getStringList("coordinates"));
    }

    private List<Location> stringListToCoordinates(List<String> stringList) {
        List<Location> coordinatesList = new ArrayList<>();
        for (String coord : stringList) {
            String[] split = coord.split(",");
            double x = Double.parseDouble(split[0].trim());
            double y = Double.parseDouble(split[1].trim());
            double z = Double.parseDouble(split[2].trim());
            coordinatesList.add(new Location(Bukkit.getWorlds().get(0), x, y, z));
        }
        return coordinatesList;
    }

    private Map<String, Reward> loadRewards() {
        Map<String, Reward> rewards = new HashMap<>();
        ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");
        if (rewardsSection != null) {
            for (String rewardKey : rewardsSection.getKeys(false)) {
                ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(rewardKey);
                if (rewardSection != null) {
                    int time = rewardSection.getInt("time");
                    ChatColor color = ChatColor.valueOf(rewardSection.getString("color"));
                    String title = rewardSection.getString("title");
                    List<String> commands = rewardSection.getStringList("commands");
                    rewards.put(rewardKey, new Reward(time, color, title, commands));
                }
            }
        }
        return rewards;
    }

    public class Reward {
        private final int time;
        private final ChatColor color;
        private final String title;
        private final List<String> commands;

        public Reward(int time, ChatColor color, String title, List<String> commands) {
            this.time = time;
            this.color = color;
            this.title = title;
            this.commands = commands;
        }

        public int getTime() {
            return time;
        }

        public ChatColor getColor() {
            return color;
        }

        public String getTitle() {
            return title;
        }

        public List<String> getCommands() {
            return commands;
        }
    }

    private void loadConfig(FileConfiguration config) {
        // Load the configuration and perform necessary operations

        createBossBars(config);
        // Other method calls
    }

    private void createBossBars(FileConfiguration config) {
        ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");
        if (rewardsSection != null) {
            for (String rewardKey : rewardsSection.getKeys(false)) {
                ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(rewardKey);
                if (rewardSection != null) {
                    int time = rewardSection.getInt("time");
                    String title = rewardSection.getString("title");
                    String colorName = rewardSection.getString("color");
                    ChatColor chatColor = ChatColor.valueOf(colorName);

                    BarColor barColor;
                    try {
                        barColor = BarColor.valueOf(colorName.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        barColor = BarColor.valueOf(chatColor.name());
                    }

                    BarStyle style = BarStyle.valueOf(rewardSection.getString("style"));
                    String message = rewardSection.getString("message");

                    List<String> commands = rewardSection.getStringList("commands");

                    if (sender instanceof Player) {
                        Player player = (Player) sender;

                        BossBar bossBar = Bukkit.createBossBar(title, barColor, style);
                        bossBar.setProgress(0.0);
                        bossBar.addPlayer(player);

                        List<String> coordinatesList = config.getStringList("afk_coordinates");

                        if (coordinatesList.size() >= 2) {
                            String[] corner1Coords = coordinatesList.get(0).split(",");
                            String[] corner2Coords = coordinatesList.get(1).split(",");

                            if (corner1Coords.length == 3 && corner2Coords.length == 3) {
                                double x1 = Double.parseDouble(corner1Coords[0].trim());
                                double y1 = Double.parseDouble(corner1Coords[1].trim());
                                double z1 = Double.parseDouble(corner1Coords[2].trim());
                                Location corner1 = new Location(player.getWorld(), x1, y1, z1);

                                double x2 = Double.parseDouble(corner2Coords[0].trim());
                                double y2 = Double.parseDouble(corner2Coords[1].trim());
                                double z2 = Double.parseDouble(corner2Coords[2].trim());
                                Location corner2 = new Location(player.getWorld(), x2, y2, z2);

                                RewardTimer rewardTimer = new RewardTimer(player, bossBar, time, commands, corner1, corner2, message, afkLocationManager);
                                rewardTimer.start();
                                rewardTimers.put(player, rewardTimer);
                            }
                        }
                    }
                }
            }
        }
    }
}
