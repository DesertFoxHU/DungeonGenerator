package me.desertfox.dgen;

import lombok.Getter;
import me.desertfox.dgen.chunk.ChunkGenerator;
import me.desertfox.dgen.chunk.DungeonShard;
import me.desertfox.dgen.chunk.gens.VoidGenerator;
import me.desertfox.dgen.room.AbstractRoom;
import me.desertfox.dgen.room.Room;
import me.desertfox.dgen.room.RoomSchematic;
import me.desertfox.dgen.schematic.OperationalSchematic;
import me.desertfox.dgen.schematic.framework.SchematicController;
import me.desertfox.dgen.utils.Cuboid;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class Dungeon {

    @Getter private static final List<Dungeon> dungeons = new ArrayList<>();

    public static @Nullable Dungeon findByID(String id){
        return dungeons.stream().filter(x -> x.id.equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public static class Builder {
        private final JavaPlugin plugin;
        private final String id;
        private final Location start;
        private final Location end;
        public int SHARD_SIZE_X = 32;
        public int SHARD_SIZE_Z = 32;
        public int MIN_ROOM_SIZE_XZ = 4;

        public Builder(JavaPlugin plugin, String id, Location start, int sizeX, int sizeY, int sizeZ){
            this.plugin = plugin;
            this.id = id;
            this.start = start.clone();
            this.end = start.clone().add(sizeX, sizeY, sizeZ);
        }

        public Builder(JavaPlugin plugin, String id, Location start, Location end){
            this.plugin = plugin;
            this.id = id;
            this.start = start.clone();
            this.end = end.clone();
        }

        /**
         * Set how many shards
         * @param val
         * @return
         */
        public Dungeon.Builder shardSizeX(int val){
            this.SHARD_SIZE_X = val;
            return this;
        }

        public Dungeon.Builder shardSizeZ(int val){
            this.SHARD_SIZE_Z = val;
            return this;
        }

        /**
         * @param val How big the smallest room is? The default value is 4x4
         */
        public Dungeon.Builder setMinRoomSizeXZ(int val){
            val = Math.abs(val);
            assert(val > 0);
            this.MIN_ROOM_SIZE_XZ = val;
            return this;
        }

        public Dungeon build(){
            return new Dungeon(plugin, id, start, end, SHARD_SIZE_X, SHARD_SIZE_Z, MIN_ROOM_SIZE_XZ);
        }
    }

    @Getter private final JavaPlugin plugin;
    @Getter private final String id;
    @Getter private final Location start;
    @Getter private final Location end;
    @Getter private DungeonShard[][] cells;
    @Getter private int shardCountX = 0;
    @Getter private int shardCountZ = 0;
    public int MAX_SHARD_BATCH = 4;

    public final int MIN_ROOM_SIZE_XZ;

    /**
     * A Chunk's size in X,Z dimensions
     */
    private int SHARD_SIZE_X = 32;
    private int SHARD_SIZE_Z = 32;

    private final Queue<Consumer<Dungeon>> updates = new LinkedList<>();

    protected Dungeon(JavaPlugin plugin, String id, Location start, Location end, int SHARD_SIZE_X, int SHARD_SIZE_Z, int MIN_ROOM_SIZE_XZ) {
        this.plugin = plugin;
        this.id = id;
        this.start = start;
        this.end = end;
        this.MIN_ROOM_SIZE_XZ = MIN_ROOM_SIZE_XZ;
        this.SHARD_SIZE_X = SHARD_SIZE_X;
        this.SHARD_SIZE_Z = SHARD_SIZE_Z;

        shardCountX = (int) Math.ceil(Math.abs((double)end.subtract(start).getBlockX()/ SHARD_SIZE_X));
        shardCountZ = (int) Math.ceil(Math.abs((double)end.subtract(start).getBlockZ()/ SHARD_SIZE_Z));

        cells = new DungeonShard[shardCountX][shardCountZ];

        setupCells();
        dungeons.add(this);

        final Dungeon dg = this;
        new BukkitRunnable() {
            final int chunksPerTick = MAX_SHARD_BATCH;
            @Override
            public void run() {
                if (updates.isEmpty()) {
                    return;
                }

                for (int i = 0; i < chunksPerTick; i++) {
                    Consumer<Dungeon> func = updates.poll();
                    if(func != null) func.accept(dg);
                }
            }
        }.runTaskTimer(plugin, 0, 2L);
    }

    public Location getCenter(){
        return new Location(start.getWorld(),
                (double) (start.getBlockX() + end.getBlockX()) / 2,
                (double) (start.getBlockY() + end.getBlockY()) / 2,
                (double) (start.getBlockZ() + end.getBlockZ()) / 2);
    }

    /**
     * @return 2D center (X, Z) of the dungeon, the Y is same as the starting location's
     */
    public Location get2DCenter(){
        return new Location(start.getWorld(),
                (double) (start.getBlockX() + end.getBlockX()) / 2,
                start.getBlockY(),
                (double) (start.getBlockZ() + end.getBlockZ()) / 2);
    }

    /**
     * Clears the blocks in the dungeon on update queue<br>
     * It also includes the debug blocks
     */
    public void clearQueue(){
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                DungeonShard cell = cells[i][j];
                updateOnQueue(d -> {
                    cell.clear();
                });
            }
        }
    }

    /**
     * Clears the blocks in the dungeon instantly<br>
     * It also includes the debug blocks
     */
    public void clear(){
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                DungeonShard cell = cells[i][j];
                cell.clear();
            }
        }
    }

    private void setupCells(){
        Location curr = start.clone().add(0, 0, 0);
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j] = new DungeonShard(this, VoidGenerator.class,
                        i, j, curr.getBlockX(), curr.getBlockY(), curr.getBlockZ(),
                        curr.getBlockX() + SHARD_SIZE_X - 1, end.getBlockY(), curr.getBlockZ() + SHARD_SIZE_Z - 1);
                curr = curr.add(SHARD_SIZE_X, 0, 0);
            }
            curr.setX(start.getBlockX());
            curr = curr.add(0, 0, SHARD_SIZE_Z);
        }
    }

    /**
     * Adds a function to the queue list
     * @param func
     */
    public void updateOnQueue(Consumer<Dungeon> func){
        updates.add(func);
    }

    /**
     * Check if the area is safe to build if: <br>
     * - The room wouldn't hit another room<br>
     * - The start location is on the grid<br>
     *
     * @param schematicName The schematic name to check
     * @param start The location to check
     * @return False if not safe to build otherwise true
     */
    public boolean isSafeToBuild(String schematicName, Location start){
        DungeonShard shard = getShardOnGrid(start);
        if(shard == null) {
            return false;
        }

        RoomSchematic firstRoom = RoomSchematic.findByName(schematicName);
        OperationalSchematic schematic = SchematicController.get(firstRoom.getSchematicName());
        Cuboid cuboid = schematic.getCuboid(start, new org.bukkit.util.Vector(0,0,0));
        if(shard.doHitOtherRoom(cuboid)){
            return false;
        }
        if(shard.getRoomOnGrid(start) != null){
            return false;
        }
        return shard.getRegion().contains(start);
    }


    /**
     * Tries to claim a room on a given location if: <br>
     * - The room wouldn't hit another room<br>
     * - The start location is on the grid<br>
     * <br>
     * To actually see the physical room you need to call {@link AbstractRoom#placeDown()}<br>
     *
     * @param schematicName The schematic to claim at the start location
     * @param start The location to claim it
     * @return Null if the claim wasn't concluded or the room reference which has been created
     */
    public AbstractRoom safeClaim(String schematicName, Location start){
        DungeonShard shard = getShardOnGrid(start);
        if(shard == null) {
            return null;
        }

        RoomSchematic firstRoom = RoomSchematic.findByName(schematicName);
        OperationalSchematic schematic = SchematicController.getHardReference(firstRoom.getSchematicName());
        Cuboid cuboid = schematic.getCuboid(start, new org.bukkit.util.Vector(0,0,0));
        if(shard.doHitOtherRoom(cuboid)){
            return null;
        }
        if(shard.getRoomOnGrid(start) != null){
            return null;
        }
        if(!shard.getRegion().contains(start)){
            return null;
        }
        AbstractRoom room = new Room(shard, schematicName, start, cuboid, firstRoom.getAllDoors());
        return room;
    }

    /**
     * Tries to claim a room on a given location if: <br>
     * - The room wouldn't hit another room<br>
     * - The start location is on the grid<br>
     * <br>
     * To actually see the physical room you need to call {@link AbstractRoom#placeDown()}<br>
     *
     * @param schematic The schematic to claim at the start location
     * @param start The location to claim it
     * @return Null if the claim wasn't concluded or the room reference which has been created
     */
    public AbstractRoom safeClaim(RoomSchematic schematic, Location start){
        return safeClaim(schematic.getSchematicName(), start);
    }

    /**
     * Returns the Shard on a given (non-relative) location
     * @param location The location to check
     * @return Shard if possible otherwise null
     */
    public DungeonShard getShardOnGrid(Location location){
        int relativeX = location.getBlockX() - start.getBlockX(); //88
        int relativeZ = location.getBlockZ() - start.getBlockZ(); //20

        int i = relativeZ / SHARD_SIZE_Z;
        int j = relativeX / SHARD_SIZE_X;

        if (i >= 0 && i < cells.length && j >= 0 && j < cells[i].length) {
            return cells[i][j];
        }

        return null;
    }

    public Location snapToGrid(Location location){
        int x = (int) (Math.floor(((double) (location.getBlockX() + MIN_ROOM_SIZE_XZ - 1) / MIN_ROOM_SIZE_XZ)) * MIN_ROOM_SIZE_XZ);
        int z = (int) (Math.floor(((double) (location.getBlockZ() + MIN_ROOM_SIZE_XZ - 1) / MIN_ROOM_SIZE_XZ)) * MIN_ROOM_SIZE_XZ);
        return location.clone().set(x, location.getY(), z);
    }

    public World getWorld() { return start.getWorld(); }

    public void generateAll(boolean debug, Class<? extends ChunkGenerator> generator){
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                DungeonShard cell = cells[i][j];
                cell.setDebug(debug);
                cell.setGenerator(generator);
                cell.populate();
            }
        }
    }
}