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
        safeBuild("corridor_WESN", start);

        int minX = Math.min(start.getBlockX(), getChunk().getEnd().getBlockX());
        int maxX = Math.max(start.getBlockX(), getChunk().getEnd().getBlockX());
        int minZ = Math.min(start.getBlockZ(), getChunk().getEnd().getBlockZ());
        int maxZ = Math.max(start.getBlockZ(), getChunk().getEnd().getBlockZ());

        int sizeX = 4;
        int sizeZ = 4;

        for (int x = minX; x <= maxX; x += sizeX) {
            for (int z = minZ; z <= maxZ; z += sizeZ) {
                if (x + sizeX - 1 <= maxX && z + sizeZ - 1 <= maxZ) {
                    Location buildingStart = new Location(getChunk().getDungeon().getWorld(), x, start.getY(), z);
                    safeBuild(roomPool.get(new Random().nextInt(roomPool.size())), buildingStart);
                }
            }
        }
    }
}