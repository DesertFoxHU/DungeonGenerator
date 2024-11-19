package me.desertfox.dgen.chunk;

import lombok.Getter;
import lombok.Setter;
import me.desertfox.dgen.chunk.gens.ConnectedDoorsGenerator;
import me.desertfox.dgen.chunk.gens.SimpleGenerator;
import me.desertfox.dgen.chunk.gens.VoidGenerator;
import me.desertfox.dgen.chunk.gens.WeightedSimpleGenerator;
import me.desertfox.dgen.room.ActiveRoom;
import me.desertfox.dgen.room.RoomSchematic;
import me.desertfox.dgen.schematic.OperationalSchematic;
import me.desertfox.dgen.schematic.framework.SchematicController;
import me.desertfox.dgen.utils.Cuboid;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class ChunkGenerator {

    @Getter private static HashMap<String, Class<? extends ChunkGenerator>> GENERATOR_REGISTRY = new HashMap<>();

    static {
        register(SimpleGenerator.class);
        register(VoidGenerator.class);
        register(WeightedSimpleGenerator.class);
        register(ConnectedDoorsGenerator.class);
    }

    public static Class<? extends ChunkGenerator> findByClassName(String name){
        return GENERATOR_REGISTRY.getOrDefault(name, VoidGenerator.class);
    }

    public static void register(Class<? extends ChunkGenerator> generator){
        if(!GENERATOR_REGISTRY.containsKey(generator.getSimpleName())){
            GENERATOR_REGISTRY.put(generator.getSimpleName(), generator);
        }
    }

    @Getter private DungeonChunk chunk;
    @Setter protected List<String> roomPool;

    public ChunkGenerator(DungeonChunk chunk){
        this.chunk = chunk;
        roomPool = new ArrayList<>(RoomSchematic.getRooms().stream().map(RoomSchematic::getSchematicName).toList());
    }

    public ActiveRoom safeBuild(String schematicName, Location start){
        RoomSchematic firstRoom = RoomSchematic.findByName(schematicName);
        OperationalSchematic schematic = SchematicController.get(firstRoom.getSchematicName());
        Cuboid cuboid = schematic.getCuboid(start, new Vector(0,0,0));
        if(getChunk().doHitOtherRoom(cuboid)){
            return null;
        }
        if(!getChunk().getRegion().contains(start)){
            return null;
        }
        schematic.populate(start, new Vector(0,0,0));
        ActiveRoom room = new ActiveRoom(schematicName, cuboid);
        chunk.getRooms().add(room);
        return room;
    }

    public abstract void begin(Location start);

}