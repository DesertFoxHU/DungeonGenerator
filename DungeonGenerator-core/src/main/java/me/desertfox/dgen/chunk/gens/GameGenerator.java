package me.desertfox.dgen.chunk.gens;

import me.desertfox.dgen.chunk.ChunkGenerator;
import me.desertfox.dgen.chunk.DungeonShard;
import me.desertfox.dgen.room.AbstractRoom;
import me.desertfox.dgen.utils.Utils;
import org.bukkit.Location;
import org.joml.Random;

public class GameGenerator extends ChunkGenerator {

    long seed = Random.newSeed();
    public GameGenerator(DungeonShard chunk) {
        super(chunk);
    }

    @Override
    public void begin(Location start) {
        int numberOfRooms = new Random(seed).nextInt(8 - 3) + 3;
        for(int i = 0; i < numberOfRooms; i++){
            getDungeon().updateOnQueue((dg) -> {
                Location l = getDungeon().snapToGrid(Utils.getRandomPointInCircle(getShard().get2DCenter(), 32));
                AbstractRoom result = getDungeon().safeClaim("corridor_16x16", l);
                result.placeDown();
            });
        }
    }
}