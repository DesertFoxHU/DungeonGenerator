package me.desertfox.dgen;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class DefaultDungeon extends AbstractDungeon {
    protected DefaultDungeon(JavaPlugin plugin, String id, Location start, Location end, Integer SHARD_SIZE_X, Integer SHARD_SIZE_Z, Integer MIN_ROOM_SIZE_XZ) {
        super(plugin, id, start, end, SHARD_SIZE_X, SHARD_SIZE_Z, MIN_ROOM_SIZE_XZ);
    }
}