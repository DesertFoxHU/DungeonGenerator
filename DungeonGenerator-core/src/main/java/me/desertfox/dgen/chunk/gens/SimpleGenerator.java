package me.desertfox.dgen.chunk.gens;

import me.desertfox.dgen.chunk.ChunkGenerator;
import me.desertfox.dgen.chunk.DungeonShard;
import org.bukkit.Location;

import java.util.Random;

public class SimpleGenerator extends ChunkGenerator {

    public SimpleGenerator(DungeonShard chunk) {
        super(chunk);
        roomPool.clear();
        roomPool.add("corridor_WESN");
        roomPool.add("corridor_SN");
        roomPool.add("corridor_WE");
        roomPool.add("wall_4x4");
    }

    @Override
    public void begin(Location start) {
        getShard().getDungeon().safeClaim("corridor_WESN", start).placeDown();

        int minX = Math.min(start.getBlockX(), getShard().getEnd().getBlockX());
        int maxX = Math.max(start.getBlockX(), getShard().getEnd().getBlockX());
        int minZ = Math.min(start.getBlockZ(), getShard().getEnd().getBlockZ());
        int maxZ = Math.max(start.getBlockZ(), getShard().getEnd().getBlockZ());

        int sizeX = 4;
        int sizeZ = 4;

        for (int x = minX; x <= maxX; x += sizeX) {
            for (int z = minZ; z <= maxZ; z += sizeZ) {
                if (x + sizeX - 1 <= maxX && z + sizeZ - 1 <= maxZ) {
                    Location buildingStart = new Location(getShard().getDungeon().getWorld(), x, start.getY(), z);
                    getShard().getDungeon().safeClaim(roomPool.get(new Random().nextInt(roomPool.size())), buildingStart).placeDown();
                }
            }
        }
    }
}