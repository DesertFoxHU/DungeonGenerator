package me.desertfox.dgen.chunk.gens;

import me.desertfox.dgen.chunk.ShardGenerator;
import me.desertfox.dgen.chunk.DungeonShard;
import me.desertfox.gl.Commons;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class WeightedSimpleGenerator extends ShardGenerator {

    private HashMap<String, Double> pool = new HashMap<>(){{
        put("corridor_WESN", 40d);
        put("corridor_SN", 30d);
        put("corridor_WE", 25d);
        put("wall_4x4", 5d);
    }};

    public WeightedSimpleGenerator(DungeonShard chunk) {
        super(chunk);
        roomPool.clear();
    }

    @Override
    public void begin(Location start, Object... params) {
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
                    getShard().getDungeon().safeClaim(draw(), buildingStart).placeDown();
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