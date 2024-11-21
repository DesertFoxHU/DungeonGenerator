package me.desertfox.dgen.room;

import me.desertfox.dgen.chunk.DungeonChunk;
import me.desertfox.dgen.utils.Cuboid;
import org.bukkit.Location;

public class ActiveRoom extends AbstractRoom {
    public ActiveRoom(DungeonChunk chunk, String schematicName, Location location, Cuboid region) {
        super(chunk, schematicName, location, region);
    }
}