package me.desertfox.dgen.room;

import lombok.Getter;
import me.desertfox.dgen.Direction4;
import me.desertfox.dgen.chunk.DungeonShard;
import me.desertfox.dgen.utils.Cuboid;
import org.bukkit.Location;

import java.util.List;

/**
 * Represents a generated room on the grid
 */
public abstract class AbstractRoom {

    @Getter private final DungeonShard chunk;
    @Getter private final String schematicName;
    @Getter private final Location location;
    @Getter private final Cuboid region;
    @Getter private final List<Direction4> doors;

    public AbstractRoom(DungeonShard chunk, String schematicName, Location location, Cuboid region, List<Direction4> doors) {
        this.chunk = chunk;
        this.schematicName = schematicName;
        this.location = location;
        this.region = region;
        this.doors = doors;

        int relativeX = location.getBlockX() - chunk.getDungeon().getStart().getBlockX();
        int relativeZ = location.getBlockZ() - chunk.getDungeon().getStart().getBlockZ();

        int col = relativeX / chunk.getDungeon().MIN_ROOM_SIZE_XZ;
        int row = relativeZ / chunk.getDungeon().MIN_ROOM_SIZE_XZ;

        chunk.roomGrid[row][col] = this;
    }
}