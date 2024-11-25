package me.desertfox.dgen.chunk;

import lombok.Getter;
import lombok.Setter;
import me.desertfox.dgen.chunk.gens.*;
import me.desertfox.dgen.room.AbstractRoom;
import me.desertfox.dgen.room.ActiveRoom;
import me.desertfox.dgen.room.RoomSchematic;
import me.desertfox.dgen.schematic.OperationalSchematic;
import me.desertfox.dgen.schematic.framework.SchematicController;
import me.desertfox.dgen.utils.Cuboid;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * The ChunkGenerator class manages the process of generating rooms<br>
 * and other objects into a shard<br>
 * <br>
 * If you made a new ChunkGenerator subclass then don't forget to register it {@link ChunkGenerator#register(Class)}<br>
 * Otherwise it won't be accessible<br>
 * <br>
 * Keep in mind every instance of your class will represent a shard<br>
 */
public abstract class ChunkGenerator {

    @Getter private static HashMap<String, Class<? extends ChunkGenerator>> GENERATOR_REGISTRY = new HashMap<>();

    static {
        register(SimpleGenerator.class);
        register(VoidGenerator.class);
        register(WeightedSimpleGenerator.class);
        register(ConnectedDoorsGenerator.class);
        register(MazeGenerator.class);
    }

    /**
     * Finds a Generator by class name (SimpleGenerator)<br>
     * @param name The name without the .class identifier
     * @return The class of ChunkGenerator
     */
    public static Class<? extends ChunkGenerator> findByClassName(String name){
        return GENERATOR_REGISTRY.getOrDefault(name, VoidGenerator.class);
    }

    /**
     * Registers your generator class
     * @param generator Your class
     */
    public static void register(Class<? extends ChunkGenerator> generator){
        if(!GENERATOR_REGISTRY.containsKey(generator.getSimpleName())){
            GENERATOR_REGISTRY.put(generator.getSimpleName(), generator);
        }
    }

    @Getter private DungeonShard shard;
    /**
     * The roomPool allows for ChunkGenerators to store the room names they want to use<br>
     * It is not mandatory to use this system, it can be easily avoided and the system doesn't force<br>
     * you to use this<br>
     */
    @Setter protected List<String> roomPool;

    public ChunkGenerator(DungeonShard shard){
        this.shard = shard;
        roomPool = new ArrayList<>(RoomSchematic.getRooms().stream().map(RoomSchematic::getSchematicName).toList());
    }

    /**
     * Tries to build a room on a given location if: <br>
     * - The room wouldn't hit another room<br>
     * - The start location is on the grid<br>
     *
     * @param schematicName The schematic to build at the start location
     * @param start The location to build it
     * @return Null if the build wasn't concluded or the room which has been created
     */
    public AbstractRoom safeBuild(String schematicName, Location start){
        RoomSchematic firstRoom = RoomSchematic.findByName(schematicName);
        OperationalSchematic schematic = SchematicController.get(firstRoom.getSchematicName());
        Cuboid cuboid = schematic.getCuboid(start, new Vector(0,0,0));
        if(getShard().doHitOtherRoom(cuboid)){
            return null;
        }
        if(!getShard().getRegion().contains(start)){
            return null;
        }
        schematic.populate(start, new Vector(0,0,0));
        AbstractRoom room = new ActiveRoom(shard, schematicName, start, cuboid, Arrays.asList(firstRoom.getDoors()));
        return room;
    }

    /**
     * {@link ChunkGenerator#begin(Location)} called every time the dungeon starts the<br>
     * generation on this shard<br>
     * @param start The location to start with
     */
    public abstract void begin(Location start);
}