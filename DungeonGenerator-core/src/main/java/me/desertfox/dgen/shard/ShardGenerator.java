package me.desertfox.dgen.shard;

import lombok.Getter;
import lombok.Setter;
import me.desertfox.dgen.AbstractDungeon;
import me.desertfox.dgen.room.RoomSchematic;
import me.desertfox.dgen.shard.gens.*;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The ShardGenerator class manages the process of generating rooms<br>
 * and other objects into a shard<br>
 * <br>
 * If you made a new ShardGenerator subclass then don't forget to register it {@link ShardGenerator#register(Class)}<br>
 * Otherwise it won't be accessible<br>
 * <br>
 * Keep in mind every instance of your class will represent a shard<br>
 */
public abstract class ShardGenerator {

    @Getter private static HashMap<String, Class<? extends ShardGenerator>> GENERATOR_REGISTRY = new HashMap<>();

    static {
        register(SimpleGenerator.class);
        register(VoidGenerator.class);
        register(WeightedSimpleGenerator.class);
        register(ConnectedDoorsGenerator.class);
        register(MazeGenerator.class);
        register(GameGenerator.class);
        register(BetterIsaacGenerator.class);
        register(IsaacLikeGenerator.class);
    }

    /**
     * Finds a Generator by class name (SimpleGenerator)<br>
     * @param name The name without the .class identifier
     * @return The class of ChunkGenerator
     */
    public static Class<? extends ShardGenerator> findByClassName(String name){
        return GENERATOR_REGISTRY.getOrDefault(name, VoidGenerator.class);
    }

    /**
     * Registers your generator class
     * @param generator Your class
     */
    public static void register(Class<? extends ShardGenerator> generator){
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
    @Setter protected List<RoomSchematic> roomPool;

    public ShardGenerator(DungeonShard shard){
        this.shard = shard;
        roomPool = new ArrayList<>(RoomSchematic.getRooms());
    }

    public AbstractDungeon getDungeon(){
        return shard.getDungeon();
    }

    /**
     * {@link ShardGenerator#begin(Location, Object...)} called every time the dungeon starts the<br>
     * generation on this shard<br>
     * @param start The location to start with
     */
    public abstract void begin(Location start, Object... params);
}