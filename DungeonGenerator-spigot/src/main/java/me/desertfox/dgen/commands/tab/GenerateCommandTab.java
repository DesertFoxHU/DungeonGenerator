package me.desertfox.dgen.commands.tab;

import me.desertfox.dgen.Dungeon;
import me.desertfox.dgen.chunk.ChunkGenerator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GenerateCommandTab implements TabCompleter {

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 1){
            return Dungeon.getDungeons().stream().map(Dungeon::getId).toList();
        }
        else if(args.length == 2){
            return Arrays.asList("true", "false");
        }
        else if(args.length == 3){
            return ChunkGenerator.getGENERATOR_REGISTRY().keySet().stream().toList();
        }
        return null;
    }
}