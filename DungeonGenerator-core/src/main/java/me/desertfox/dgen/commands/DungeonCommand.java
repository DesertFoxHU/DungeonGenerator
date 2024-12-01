package me.desertfox.dgen.commands;

import me.desertfox.dgen.Dungeon;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class DungeonCommand implements CommandExecutor {

    public static void register(JavaPlugin plugin){
        plugin.getCommand("dungeon").setExecutor(new DungeonCommand());
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
        }

        String id = args[0];
        if(args.length == 2){
            if(args[1].equalsIgnoreCase("clear")){
                Dungeon dungeon = Dungeon.findByID(id);
                dungeon.clearQueue();
                player.sendMessage("§2§lStarted clearing!");
            }
        }
        return false;
    }

}