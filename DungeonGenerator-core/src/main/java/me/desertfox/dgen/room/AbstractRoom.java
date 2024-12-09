package me.desertfox.dgen.room;

import lombok.Getter;
import me.desertfox.dgen.Direction4;
import me.desertfox.dgen.chunk.DungeonShard;
import me.desertfox.dgen.schematic.OperationalSchematic;
import me.desertfox.dgen.schematic.framework.SchematicController;
import me.desertfox.dgen.utils.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Represents a generated room on the grid
 */
public abstract class AbstractRoom {

    @Getter private final DungeonShard shard;
    @Getter private final String schematicName;
    @Getter private final Location location;
    @Getter private final Cuboid region;
    @Getter private final List<Direction4> doors;

    public AbstractRoom(DungeonShard shard, String schematicName, Location location, Cuboid region, List<Direction4> doors) {
        this.shard = shard;
        this.schematicName = schematicName;
        this.location = location;
        this.region = region;
        this.doors = doors;

        int sizeX = region.getSizeX()+1;
        int sizeZ = region.getSizeZ();

        int gridWidth = (int) Math.ceil((double) sizeX / shard.getDungeon().MIN_ROOM_SIZE_XZ);
        int gridHeight = (int) Math.ceil((double) sizeZ / shard.getDungeon().MIN_ROOM_SIZE_XZ);

        int relativeX = location.getBlockX() - shard.getStart().getBlockX();
        int relativeZ = location.getBlockZ() - shard.getStart().getBlockZ();

        int startCol = relativeX / shard.getDungeon().MIN_ROOM_SIZE_XZ;
        int startRow = relativeZ / shard.getDungeon().MIN_ROOM_SIZE_XZ;

        for (int row = startRow; row < startRow + gridHeight; row++) {
            for (int col = startCol; col < startCol + gridWidth; col++) {
                if (!(col >= 0 && col < shard.roomGrid[0].length)) {
                    Bukkit.getLogger().info("Skipping column: " + col + " (out of bounds) " + location);
                }

                if (row >= 0 && row < shard.roomGrid.length && col >= 0 && col < shard.roomGrid[0].length) {
                    shard.roomGrid[row][col] = this;
                    if(getShard().isDebug())
                    {
                        Cuboid grid = shard.getGridCuboid(row, col);
                        grid = grid.expand(Cuboid.CuboidDirection.Up, -grid.getSizeY());
                        grid = grid.expand(Cuboid.CuboidDirection.Down, 1);
                        grid = grid.expand(Cuboid.CuboidDirection.Up, -1);
                        grid.getBlocks().forEach(b -> b.setType(Material.GREEN_WOOL));
                    }
                }
            }
        }
    }

    public AbstractRoom placeDown(){
        OperationalSchematic schematic = SchematicController.get(schematicName);
        schematic.populate(location, new Vector(0,0,0));
        return this;
    }
}