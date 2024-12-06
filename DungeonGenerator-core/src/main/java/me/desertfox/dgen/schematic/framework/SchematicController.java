package me.desertfox.dgen.schematic.framework;

import me.desertfox.dgen.Direction4;
import me.desertfox.dgen.schematic.OperationalSchematic;
import me.desertfox.dgen.utils.Cuboid;
import me.desertfox.dgen.utils.CustomYml;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Structure;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.block.structure.UsageMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * {@link SchematicController#init(JavaPlugin, String)} to init
 */
public class SchematicController {

  public static JavaPlugin plugin;
  public static String schemaDir;
  private static HashMap<String, OperationalSchematic> schematics = new HashMap<>();

  public static void init(JavaPlugin plugin, String schemaDir){
    SchematicController.plugin = plugin;
    SchematicController.schemaDir = schemaDir;
    File dir = new File(plugin.getDataFolder() + File.separator + schemaDir);
    if(!dir.exists()){
      dir.mkdirs();
    }

    if(dir.listFiles() != null){
      for(File file : dir.listFiles()){
        schematics.put(file.getName().split("\\.")[0], loadSchematic(getYml(file.getName())));
      }
    }
  }

  /**
   * Try to avoid using this as much as you can<br>
   * The returned value is a reference to the original schematic<br>
   * Any modification could lead to unexpected errors, and trust me<br>
   * it will be hard to track it down<br>
   * @param name
   * @return Reference to the original schematic
   */
  public static OperationalSchematic getHardReference(String name){
    if(!schematics.containsKey(name)) return null;
    return schematics.get(name);
  }

  public static OperationalSchematic get(String name){
    if(!schematics.containsKey(name)) return null;
    return schematics.get(name).clone();
  }

  public static CustomYml getYml(String fileName){
    if(!fileName.contains(".yml")){
      fileName += ".yml";
    }
    return new CustomYml(plugin).createNew(schemaDir + File.separator + fileName, false);
  }

  public static Vector getRelativeCorner(CustomYml yml, String configTag) {
    String raw = yml.getConfig().getString("schematic.relative_corner." + configTag);
    if(raw == null){
      Bukkit.getLogger().info("Couldn't load relative corner here: " + yml.file.getAbsolutePath() + " (" + configTag + ")");
      return new Vector(0, 0, 0);
    }
    String[] split = raw.split(" ");
    int X = Integer.parseInt(split[0]);
    int Y = Integer.parseInt(split[1]);
    int Z = Integer.parseInt(split[2]);
    return new Vector(X, Y, Z);
  }

  private static void saveRelativeCorner(CustomYml yml, Location center, Location currentLocation, String configTag) {
    int relativeX = center.getBlockX() - currentLocation.getBlockX();
    int relativeY = center.getBlockY() - currentLocation.getBlockY();
    int relativeZ = center.getBlockZ() - currentLocation.getBlockZ();
    yml.getConfig().set("schematic.relative_corner." + configTag, "" + relativeX + " " + relativeY + " " + relativeZ);
    yml.save();
  }

  public static void saveSchematic(CustomYml yml, Location center, Cuboid c) {
    saveSchematic(yml, center, c, null);
  }

  public static void saveSchematic(CustomYml yml, Location center, Cuboid c, @Nullable Material exclude) {
    List<String> relativeBlockList = new ArrayList<>();
    saveRelativeCorner(yml, center, c.getLocation1(), "pos1");
    saveRelativeCorner(yml, center, c.getLocation2(), "pos2");

    List<String> doors = new ArrayList<>();
    for (Block block : c.getBlocks()) {
      Location currentLocation = block.getLocation();
      if (block.getType() == Material.AIR || block.getType() == exclude) continue;
      int relativeX = center.getBlockX() - currentLocation.getBlockX();
      int relativeY = center.getBlockY() - currentLocation.getBlockY();
      int relativeZ = center.getBlockZ() - currentLocation.getBlockZ();
      String blockData = currentLocation.getBlock().getBlockData().getAsString();

      if(block.getBlockData() instanceof Jigsaw jigsaw){
        doors.add(relativeX + " " + relativeY + " " + relativeZ + " " + Direction4.convertFrom(jigsaw.getOrientation()));
      }
      else relativeBlockList.add(relativeX + " " + relativeY + " " + relativeZ + " " + blockData);
    }
    yml.getConfig().set("schematic.doors", doors);
    yml.getConfig().set("schematic.list", relativeBlockList);
    yml.save();

    //NEW
    Location structure = c.getLocation1().clone().add(-1, -1, -1);
    structure.getBlock().setType(Material.STRUCTURE_BLOCK);
    Structure struct = (Structure) structure.getBlock().getState();
    struct.setBoundingBoxVisible(true);
    struct.setUsageMode(UsageMode.SAVE);
    struct.setStructureName(yml.file.getName());
    struct.setRelativePosition(new BlockVector(1, 1, 1));
    struct.setStructureSize(new BlockVector(c.getSizeX()+1, c.getSizeY()+1, c.getSizeZ()));
    struct.update();
  }

  private static OperationalSchematic loadSchematic(CustomYml yml) {
    FileConfiguration config = yml.getConfig();
    List<String> blockList = config.getStringList("schematic.list");
    Vector pos1 = getRelativeCorner(yml, "pos1");
    Vector pos2 = getRelativeCorner(yml, "pos2");

    List<OperationalSchematic.Data> data = new ArrayList<>();
    for (String blockRaw : blockList) {
      String[] splitted = blockRaw.split(" ");
      int X = Integer.parseInt(splitted[0]);
      int Y = Integer.parseInt(splitted[1]);
      int Z = Integer.parseInt(splitted[2]);
      if(splitted[3].equals("minecraft:grass")){
        splitted[3] = "minecraft:short_grass";
        Bukkit.getLogger().warning("Warning! Found minecraft_grass as material in " + yml.file.getName() + " it is outdated! Use short_grass or tall_grass");
      }
      BlockData blockData = Bukkit.createBlockData(splitted[3]);
      data.add(new OperationalSchematic.Data(new Vector(X, Y, Z), blockData));
    }
    return new OperationalSchematic(yml.file.getName().split("\\.")[0], pos1, pos2, data);
  }

}