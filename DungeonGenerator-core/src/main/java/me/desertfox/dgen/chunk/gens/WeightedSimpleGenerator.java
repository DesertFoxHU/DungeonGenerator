package me.desertfox.dgen.chunk.gens;

import com.mysql.cj.protocol.InternalDate;
import me.desertfox.dgen.chunk.ChunkGenerator;
import me.desertfox.dgen.chunk.DungeonChunk;
import me.desertfox.dgen.room.RoomSchematic;
import me.desertfox.gl.Commons;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class WeightedSimpleGenerator extends ChunkGenerator {

    private HashMap<String, Double> pool = new HashMap<>(){{
        put("corridor_WESN", 40d);
        put("corridor_SN", 30d);
        put("corridor_WE", 25d);
        put("wall_4x4", 5d);
    }};

    public WeightedSimpleGenerator(DungeonChunk chunk) {
        super(chunk);
        roomPool.clear();
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
                    safeBuild(draw(), buildingStart);
                }
            }
        }
    }

    private String draw(){
        double chance = 0;
        List<String> remains = new ArrayList<>(pool.keySet());
        while(!remains.isEmpty()){
            int index = new Random().nextInt(remains.size());
            String current = remains.get(index);
            chance += pool.get(remains.get(index));

            if(Commons.roll(chance)){
                return current;
            }
            remains.remove(index);
        }
        return null;
    }
}