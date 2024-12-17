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
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
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
    @Getter private final List<String> activeVariantGroups = new ArrayList<>();

    @Getter private final int gridWidth;
    @Getter private final int gridHeight;
    @Getter private final int startCol;
    @Getter private final int startRow;

    public AbstractRoom(DungeonShard shard, String schematicName, Location location, Cuboid region, List<Direction4> doors) {
        this.shard = shard;
        this.schematicName = schematicName;
        this.location = location;
        this.region = region;
        this.doors = doors;

        int sizeX = region.getSizeX()+1;
        int sizeZ = region.getSizeZ();

        gridWidth = (int) Math.ceil((double) sizeX / shard.getDungeon().MIN_ROOM_SIZE_XZ);
        gridHeight = (int) Math.ceil((double) sizeZ / shard.getDungeon().MIN_ROOM_SIZE_XZ);

        int relativeX = location.getBlockX() - shard.getStart().getBlockX();
        int relativeZ = location.getBlockZ() - shard.getStart().getBlockZ();

        startCol = relativeX / shard.getDungeon().MIN_ROOM_SIZE_XZ;
        startRow = relativeZ / shard.getDungeon().MIN_ROOM_SIZE_XZ;

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

    public List<int[]> getGridIndexes(){
        List<int[]> indexes = new ArrayList<>();
        for (int row = startRow; row < startRow + gridHeight; row++) {
            for (int col = startCol; col < startCol + gridWidth; col++) {
                if (!(col >= 0 && col < shard.roomGrid[0].length)) {
                    Bukkit.getLogger().info("Skipping column: " + col + " (out of bounds) " + location);
                }

                if (row >= 0 && row < shard.roomGrid.length && col >= 0 && col < shard.roomGrid[0].length) {
                    indexes.add(new int[] {row, col});
                }
            }
        }
        return indexes;
    }

    public int[] getIndexOfPiece(int relX, int relZ){
        if (relX < 0 || relZ < 0) {
            throw new IllegalArgumentException("relX and relZ cannot be negative.");
        }

        int row = startRow + relZ;
        int col = startCol + relX;
        return new int[]{row, col};
    }

    public AbstractRoom placeDown(){
        OperationalSchematic schematic = SchematicController.get(schematicName);
        schematic.populate(location, new Vector(0,0,0));
        return this;
    }

    public List<Block> placeVariant(String variantGroup){
        if(!SchematicController.get(schematicName).getVariantGroups().containsKey(variantGroup)){
            Bukkit.getLogger().info("§4There is no variantGroup named " + variantGroup + " for " + getSchematicName());
            return null;
        }
        OperationalSchematic schematic = SchematicController.get(schematicName);
        List<Block> blocks = schematic.populateVariantGroup(variantGroup, location, new Vector(0,0,0), false);
        activeVariantGroups.add(variantGroup);
        return blocks;
    }
}