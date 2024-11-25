package me.desertfox.dgen.room;

import me.desertfox.dgen.Direction4;
import me.desertfox.dgen.chunk.DungeonShard;
import me.desertfox.dgen.utils.Cuboid;
import org.bukkit.Location;

import java.util.List;

public class ActiveRoom extends AbstractRoom {
    public ActiveRoom(DungeonShard chunk, String schematicName, Location location, Cuboid region, List<Direction4> doors) {
        super(chunk, schematicName, location, region, doors);
    }
}