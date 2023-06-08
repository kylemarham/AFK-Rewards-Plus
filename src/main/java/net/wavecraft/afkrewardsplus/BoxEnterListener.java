package net.wavecraft.afkrewardsplus;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

public class BoxEnterListener implements Listener {
    private final Plugin plugin; 
    private final Location corner1;
    private final Location corner2;

    public BoxEnterListener(Plugin plugin, Location corner1, Location corner2) {
        this.plugin = plugin;
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();

        // Check if the player is inside the bounding box
        if (isInsideBoundingBox(playerLocation)) {
            // Trigger the desired action, such as sending a message to the player
            player.sendMessage("Hello! You entered the bounding box.");
        }
    }

    private boolean isInsideBoundingBox(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
}
