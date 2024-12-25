package me.desertfox.dgen;

import me.desertfox.dgen.shard.gens.BetterIsaacGenerator;
import me.desertfox.dgen.commands.DungeonCommand;
import me.desertfox.dgen.commands.GenerateCommand;
import me.desertfox.dgen.commands.SchematicCommand;
import me.desertfox.dgen.commands.tab.GenerateCommandTab;
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
        RoomSchematic.IOLoadAll(this, "schemas");
        Bukkit.getLogger().info("Loaded " + RoomSchematic.getRooms().size() + " rooms!");

        DungeonCommand.register(this);
        //test1();
        //test2();
        AbstractDungeon dungeon = BetterIsaacGenerator.build(this, "test", new Location(Bukkit.getWorld("world"), 0, 65, 0));

        getCommand("generate").setExecutor(new GenerateCommand());
        getCommand("generate").setTabCompleter(new GenerateCommandTab());
        getCommand("schema").setExecutor(new SchematicCommand());

        PluginManager pm = Bukkit.getPluginManager();
    }

    public void test1(){
        AbstractDungeon dungeon = new AbstractDungeon.Builder(this, "test",
                new Location(Bukkit.getWorld("world"), 0, 65, 0),
                200, 200, 200).setMinRoomSizeXZ(4).build(DefaultDungeon.class);
        Bukkit.getLogger().info("Loaded " + dungeon.getId() + " dungeon!");
    }

    public void test2(){
        AbstractDungeon dungeon = new AbstractDungeon.Builder(this, "test",
                new Location(Bukkit.getWorld("world"), 0, 65, 0), 64*5, 200, 64*5)
                .setMinRoomSizeXZ(4)
                .shardSizeX(64)
                .shardSizeZ(64)
                .build(DefaultDungeon.class);
    }

    public void onDisable() {
    }

}