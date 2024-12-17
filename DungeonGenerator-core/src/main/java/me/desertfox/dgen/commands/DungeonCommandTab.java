package me.desertfox.dgen.commands;

import me.desertfox.dgen.AbstractDungeon;
import me.desertfox.dgen.chunk.ShardGenerator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class DungeonCommandTab implements TabCompleter {

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1){
            return AbstractDungeon.getDungeons().stream().map(AbstractDungeon::getId).toList();
        }
        if(args.length == 2){
            return Arrays.asList("generate", "clear");
        }
        else if(args.length == 3){
            return Arrays.asList("true", "false");
        }
        else if(args.length == 4){
            return ShardGenerator.getGENERATOR_REGISTRY().keySet().stream().toList();
        }
        return null;
    }
}