package net.wavecraft.afkrewardsplus;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AfkLocationManager {
    private final Map<Player, Location> afkLocations;

    public AfkLocationManager() {
        this.afkLocations = new HashMap<>();
    }

    public void setPlayerLocation(Player player, Location location) {
        afkLocations.put(player, location);
    }

    public Location getPlayerLocation(Player player) {
        return afkLocations.get(player);
    }

    public void removePlayerLocation(Player player) {
        afkLocations.remove(player);
    }
}