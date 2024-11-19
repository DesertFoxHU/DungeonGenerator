package me.desertfox.dgen;

import me.desertfox.dgen.commands.GenerateCommand;
import me.desertfox.dgen.commands.SchematicCommand;
import me.desertfox.dgen.room.RoomSchematic;
import me.desertfox.dgen.schematic.framework.SchematicController;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DungeonGenerator extends JavaPlugin {

    public static DungeonGenerator instance;

    @Override
    public void onEnable(){
        instance = this;

        SchematicController.init(this, "schemas");

        new RoomSchematic(this, "corridor_SN", Direction4.SOUTH, Direction4.NORTH);
        new RoomSchematic(this, "corridor_WE", Direction4.WEST, Direction4.EAST);
        new RoomSchematic(this, "corridor_WESN", Direction4.NORTH, Direction4.EAST, Direction4.WEST, Direction4.SOUTH);
        new RoomSchematic(this, "wall_4x4");
        new RoomSchematic(this, "garage_W", Direction4.WEST);
        Bukkit.getLogger().info("Loaded " + RoomSchematic.getRooms().size() + " rooms!");

        Dungeon dungeon = new Dungeon.Builder(this, "test",
                new Location(Bukkit.getWorld("world"), 0, 65, 0),
                200, 200, 200).build();
        Bukkit.getLogger().info("Loaded " + dungeon.getId() + " dungeon!");

        getCommand("generate").setExecutor(new GenerateCommand());
        getCommand("schema").setExecutor(new SchematicCommand());

        PluginManager pm = Bukkit.getPluginManager();
    }

    public void onDisable() {
    }

}