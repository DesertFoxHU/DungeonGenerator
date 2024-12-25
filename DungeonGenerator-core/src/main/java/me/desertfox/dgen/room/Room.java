package me.desertfox.dgen.room;

import me.desertfox.dgen.Direction4;
import me.desertfox.dgen.shard.DungeonShard;
import me.desertfox.dgen.utils.Cuboid;
import org.bukkit.Location;

import java.util.List;

public class Room extends AbstractRoom {
    public Room(DungeonShard chunk, String schematicName, Location location, Cuboid region, List<Direction4> doors) {
        super(chunk, schematicName, location, region, doors);
    }

    @Override
    public void destroy() {

    }
}