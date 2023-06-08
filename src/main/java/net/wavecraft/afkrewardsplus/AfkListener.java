package net.wavecraft.afkrewardsplus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;

public class AfkListener implements Listener {
    private final AfkRewardsPlus plugin;

    public AfkListener(AfkRewardsPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();

        for (RewardTimer timer : plugin.getPlayerTimers().getOrDefault(player.getUniqueId(), new ArrayList<>())) {
            if (timer.isInsideBounds(location)) {
                timer.start();
            } else {
                timer.stop();
            }
        }
    }
}
