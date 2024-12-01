package me.desertfox.dgen.chunk.gens;

import me.desertfox.dgen.Direction4;
import me.desertfox.dgen.Dungeon;
import me.desertfox.dgen.chunk.ChunkGenerator;
import me.desertfox.dgen.chunk.DungeonShard;
import me.desertfox.dgen.room.AbstractRoom;
import me.desertfox.dgen.room.RoomSchematic;
import net.minecraft.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Random;

import java.util.*;
import java.util.stream.Collectors;

public class IsaacLikeGenerator extends ChunkGenerator {

    public static Dungeon build(JavaPlugin plugin, String id, Location location){
        return new Dungeon.Builder(plugin, id, location, 64*4, 200, 64*4)
                .setMinRoomSizeXZ(8)
                .shardSizeX(64*4)
                .shardSizeZ(64*4)
                .build();
    }

    private static final HashMap<Integer, Integer> DOOR_COST = new HashMap<>(){{
        put(0, 2);
        put(1, 3);
        put(2, 4);
        put(3, 5);
    }};

    HashMap<RoomSchematic, Integer> ROOMS = new HashMap<>();
    long seed = Random.newSeed();
    public IsaacLikeGenerator(DungeonShard chunk) {
        super(chunk);
        roomPool.clear();
        for(RoomSchematic schema : RoomSchematic.getRooms()){
            String name = schema.getSchematicName();
            if(name.startsWith("isaac")){
                int doors = schema.getDoors().length;
                ROOMS.put(schema, --doors);
            }
        }
    }

    private RoomSchematic getRandomByDoors(int value){
        List<RoomSchematic> matchingKeys = new ArrayList<>();
        for (Map.Entry<RoomSchematic, Integer> entry : ROOMS.entrySet()) {
            if (entry.getValue() == value) {
                matchingKeys.add(entry.getKey());
            }
        }

        // Randomly select one of the matching keys
        if (!matchingKeys.isEmpty()) {
            Random random = new Random(seed);
            return matchingKeys.get(random.nextInt(matchingKeys.size()));
        }
        Bukkit.getLogger().info("Returning getRandomByDoors null because input: " + value);
        return null;
    }

    private int randomGen(int incMin, int incMax){
        return new Random(seed).nextInt(incMax - incMin + 1) + incMin;
    }

    private void registerBuilding(AbstractRoom room, Location start, Direction4 cut){
        for(Direction4 dir4 : room.getDoors()){
            if(dir4 == cut) continue;
            Bukkit.getLogger().info("Added to array: " + dir4 + " from come: " + start + " | " + room.getSchematicName());
            Location l = start.clone().add(dir4.vector.getX() * (room.getRegion().getSizeX()+1), 0, dir4.vector.getZ() * room.getRegion().getSizeZ());
            array.add(new Tuple<>(dir4, l));
        }
    }

    public List<Tuple<Direction4, Location>> array = new ArrayList<>();
    @Override
    public void begin(Location start) {
        Bukkit.getLogger().info("Seed: " + seed);
        int numberOfRooms = randomGen(15, 30);
        Bukkit.getLogger().info("Generating " + numberOfRooms + " rooms!");
        start = getShard().getDungeon().snapToGrid(getShard().get2DCenter()); //We ignore the default start

        int score = numberOfRooms;
        int randomNumber = randomGen(1, 3);
        score -= DOOR_COST.get(randomNumber);
        AbstractRoom room = getDungeon().safeBuild(getRandomByDoors(randomNumber), start);
        Bukkit.getLogger().info("Start location: " + start + " placed: " + room.getSchematicName() + " remaining score: " + score);
        registerBuilding(room, start, null);
        List<String> pool = ROOMS.keySet().stream().map(RoomSchematic::getSchematicName).toList();
        do{
            randomNumber = randomGen(1, Math.min(3, score));
            int inDoors = randomNumber+1;
            score -= randomNumber;
            Bukkit.getLogger().info("  New building with: " + inDoors + " door(s)! Remaining score: " + score);
            Tuple<Direction4, Location> entry = array.get(0);

            Direction4 flip = Direction4.flip(entry.a());
            List<RoomSchematic> possibilities = RoomSchematic.findByDoors(pool, inDoors, flip);
            RoomSchematic schema = possibilities.get(new Random(seed).nextInt(possibilities.size()));
            Bukkit.getLogger().info("  Building " + schema.getSchematicName() + " in " + entry.b());
            room = getDungeon().safeBuild(schema, entry.b());
            registerBuilding(room, entry.b(), flip);
            array.remove(0);
        } while(score > 0);
        Bukkit.getLogger().info("MAIN IS FINISHED!");
        Bukkit.getLogger().info(" ");
        Iterator<Tuple<Direction4, Location>> iterator = array.iterator();
        while(iterator.hasNext()){
            Tuple<Direction4, Location> entry = iterator.next();
            List<Direction4> doors = new ArrayList<>();
            logicA(doors, entry.b(), Direction4.WEST);
            logicA(doors, entry.b(), Direction4.EAST);
            logicA(doors, entry.b(), Direction4.NORTH);
            logicA(doors, entry.b(), Direction4.SOUTH);
            List<RoomSchematic> possibilities = RoomSchematic.findByExactDoors(pool, doors.toArray(new Direction4[0]));
            if(possibilities.isEmpty()){
                Bukkit.getLogger().info("There are no solution for these doors: " + doors.size());
                for(Direction4 dir : doors){
                    Bukkit.getLogger().info("" + dir);
                }
            }
            RoomSchematic schema = possibilities.get(new Random(seed).nextInt(possibilities.size()));
            room = getDungeon().safeBuild(schema, entry.b());

            iterator.remove();
        }
    }

    private void logicA(List<Direction4> list, Location start, Direction4 dir){
        if(getShard().getNeighbor(start, dir) == null) return;
        if(getShard().getNeighbor(start, dir).getDoors().contains(Direction4.flip(dir))) list.add(dir);
    }
}