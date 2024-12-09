package me.desertfox.dgen.schematic;

import lombok.Getter;
import me.desertfox.dgen.schematic.framework.Rotation;
import me.desertfox.dgen.utils.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

@Getter
public class OperationalSchematic implements Cloneable {

  private String name;
  private Vector pos1;
  private Vector pos2;
  private List<Data> data;

  public OperationalSchematic(String name, Vector pos1, Vector pos2, List<Data> data) {
    this.name = name;
    this.pos1 = pos1;
    this.pos2 = pos2;
    this.data = data;
  }

  public List<Block> populate(Location start, Vector shift){
    return populate(start, shift, false, false);
  }

  public List<Block> populate(Location start, Vector shift, boolean clearArea){
    return populate(start, shift, false, clearArea);
  }

  public List<Block> populate(Location start, Vector shift, boolean onlyReplaceAir, boolean clearArea){
    List<Block> blocks = new ArrayList<>();
    if(clearArea){
      getCuboid(start, shift).clearRegion();
    }
    start = start.clone().add(shift);
    for(Data d : data){
      int X = (int) (start.getX() - d.relative.getX());
      int Y = (int) (start.getY() - d.relative.getY());
      int Z = (int) (start.getZ() - d.relative.getZ());
      Block block = new Location(start.getWorld(), X, Y, Z).getBlock();
      if(onlyReplaceAir){
        if(block.getType() == Material.AIR){
          block.setBlockData(d.bData);
          blocks.add(block);
        }
        continue;
      }
      block.setBlockData(d.bData);
      blocks.add(block);
    }
    return blocks;
  }

  public Cuboid getCuboid(Location start){
    int X1 = (int) (start.getX() - pos1.getX());
    int Y1 = (int) (start.getY() - pos1.getY());
    int Z1 = (int) (start.getZ() - pos1.getZ());

    int X2 = (int) (start.getX() - pos2.getX());
    int Y2 = (int) (start.getY() - pos2.getY());
    int Z2 = (int) (start.getZ() - pos2.getZ());
    return new Cuboid(new Location(start.getWorld(), X1, Y1, Z1), new Location(start.getWorld(), X2, Y2, Z2));
  }

  public Cuboid getCuboid(Location start, Vector shift){
    start = start.clone().add(shift);
    int X1 = (int) (start.getX() - pos1.getX());
    int Y1 = (int) (start.getY() - pos1.getY());
    int Z1 = (int) (start.getZ() - pos1.getZ());

    int X2 = (int) (start.getX() - pos2.getX());
    int Y2 = (int) (start.getY() - pos2.getY());
    int Z2 = (int) (start.getZ() - pos2.getZ());
    return new Cuboid(new Location(start.getWorld(), X1, Y1, Z1), new Location(start.getWorld(), X2, Y2, Z2));
  }

  public Operation beginOperation(){
    return new Operation(this);
  }

  @Override
  public OperationalSchematic clone() {
    try {
      OperationalSchematic clone = (OperationalSchematic) super.clone();
      clone.name = this.name;
      clone.data = new ArrayList<>();
      for(Data d : data){
        clone.data.add(d.clone());
      }
      clone.pos1 = this.pos1.clone();
      clone.pos2 = this.pos2.clone();
      // TODO: copy mutable state here, so the clone can't change the internals of the original
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

  public static class Operation {

    private final OperationalSchematic schematic;

    public Operation(OperationalSchematic schematic){
      this.schematic = schematic.clone();
    }

    public Operation transform(Vector by){
      schematic.data.forEach(d -> {
        d.relative.add(by);
      });
      schematic.pos1.add(by);
      schematic.pos2.add(by);
      return this;
    }

    public Operation rotate(Rotation.Direction rotation){
      if(rotation == null) return this;
      schematic.pos1 = Rotation.rotateRelative(schematic.pos1, rotation);
      schematic.pos2 = Rotation.rotateRelative(schematic.pos2, rotation);
      switch(rotation){
        case CLOCKWISE_90 -> {
          schematic.data.forEach(d -> {
            if(d.bData instanceof Directional){
              d.bData.rotate(StructureRotation.CLOCKWISE_90);
            }
            d.bData = Rotation.rotateBlockData(d.bData, rotation);

            double x = 0 * d.relative.getX() - 1 * d.relative.getZ();
            double z = 1 * d.relative.getX() + 0 * d.relative.getZ();
            d.relative = new Vector(x, d.relative.getY(), z);
          });
        }
        case CLOCKWISE_180 -> {
          schematic.data.forEach(d -> {
            if(d.bData instanceof Directional){
              d.bData.rotate(StructureRotation.CLOCKWISE_180);
            }
            d.bData = Rotation.rotateBlockData(d.bData, rotation);

            double x = -1 * d.relative.getX();
            double z = -1 * d.relative.getZ();
            d.relative = new Vector(x, d.relative.getY(), z);
          });
        }
        case CLOCKWISE_270 -> {
          schematic.data.forEach(d -> {
            if(d.bData instanceof Directional){
              d.bData.rotate(StructureRotation.COUNTERCLOCKWISE_90);
            }
            d.bData = Rotation.rotateBlockData(d.bData, rotation);

            double x = 0 * d.relative.getX() + 1 * d.relative.getZ();
            double z = -1 * d.relative.getX() + 0 * d.relative.getZ();
            d.relative = new Vector(x, d.relative.getY(), z);
          });
        }
      }
      return this;
    }

    public OperationalSchematic retrive(){
      return schematic;
    }
  }

  public static class Data implements Cloneable {
    public Vector relative;
    @Getter private BlockData original;
    public BlockData bData;

    public Data(Vector relative, BlockData bData) {
      this.relative = relative;
      this.original = bData.clone();
      this.bData = bData;
    }

    public Location toReal(Location start){
      int X1 = (int) (start.getX() - relative.getX());
      int Y1 = (int) (start.getY() - relative.getY());
      int Z1 = (int) (start.getZ() - relative.getZ());
      return new Location(start.getWorld(), X1, Y1, Z1);
    }

    public Location toRealAndShift(Location start, Vector shift){
      start = start.clone().add(shift);
      int X1 = (int) (start.getX() - relative.getX());
      int Y1 = (int) (start.getY() - relative.getY());
      int Z1 = (int) (start.getZ() - relative.getZ());
      return new Location(start.getWorld(), X1, Y1, Z1);
    }

    @Override
    public Data clone() {
      try {
        Data clone = (Data) super.clone();
        clone.relative = this.relative.clone();
        clone.original = this.original.clone();
        clone.bData = this.bData.clone();
        // TODO: copy mutable state here, so the clone can't change the internals of the original
        return clone;
      } catch (CloneNotSupportedException e) {
        throw new AssertionError();
      }
    }
  }

}