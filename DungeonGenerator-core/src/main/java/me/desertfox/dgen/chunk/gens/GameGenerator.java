package me.desertfox.dgen.chunk.gens;

import me.desertfox.dgen.chunk.ChunkGenerator;
import me.desertfox.dgen.chunk.DungeonShard;
import me.desertfox.dgen.room.AbstractRoom;
import me.desertfox.dgen.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class GameGenerator extends ChunkGenerator {

    public GameGenerator(DungeonShard chunk) {
        super(chunk);
    }

    @Override
    public void begin(Location start) {
        for(int i = 0; i < 7; i++){
            getDungeon().updateOnQueue((dg) -> {
                Location l = getDungeon().snapToGrid(Utils.getRandomPointInCircle(getShard().get2DCenter(), (double) 64 /2));
                Bukkit.getLogger().info("Populating there: " + l);
                AbstractRoom result = getDungeon().safeBuild("wall_4x4", l);
                Bukkit.getLogger().info("Result: " + result);
            });
        }
    }
}