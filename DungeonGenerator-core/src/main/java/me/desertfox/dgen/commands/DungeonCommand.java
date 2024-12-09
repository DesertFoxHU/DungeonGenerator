package me.desertfox.dgen.commands;

import me.desertfox.dgen.AbstractDungeon;
import me.desertfox.dgen.chunk.ChunkGenerator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class DungeonCommand implements CommandExecutor {

    public static void register(JavaPlugin plugin){
        plugin.getCommand("dungeon").setExecutor(new DungeonCommand());
        plugin.getCommand("dungeon").setTabCompleter(new DungeonCommandTab());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!command.getName().equals("dungeon")) return false;

        if(!(sender instanceof Player player)){
            return false;
        }

        if(!player.isOp()){
            return false;
        }

        if(args.length == 0){
            player.sendMessage("§c/dungeon [id] clear");
            player.sendMessage("§c/dungeon [id] generate [debug] [generatorName]");
            return false;
        }

        String id = args[0];
        if(args.length == 2){
            if(args[1].equalsIgnoreCase("clear")){
                AbstractDungeon dungeon = AbstractDungeon.findByID(id);
                if(dungeon == null){
                    player.sendMessage("§4§lDidn't found dungeon by id: " + id);
                    return false;
                }
                dungeon.clearQueue();
                player.sendMessage("§2§lStarted clearing!");
            }
        }

        if(args.length == 4){
            if(args[1].equalsIgnoreCase("generate")){
                boolean debug = Boolean.parseBoolean(args[2]);
                String generator = args[3];

                AbstractDungeon dungeon = AbstractDungeon.findByID(id);
                dungeon.configShards(debug, ChunkGenerator.findByClassName(generator));
                dungeon.generateAll();
                player.sendMessage("§2§lStarted generating!");
            }
            return false;
        }
        return false;
    }

}