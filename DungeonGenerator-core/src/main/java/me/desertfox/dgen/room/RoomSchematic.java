package me.desertfox.dgen.room;

import lombok.Getter;
import me.desertfox.dgen.Direction4;
import me.desertfox.dgen.schematic.framework.SchematicController;
import me.desertfox.dgen.utils.CustomYml;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoomSchematic {

    @Getter private static List<RoomSchematic> rooms = new ArrayList<>();

    public static @Nullable RoomSchematic findByName(String schematicName){
        return rooms.stream().filter(x -> x.schematicName.equalsIgnoreCase(schematicName)).findFirst().orElse(null);
    }

    /**
     * Returns an array of possible schematics which has these doors or more
     * @param doors
     * @return
     */
    public static List<RoomSchematic> findByDoors(Direction4... doors){
        List<RoomSchematic> results = new ArrayList<>();
        for(RoomSchematic room : rooms){
            boolean match = true;
            for(Direction4 dir4 : doors){
                if(!room.containsDir4(dir4)){
                    match = false;
                    break;
                }
            }

            if(match) results.add(room);
        }
        return results;
    }

    @Getter private String schematicName;
    @Getter private CustomYml yml;
    @Getter Direction4[] doors;
    @Getter private int sizeX;
    @Getter private int sizeY;
    @Getter private int sizeZ;

    /**
     *
     * @param plugin
     * @param schematicName without extensions
     * @param doors The direction where is a door from INSIDE the schematic
     */
    public RoomSchematic(JavaPlugin plugin, String schematicName, Direction4... doors) {
        this.schematicName = schematicName;
        this.doors = doors;
        this.yml = SchematicController.getYml(schematicName);
        rooms.add(this);
    }

    public boolean containsDir4(Direction4 dir){
        return Arrays.stream(doors).anyMatch(d -> d == dir);
    }

    /*public Region paste(Location start){
        OperationalSchematic schema = SchematicController.get(schematicName);
        schema.populate(start, new Vector(0,0,0));
        return schema.getCuboid(start, new Vector(0,0,0));
    }*/
}