package me.desertfox.dgen.chunk;

import lombok.Getter;

import lombok.Setter;
import me.desertfox.dgen.Direction4;
import me.desertfox.dgen.Dungeon;
import me.desertfox.dgen.room.AbstractRoom;
import me.desertfox.dgen.utils.Cuboid;
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
    private final Dungeon dungeon;
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
    public ChunkGenerator generator;

    public DungeonShard(Dungeon dungeon, Class<? extends ChunkGenerator> generatorClass, int indexX, int indexY, int coordX, int coordY, int coordZ, int endX, int endY, int endZ) {
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

        int rows = region.getSizeZ() / getDungeon().MIN_ROOM_SIZE_XZ;
        int cols = region.getSizeX() / getDungeon().MIN_ROOM_SIZE_XZ;

        roomGrid = new AbstractRoom[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                roomGrid[row][col] = null;
            }
        }

        setGenerator(generatorClass);
    }

    /**
     * Sets a new generator but doesn't start the generation process
     * @param generatorClass Generator
     */
    public void setGenerator(Class<? extends ChunkGenerator> generatorClass) {
        try {
            this.generator = generatorClass.getConstructor(DungeonShard.class).newInstance(this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the room present on a grid or null if not
     * @param location Physical (non-relative) location
     * @return The AbstractRoom on the grid position
     */
    public @Nullable AbstractRoom getRoomOnGrid(Location location){
        int relativeX = location.getBlockX() - dungeon.getStart().getBlockX();
        int relativeZ = location.getBlockZ() - dungeon.getStart().getBlockZ();

        int col = relativeX / getDungeon().MIN_ROOM_SIZE_XZ;
        int row = relativeZ / getDungeon().MIN_ROOM_SIZE_XZ;

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
        int relativeX = location.getBlockX() - dungeon.getStart().getBlockX();
        int relativeZ = location.getBlockZ() - dungeon.getStart().getBlockZ();

        int col = relativeX / getDungeon().MIN_ROOM_SIZE_XZ;
        int row = relativeZ / getDungeon().MIN_ROOM_SIZE_XZ;

        return row >= 0 && row < roomGrid.length && col >= 0 && col < roomGrid[0].length; // Out of bounds
    }

    /**
     * @param room
     * @return A list of a room's neighbors or an empty list
     */
    public List<AbstractRoom> getNeighbors(AbstractRoom room) {
        List<AbstractRoom> neighbors = new ArrayList<>();
        Location location = room.getLocation();
        AbstractRoom currentRoom = getRoomOnGrid(location);

        if (currentRoom == null) {
            return neighbors;
        }

        int relativeX = location.getBlockX() - dungeon.getStart().getBlockX();
        int relativeZ = location.getBlockZ() - dungeon.getStart().getBlockZ();
        int col = relativeX / dungeon.MIN_ROOM_SIZE_XZ;
        int row = relativeZ / dungeon.MIN_ROOM_SIZE_XZ;

        int[][] directions = {
                {-1, 0}, // North
                {1, 0},  // South
                {0, -1}, // West
                {0, 1}   // East
        };

        for (int[] dir : directions) {
            int neighborRow = row + dir[0];
            int neighborCol = col + dir[1];

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
     * @param location Non-relative location
     * @param direction The direction we are searching
     * @return An AbstractRoom if exist or null
     */
    public @Nullable AbstractRoom getNeighbor(Location location, Direction4 direction) {
        if (location == null || direction == null) {
            return null;
        }

        int relativeX = location.getBlockX() - dungeon.getStart().getBlockX();
        int relativeZ = location.getBlockZ() - dungeon.getStart().getBlockZ();
        int col = relativeX / dungeon.MIN_ROOM_SIZE_XZ;
        int row = relativeZ / dungeon.MIN_ROOM_SIZE_XZ;

        switch (direction) {
            case NORTH:
                row -= 1;
                break;
            case SOUTH:
                row += 1;
                break;
            case WEST:
                col -= 1;
                break;
            case EAST:
                col += 1;
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

    public @Nullable AbstractRoom getNeighbor(AbstractRoom room, Direction4 direction) {
        if (room == null || direction == null) {
            return null;
        }

        Location location = room.getLocation();
        AbstractRoom currentRoom = getRoomOnGrid(location);

        if (currentRoom == null) {
            return null;
        }

        return getNeighbor(location, direction);
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

    private final List<Location> startHere = new ArrayList<>();
    /**
     * Starts the generator to populate the specific shard (chunk)<br>
     * The code first clears the area then starts the generation process
     */
    public void populate(){
        new Cuboid(getStart(), getEnd()).clearRegion();
        if(debug){
            drawDebug2DBox();
        }

        if(indexX != 0 || indexY != 0) return;
        generator.begin(getStart());
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

    public Location getStart(){
        return new Location(dungeon.getWorld(), coordX, coordY, coordZ);
    }

    public Location getEnd(){
        return new Location(dungeon.getWorld(), endX, endY, endZ);
    }

}