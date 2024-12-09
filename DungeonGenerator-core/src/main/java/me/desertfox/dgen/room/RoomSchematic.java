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
import java.util.*;

public class RoomSchematic {

    public static class Index {
        public int x;
        public int z;

        public Index(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

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
                new RoomSchematic(plugin, file.getName().split("\\.")[0], 8, 8);
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
                if(!room.containsDir4(room.getIndex(0,0), dir4)){
                    match = false;
                    break;
                }
            }

            if(match) results.add(room);
        }
        return results;
    }

    public static List<RoomSchematic> findByExactDoors(List<RoomSchematic> pool, Direction4... doors){
        List<RoomSchematic> results = new ArrayList<>();
        for(RoomSchematic room : pool){
            if(room == null) continue;
            if(room.getAllDoors().size() != doors.length) continue;
            boolean match = true;
            for(Direction4 dir4 : doors){
                if(!room.containsDir4(room.getIndex(0,0), dir4)){
                    match = false;
                    break;
                }
            }

            if(match) results.add(room);
        }
        return results;
    }

    public static List<RoomSchematic> findByDoors(List<RoomSchematic> pool, int doorCount){
        List<RoomSchematic> results = new ArrayList<>();
        for(RoomSchematic room : pool){
            if(room == null) continue;
            if(room.getAllDoors().size() != doorCount) continue;
            results.add(room);
        }
        return results;
    }

    public static List<RoomSchematic> findByDoors(List<RoomSchematic> pool, int doorCount, Direction4... haveExactDoors){
        List<RoomSchematic> results = new ArrayList<>();
        for(RoomSchematic room : pool){
            if(room == null) continue;
            if(room.getAllDoors().size() != doorCount) continue;
            boolean match = true;
            for(Direction4 dir4 : haveExactDoors){
                if(!room.containsDir4(room.getIndex(0,0), dir4)){
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
    @Getter private Map<Index, List<Direction4>> doors = new HashMap<>();
    @Getter private int sizeX;
    @Getter private int sizeY;
    @Getter private int sizeZ;
    @Getter private int gridSizeX;
    @Getter private int gridSizeZ;

    /**
     *
     * @param plugin
     * @param schematicName without extensions
     */
    public RoomSchematic(JavaPlugin plugin, String schematicName, int gridSizeX, int gridSizeZ) {
        this.schematicName = schematicName;
        this.yml = SchematicController.getYml(schematicName);
        FileConfiguration config = yml.getConfig();
        if(config.contains("schematic.relative_corner")){
            String raw = config.getString("schematic.relative_corner.pos2", "1 1 1");
            sizeX = Math.abs(Integer.parseInt(raw.split(" ")[0])) + 1;
            sizeY = Math.abs(Integer.parseInt(raw.split(" ")[1])) + 1;
            sizeZ = Math.abs(Integer.parseInt(raw.split(" ")[2])) + 1;
        }

        align(gridSizeX, gridSizeZ);
        rooms.add(this);
    }

    /**
     * Useful when you know a schematic is bigger than your grid<br>
     * Recalculates the doors' indexes and position in the grid<br>
     * <br>
     * Call this if you made changes real-time to the grid's size<br>
     * or you want to use it for a different sized grid
     * @param gridSizeX
     * @param gridSizeZ
     */
    public void align(int gridSizeX, int gridSizeZ){
        this.gridSizeX = gridSizeX;
        this.gridSizeZ = gridSizeZ;
        calculateDoors();
    }

    private void calculateDoors(){
        doors.clear();
        FileConfiguration config = yml.getConfig();
        if(config.contains("schematic.doors")){
            int gridX = (int) Math.ceil((double) sizeX / gridSizeX);
            int gridZ = (int) Math.ceil((double) sizeZ / gridSizeZ);

            for(int x = 0; x < gridX; x++){
                for(int z = 0; z < gridZ; z++){
                    doors.put(new Index(x, z), new ArrayList<>());
                }
            }

            for(String doorRaw : config.getStringList("schematic.doors")){
                String[] splitted = doorRaw.split(" ");
                int x = Integer.parseInt(splitted[0]);
                int y = Integer.parseInt(splitted[1]);
                int z = Integer.parseInt(splitted[2]);
                Direction4 dir = Direction4.valueOf(splitted[3].toUpperCase());

                int gridPosX = Math.abs(x / gridSizeX);
                int gridPosZ = Math.abs(z / gridSizeZ);

                doors.get(getIndex(gridPosX, gridPosZ)).add(dir);
            }
        }
        else {
            Bukkit.getLogger().info("§4" + schematicName + " doesn't have doors registered!");
        }
    }

    public Index getIndex(int x, int z){
        return doors.keySet().stream().filter(e -> e.x == x && e.z == z).findFirst().orElse(null);
    }

    public boolean containsDir4(Index index, Direction4 dir){
        if(!doors.containsKey(index)){
            return false;
        }
        List<Direction4> door = doors.get(index);
        if(door == null) {
            return false;
        }

        return door.contains(dir);
    }

    public List<Direction4> getAllDoors(){
        return doors.values().stream().flatMap(List::stream).toList();
    }

}