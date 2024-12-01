package me.desertfox.dgen.room;

import lombok.Getter;
import me.desertfox.dgen.Direction4;
import me.desertfox.dgen.chunk.DungeonShard;
import me.desertfox.dgen.utils.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
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

        int sizeX = region.getSizeX()+1;
        int sizeZ = region.getSizeZ();

        int gridWidth = (int) Math.ceil((double) sizeX / chunk.getDungeon().MIN_ROOM_SIZE_XZ);
        int gridHeight = (int) Math.ceil((double) sizeZ / chunk.getDungeon().MIN_ROOM_SIZE_XZ);

        int relativeX = location.getBlockX() - chunk.getStart().getBlockX();
        int relativeZ = location.getBlockZ() - chunk.getStart().getBlockZ();

        int startCol = relativeX / chunk.getDungeon().MIN_ROOM_SIZE_XZ;
        int startRow = relativeZ / chunk.getDungeon().MIN_ROOM_SIZE_XZ;

        for (int row = startRow; row < startRow + gridHeight; row++) {
            for (int col = startCol; col < startCol + gridWidth; col++) {
                if (!(col >= 0 && col < chunk.roomGrid[0].length)) {
                    Bukkit.getLogger().info("Skipping column: " + col + " (out of bounds) " + location);
                }

                if (row >= 0 && row < chunk.roomGrid.length && col >= 0 && col < chunk.roomGrid[0].length) {
                    chunk.roomGrid[row][col] = this;
                    if(getChunk().isDebug())
                    {
                        Cuboid grid = chunk.getGridCuboid(row, col);
                        grid = grid.expand(Cuboid.CuboidDirection.Up, -grid.getSizeY());
                        grid = grid.expand(Cuboid.CuboidDirection.Down, 1);
                        grid = grid.expand(Cuboid.CuboidDirection.Up, -1);
                        grid.getBlocks().forEach(b -> b.setType(Material.GREEN_WOOL));
                    }
                }
            }
        }
    }
}