package me.desertfox.dgen.shard;

import lombok.Getter;

import lombok.Setter;
import me.desertfox.dgen.AbstractDungeon;
import me.desertfox.dgen.Direction4;
import me.desertfox.dgen.room.AbstractRoom;
import me.desertfox.gl.region.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Represents a physical chunk for Dungeons
 */
@Getter
public class DungeonShard {
    private final AbstractDungeon dungeon;
    private final int indexX;
    private final int indexY;
    private final int coordX;
    private final int coordY;
    private final int coordZ;
    private final int endX;
    private final int endY;
    private final int endZ;
    private final Cuboid region;
    @Setter private boolean debug;
    public AbstractRoom[][] roomGrid;
    public ShardGenerator generator;

    public DungeonShard(AbstractDungeon dungeon, Class<? extends ShardGenerator> generatorClass, int indexX, int indexY, int coordX, int coordY, int coordZ, int endX, int endY, int endZ) {
        this.dungeon = dungeon;
        this.indexX = indexX;
        this.indexY = indexY;
        this.coordX = coordX;
        this.coordY = coordY;
        this.coordZ = coordZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        region = new Cuboid(getStart(), getEnd());

        int rows = region.getSizeX() / getDungeon().MIN_ROOM_SIZE_XZ;
        int cols = region.getSizeZ() / getDungeon().MIN_ROOM_SIZE_XZ;

        roomGrid = new AbstractRoom[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                roomGrid[row][col] = null;
            }
        }

