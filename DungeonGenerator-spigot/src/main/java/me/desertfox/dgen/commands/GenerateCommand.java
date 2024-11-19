package me.desertfox.dgen.commands;

import me.desertfox.dgen.Dungeon;
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

        String id = args[0];
        boolean debug = Boolean.parseBoolean(args[1]);

        Dungeon dungeon = Dungeon.findByID(id);
        dungeon.generateAll(debug);
        player.sendMessage("§2§lDone!");
        return false;
    }
}