package me.desertfox.dgen.commands.tab;

import me.desertfox.dgen.AbstractDungeon;
import me.desertfox.dgen.shard.ShardGenerator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class GenerateCommandTab implements TabCompleter {

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1){
            return AbstractDungeon.getDungeons().stream().map(AbstractDungeon::getId).toList();
        }
        else if(args.length == 2){
            return Arrays.asList("true", "false");
        }
        else if(args.length == 3){
            return ShardGenerator.getGENERATOR_REGISTRY().keySet().stream().toList();
        }
        return null;
    }
}