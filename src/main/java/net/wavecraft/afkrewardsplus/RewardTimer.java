package net.wavecraft.afkrewardsplus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardTimer extends BukkitRunnable {
    static final Map<Player, RewardTimer> activeTimers = new HashMap<>();

    private final Player player;
    private final BossBar bossBar;
    private final int time;
    private final List<String> commands;
    private final Location corner1;
    private final Location corner2;
    private int currentTime;
    BukkitTask timerTask;
    private final String message;
    private static AfkLocationManager afkLocationManager;

    RewardTimer(Player player, BossBar bossBar, int time, List<String> commands, Location corner1, Location corner2, String message, AfkLocationManager afkLocationManager) {
        this.player = player;
        this.bossBar = bossBar;
        this.time = time;
        this.commands = commands;
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.currentTime = 0;
        this.timerTask = null;
        this.message = message;
        this.afkLocationManager = afkLocationManager;
    }

    public static RewardTimer create(Player player, BossBar bossBar, int time, List<String> commands, Location corner1, Location corner2, String message) {
        if (isActive(player)) {
            player.sendMessage("You are already AFK!");
            return null;
        } else {
            RewardTimer rewardTimer = new RewardTimer(player, bossBar, time, commands, corner1, corner2, message, afkLocationManager);
            activeTimers.put(player, rewardTimer);
            rewardTimer.start();
            return rewardTimer;
        }
    }

    public static boolean isActive(Player player) {
        return activeTimers.containsKey(player);
    }

    public static List getTimer(Player player) {
        return (List) activeTimers.get(player);
    }

    public void start() {
        bossBar.setVisible(true);
        bossBar.setProgress(0);
        timerTask = runTaskTimer(AfkRewardsPlus.getInstance(), 0, 20);
    }

    public void stop() {
        bossBar.setVisible(false);
        cancel();
        activeTimers.remove(player);
    }

    @Override
    public void run() {
        currentTime++;

        if (!player.isOnline()) {
            stop();
            return;
        }

        if (isInsideBounds(player.getLocation())) {
            // Player is still inside the bounds
            if (currentTime >= time) {
                executeCommands();
                currentTime = 0;
            } else {
                float progress = (float) currentTime / time;
                bossBar.setProgress(progress);
            }
        } else {
            // Player has left the bounds
            removePlayerLocation(player);
            stop();
        }
    }

    private void removePlayerLocation(Player player) {
        afkLocationManager.removePlayerLocation(player);
    }

    boolean isInsideBounds(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        double minX = Math.min(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());

        double maxX = Math.max(corner1.getX(), corner2.getX());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }

    private void executeCommands() {
        for (String command : commands) {
            String formattedCommand = command.replace("@p", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);
        }
        player.sendMessage(message); // Display the reward message to the player
    }
}
