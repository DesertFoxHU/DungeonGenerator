package me.desertfox.dgen.room;

import lombok.Getter;
import me.desertfox.dgen.Direction4;
import me.desertfox.dgen.schematic.framework.SchematicController;
import me.desertfox.dgen.utils.CustomYml;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoomSchematic {

    @Getter private static List<RoomSchematic> rooms = new ArrayList<>();

    public static @Nullable RoomSchematic findByName(String schematicName){
        return rooms.stream().filter(x -> x.schematicName.equalsIgnoreCase(schematicName)).findFirst().orElse(null);
    }

    /**
     * Loads all schematic available in the present directory
     * @param plugin
     * @param directory
     */
    public static void IOLoadAll(JavaPlugin plugin, String directory){
        File dir = new File(plugin.getDataFolder() + File.separator + directory);
        if(!dir.exists()){
            dir.mkdirs();
        }

        if(dir.listFiles() != null){
            for(File file : dir.listFiles()){
                new RoomSchematic(plugin, file.getName().split("\\.")[0]);
            }
        }
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
     */
    public RoomSchematic(JavaPlugin plugin, String schematicName) {
        this.schematicName = schematicName;
        this.yml = SchematicController.getYml(schematicName);
        FileConfiguration config = yml.getConfig();
        if(config.contains("schematic.doors")){
            String str = config.getString("schematic.doors", "");
            doors = new Direction4[str.length()];
            int i = 0;
            for(char ch : str.toCharArray()){
                doors[i] = Direction4.getByChar(ch);
                i++;
            }
        }
        else {
            Bukkit.getLogger().info("§4" + schematicName + " doesn't have doors registered!");
        }
        rooms.add(this);
    }

    /**
     *
     * @param plugin
     * @param schematicName without extensions
     * @param overwrite The direction where is a door from INSIDE the schematic
     */
    public RoomSchematic(JavaPlugin plugin, String schematicName, Direction4... overwrite) {
        this.schematicName = schematicName;
        this.doors = overwrite;
        this.yml = SchematicController.getYml(schematicName);
        rooms.add(this);
    }

    public boolean containsDir4(Direction4 dir){
        if(doors == null) return false;
        return Arrays.stream(doors).anyMatch(d -> d == dir);
    }

}