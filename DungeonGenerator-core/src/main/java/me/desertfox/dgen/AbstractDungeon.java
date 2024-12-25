package me.desertfox.dgen;

import lombok.Getter;
import me.desertfox.dgen.shard.ShardGenerator;
import me.desertfox.dgen.shard.DungeonShard;
import me.desertfox.dgen.shard.gens.VoidGenerator;
import me.desertfox.dgen.room.AbstractRoom;
import me.desertfox.dgen.room.Room;
import me.desertfox.dgen.room.RoomSchematic;
import me.desertfox.dgen.schematic.OperationalSchematic;
import me.desertfox.dgen.schematic.framework.SchematicController;
import me.desertfox.dgen.utils.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class AbstractDungeon {

    @Getter private static final List<AbstractDungeon> dungeons = new ArrayList<>();

    public static @Nullable AbstractDungeon findByID(String id){
        return dungeons.stream().filter(x -> x.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
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
        public AbstractDungeon.Builder shardSizeX(int val){
            this.SHARD_SIZE_X = val;
            return this;
        }

        public AbstractDungeon.Builder shardSizeZ(int val){
            this.SHARD_SIZE_Z = val;
            return this;
        }

        /**
         * @param val How big the smallest room is? The default value is 4x4
         */
        public AbstractDungeon.Builder setMinRoomSizeXZ(int val){
            val = Math.abs(val);
            assert(val > 0);
            this.MIN_ROOM_SIZE_XZ = val;
            return this;
        }

        public AbstractDungeon build(Class<? extends AbstractDungeon> clazz){
            try {
                return clazz.getConstructor(JavaPlugin.class, String.class, Location.class, Location.class, Integer.class, Integer.class, Integer.class)
                        .newInstance(plugin, id, start, end, SHARD_SIZE_X, SHARD_SIZE_Z, MIN_ROOM_SIZE_XZ);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Getter private final JavaPlugin plugin;
    @Getter private final String id;
    @Getter private final Location start;
    @Getter private final Location end;
    @Getter private final Cuboid region;
    @Getter private Cuboid clearRegion;
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

    private final Queue<Consumer<AbstractDungeon>> updates = new LinkedList<>();

    protected AbstractDungeon(JavaPlugin plugin, String id, Location start, Location end, Integer SHARD_SIZE_X, Integer SHARD_SIZE_Z, Integer MIN_ROOM_SIZE_XZ) {
        this.plugin = plugin;
        this.id = id;
        this.start = start;
        this.end = end;
        this.MIN_ROOM_SIZE_XZ = MIN_ROOM_SIZE_XZ;
        this.SHARD_SIZE_X = SHARD_SIZE_X;
        this.SHARD_SIZE_Z = SHARD_SIZE_Z;
        region = new Cuboid(start, end);
        clearRegion = region.clone();

        shardCountX = (int) Math.ceil(Math.abs((double)end.clone().subtract(start).getBlockX()/ SHARD_SIZE_X));
        shardCountZ = (int) Math.ceil(Math.abs((double)end.clone().subtract(start).getBlockZ()/ SHARD_SIZE_Z));

        cells = new DungeonShard[shardCountX][shardCountZ];

        setupShards();
        dungeons.add(this);

        final AbstractDungeon dg = this;
        new BukkitRunnable() {
            final int chunksPerTick = MAX_SHARD_BATCH;
            @Override
            public void run() {
                if (updates.isEmpty()) {
                    return;
                }

                for (int i = 0; i < chunksPerTick; i++) {
                    Consumer<AbstractDungeon> func = updates.poll();
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
     * Retrieves all active rooms across all shards
     *
     * @return All active rooms across all shards
     */
    public List<AbstractRoom> getActiveRooms(){
        List<AbstractRoom> rooms = new ArrayList<>();
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                DungeonShard cell = cells[i][j];
                rooms.addAll(cell.getActiveRooms());
            }
        }
        return rooms;
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

    private void setupShards(){
        Location curr = start.clone().add(0, 0, 0);
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                int endX = curr.getBlockX() + SHARD_SIZE_X - 1;
                int endY = getEnd().getBlockY();
                int endZ = curr.getBlockZ() + SHARD_SIZE_Z - 1;
                cells[i][j] = new DungeonShard(this, VoidGenerator.class,
                        i, j, curr.getBlockX(), curr.getBlockY(), curr.getBlockZ(),
                        endX, endY, endZ);
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
    public void updateOnQueue(Consumer<AbstractDungeon> func){
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
     * @param roomClass The class to make a new instance of
     * @param schematicName The schematic to claim at the start location
     * @param start The location to claim it
     * @return Null if the claim wasn't concluded or the room reference which has been created
     */
    public AbstractRoom safeClaim(Class<? extends AbstractRoom> roomClass, String schematicName, Location start){
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
        AbstractRoom room = null;
        try {
            room = roomClass.getConstructor(DungeonShard.class, String.class, Location.class, Cuboid.class, List.class)
                    .newInstance(shard, schematicName, start, cuboid, firstRoom.getAllDoors());
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return room;
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
        return safeClaim(Room.class, schematicName, start);
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
     * Tries to claim a room on a given location if: <br>
     * - The room wouldn't hit another room<br>
     * - The start location is on the grid<br>
     * <br>
     * To actually see the physical room you need to call {@link AbstractRoom#placeDown()}<br>
     *
     * @param roomClass The class to make a new instance of
     * @param schematic The schematic to claim at the start location
     * @param start The location to claim it
     * @return Null if the claim wasn't concluded or the room reference which has been created
     */
    public AbstractRoom safeClaim(Class<? extends AbstractRoom> roomClass, RoomSchematic schematic, Location start){
        return safeClaim(roomClass, schematic.getSchematicName(), start);
    }

    /**
     * Returns the Shard on a given (non-relative) location
     * @param location The location to check
     * @return Shard if possible otherwise null
     */
    public DungeonShard getShardOnGrid(Location location){
        int relativeX = location.getBlockX() - start.getBlockX(); //88
        int relativeZ = location.getBlockZ() - start.getBlockZ(); //20

        int i = relativeX / SHARD_SIZE_X;
        int j = relativeZ / SHARD_SIZE_Z;

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

    /**
     * Set all of the shards' generator class and debug info
     * @param debug Pass debug data
     * @param generator The generator
     */
    public void configShards(boolean debug, Class<? extends ShardGenerator> generator){
        for (DungeonShard[] dungeonShards : cells) {
            for (DungeonShard cell : dungeonShards) {
                cell.setDebug(debug);
                cell.setGenerator(generator);
            }
        }
    }

    /**
     * Use {@link AbstractDungeon#configShards(boolean, Class)} for once to initialize before using this
     */
    public void generateAll(Object... params){
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                DungeonShard cell = cells[i][j];
                cell.populate(params);
            }
        }
    }

    /**
     * Clears the dungeon's region using the {@link AbstractDungeon#clearRegion} field<br>
     * If there isn't a set clear region then the default region will be used<br>
     */
    public void clearRegion(){
        clearRegion(null);
    }

    /**
     * Clears the dungeon's region using the {@link AbstractDungeon#clearRegion} field<br>
     * If there isn't a set clear region then the default region will be used<br>
     */
    public void clearRegion(Predicate<? super Entity> kill){
        clearRegion.forEach(b -> b.setType(Material.AIR));
        for(Entity entity : clearRegion.getWorld().getEntities().stream().filter(kill).toList()) {
            if (clearRegion.contains(entity.getLocation())) {
                entity.remove();
            }
        }
    }

    /**
     * Sets the current clear region which is used when<br>
     * {@link AbstractDungeon#clearRegion()}
     * @param region
     */
    public void setClearRegion(Cuboid region){
        this.clearRegion = region.clone();
    }
}