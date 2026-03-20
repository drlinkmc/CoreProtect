package net.coreprotect.listener.entity;

import net.coreprotect.bukkit.BukkitAdapter;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;
import net.coreprotect.listener.block.BlockExplodeListener;

public final class EntityExplodeListener extends Queue implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    protected void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();

        if (!BukkitAdapter.ADAPTER.shouldLogExplosion(event)) {
            return;
        }

        // Somehow windburst enchanted maces trigger
        // ExplosionResult.DESTROY_WITH_DECAY instead of ExplosionResult.TRIGGER_BLOCK
        if (entity instanceof Player){
            return;
        }

        World world = event.getLocation().getWorld();
        String user = "#explosion";

        if (entity instanceof TNTPrimed) {
            user = "#tnt";
        }
        else if (entity instanceof Minecart) {
            String name = entity.getType().name();
            if (name.contains("TNT")) {
                user = "#tnt";
            }
        }
        else if (entity instanceof Creeper) {
            user = "#creeper";
        }
        else if (entity instanceof EnderDragon || entity instanceof EnderDragonPart) {
            user = "#enderdragon";
        }
        else if (entity instanceof Wither || entity instanceof WitherSkull) {
            user = "#wither";
        }
        else if (entity instanceof EnderCrystal) {
            user = "#end_crystal";
        }

        boolean log = false;
        if (Config.getConfig(world).EXPLOSIONS) {
            log = true;
        }

        if ((user.equals("#enderdragon") || user.equals("#wither")) && !Config.getConfig(world).ENTITY_CHANGE) {
            log = false;
        }

        if (!event.isCancelled() && log) {
            BlockExplodeListener.processBlockExplode(user, world, event.blockList());
        }
    }
}