        setGenerator(generatorClass);
    }

    /**
     * Returns a location's grid index<br>
     * Only has two element or null<br>
     * <pre>
     *     int[] index = getGridIndex(...);
     *     int row = index[0];
     *     int col = index[1];
     * </pre>
     * @param location Location to check
     * @return Two element array or null
     */
    public int[] getGridIndex(Location location){
        if(!isOnGrid(location)) return null;

        int relativeX = location.getBlockX() - getStart().getBlockX();
        int relativeZ = location.getBlockZ() - getStart().getBlockZ();

        int row = relativeX / getDungeon().MIN_ROOM_SIZE_XZ;
        int col = relativeZ / getDungeon().MIN_ROOM_SIZE_XZ;

        return new int[] {row, col};
    }

    /**
     * Sets a new generator but doesn't start the generation process
     * @param generatorClass Generator
     */
    public void setGenerator(Class<? extends ShardGenerator> generatorClass) {
        try {
            this.generator = generatorClass.getConstructor(DungeonShard.class).newInstance(this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Location getCenter(){
        return new Location(dungeon.getWorld(),
                (double) (coordX + endX) / 2,
                (double) (coordY + endY) / 2,
                (double) (coordZ + endZ) / 2);
    }

    /**
     * @return 2D center (X, Z) of the shard, the Y is same as the starting location's
     */
    public Location get2DCenter(){
        return new Location(dungeon.getWorld(),
                (double) (coordX + endX) / 2,
                coordY,
                (double) (coordZ + endZ) / 2);
    }

    /**
     * Returns the room present on a grid or null if not
     * @param location Physical (non-relative) location
     * @return The AbstractRoom on the grid position
     */
    public @Nullable AbstractRoom getRoomOnGrid(Location location){
        int relativeX = location.getBlockX() - getStart().getBlockX();
        int relativeZ = location.getBlockZ() - getStart().getBlockZ();

        int row = relativeX / getDungeon().MIN_ROOM_SIZE_XZ;
        int col = relativeZ / getDungeon().MIN_ROOM_SIZE_XZ;

        if (row < 0 || row >= roomGrid.length || col < 0 || col >= roomGrid[0].length) {
            return null; // Out of bounds
        }
        return roomGrid[row][col];
    }

    /**
     * Checks if a location (non-relative) is on the grid
     * @param location The location to check
     * @return True if it's on the grid
     */
    public boolean isOnGrid(Location location){
        int relativeX = location.getBlockX() - getStart().getBlockX();
        int relativeZ = location.getBlockZ() - getStart().getBlockZ();

        int row = relativeX / getDungeon().MIN_ROOM_SIZE_XZ;
        int col = relativeZ / getDungeon().MIN_ROOM_SIZE_XZ;

        return row >= 0 && row < roomGrid.length && col >= 0 && col < roomGrid[0].length; // Out of bounds
    }

    /**
     * Returns a grid actual region (cuboid)
     * @param row The row number of grid
     * @param column The column number of grid
     * @return The region of the given grid or null
     */
    public Cuboid getGridCuboid(int row, int column) {
        if (row < 0 || row >= roomGrid.length || column < 0 || column >= roomGrid[0].length) {
            return null;
        }

        int startX = getStart().getBlockX() + row * getDungeon().MIN_ROOM_SIZE_XZ;
        int startZ = getStart().getBlockZ() + column * getDungeon().MIN_ROOM_SIZE_XZ;

        int endX = startX + getDungeon().MIN_ROOM_SIZE_XZ - 1;
        int endZ = startZ + getDungeon().MIN_ROOM_SIZE_XZ - 1;

        int startY = getStart().getBlockY();
        int endY = startY + getEnd().getBlockY() - 1;

        return new Cuboid(new Location(dungeon.getWorld(), startX, startY, startZ), new Location(dungeon.getWorld(), endX, endY, endZ));
    }

    /**
     * <b>Important!</b>
     * This respects the room actual size (so if your grid is 8x8 and this room is 16x16, meaning it claims 4 grid)<br>
     * it will also return all of the neighbors, so in this case there could be 8 neighbors<br>
     * <br>
     * Room grid: 0 - room<br>
     * 00<br>
     * 00<br>
     * <br>
     * Possible neighbors: X - checked location<br>
     *  XX<br>
     * X00X<br>
     * X00X<br>
     *  XX<br>
     *
     * @param room The room to check
     * @return A list of a room's neighbors or an empty list
     */
    public List<AbstractRoom> getNeighbors(AbstractRoom room) {
        List<AbstractRoom> neighbors = new ArrayList<>();
        Location location = room.getLocation();
        AbstractRoom currentRoom = getRoomOnGrid(location);

        if (currentRoom == null) {
            return neighbors;
        }

        int[][] directions = {
                {-1, 0}, // North
                {1, 0},  // South
                {0, -1}, // West
                {0, 1}   // East
        };

        for(int[] indexPair : room.getGridIndexes()){
            for(int[] dir : directions){
                int neighborRow = indexPair[0] + dir[0];
                int neighborCol = indexPair[1] + dir[1];

                if (neighborRow >= 0 && neighborRow < roomGrid.length &&
                        neighborCol >= 0 && neighborCol < roomGrid[0].length) {

                    AbstractRoom neighbor = roomGrid[neighborRow][neighborCol];
                    if (neighbor != null && !neighbor.equals(room)) {
                        neighbors.add(neighbor);
                    }
                }
            }
        }

        return neighbors;
    }

    /**
     * <b>Important!</b>
     * This won't respects the room actual size, so it will return only the 4 possible neighbors (West, North, East, South)<br>
     *
     * @param x row
     * @param z column
     * @return A list of a room's neighbors or an empty list
     */
    public List<AbstractRoom> getGridNeighbors(int x, int z){
        List<AbstractRoom> neighbors = new ArrayList<>();
        int[][] directions = {
                {-1, 0}, // North
                {1, 0},  // South
                {0, -1}, // West
                {0, 1}   // East
        };

        for (int[] dir : directions) {
            int neighborRow = x + dir[0];
            int neighborCol = z + dir[1];

            if (neighborRow >= 0 && neighborRow < roomGrid.length &&
                    neighborCol >= 0 && neighborCol < roomGrid[0].length) {

                AbstractRoom neighbor = roomGrid[neighborRow][neighborCol];
                if (neighbor != null) {
                    neighbors.add(neighbor);
                }
            }
        }
        return neighbors;
    }

    /**
     * <b>Important!</b>
     * This won't respects the room actual size, so it will return only the 4 possible neighbors (West, North, East, South)<br>
     * The list will contain this grid neighbors direction (so it will point from the center of grid to the neighbor)<br>
     *
     * @param x row
     * @param z column
     * @return A list of a room's neighbors direction or empty list
     */
    public List<Direction4> getGridNeighborsDir4(int x, int z){
        List<Direction4> neighbors = new ArrayList<>();
        int[][] directions = {
                {-1, 0}, // North
                {1, 0},  // South
                {0, -1}, // West
                {0, 1}   // East
        };

        for (int[] dir : directions) {
            int neighborRow = x + dir[0];
            int neighborCol = z + dir[1];

            if (neighborRow >= 0 && neighborRow < roomGrid.length &&
                    neighborCol >= 0 && neighborCol < roomGrid[0].length) {

                AbstractRoom neighbor = roomGrid[neighborRow][neighborCol];
                if (neighbor != null) {
                    Direction4 val;
                    if(dir[0] == -1 && dir[1] == 0) val = Direction4.WEST;
                    else if(dir[0] == 1 && dir[1] == 0) val = Direction4.EAST;
                    else if(dir[0] == 0 && dir[1] == -1) val = Direction4.NORTH;
                    else val = Direction4.SOUTH;
                    neighbors.add(val);
                }
            }
        }
        return neighbors;
    }

    /**
     * Checks if there is a room in that direction starting from this grid position<br>
     * <pre>
     *     if(getGridNeighborDir4(0, 0, Direction4.SOUTH) == null){
     *         Bukkit.getLogger().info("There is a single room here!");
     *     }
     * </pre>
     *
     * @param x row
     * @param z column
     * @param direction the direction we are looking for neighbor
     * @return An instance of the room there or null
     */
    public @Nullable AbstractRoom getGridNeighborDir4(int x, int z, Direction4 direction){
        if (direction == null) {
            return null;
        }

        int row = x;
        int col = z;

        switch (direction) {
            case NORTH:
                col -= 1;
                break;
            case SOUTH:
                col += 1;
                break;
            case WEST:
                row -= 1;
                break;
            case EAST:
                row += 1;
                break;
            default:
                return null;
        }

        if (row >= 0 && row < roomGrid.length &&
                col >= 0 && row < roomGrid[0].length) {

            return roomGrid[row][col];
        }
        return null;
    }

    /**
     * <pre>
     *   int[] index = getGridIndex(location);
     *   if (index == null) return new ArrayList&lt;&gt;();
     *
     *   return getGridNeighbors(index[0], index[1]);
     * </pre>
     *
     * @param location Location to check
     * @return A list of a room's neighbors or an empty list
     */
    public List<AbstractRoom> getNeighbors(Location location) {
        int[] index = getGridIndex(location);
        if(index == null) return new ArrayList<>();

        return getGridNeighbors(index[0], index[1]);
    }

    /**
     * @param location Non-relative location
     * @param direction The direction we are searching
     * @return An AbstractRoom if exist or null
     */
    public @Nullable AbstractRoom getNeighbor(Location location, Direction4 direction) {
        int[] index = getGridIndex(location);
        if(index == null) return null;

        return getGridNeighborDir4(index[0], index[1], direction);
    }

    /**
     * Checks if any other room collides with the given cuboid
     * @param cuboid input
     * @return true if the cuboid collides with other rooms
     */
    public boolean doHitOtherRoom(Cuboid cuboid){
        for(AbstractRoom[] rooms : roomGrid){
            for(AbstractRoom room : rooms){
                if(room == null) continue;
                if(room.getRegion().isCollidingWith(cuboid)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Starts the generator to populate the specific shard (chunk)<br>
     * The code first clears the area then starts the generation process
     */
    public void populate(Object... params){
        region.clearRegion(true);
        if(debug){
            drawDebug2DBox();
        }

        generator.begin(getStart(), params);
    }

    /**
     * Clears this shard cell blocks<br>
     * It also includes the debug blocks
     */
    public void clear(boolean applyPyhsics){
        region.expand(Cuboid.CuboidDirection.Down, 1).clearRegion(applyPyhsics);
        for (AbstractRoom[] abstractRooms : roomGrid) {
            for(AbstractRoom room : abstractRooms){
                if(room != null) room.onDestroy();
            }
            Arrays.fill(abstractRooms, null);
        }
    }

    /**
     * Draw manually the 2D hollow box below the shard<br>
     * It is used for debug purposes
     */
    public void drawDebug2DBox(){
        int minX = Math.min(coordX, endX);
        int maxX = Math.max(coordX, endX);
        int minZ = Math.min(coordZ, endZ);
        int maxZ = Math.max(coordZ, endZ);

        for (int x = minX; x <= maxX; x++) {
            dungeon.getWorld().getBlockAt(x, coordY-1, minZ).setType(Material.BLACK_WOOL);
            dungeon.getWorld().getBlockAt(x, coordY-1, maxZ).setType(Material.BLACK_WOOL);
        }

        for (int z = minZ; z <= maxZ; z++) {
            dungeon.getWorld().getBlockAt(minX, coordY-1, z).setType(Material.BLACK_WOOL);
            dungeon.getWorld().getBlockAt(maxX, coordY-1, z).setType(Material.BLACK_WOOL);
        }
    }

    /**
     * @return The room instances
     */
    public List<AbstractRoom> getActiveRooms(){
        List<AbstractRoom> roomList = new ArrayList<>();
        for (AbstractRoom[] row : roomGrid) {
            for (AbstractRoom room : row) {
                if (room != null) {
                    roomList.add(room);
                }
            }
        }
        return roomList;
    }

    public Location getStart(){
        return new Location(dungeon.getWorld(), coordX, coordY, coordZ);
    }

    public Location getEnd(){
        return new Location(dungeon.getWorld(), endX, endY, endZ);
    }

}