package me.desertfox.dgen.room;

import lombok.Getter;
import me.desertfox.dgen.chunk.DungeonChunk;
import me.desertfox.dgen.utils.Cuboid;
import org.bukkit.Location;

public abstract class AbstractRoom {

    @Getter private final DungeonChunk chunk;
    @Getter private final String schematicName;
    @Getter private final Location location;
    @Getter private final Cuboid region;

    public AbstractRoom(DungeonChunk chunk, String schematicName, Location location, Cuboid region) {
        this.chunk = chunk;
        this.schematicName = schematicName;
        this.location = location.clone();
        this.region = region;

        int relativeX = location.getBlockX() - chunk.getDungeon().getStart().getBlockX();
        int relativeZ = location.getBlockZ() - chunk.getDungeon().getStart().getBlockZ();

        int col = relativeX / chunk.getDungeon().MIN_ROOM_SIZE_XZ;
        int row = relativeZ / chunk.getDungeon().MIN_ROOM_SIZE_XZ;

        chunk.roomGrid[row][col] = this;
     }
}