package me.desertfox.dgen.commands;

import me.desertfox.dgen.DungeonGenerator;
import me.desertfox.dgen.schematic.OperationalSchematic;
import me.desertfox.dgen.schematic.framework.Rotation;
import me.desertfox.dgen.schematic.framework.SchematicController;
import me.desertfox.dgen.utils.CustomYml;
import me.desertfox.gl.region.Cuboid;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Structure;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.WeakHashMap;

public class SchematicCommand implements CommandExecutor {

    public static class EditSession {
        public String name;
        public Location location;
        public Location start;
        public Location end;

        public EditSession(String name, Location location, Location start, Location end) {
            this.name = name;
            this.location = location;
            this.start = start;
            this.end = end;
        }

        public CustomYml getYml(){
            return new CustomYml(DungeonGenerator.instance).createNew("schemas" + File.separator + name + ".yml", false);
        }
    }

    private final WeakHashMap<Player, Location[]> selections = new WeakHashMap<>();
    private final WeakHashMap<Player, EditSession> editSession = new WeakHashMap<>();
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)){
            return false;
        }

        if(!player.isOp()) return false;

        if(args.length == 0){
            player.sendMessage("§e/schema create [Name]");
            player.sendMessage("§e/schema rescan §f- rescans the structure | look at the structure block");
            player.sendMessage("§e/schema load [Name]");
            player.sendMessage("§e/schema setPos1 §f- Sets the position which block are you facing");
            player.sendMessage("§e/schema setPos2 §f- Sets the position which block are you facing");
            player.sendMessage("§e/schema edit §f- starts editing | look at the structure block");
            player.sendMessage("§e/schema cancelEdit");
            player.sendMessage("§e/schema addVariantGroup [GroupId]");
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("cancelEdit")){
                if(!editSession.containsKey(player)){
                    player.sendMessage("§cYou aren't in an edit session!");
                    return false;
                }

                editSession.remove(player);
                player.sendMessage("§cYou stopped editing!");
                return false;
            }
            if(args[0].equalsIgnoreCase("rescan")){
                Block block = player.getTargetBlockExact(5);
                if (block == null) {
                    return false;
                }

                if(block.getState() instanceof Structure sblock){
                    String name = sblock.getStructureName().split("minecraft:")[1].split(".yml")[0];
                    Location location = sblock.getLocation();
                    Location start = location.clone().add(1, 1, 1);
                    Location end = start.clone().add(sblock.getStructureSize().getX()-1, sblock.getStructureSize().getY()-1, sblock.getStructureSize().getZ()-1);

                    Cuboid cuboid = new Cuboid(start, end);

                    SchematicController.saveSchematic(
                            new CustomYml(DungeonGenerator.instance).createNew("schemas" + File.separator + name + ".yml", false),
                            start,
                            cuboid,
                            null
                    );
                    player.sendMessage("§2Successfully saved as §6" + name + "§2!");
                }
            }
            if(args[0].equalsIgnoreCase("setPos1")){
                Block block = player.getTargetBlockExact(5);
                if (block == null) {
                    return false;
                }
                if (!selections.containsKey(player)) {
                    selections.put(player,new Location[2]);
                }
                selections.computeIfPresent(player, (player1, vectors) -> {
                    vectors[0] = block.getLocation();
                    return vectors;
                });
                player.sendMessage("§2Position §e1st §2has been set");
                return false;
            }
            if(args[0].equalsIgnoreCase("setPos2")){
                Block block = player.getTargetBlockExact(5);
                if (block == null) {
                    return false;
                }
                if (!selections.containsKey(player)) {
                    selections.put(player,new Location[2]);
                }
                selections.computeIfPresent(player, (player1, vectors) -> {
                    vectors[1] = block.getLocation();
                    return vectors;
                });
                player.sendMessage("§2Position §e2nd §2has been set!");
                return false;
            }
            if(args[0].equalsIgnoreCase("edit")){
                Block block = player.getTargetBlockExact(5);
                if (block == null) {
                    return false;
                }

                if(block.getState() instanceof Structure sblock){
                    String name = sblock.getStructureName().split("minecraft:")[1].split(".yml")[0];
                    Location location = sblock.getLocation();
                    Location start = location.clone().add(1, 1, 1);
                    Location end = start.clone().add(sblock.getStructureSize().getX(), sblock.getStructureSize().getY(), sblock.getStructureSize().getZ());

                    if(editSession.containsKey(player)) editSession.replace(player, new EditSession(name, location, start, end));
                    else editSession.put(player, new EditSession(name, location, start, end));
                    player.sendMessage("§2Started editing §6" + name + "§2!");
                }
            }
        }

        if(args.length == 2){
            if(args[0].equalsIgnoreCase("addVariantGroup")){
                String groupId = args[1];
                if(!editSession.containsKey(player)){
                    player.sendMessage("§cYou aren't in an edit session!");
                    return false;
                }

                if(!selections.containsKey(player)){
                    player.sendMessage("§cThere isn't something selected, use /schema [setPos1|setPos2]!");
                    return false;
                }
                Location[] location = selections.get(player);
                EditSession session = editSession.get(player);
                Location center = session.start.clone();
                Cuboid cuboid = new Cuboid(location[0], location[1]);
                CustomYml yml = session.getYml();

                SchematicController.setVariantGroup(yml, center, cuboid, groupId);
                player.sendMessage("§2Successfully saved variantGroup named §6" + groupId + "§2!");
            }

            if(args[0].equalsIgnoreCase("create")){
                if(!selections.containsKey(player)){
                    player.sendMessage("§cThere isn't something selected, use /schema [setPos1|setPos2]!");
                    return false;
                }
                Location[] location = selections.get(player);

                String name = args[1];
                Cuboid cuboid = new Cuboid(location[0], location[1]);

                SchematicController.saveSchematic(
                        new CustomYml(DungeonGenerator.instance).createNew("schemas" + File.separator + name + ".yml", false),
                        location[0],
                        cuboid,
                        null
                );

                player.sendMessage("§2Successfully saved as §6" + name + "§2!");
                return false;
            }
            if(args[0].equalsIgnoreCase("load")){
                String name = args[1];
                OperationalSchematic schema = SchematicController.get(name);
                schema.populate(player.getTargetBlockExact(5).getLocation(), new Vector(0,0,0));
                return false;
            }
        }

        if(args.length == 3){
            if(args[0].equalsIgnoreCase("load")){
                String name = args[1];
                Location start = player.getTargetBlockExact(5).getLocation();
                Rotation.Direction rotation = Rotation.Direction.valueOf(args[2]);
                OperationalSchematic schema = SchematicController.get(name)
                        .beginOperation().rotate(rotation).retrive();
                schema.populate(start, new Vector(0,0,0));
                return false;
            }
        }

        return false;
    }
}