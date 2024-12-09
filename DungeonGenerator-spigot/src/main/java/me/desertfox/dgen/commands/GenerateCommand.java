package me.desertfox.dgen.commands;

import me.desertfox.dgen.AbstractDungeon;
import me.desertfox.dgen.chunk.ChunkGenerator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GenerateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!command.getName().equals("generate")) return false;

        if(!(sender instanceof Player player)){
            return false;
        }

        if(args.length != 3){
            player.sendMessage("§cUsage: /generate [DungeonName] [debug] [generatorName]");
            return false;
        }

        String id = args[0];
        boolean debug = Boolean.parseBoolean(args[1]);
        String generator = args[2];

        AbstractDungeon dungeon = AbstractDungeon.findByID(id);
        dungeon.configShards(debug, ChunkGenerator.findByClassName(generator));
        dungeon.generateAll();
        player.sendMessage("§2§lStarted!");
        return false;
    }
}