package me.desertfox.dgen.chunk;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import lombok.Getter;

import me.desertfox.dgen.Dungeon;
import me.desertfox.dgen.room.ActiveRoom;
import me.desertfox.dgen.utils.Cuboid;
import org.bukkit.Location;
import org.bukkit.Material;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Represents a physical chunk for Dungeons
 */
@Getter
public class DungeonChunk {
    private final Dungeon dungeon;
    private final int indexX;
    private final int indexY;
    private final int coordX;
    private final int coordY;
    private final int coordZ;
    private final int endX;
    private final int endY;
    private final int endZ;
    private final Cuboid region;
    public List<ActiveRoom> rooms = new ArrayList<>();
    public ChunkGenerator generator;

    public DungeonChunk(Dungeon dungeon, Class<? extends ChunkGenerator> generatorClass, int indexX, int indexY, int coordX, int coordY, int coordZ, int endX, int endY, int endZ) {
        this.dungeon = dungeon;
        this.indexX = indexX;
        this.indexY = indexY;
        this.coordX = coordX;
        this.coordY = coordY;
        this.coordZ = coordZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        region = new Cuboid(getStart(), getEnd());
        setGenerator(generatorClass);
    }

    public void setGenerator(Class<? extends ChunkGenerator> generatorClass){
        try {
            this.generator = generatorClass.getConstructor(DungeonChunk.class).newInstance(this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean doHitOtherRoom(Cuboid cuboid){
        for(ActiveRoom room : rooms){
            if(room.getRegion().isCollidingWith(cuboid)){
                return true;
            }
        }
        return false;
    }

    private final List<Location> startHere = new ArrayList<>();
    public void populate(boolean debug, Class<? extends ChunkGenerator> generator){
        new Cuboid(getStart(), getEnd()).clearRegion();
        if(debug){
            drawDebug2DBox();
        }

        if(indexX != 0 || indexY != 0) return;

        ChunkGenerator gen;
        try{
            gen = generator.getConstructor(DungeonChunk.class).newInstance(this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        gen.begin(getStart());
    }

    public void drawDebug2DBox(){
        int minX = Math.min(coordX, endX);
        int maxX = Math.max(coordX, endX);
        int minZ = Math.min(coordZ, endZ);
        int maxZ = Math.max(coordZ, endZ);

        for (int x = minX; x <= maxX; x++) {
            dungeon.getWorld().getBlockAt(x, coordY-1, minZ).setType(Material.BLACK_WOOL);
            dungeon.getWorld().getBlockAt(x, coordY-1, maxZ).setType(Material.BLACK_WOOL);
        }

        for (int z = minZ; z <= maxZ; z++) {
            dungeon.getWorld().getBlockAt(minX, coordY-1, z).setType(Material.BLACK_WOOL);
            dungeon.getWorld().getBlockAt(maxX, coordY-1, z).setType(Material.BLACK_WOOL);
        }
    }

    public Location getStart(){
        return new Location(dungeon.getWorld(), coordX, coordY, coordZ);
    }

    public Location getEnd(){
        return new Location(dungeon.getWorld(), endX, endY, endZ);
    }

}