package me.desertfox.dgen.room;

import lombok.Getter;
import me.desertfox.dgen.Direction4;
import me.desertfox.dgen.shard.DungeonShard;
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

        gridWidth = (int) Math.ceil((double) sizeZ / shard.getDungeon().MIN_ROOM_SIZE_XZ);
        gridHeight = (int) Math.ceil((double) sizeX / shard.getDungeon().MIN_ROOM_SIZE_XZ);

        int relativeX = location.getBlockX() - shard.getStart().getBlockX();
        int relativeZ = location.getBlockZ() - shard.getStart().getBlockZ();

        startRow = relativeX / shard.getDungeon().MIN_ROOM_SIZE_XZ;
        startCol = relativeZ / shard.getDungeon().MIN_ROOM_SIZE_XZ;

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

    /**
     * Returns the array of grid indexes this room have<br>
     * The elements (int[]) first number is the row the second is the column for the grid<br>
     * <pre>
     *     List&lt;int[]&gt; indexes = room.getGrindIndexes();
     *     for(int[] index : indexes){
     *         AbstractRoom roomHere = room.getShard().roomGrid[index[0]][index[1]];
     *         if(roomHere.equals(room)){ //Always true
     *             Bukkit.getLogger().info("true");
     *         }
     *     }
     * </pre>
     *
     * Examples:<br>
     * - Output: [12,3]
     * - If the RoomSchematic's size is 8x8 and this room is 16x16 big<br>
     *   Output: [0,0] [0,1] [1,1] [0,1]<br>
     * @return array of grid's indexes
     */
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

    /**
     * Returns the relative row and col index's grid index<br>
     * Example:<br>
     * - This room is aligned to 8x8, but it's size is 16x16, so it has 4 grid index<br>
     *   [0,0] [0,1] [1,1] [0,1]<br>
     *   This room placed on the [3,0] grid index<br>
     *   Input: [0,1] Output: [3,1]<br>
     *
     * <pre>
     *     int[] index = getIndexOfPiece(1,1);
     *     Cuboid pieceRegion = getShard().getGridCuboid(index[0], index[1]);
     * </pre>
     *
     * @param relX
     * @param relZ
     * @return
     */
    public int[] getIndexOfPiece(int relX, int relZ){
        if (relX < 0 || relZ < 0) {
            throw new IllegalArgumentException("relX and relZ cannot be negative.");
        }

        int row = startRow + relX;
        int col = startCol + relZ;
        return new int[]{row, col};
    }

    /**
     * Physically place down the room
     * @return This instance
     */
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

    public abstract void destroy();
}