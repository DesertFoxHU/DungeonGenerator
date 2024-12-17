package me.desertfox.dgen.chunk.gens;

import me.desertfox.dgen.AbstractDungeon;
import me.desertfox.dgen.DefaultDungeon;
import me.desertfox.dgen.chunk.ShardGenerator;
import me.desertfox.dgen.chunk.DungeonShard;
import me.desertfox.dgen.examples.IsaacMatrix;
import me.desertfox.dgen.room.RoomSchematic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Random;

import java.util.*;

public class BetterIsaacGenerator extends ShardGenerator {

    public static int SHARD_SIZE_XZ = 64*3;
    public static int SMALLEST_ROOM_SIZE = 8;

    public static AbstractDungeon build(JavaPlugin plugin, String id, Location location){
        return new AbstractDungeon.Builder(plugin, id, location, SHARD_SIZE_XZ, 200, SHARD_SIZE_XZ)
                .setMinRoomSizeXZ(SMALLEST_ROOM_SIZE)
                .shardSizeX(SHARD_SIZE_XZ)
                .shardSizeZ(SHARD_SIZE_XZ)
                .build(DefaultDungeon.class);
    }

    HashMap<RoomSchematic, Integer> ROOMS = new HashMap<>();
    long seed = Random.newSeed();
    public BetterIsaacGenerator(DungeonShard chunk) {
        super(chunk);
        roomPool.clear();
        for(RoomSchematic schema : RoomSchematic.getRooms()){
            String name = schema.getSchematicName();
            if(name.startsWith("isaac")){
                int doors = schema.getAllDoors().size();
                ROOMS.put(schema, --doors);
                roomPool.add(schema);
            }
        }
    }

    private int randomGen(int incMin, int incMax){
        return new Random(seed).nextInt(incMax - incMin + 1) + incMin;
    }

    @Override
    public void begin(Location start, Object... params) {
        int roomCount = randomGen(50, 100);
        Bukkit.getLogger().info("Generating " + roomCount + " room(s)!");
        IsaacMatrix graph = new IsaacMatrix(seed,SHARD_SIZE_XZ / SMALLEST_ROOM_SIZE, roomCount);
        graph.generateDungeon();
        graph.printDungeon();

        boolean[][] grid = graph.getDungeonGrid();
        for (int x = 0; x < grid.length; x++) {
            for (int z = 0; z < grid[x].length; z++) {
                boolean value = grid[x][z];
                Location location = start.clone().add(SMALLEST_ROOM_SIZE * x, 0, SMALLEST_ROOM_SIZE * z);
                if(value){
                    List<RoomSchematic> possibilities = RoomSchematic.findByExactDoors(roomPool, graph.getNeighborRooms(x, z));
                    RoomSchematic schema = possibilities.get(new Random(seed).nextInt(possibilities.size()));
                    getDungeon().safeClaim(schema, location).placeDown();
                }
            }
        }
    }

}